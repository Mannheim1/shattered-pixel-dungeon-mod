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
// They take damage each turn and deal reduced damage, both scaling with lute level.
public class LaidToRest extends Buff {

	public static final float DURATION	= 10f;

	private int left = (int)DURATION;
	private int luteLvl = 0;

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	public void set( int lvl ) {
		left = (int)DURATION;
		luteLvl = lvl;
	}

	//re-extends the buff without changing its power, used by grand finale's reprise
	public void refresh() {
		left = (int)DURATION;
	}

	//maestro finisher: the requiem claims the undead once they are weak enough
	private boolean execute = false;

	public void setExecute() {
		execute = true;
	}

	public static int minDmg( int lvl ) {
		return 2 + lvl/4;
	}

	public static int maxDmg( int lvl ) {
		return 4 + lvl/2;
	}

	//the bearer's dealt damage is reduced by 25%, up to 50% at lute level 10
	public static float dealtDamageFactor( int lvl ) {
		return 0.75f - 0.025f*lvl;
	}

	public float damageFactor( float dmg ) {
		return dmg * dealtDamageFactor(luteLvl);
	}

	@Override
	public boolean act() {

		target.damage(Random.NormalIntRange(minDmg(luteLvl), maxDmg(luteLvl)), this);

		if (!target.isAlive()) {
			return true;
		}

		//executing requiem: the target dies outright below one fifth of its max health
		if (execute && target.HP <= target.HT/5) {
			target.die(this);
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
	private static final String LUTE_LVL = "lute_lvl";
	private static final String EXECUTE = "execute";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LEFT, left);
		bundle.put(LUTE_LVL, luteLvl);
		bundle.put(EXECUTE, execute);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		left = bundle.getInt(LEFT);
		luteLvl = bundle.getInt(LUTE_LVL);
		execute = bundle.getBoolean(EXECUTE);
	}

	@Override
	public int icon() {
		return BuffIndicator.LAID_TO_REST;
	}

	@Override
	public String desc() {
		String desc = Messages.get(this, "desc", (int)(100 * (1f - dealtDamageFactor(luteLvl))), left);
		if (execute){
			desc += "\n\n" + Messages.get(this, "execute_desc");
		}
		return desc;
	}

}
