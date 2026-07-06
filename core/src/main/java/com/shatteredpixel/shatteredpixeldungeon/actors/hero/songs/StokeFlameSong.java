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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class StokeFlameSong extends Song {

	public static final StokeFlameSong INSTANCE = new StokeFlameSong();

	private static final int STOKE_RADIUS = 4;

	@Override
	public int icon() {
		//placeholder, no song icons exist yet
		return HeroIcon.ELEMENTAL_BLAST;
	}

	@Override
	public void onCast(Lute lute, Hero hero) {

		//gather affected enemies and ember tiles up front, so we can refuse a wasted cast
		ArrayList<Char> enemies = new ArrayList<>();
		ArrayList<Integer> embers = new ArrayList<>();

		for (int i = 0; i < Dungeon.level.length(); i++) {
			if (!Dungeon.level.solid[i] && Dungeon.level.distance(hero.pos, i) <= STOKE_RADIUS) {

				if (Dungeon.level.map[i] == Terrain.EMBERS) {
					embers.add(i);
				}

				Char ch = Actor.findChar(i);
				if (ch != null && ch.alignment == Char.Alignment.ENEMY) {
					enemies.add(ch);
				}

			}
		}

		if (enemies.isEmpty() && embers.isEmpty()) {
			GLog.w(Messages.get(this, "no_targets"));
			return;
		}

		hero.sprite.operate(hero.pos);
		Sample.INSTANCE.play(Assets.Sounds.BURNING);

		//reignite embers into open flame
		for (int cell : embers) {
			GameScene.add(Blob.seed(cell, 2, Fire.class));
			if (Dungeon.level.heroFOV[cell]) {
				CellEmitter.get(cell).burst(FlameParticle.FACTORY, 3);
			}
		}

		for (Char ch : enemies) {

			//enemies that were already burning take an immediate burst of damage
			if (ch.buff(Burning.class) != null) {
				ch.damage(Hero.heroDamageIntRange(4, 10), this);
			}

			//then everything still standing is set ablaze
			if (ch.isAlive()) {
				Buff.affect(ch, Burning.class).reignite(ch);
			}

			ch.sprite.emitter().burst(FlameParticle.FACTORY, 5);
		}

		hero.spend(1f);
		hero.next();

		onSongCast(lute, hero);
	}

}
