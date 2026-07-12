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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FireImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.NoteParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class StokeFlameSong extends Song {

	public static final StokeFlameSong INSTANCE = new StokeFlameSong();

	@Override
	public int icon() {
		return HeroIcon.STOKE_FLAME;
	}

	@Override
	public int noteColor() {
		return 0xFF7722;
	}

	//the song's radius grows from 4 to 8 tiles by lute level 10
	public static int radius(int lvl) {
		return 4 + 2*lvl/5;
	}

	//maestro finisher: fire imbue duration
	public static final float FINISHER_IMBUE = 10f;

	//the burst against burning enemies scales linearly, to about 2x its base value at lute level 10
	public static int min(int lvl) {
		return Math.round(4 * (1f + 0.1f*lvl));
	}

	public static int max(int lvl) {
		return Math.round(10 * (1f + 0.1f*lvl));
	}

	@Override
	public void onCast(Lute lute, Hero hero) {

		int lvl = lute.buffedLvl();

		//gather affected enemies and ember tiles up front, so we can refuse a wasted cast
		ArrayList<Char> enemies = new ArrayList<>();
		ArrayList<Integer> embers = new ArrayList<>();

		for (int i = 0; i < Dungeon.level.length(); i++) {
			if (!Dungeon.level.solid[i] && Dungeon.level.distance(hero.pos, i) <= radius(lvl)) {

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
		hero.sprite.centerEmitter().start(noteFactory(), 0.3f, 5);
		hero.sprite.centerEmitter().burst(FLARE, 30);

		//maestro finisher: the bard is imbued with fire, and the heat boils away
		// standing water before the flames catch
		if (maestroFinisher()){
			Buff.affect(hero, FireImbue.class).set(FINISHER_IMBUE);
			for (int i = 0; i < Dungeon.level.length(); i++) {
				if (Dungeon.level.map[i] == Terrain.WATER
						&& Dungeon.level.distance(hero.pos, i) <= radius(lvl)) {
					Level.set(i, Terrain.EMPTY);
					GameScene.updateMap(i);
					if (Dungeon.level.heroFOV[i]) {
						CellEmitter.get(i).burst(Speck.factory(Speck.STEAM), 3);
					}
				}
			}
		}

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
				ch.damage(modifyDamage(Hero.heroDamageIntRange(min(lvl), max(lvl))), this);
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

	@Override
	protected Object[] descArgs() {
		int lvl = luteLvl();
		return new Object[]{ radius(lvl), min(lvl), max(lvl) };
	}

	//flames that lick outward from the bard in all directions.
	// safe to redirect recycled flames, as FlameParticle.reset always zeroes speed
	private static final Emitter.Factory FLARE = new Emitter.Factory() {
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			FlameParticle p = (FlameParticle)emitter.recycle( FlameParticle.class );
			p.reset( x, y );
			p.speed.polar( Random.Float( PointF.PI2 ), Random.Float( 32, 96 ) );
		}
		@Override
		public boolean lightMode() {
			return true;
		}
	};

}
