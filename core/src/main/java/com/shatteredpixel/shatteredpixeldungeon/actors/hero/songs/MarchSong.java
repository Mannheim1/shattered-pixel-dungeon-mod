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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Marching;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.NoteParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

public class MarchSong extends Song {

	public static final MarchSong INSTANCE = new MarchSong();

	@Override
	public int icon() {
		return HeroIcon.MARCH;
	}

	public static float duration(int lvl) {
		return Marching.DURATION + 10*lvl;
	}

	@Override
	public int noteColor() {
		return 0x005826;
	}

	//maestro finisher: the bard is also briefly invisible
	public static final float FINISHER_INVIS = 5f;

	@Override
	public void onCast(Lute lute, Hero hero) {

		//captured up front: onSongCast consumes the performance, and its
		// Invisibility.dispel() would remove an invisibility applied before it
		boolean finisher = maestroFinisher();

		hero.sprite.operate(hero.pos);
		Sample.INSTANCE.play(Assets.Sounds.MASTERY);
		hero.sprite.centerEmitter().start(noteFactory(), 0.3f, 5);

		float duration = modifyDuration(duration(lute.buffedLvl()));

		Buff.prolong(hero, Marching.class, duration);

		for (Char ch : Actor.chars()) {
			if (ch != hero
					&& ch.alignment == Char.Alignment.ALLY
					&& Dungeon.level.heroFOV[ch.pos]) {
				Buff.prolong(ch, Marching.class, duration);
				ch.sprite.centerEmitter().start(noteFactory(), 0.3f, 3);
			}
		}

		hero.spend(1f);
		hero.next();

		onSongCast(lute, hero);

		if (finisher){
			Buff.affect(hero, Invisibility.class, FINISHER_INVIS);
		}
	}

	@Override
	protected Object[] descArgs() {
		return new Object[]{ (int)duration(luteLvl()) };
	}

}
