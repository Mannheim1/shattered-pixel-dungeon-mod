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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dancing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.NoteParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class DanceSong extends TargetedSong {

	public static final DanceSong INSTANCE = new DanceSong();

	@Override
	public int icon() {
		return HeroIcon.DANCE_SONG;
	}

	@Override
	public int noteColor() {
		return 0x52ABFF;
	}

	@Override
	protected String castSound() {
		return Assets.Sounds.CHARMS;
	}

	public static float duration(int lvl) {
		return Dancing.DURATION + lvl;
	}

	public static float breakChance(int lvl) {
		return 0.50f - 0.025f*lvl;
	}

	//maestro finisher: the target is also briefly vulnerable
	public static final float FINISHER_VULNERABLE = 5f;

	@Override
	protected void affectTarget(Lute lute, Hero hero, Char ch) {
		ch.sprite.centerEmitter().start(noteFactory(), 0.3f, 5);
		int lvl = lute.buffedLvl();
		Buff.prolong(ch, Dancing.class, modifyDuration(duration(lvl))).setBreakChance(breakChance(lvl));

		if (maestroFinisher()){
			Buff.prolong(ch, Vulnerable.class, FINISHER_VULNERABLE);
		}
	}

	@Override
	protected Object[] descArgs() {
		int lvl = luteLvl();
		return new Object[]{ (int)duration(lvl), (int)(100*breakChance(lvl)) };
	}

}
