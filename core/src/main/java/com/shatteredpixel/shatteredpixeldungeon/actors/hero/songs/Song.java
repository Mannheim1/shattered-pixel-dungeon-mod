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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

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
