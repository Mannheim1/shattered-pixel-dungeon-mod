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

//a tintable musical note glyph, used by the bard's songs and debuffs. Picks a
// random glyph from a mod-owned sheet, and takes a color and a motion pattern
public class NoteParticle extends Image {

	private static final int SIZE = 7;
	//the number of glyphs on the sheet. Update when adding glyphs to mod_note_specks.png
	private static final int GLYPHS = 4;

	//how the note moves over its lifespan
	public enum Motion {
		DRIFT,  //drifts gently upward while swaying, like the stock note speck
		SWING,  //swings side to side in place, used by the dancing debuff
		ORBIT,  //circles counterclockwise around its spawn point, used by the trance debuff
		BURST   //flies outward in a random direction, used for striking effects
	}

	private static final float SWING_AMP    = 5f;   //pixels of horizontal swing
	private static final float SWING_CYCLES = 1.5f; //full swings per lifespan
	private static final float ORBIT_ARC    = 0.6f * PointF.PI; //radians travelled per lifespan

	private static TextureFilm film;

	private float lifespan;
	private float left;

	private Motion motion;
	private float anchorX, anchorY;
	private float phase;
	private float radius;

	public NoteParticle() {
		super();

		texture( Assets.Effects.MOD_NOTE_SPECKS );
		if (film == null) {
			film = new TextureFilm( texture, SIZE, SIZE );
		}

		origin.set( SIZE / 2f );
	}

	public void reset( float x, float y, int color, Motion motion ) {
		revive();

		frame( film.get( Random.Int( GLYPHS ) ));
		hardlight( color );

		this.motion = motion;
		this.x = x - origin.x;
		this.y = y - origin.y;

		scale.set( 1 );
		angle = 0;
		angularSpeed = 0;
		speed.set( 0 );
		phase = Random.Float( PointF.PI2 );

		switch (motion) {
			case DRIFT:
				angularSpeed = Random.Float( -30, +30 );
				speed.polar( (angularSpeed - 90) * PointF.G2R, 20 );
				break;
			case SWING:
				anchorX = x;
				anchorY = y + Random.Float( -4, +4 );
				this.y = anchorY - origin.y;
				//rises and skews like a drifting note, on top of the swinging
				angularSpeed = Random.Float( -30, +30 );
				speed.set( 0, -20 );
				break;
			case ORBIT:
				anchorX = x;
				anchorY = y;
				radius = Random.Float( 8, 11 );
				break;
			case BURST:
				angularSpeed = Random.Float( -30, +30 );
				speed.polar( Random.Float( PointF.PI2 ), Random.Float( 48, 80 ) );
				break;
		}

		//burst notes are quick and short-lived, other motions linger for a full second
		left = lifespan = motion == Motion.BURST ? 0.4f : 1f;
	}

	@Override
	public void update() {
		super.update();

		if ((left -= Game.elapsed) <= 0) {
			kill();
		} else {
			float p = 1 - left / lifespan;
			am = 1 - p * p;

			switch (motion) {
				case SWING:
					x = anchorX + SWING_AMP * (float)Math.sin( phase + p * SWING_CYCLES * PointF.PI2 ) - origin.x;
					break;
				case ORBIT:
					//the angle decreases so the note circles counterclockwise on screen
					float a = phase - p * ORBIT_ARC;
					x = anchorX + radius * (float)Math.cos( a ) - origin.x;
					y = anchorY + radius * (float)Math.sin( a ) - origin.y;
					break;
				case DRIFT: default:
					//super.update() already applies drift movement
					break;
			}
		}
	}

	public static class Factory extends Emitter.Factory {

		private final int color;
		private final Motion motion;

		public Factory( int color ) {
			this( color, Motion.DRIFT );
		}

		public Factory( int color, Motion motion ) {
			this.color = color;
			this.motion = motion;
		}

		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			((NoteParticle)emitter.recycle( NoteParticle.class )).reset( x, y, color, motion );
		}
	}

}
