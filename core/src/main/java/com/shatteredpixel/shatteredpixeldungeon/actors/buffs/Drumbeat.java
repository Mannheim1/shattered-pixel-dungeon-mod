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

//applied by the bard's drumbeat song. Each cast adds a beat and refreshes the duration,
// the bearer deals bonus damage per beat. All beats are lost when the buff expires.
public class Drumbeat extends FlavourBuff {

	public static final float DURATION	= 10f;

	private int beats = 0;
	private int dmgPerBeat = 10;

	{
		type = buffType.POSITIVE;
		announced = true;
	}

	public void addBeat( int dmgPer ) {
		beats++;
		dmgPerBeat = dmgPer;
	}

	public float damageFactor( float dmg ) {
		return dmg * (1f + (dmgPerBeat/100f) * beats);
	}

	private static final String BEATS = "beats";
	private static final String DMG_PER_BEAT = "dmg_per_beat";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(BEATS, beats);
		bundle.put(DMG_PER_BEAT, dmgPerBeat);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		beats = bundle.getInt(BEATS);
		dmgPerBeat = bundle.getInt(DMG_PER_BEAT);
	}

	@Override
	public int icon() {
		return BuffIndicator.DRUMBEAT;
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", beats, dmgPerBeat*beats, dispTurns());
	}

}
