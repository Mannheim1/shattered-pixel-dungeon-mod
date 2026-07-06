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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

//applied by the bard's dance song. The target cannot move or make melee attacks,
// but can still use ranged and magical attacks. It appears to dance in place.
public class Dancing extends FlavourBuff {

	public static final float DURATION	= 10f;

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	@Override
	public boolean attachTo( Char target ) {
		if (super.attachTo( target )) {
			target.rooted = true;
			//sleeping characters are woken by the compulsion to dance
			// (also ensures the dancing animation isn't overridden by sleep visuals)
			if (target instanceof Mob && ((Mob) target).state == ((Mob) target).SLEEPING) {
				((Mob) target).state = ((Mob) target).WANDERING;
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void detach() {
		super.detach();
		//don't un-root the target if something else is also rooting it
		if (target.buff(Roots.class) == null
				&& target.buff(Dancing.class) == null
				&& target.buff(Marionette.class) == null) {
			target.rooted = false;
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.DANCING;
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	@Override
	public void fx(boolean on) {
		if (target.sprite != null) {
			if (on) {
				target.sprite.dance();
			} else if (target.paralysed == 0) {
				target.sprite.idle();
			}
		}
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns());
	}

}
