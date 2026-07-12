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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Drumbeat;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.NoteParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

public class DrumbeatSong extends Song {

	public static final DrumbeatSong INSTANCE = new DrumbeatSong();

	@Override
	public int icon() {
		return HeroIcon.DRUMBEAT;
	}

	public static int dmgPerBeat(int lvl) {
		return 10 + lvl;
	}

	@Override
	public int noteColor() {
		return 0xFFFF66;
	}

	@Override
	public void onCast(Lute lute, Hero hero) {

		hero.sprite.operate(hero.pos);
		Sample.INSTANCE.play(Assets.Sounds.HIT_CRUSH, 1f, 0.8f);
		hero.sprite.centerEmitter().start(noteFactory(), 0.3f, 5);

		//maestro finisher: the song grants three beats instead of one, converting a
		// long performance into an instant war-rhythm
		int beats = maestroFinisher() ? 3 : 1;
		Drumbeat drumbeat = Buff.prolong(hero, Drumbeat.class, modifyDuration(Drumbeat.DURATION));
		for (int i = 0; i < beats; i++){
			drumbeat.addBeat(dmgPerBeat(lute.buffedLvl()));
		}

		hero.spend(1f);
		hero.next();

		onSongCast(lute, hero);
	}

	@Override
	protected Object[] descArgs() {
		return new Object[]{ dmgPerBeat(luteLvl()) };
	}

}
