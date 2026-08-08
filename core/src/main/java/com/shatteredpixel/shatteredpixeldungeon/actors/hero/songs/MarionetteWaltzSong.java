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
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
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
		Marionette marionette = Buff.affect(ch, Marionette.class);
		marionette.set((int)duration(lvl));

		//maestro finisher: the puppet also mirrors the hero's attacks
		if (maestroFinisher()){
			marionette.setMirrorAttacks();
		}
	}

	//a single puppet string that hangs from above the marionette's head for as long
	// as the debuff lasts. It is solid near the head but fades out higher up, as if
	// extending up out of the map
	public static class PuppetString extends Image {

		private static final float WIDTH    = 1;
		private static final float HEIGHT   = 48;
		//how far the string's lower end reaches down past the top of the target's sprite
		private static final float OVERLAP  = 2;

		private CharSprite target;

		public PuppetString( CharSprite target ) {
			//a linearly filtered strip: the lower half of the string is solid,
			// the upper half fades out
			super( TextureCache.createGradient( 0xFFFFFFFF, 0xFFFFFFFF, 0x00FFFFFF ) );
			this.target = target;
			hardlight( INSTANCE.noteColor() );

			//rotate the strip so it runs vertically, solid end at the bottom
			origin.set( 0 );
			angle = -90;
			scale.set( HEIGHT / width, WIDTH );
		}

		@Override
		public void update() {
			super.update();

			visible = target.visible;
			//(x, y) anchors the bottom of the string
			x = target.center().x - WIDTH / 2f;
			y = target.y + OVERLAP;
		}
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
