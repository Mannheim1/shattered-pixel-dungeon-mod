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

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

//applied by the bard's requiem to undead and demonic enemies.
// They take damage each turn and deal reduced damage.
public class LaidToRest extends Buff {

	public static final float DURATION	= 10f;

	private int left = (int)DURATION;

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	public void reset() {
		left = (int)DURATION;
	}

	@Override
	public boolean act() {

		target.damage(Random.NormalIntRange(1, 3), this);

		if (!target.isAlive()) {
			return true;
		}

		left--;
		if (left <= 0) {
			detach();
			return true;
		}

		spend(TICK);
		return true;
	}

	private static final String LEFT = "left";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LEFT, left);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		left = bundle.getInt(LEFT);
	}

	@Override
	public int icon() {
		return BuffIndicator.LAID_TO_REST;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", left);
	}

}
