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

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalWisp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Eye;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.FungalSentry;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warlock;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

import java.util.Arrays;
import java.util.HashSet;

//applied by the bard's nocturne song. Silenced characters cannot use magical attacks
// from range, which neuters casters. Melee and physical ranged attacks are unaffected.
public class Silenced extends FlavourBuff {

	public static final float DURATION	= 5f;

	//mobs which attack from a distance using magic, whose ranged attacks are blocked
	// while silenced. Subclasses (e.g. shaman variants) are included automatically.
	//centralized here, like AntiMagic.RESISTS, so upstream mob files stay unmodified.
	//NOTE: when merging future updates, any new ranged caster must be added here
	// manually, or silence will have no effect on it
	public static final HashSet<Class> MAGICAL_RANGED_MOBS = new HashSet<>(Arrays.asList(
			Shaman.class,
			CrystalWisp.class,
			DM100.class,
			Elemental.class,
			Eye.class,
			FungalSentry.class,
			Warlock.class,
			YogFist.class
	));

	public static boolean blocksRangedAttack( Mob mob ){
		for (Class<?> cls : MAGICAL_RANGED_MOBS){
			if (cls.isInstance(mob)){
				return true;
			}
		}
		return false;
	}

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	@Override
	public int icon() {
		return BuffIndicator.SILENCED;
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns());
	}

}
