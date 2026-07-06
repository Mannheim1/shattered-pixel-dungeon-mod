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
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

public class DrumbeatSong extends Song {

	public static final DrumbeatSong INSTANCE = new DrumbeatSong();

	@Override
	public int icon() {
		//placeholder, no song icons exist yet
		return HeroIcon.SMITE;
	}

	@Override
	public void onCast(Lute lute, Hero hero) {

		hero.sprite.operate(hero.pos);
		Sample.INSTANCE.play(Assets.Sounds.HIT_CRUSH, 1f, 0.8f);
		hero.sprite.centerEmitter().start(Speck.factory(Speck.NOTE), 0.3f, 5);

		Buff.prolong(hero, Drumbeat.class, modifyDuration(Drumbeat.DURATION)).addBeat();

		hero.spend(1f);
		hero.next();

		onSongCast(lute, hero);
	}

}
