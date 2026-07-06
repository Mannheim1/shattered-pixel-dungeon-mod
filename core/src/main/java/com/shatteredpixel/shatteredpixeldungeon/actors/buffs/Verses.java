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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

//the skald's subclass mechanic. Landing weapon attacks builds verses,
// which are all spent to empower the next song played
public class Verses extends Buff {

	public static final int BASE_CAP = 3;

	private int verses = 0;

	{
		type = buffType.POSITIVE;
	}

	public int verses() {
		return verses;
	}

	public void addVerse() {
		int cap = BASE_CAP;
		if (target instanceof Hero) {
			cap += ((Hero) target).pointsInTalent(Talent.EXTENDED_BALLAD);
		}

		if (verses < cap) {
			verses++;

			//rousing verse: gaining a verse also grants shielding
			if (target instanceof Hero && ((Hero) target).hasTalent(Talent.ROUSING_VERSE)) {
				Buff.affect(target, Barrier.class).incShield(((Hero) target).pointsInTalent(Talent.ROUSING_VERSE));
			}
		}

		BuffIndicator.refreshHero();
	}

	@Override
	public boolean act() {
		spend(TICK);
		return true;
	}

	private static final String VERSES = "verses";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(VERSES, verses);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		verses = bundle.getInt(VERSES);
	}

	@Override
	public int icon() {
		return BuffIndicator.MOMENTUM; //placeholder, no verses icon yet
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", verses, 20*verses);
	}

}
