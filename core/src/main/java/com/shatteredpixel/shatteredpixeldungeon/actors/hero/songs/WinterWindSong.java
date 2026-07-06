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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blizzard;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ConfusionGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Inferno;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SmokeScreen;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.StenchGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.StormCloud;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SnowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class WinterWindSong extends Song {

	public static final WinterWindSong INSTANCE = new WinterWindSong();

	private static final int WIND_RADIUS    = 4;
	private static final int PUSH_DISTANCE  = 3;

	//gas-like blobs which the wind can push around
	private static final Class<?>[] PUSHABLE_GASES = new Class<?>[]{
			ToxicGas.class, CorrosiveGas.class, ConfusionGas.class, ParalyticGas.class,
			StenchGas.class, SmokeScreen.class, Inferno.class, Blizzard.class, StormCloud.class
	};

	@Override
	public int icon() {
		//placeholder, no song icons exist yet
		return HeroIcon.RECALL_GLYPH;
	}

	@Override
	public void onCast(Lute lute, Hero hero) {

		hero.sprite.operate(hero.pos);
		Sample.INSTANCE.play(Assets.Sounds.PUFF);

		//all non-solid cells within the wind's radius, including the hero's own cell
		ArrayList<Integer> cells = new ArrayList<>();
		for (int i = 0; i < Dungeon.level.length(); i++) {
			if (!Dungeon.level.solid[i] && Dungeon.level.distance(hero.pos, i) <= WIND_RADIUS) {
				cells.add(i);
			}
		}

		//push gases before anything else, so the wind's other effects apply to the cleared cells
		for (Class<?> gasClass : PUSHABLE_GASES) {
			Blob gas = Dungeon.level.blobs.get(gasClass);
			if (gas == null || gas.volume <= 0) {
				continue;
			}

			//collect all moves first, so volume never gets pushed twice in one gust
			ArrayList<int[]> moves = new ArrayList<>();
			for (int cell : cells) {
				int vol = gas.cur[cell];
				if (vol > 0) {
					int dest = pushDest(hero.pos, cell);
					if (dest != cell) {
						moves.add(new int[]{cell, dest, vol});
					}
				}
			}

			for (int[] move : moves) {
				gas.clear(move[0]);
				gas.seed(Dungeon.level, move[1], move[2]);
			}
		}

		Blob fire = Dungeon.level.blobs.get(Fire.class);
		for (int cell : cells) {

			//extinguish fires
			if (fire != null && fire.cur[cell] > 0) {
				fire.clear(cell);
			}

			if (Dungeon.level.heroFOV[cell]) {
				CellEmitter.get(cell).burst(SnowParticle.FACTORY, 2);
			}

			//chill and douse characters caught in the gust. The bard is doused but not chilled
			Char ch = Actor.findChar(cell);
			if (ch != null) {
				if (ch.buff(Burning.class) != null) {
					ch.buff(Burning.class).detach();
				}
				if (ch != hero) {
					Buff.affect(ch, Chill.class, 5f);
				}
			}
		}

		hero.spend(1f);
		hero.next();

		onSongCast(lute, hero);
	}

	//walks up to PUSH_DISTANCE steps away from the wind's origin, stopping at solid terrain
	private static int pushDest(int origin, int cell) {

		int width = Dungeon.level.width();
		int dx = (cell % width) - (origin % width);
		int dy = (cell / width) - (origin / width);
		dx = Math.max(-1, Math.min(1, dx));
		dy = Math.max(-1, Math.min(1, dy));

		//gas on the origin itself is blown in a random open direction
		if (dx == 0 && dy == 0) {
			ArrayList<Integer> dirs = new ArrayList<>();
			for (int offset : PathFinder.NEIGHBOURS8) {
				if (!Dungeon.level.solid[cell + offset]) {
					dirs.add(offset);
				}
			}
			if (dirs.isEmpty()) {
				return cell;
			}
			int dir = Random.element(dirs);
			dx = ((cell + dir) % width) - (cell % width);
			dy = ((cell + dir) / width) - (cell / width);
		}

		int dest = cell;
		for (int i = 0; i < PUSH_DISTANCE; i++) {
			int next = dest + dx + dy * width;
			if (Dungeon.level.solid[next]) {
				break;
			}
			dest = next;
		}
		return dest;
	}

}
