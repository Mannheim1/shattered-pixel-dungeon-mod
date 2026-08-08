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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silenced;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.NoteParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.AlarmTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GuardianTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class NocturneSong extends TargetedSong {

	public static final NocturneSong INSTANCE = new NocturneSong();

	@Override
	public int icon() {
		return HeroIcon.NOCTURNE;
	}

	@Override
	public int noteColor() {
		return 0x5A00B2;
	}

	public static float duration(int lvl) {
		return Silenced.DURATION + lvl;
	}

	//maestro finisher: the target is also briefly blinded
	public static final float FINISHER_BLIND = 5f;

	//the song harmlessly deactivates alarm and guardian traps it passes over
	@Override
	protected void affectPath(Ballistica aim) {
		for (int cell : aim.subPath(1, aim.dist)) {
			Trap trap = Dungeon.level.traps.get(cell);
			if (trap != null && trap.active
					&& (trap instanceof AlarmTrap || trap instanceof GuardianTrap)) {
				trap.reveal();
				trap.disarm();
				CellEmitter.get(cell).start(noteFactory(), 0.3f, 3);
				GLog.i(Messages.get(this, "trap_disarmed"));
			}
		}
	}

	@Override
	protected void affectTarget(Lute lute, Hero hero, Char ch) {
		ch.sprite.centerEmitter().start(noteFactory(), 0.3f, 5);
		Buff.prolong(ch, Silenced.class, modifyDuration(duration(lute.buffedLvl())));

		if (maestroFinisher()){
			Buff.prolong(ch, Blindness.class, FINISHER_BLIND);
		}
	}

	@Override
	protected Object[] descArgs() {
		return new Object[]{ (int)duration(luteLvl()) };
	}

}
