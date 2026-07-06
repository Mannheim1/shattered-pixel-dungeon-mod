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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.songs.Song;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

//TEMPORARY dev item: makes the hero invulnerable while it is in their inventory,
// and provides cheats for playtesting the bard. Remove before any release
public class TestersCharm extends Item {

	public static final String AC_SONGS     = "SONGS";
	public static final String AC_RECHARGE  = "RECHARGE";

	{
		image = ItemSpriteSheet.SOMETHING;

		unique = true;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_SONGS);
		actions.add(AC_RECHARGE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {

		super.execute(hero, action);

		if (action.equals(AC_SONGS)) {

			Lute lute = hero.belongings.getItem(Lute.class);
			if (lute == null) {
				GLog.w(Messages.get(this, "no_lute"));
			} else {
				for (Song song : Song.getAllSongs()) {
					lute.learnSong(song);
				}
				GLog.p(Messages.get(this, "songs_unlocked"));
			}

		} else if (action.equals(AC_RECHARGE)) {

			Lute lute = hero.belongings.getItem(Lute.class);
			if (lute != null) {
				lute.fullCharge();
			}
			//covers any other artifacts the hero is testing with
			Buff.affect(hero, ArtifactRecharge.class).set(50);
			GLog.p(Messages.get(this, "recharged"));

		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

}
