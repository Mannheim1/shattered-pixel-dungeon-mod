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

package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

//a tintable musical note glyph, used by the bard's songs. Behaves like the stock
// note speck, but picks a random glyph from a mod-owned sheet and takes a color
public class NoteParticle extends Image {

	private static final int SIZE = 7;
	//the number of glyphs on the sheet. Update when adding glyphs to mod_note_specks.png
	private static final int GLYPHS = 4;

	private static TextureFilm film;

	private float lifespan;
	private float left;

	public NoteParticle() {
		super();

		texture( Assets.Effects.MOD_NOTE_SPECKS );
		if (film == null) {
			film = new TextureFilm( texture, SIZE, SIZE );
		}

		origin.set( SIZE / 2f );
	}

	public void reset( float x, float y, int color ) {
		revive();

		frame( film.get( Random.Int( GLYPHS ) ));
		hardlight( color );

		this.x = x - origin.x;
		this.y = y - origin.y;

		scale.set( 1 );

		//drifts gently upward while swaying, like the stock note speck
		angle = 0;
		angularSpeed = Random.Float( -30, +30 );
		speed.polar( (angularSpeed - 90) * PointF.G2R, 30 );

		left = lifespan = 1f;
	}

	@Override
	public void update() {
		super.update();

		if ((left -= Game.elapsed) <= 0) {
			kill();
		} else {
			float p = 1 - left / lifespan;
			am = 1 - p * p;
		}
	}

	public static class Factory extends Emitter.Factory {

		private final int color;

		public Factory( int color ) {
			this.color = color;
		}

		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((NoteParticle)emitter.recycle( NoteParticle.class )).reset( x, y, color );
		}
	}

}
