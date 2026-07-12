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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.songs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dancing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LaidToRest;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Marionette;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silenced;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Trance;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.NoteParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class GrandFinaleSong extends Song {

	public static final GrandFinaleSong INSTANCE = new GrandFinaleSong();

	public static final int RANGE = 10;

	@Override
	public int icon() {
		return HeroIcon.GRAND_FINALE;
	}

	//damage per debuff scales linearly, to about 3x its base value at lute level 10
	public static int min(int lvl) {
		return Math.round(8 * (1f + 0.2f*lvl));
	}

	public static int max(int lvl) {
		return Math.round(12 * (1f + 0.2f*lvl));
	}

	@Override
	public void onCast(Lute lute, Hero hero) {

		boolean anyDebuffed = false;
		for (Char ch : Actor.chars()) {
			if (ch.alignment == Char.Alignment.ENEMY
					&& Dungeon.level.heroFOV[ch.pos]
					&& Dungeon.level.distance(hero.pos, ch.pos) <= RANGE
					&& bardicDebuffs(ch) > 0) {
				anyDebuffed = true;
				break;
			}
		}

		if (!anyDebuffed) {
			GLog.w(Messages.get(this, "no_targets"));
			return;
		}

		hero.sprite.operate(hero.pos);
		Sample.INSTANCE.play(Assets.Sounds.BLAST);
		hero.sprite.centerEmitter().start(noteFactory(), 0.2f, 8);

		int lvl = lute.buffedLvl();
		for (Char ch : Actor.chars()) {
			if (ch.alignment == Char.Alignment.ENEMY
					&& Dungeon.level.heroFOV[ch.pos]
					&& Dungeon.level.distance(hero.pos, ch.pos) <= RANGE) {
				int debuffs = bardicDebuffs(ch);
				if (debuffs > 0) {
					ch.sprite.centerEmitter().start(noteFactory(), 0.2f, 5);
					ch.sprite.burst(0xFFFFFF44, 5);
					ch.damage(modifyDamage(Random.NormalIntRange(min(lvl) * debuffs, max(lvl) * debuffs)), this);
				}
			}
		}

		hero.spend(1f);
		hero.next();

		onSongCast(lute, hero);
	}

	@Override
	protected Object[] descArgs() {
		int lvl = luteLvl();
		return new Object[]{ min(lvl), max(lvl) };
	}

	//counts all debuffs on a character which were applied by the bard's songs
	public static int bardicDebuffs( Char ch ) {
		int count = 0;

		if (ch.buff(Trance.class) != null)      count++;
		if (ch.buff(Dancing.class) != null)     count++;
		if (ch.buff(Marionette.class) != null)  count++;
		if (ch.buff(Silenced.class) != null)    count++;
		if (ch.buff(LaidToRest.class) != null)  count++;

		//terror and amok are generic buffs, so only count them if a bard tracker is present
		if (ch.buff(Terror.class) != null
				&& ch.buff(DirgeSong.BardTerrorTracker.class) != null)      count++;
		if (ch.buff(Amok.class) != null
				&& ch.buff(DiscordSong.BardAmokTracker.class) != null)      count++;

		return count;
	}

}
