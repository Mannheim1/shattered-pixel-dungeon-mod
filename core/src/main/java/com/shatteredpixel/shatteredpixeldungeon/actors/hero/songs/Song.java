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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Verses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import java.util.ArrayList;

public abstract class Song {

	public abstract void onCast(Lute lute, Hero hero);

	public float chargeUse( Hero hero ){
		return 1;
	}

	public boolean canCast( Hero hero ){
		return true;
	}

	public String name(){
		return Messages.get(this, "name");
	}

	public String shortDesc(){
		return Messages.get(this, "short_desc") + " " + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
	}

	public String desc(){
		return Messages.get(this, "desc") + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
	}

	public int targetingFlags(){
		return -1; //-1 for no targeting
	}

	public int icon(){
		return HeroIcon.NONE;
	}

	//effects that trigger on every song cast
	public void onSongCast(Lute lute, Hero hero){
		Invisibility.dispel();
		lute.spendCharge(chargeUse(hero));
		Talent.onArtifactUsed(hero);

		//dinner show: the lute is +1 level for a limited number of songs, this one included
		Talent.DinnerShowTracker dinnerShow = hero.buff(Talent.DinnerShowTracker.class);
		if (dinnerShow != null){
			dinnerShow.countDown(1);
			if (dinnerShow.count() <= 0){
				dinnerShow.detach();
			}
		}

		//accented strike: playing a song accents the bard's next physical attack
		if (hero.hasTalent(Talent.ACCENTED_STRIKE)){
			Buff.affect(hero, Talent.AccentedStrikeTracker.class, 5f);
		}

		//liquid cadenza is consumed by playing a song
		Talent.LiquidCadenzaTracker cadenza = hero.buff(Talent.LiquidCadenzaTracker.class);
		if (cadenza != null){
			cadenza.detach();
		}

		//echolocation: performing reveals nearby enemies through walls
		if (hero.hasTalent(Talent.ECHOLOCATION)){
			int range = 2 + 2*hero.pointsInTalent(Talent.ECHOLOCATION);
			int duration = 1 + 2*hero.pointsInTalent(Talent.ECHOLOCATION);
			for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
				if (Dungeon.level.distance(hero.pos, mob.pos) <= range){
					Buff.append(hero, TalismanOfForesight.CharAwareness.class, duration).charID = mob.id();
				}
			}
		}

		//encore: chance to refund the song's charge cost
		if (hero.hasTalent(Talent.ENCORE)
				&& Random.Float() < 0.10f*hero.pointsInTalent(Talent.ENCORE)){
			lute.directCharge(chargeUse(hero));
			GLog.p(Messages.get(Song.class, "encore"));
		}

		//the skald's verses are all spent by playing a song
		Verses verses = hero.buff(Verses.class);
		if (verses != null){
			verses.detach();
		}

		//whetted blade: playing a song whets the skald's next attacks
		if (hero.subClass == HeroSubClass.SKALD && hero.hasTalent(Talent.WHETTED_BLADE)){
			Talent.WhettedBladeTracker whet = Buff.affect(hero, Talent.WhettedBladeTracker.class);
			float attacks = hero.pointsInTalent(Talent.WHETTED_BLADE);
			if (whet.count() < attacks){
				whet.countUp(attacks - whet.count());
			}
		}
	}

	//applies talent and subclass effects (e.g. liquid cadenza, verses) to the duration of a song's effect
	public static float modifyDuration(float baseDuration){
		Hero hero = Dungeon.hero;
		if (hero != null){
			if (hero.buff(Talent.LiquidCadenzaTracker.class) != null){
				baseDuration *= 1f + 0.25f*hero.pointsInTalent(Talent.LIQUID_CADENZA);
			}
			Verses verses = hero.buff(Verses.class);
			if (verses != null){
				baseDuration *= 1f + 0.2f*verses.verses();
			}
		}
		return baseDuration;
	}

	//applies subclass effects (verses) to the damage a song deals
	public static int modifyDamage(int damage){
		Hero hero = Dungeon.hero;
		if (hero != null){
			Verses verses = hero.buff(Verses.class);
			if (verses != null){
				damage = Math.round(damage * (1f + 0.2f*verses.verses()));
			}
		}
		return damage;
	}

	public static ArrayList<Song> getAllSongs(){
		ArrayList<Song> songs = new ArrayList<>();
		songs.add(TranceSong.INSTANCE);
		songs.add(DanceSong.INSTANCE);
		songs.add(DissonantChordSong.INSTANCE);
		songs.add(DirgeSong.INSTANCE);
		songs.add(DiscordSong.INSTANCE);
		songs.add(MarchSong.INSTANCE);
		songs.add(DrumbeatSong.INSTANCE);
		songs.add(NocturneSong.INSTANCE);
		songs.add(MarionetteWaltzSong.INSTANCE);
		songs.add(RequiemSong.INSTANCE);
		songs.add(GrandFinaleSong.INSTANCE);
		songs.add(WinterWindSong.INSTANCE);
		songs.add(StokeFlameSong.INSTANCE);
		return songs;
	}

	public static ArrayList<Song> getKnownSongs(Hero hero){
		Lute lute = hero.belongings.getItem(Lute.class);
		if (lute != null){
			return lute.knownSongs();
		}
		return new ArrayList<>();
	}

}
