/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.songs.Song;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.NoteParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

//the maestro's subclass mechanic. Turns spent doing nothing but moving or waiting while
// an awake enemy is in view build the bard's performance. The performance is spent on his
// next song, which plays at +2 effective lute levels per tier reached. A full performance
// also unleashes the song's finisher effect.
public class Performing extends Buff {

	{
		type = buffType.POSITIVE;

		//acts right before the hero, so the previous hero turn is counted first
		actPriority = HERO_PRIO+1;
	}

	//effective lute levels granted per performance tier
	public static final int LEVELS_PER_TIER = 2;

	//performance turns needed to reach each tier, by points in accelerando
	private static final int[][] TIER_THRESHOLDS = {
			{3, 6, 9},
			{3, 6, 8},
			{3, 5, 7},
			{2, 4, 6}
	};

	//turns the performance lingers with no awake enemy in view, by points in fermata
	private static final int[] GRACE_TURNS = {3, 5, 10, 20};

	private int turns = 0;
	private int graceLeft = GRACE_TURNS[0];

	//whether the hero's last action was only moving or waiting
	private boolean qualifiedLastTurn = false;

	public static int[] thresholds(){
		int points = Dungeon.hero == null ? 0 : Dungeon.hero.pointsInTalent(Talent.ACCELERANDO);
		return TIER_THRESHOLDS[points];
	}

	public static int maxGrace(){
		int points = Dungeon.hero == null ? 0 : Dungeon.hero.pointsInTalent(Talent.FERMATA);
		return GRACE_TURNS[points];
	}

	public int tier(){
		int[] thresholds = thresholds();
		int tier = 0;
		for (int i = 0; i < thresholds.length; i++){
			if (turns >= thresholds[i]) tier = i+1;
		}
		return tier;
	}

	public int levelBonus(){
		return LEVELS_PER_TIER * tier();
	}

	//called whenever the maestro spends a turn only moving or waiting
	public static void qualify( Hero hero ){
		if (hero.subClass != HeroSubClass.MAESTRO) return;

		Performing performing = hero.buff(Performing.class);
		if (performing == null){
			//a performance only begins in front of an awake audience
			if (!awakeEnemySeen(hero)) return;
			performing = Buff.affect(hero, Performing.class);
		}
		performing.qualifiedLastTurn = true;
	}

	public enum Interrupt {
		POTION_OR_SCROLL, //forgiven with 1+ points in the show must go on
		ITEM,             //forgiven with 2+ points
		ATTACK            //forgiven with 3 points
	}

	//violating actions instantly end the performance, unless forgiven by the show must go on.
	// Forgiven actions (and actions with no hook, e.g. searching) pause its growth instead.
	public static void interrupt( Interrupt type ){
		Hero hero = Dungeon.hero;
		if (hero == null || hero.subClass != HeroSubClass.MAESTRO) return;

		Performing performing = hero.buff(Performing.class);
		if (performing == null) return;

		int forgiveness = hero.pointsInTalent(Talent.THE_SHOW_MUST_GO_ON);
		switch (type){
			case POTION_OR_SCROLL: if (forgiveness >= 1) return; break;
			case ITEM:             if (forgiveness >= 2) return; break;
			case ATTACK:           if (forgiveness >= 3) return; break;
		}

		if (performing.turns > 0){
			GLog.w(Messages.get(Performing.class, "interrupted"));
		}
		performing.detach();
	}

	//playing a song spends the entire performance, at whatever tier it reached
	public void consume( Song song ){
		if (tier() > 0){
			if (target.sprite != null) {
				target.sprite.centerEmitter().burst(song.noteFactory(), 3 + 3 * tier());
			}
			if (tier() == 3){
				GLog.p(Messages.get(this, "crescendo"));
			}
		}
		detach();
	}

	@Override
	public boolean act() {

		if (!(target instanceof Hero) || ((Hero)target).subClass != HeroSubClass.MAESTRO){
			detach();
			return true;
		}

		if (awakeEnemySeen((Hero)target)){
			graceLeft = maxGrace();
			int[] thresholds = thresholds();
			if (qualifiedLastTurn && turns < thresholds[2]){
				int prevTier = tier();
				turns++;
				//the performance is visibly (and audibly, one imagines) building
				if (target.sprite != null) {
					target.sprite.centerEmitter().start(new NoteParticle.Factory(0xFFFFFF), 0.3f, 1 + tier());
				}
				if (tier() > prevTier){
					GLog.p(Messages.get(this, tier() == 3 ? "finisher_ready" : "tier_up"));
				}
			}
		} else {
			graceLeft--;
			if (graceLeft <= 0){
				if (turns > 0){
					GLog.i(Messages.get(this, "fades"));
				}
				detach();
				return true;
			}
		}

		qualifiedLastTurn = false;
		BuffIndicator.refreshHero();
		spend(TICK);
		return true;
	}

	private static boolean awakeEnemySeen( Hero hero ){
		for (Mob mob : hero.getVisibleEnemies()){
			if (mob.state != mob.SLEEPING){
				return true;
			}
		}
		return false;
	}

	@Override
	public int icon() {
		//placeholder art: reuses the skald's verses icon, tinted by tier
		return BuffIndicator.VERSES;
	}

	@Override
	public void tintIcon(Image icon) {
		switch (tier()){
			case 0: default: icon.hardlight(0f, 1f, 0f);   break;
			case 1:          icon.hardlight(1f, 1f, 0f);   break;
			case 2:          icon.hardlight(1f, 0.5f, 0f); break;
			case 3:          icon.hardlight(1f, 0f, 0f);   break;
		}
	}

	@Override
	public float iconFadePercent() {
		int max = thresholds()[2];
		return Math.max(0, (max - turns) / (float)max);
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(turns);
	}

	@Override
	public String desc() {
		String desc = Messages.get(this, "desc", turns, levelBonus());

		if (tier() == 3){
			desc += "\n\n" + Messages.get(this, "desc_finisher");
		} else {
			desc += "\n\n" + Messages.get(this, "desc_next", thresholds()[tier()] - turns);
		}

		if (!awakeEnemySeen((Hero)target)){
			desc += "\n\n" + Messages.get(this, "desc_fading", graceLeft);
		}

		return desc;
	}

	private static final String TURNS = "turns";
	private static final String GRACE_LEFT = "grace_left";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TURNS, turns);
		bundle.put(GRACE_LEFT, graceLeft);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		turns = bundle.getInt(TURNS);
		graceLeft = bundle.getInt(GRACE_LEFT);
		qualifiedLastTurn = false;
	}

}
