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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Marionette;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.NoteParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShaftParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.particles.Emitter;

public class MarionetteWaltzSong extends TargetedSong {

	public static final MarionetteWaltzSong INSTANCE = new MarionetteWaltzSong();

	public static final float BASE_DURATION = 5f;
	//the song casts at +4 lute levels against a dancing target
	public static final int DANCE_BONUS_LVLS = 4;

	@Override
	public int icon() {
		return HeroIcon.MARIONETTE_WALTZ;
	}

	@Override
	protected String castSound() {
		return Assets.Sounds.CHARMS;
	}

	@Override
	public int noteColor() {
		return 0x998F5C;
	}

	public static float duration(int lvl) {
		return BASE_DURATION + lvl;
	}

	@Override
	protected void affectTarget(Lute lute, Hero hero, Char ch) {
		//against a dancing target the waltz casts at bonus levels, consuming the dance
		int lvl = lute.buffedLvl();
		Dancing dance = ch.buff(Dancing.class);
		if (dance != null) {
			lvl += DANCE_BONUS_LVLS;
			dance.detach();
		}

		//note that the marionette's duration is internal, so liquid cadenza doesn't affect it
		ch.sprite.centerEmitter().start(noteFactory(), 0.3f, 5);
		ch.sprite.emitter().start(StringParticle.FACTORY, 0.2f, 4);
		Buff.affect(ch, Marionette.class).set((int)duration(lvl));
	}

	//puppet strings: thin shafts that rise from the target and fade away
	public static class StringParticle extends ShaftParticle {

		public static final Emitter.Factory FACTORY = new Emitter.Factory() {
			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				((StringParticle)emitter.recycle( StringParticle.class )).reset( x, y );
			}
			@Override
			public boolean lightMode() {
				return true;
			}
		};

		@Override
		public void reset( float x, float y ) {
			super.reset( x, y );
			hardlight( INSTANCE.noteColor() );
		}
	}

	@Override
	protected Object[] descArgs() {
		int lvl = luteLvl();
		return new Object[]{ (int)duration(lvl), (int)duration(lvl + DANCE_BONUS_LVLS) };
	}

}
