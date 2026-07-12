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

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.songs.DanceSong;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.songs.DissonantChordSong;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.songs.Song;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.songs.TranceSong;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBardSongs;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Arrays;

//the bard's signature instrument, used to play songs. modeled on the cleric's holy tome
public class Lute extends Artifact {

	{
		image = ItemSpriteSheet.LUTE;

		exp = 0;
		levelCap = 10;

		charge = Math.min(level()+3, 10);
		partialCharge = 0;
		chargeCap = Math.min(level()+3, 10);

		defaultAction = AC_PLAY;

		unique = true;
		bones = false;
	}

	public static final String AC_PLAY = "PLAY";

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if ((isEquipped( hero ) || hero.hasTalent(Talent.TRAVELING_MUSICIAN))
				&& !cursed
				&& hero.buff(MagicImmune.class) == null) {
			actions.add(AC_PLAY);
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute(hero, action);

		if (hero.buff(MagicImmune.class) != null) return;

		if (action.equals(AC_PLAY)) {

			if (!isEquipped(hero) && !hero.hasTalent(Talent.TRAVELING_MUSICIAN)) GLog.i(Messages.get(Artifact.class, "need_to_equip"));
			else if (cursed)       GLog.i( Messages.get(this, "cursed") );
			else {

				GameScene.show(new WndBardSongs(this, hero, false));

			}

		}
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)){
			if (collect && hero.hasTalent(Talent.TRAVELING_MUSICIAN)){
				activate(hero);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean collect( Bag container ) {
		if (super.collect(container)){
			if (container.owner instanceof Hero
					&& passiveBuff == null
					&& ((Hero) container.owner).hasTalent(Talent.TRAVELING_MUSICIAN)){
				activate((Hero) container.owner);
			}
			return true;
		} else{
			return false;
		}
	}

	@Override
	protected void onDetach() {
		if (passiveBuff != null){
			passiveBuff.detach();
			passiveBuff = null;
		}
	}

	//used to ensure the lute has variable targeting logic for whatever song is being played
	public Song targetingSong = null;

	@Override
	public int targetingPos(Hero user, int dst) {
		if (targetingSong == null || targetingSong.targetingFlags() == -1) {
			return super.targetingPos(user, dst);
		} else {
			return new Ballistica( user.pos, dst, targetingSong.targetingFlags() ).collisionPos;
		}
	}

	//the songs the bard has learned. All others are learned via sheet music
	private ArrayList<Class<? extends Song>> knownSongs = new ArrayList<>(Arrays.asList(
			TranceSong.class,
			DanceSong.class,
			DissonantChordSong.class));

	public ArrayList<Song> knownSongs(){
		ArrayList<Song> songs = new ArrayList<>();
		for (Song song : Song.getAllSongs()){
			if (knownSongs.contains(song.getClass())){
				songs.add(song);
			}
		}
		return songs;
	}

	public boolean knowsSong( Song song ){
		return knownSongs.contains(song.getClass());
	}

	public void learnSong( Song song ){
		if (!knownSongs.contains(song.getClass())){
			knownSongs.add(song.getClass());
		}
	}

	public boolean canPlay( Hero hero, Song song ){
		return (isEquipped(hero) || (hero.hasTalent(Talent.TRAVELING_MUSICIAN) && hero.belongings.contains(this)))
				&& hero.buff(MagicImmune.class) == null
				&& charge >= song.chargeUse(hero)
				&& song.canCast(hero);
	}

	public void spendCharge( float chargesSpent ){
		partialCharge -= chargesSpent;
		while (partialCharge < 0){
			charge--;
			partialCharge++;
		}

		//closing chord: spending the lute's last charge grants shielding
		if (charge <= 0 && Dungeon.hero != null && Dungeon.hero.hasTalent(Talent.CLOSING_CHORD)){
			int shield = 1 + 2*Dungeon.hero.pointsInTalent(Talent.CLOSING_CHORD);
			Buff.affect(Dungeon.hero, Barrier.class).setShield(shield);
			Dungeon.hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shield), FloatingText.SHIELDING);
		}

		//target hero level is 1 + 2*lute level
		int lvlDiffFromTarget = Dungeon.hero.lvl - (1+level()*2);
		//plus an extra one for each level after 6
		if (level() >= 7){
			lvlDiffFromTarget -= level()-6;
		}

		if (lvlDiffFromTarget >= 0){
			exp += Math.round(chargesSpent * 10f * Math.pow(1.1f, lvlDiffFromTarget));
		} else {
			exp += Math.round(chargesSpent * 10f * Math.pow(0.75f, -lvlDiffFromTarget));
		}

		if (exp >= (level() + 1) * 50 && level() < levelCap) {
			upgrade();
			Catalog.countUse(Lute.class);
			exp -= level() * 50;
			GLog.p(Messages.get(this, "levelup"));
		}

		updateQuickslot();
	}

	public void directCharge(float amount){
		if (charge < chargeCap) {
			partialCharge += amount;
			while (partialCharge >= 1f) {
				charge++;
				partialCharge--;
			}
			if (charge >= chargeCap){
				partialCharge = 0;
				charge = chargeCap;
			}
			updateQuickslot();
		}
	}

	//instantly fills the lute's charge, used by the tester's charm
	public void fullCharge() {
		charge = chargeCap;
		partialCharge = 0;
		updateQuickslot();
	}

	@Override
	public Item upgrade() {
		chargeCap = Math.min(chargeCap + 1, 10);
		return super.upgrade();
	}

	//dinner show: the lute is +1 level for a few songs after the bard eats.
	// Song effects that scale with lute level should read buffedLvl(), not level()
	@Override
	public int buffedLvl() {
		if (Dungeon.hero != null && Dungeon.hero.buff(Talent.DinnerShowTracker.class) != null
				&& Dungeon.hero.belongings.contains(this)){
			return level() + 1;
		}
		return level();
	}

	@Override
	public int buffedVisiblyUpgraded() {
		return levelKnown ? Math.round((buffedLvl()*10)/(float)levelCap): 0;
	}

	//a song the bard has set for quick casting, via the action indicator
	private Song quickSong = null;

	public void setQuickSong(Song song){
		if (quickSong == song){
			quickSong = null; //re-assigning the same song clears the quick song
			if (passiveBuff != null){
				ActionIndicator.clearAction((ActionIndicator.Action) passiveBuff);
			}
		} else {
			quickSong = song;
			if (passiveBuff != null){
				ActionIndicator.setAction((ActionIndicator.Action) passiveBuff);
			}
		}
	}

	private static final String KNOWN_SONGS = "known_songs";
	private static final String QUICK_CLS = "quick_cls";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(KNOWN_SONGS, knownSongs.toArray(new Class[0]));
		if (quickSong != null) {
			bundle.put(QUICK_CLS, quickSong.getClass());
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(KNOWN_SONGS)){
			knownSongs.clear();
			for (Class<?> cls : bundle.getClassArray(KNOWN_SONGS)){
				knownSongs.add((Class<? extends Song>) cls);
			}
		}
		if (bundle.contains(QUICK_CLS)){
			Class quickCls = bundle.getClass(QUICK_CLS);
			for (Song song : Song.getAllSongs()){
				if (song.getClass() == quickCls){
					quickSong = song;
				}
			}
		}
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new LuteRecharge();
	}

	@Override
	public void charge(Hero target, float amount) {
		if (cursed || target.buff(MagicImmune.class) != null) return;

		if (charge < chargeCap) {
			if (!isEquipped(target)) amount *= 0.25f*target.pointsInTalent(Talent.TRAVELING_MUSICIAN);
			partialCharge += 0.25f*amount;
			while (partialCharge >= 1f) {
				charge++;
				partialCharge--;
			}
			if (charge >= chargeCap){
				partialCharge = 0;
				charge = chargeCap;
			}
			updateQuickslot();
		}
	}

	public class LuteRecharge extends ArtifactBuff implements ActionIndicator.Action {

		@Override
		public boolean attachTo(Char target) {
			if (super.attachTo(target)) {
				if (quickSong != null) ActionIndicator.setAction(this);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void detach() {
			super.detach();
			ActionIndicator.clearAction(this);
		}

		@Override
		public String actionName() {
			return quickSong.name();
		}

		@Override
		public int actionIcon() {
			return quickSong.icon();
		}

		@Override
		public int indicatorColor() {
			return 0x442266;
		}

		@Override
		public void doAction() {
			if (cursed){
				GLog.w(Messages.get(Lute.this, "cursed"));
				return;
			}

			if (!canPlay(Dungeon.hero, quickSong)){
				GLog.w(Messages.get(Lute.this, "no_song"));
				return;
			}

			if (QuickSlotButton.targetingSlot != -1 &&
					Dungeon.quickslot.getItem(QuickSlotButton.targetingSlot) == Lute.this) {
				targetingSong = quickSong;
				int cell = QuickSlotButton.autoAim(QuickSlotButton.lastTarget, Lute.this);

				if (cell != -1){
					GameScene.handleCell(cell);
				} else {
					//couldn't auto-aim, just target the position and hope for the best.
					GameScene.handleCell( QuickSlotButton.lastTarget.pos );
				}
			} else {
				quickSong.onCast(Lute.this, Dungeon.hero);

				if (quickSong.targetingFlags() != -1 && Dungeon.quickslot.contains(Lute.this)){
					targetingSong = quickSong;
					QuickSlotButton.useTargeting(Dungeon.quickslot.getSlot(Lute.this));
				}
			}
		}

		@Override
		public boolean act() {
			if (charge < chargeCap && !cursed && target.buff(MagicImmune.class) == null) {
				if (Regeneration.regenOn()) {
					float turnsToCharge = 45 - (chargeCap - charge);
					turnsToCharge /= RingOfEnergy.artifactChargeMultiplier(target);
					float chargeToGain = (1f / turnsToCharge);
					//25/50/75% recharge speed when unequipped, from traveling musician
					if (!isEquipped(Dungeon.hero)){
						chargeToGain *= 0.25f*Dungeon.hero.pointsInTalent(Talent.TRAVELING_MUSICIAN);
					}
					partialCharge += chargeToGain;
				}

				while (partialCharge >= 1) {
					charge++;
					partialCharge -= 1;
					if (charge == chargeCap){
						partialCharge = 0;
					}
				}
			} else {
				partialCharge = 0;
			}

			updateQuickslot();

			spend( TICK );

			return true;
		}

	}

}
