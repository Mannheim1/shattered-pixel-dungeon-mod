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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LaidToRest;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class RequiemSong extends Song {

	public static final RequiemSong INSTANCE = new RequiemSong();

	@Override
	public int icon() {
		//placeholder, no song icons exist yet
		return HeroIcon.HALLOWED_GROUND;
	}

	@Override
	public void onCast(Lute lute, Hero hero) {

		ArrayList<Char> affected = new ArrayList<>();
		for (Char ch : Actor.chars()) {
			if (ch.alignment == Char.Alignment.ENEMY
					&& Dungeon.level.heroFOV[ch.pos]
					&& (Char.hasProp(ch, Char.Property.UNDEAD) || Char.hasProp(ch, Char.Property.DEMONIC))) {
				affected.add(ch);
			}
		}

		if (affected.isEmpty()) {
			GLog.w(Messages.get(this, "no_targets"));
			return;
		}

		hero.sprite.operate(hero.pos);
		Sample.INSTANCE.play(Assets.Sounds.GHOST);
		hero.sprite.centerEmitter().start(Speck.factory(Speck.NOTE), 0.3f, 5);

		for (Char ch : affected) {
			Buff.affect(ch, LaidToRest.class).reset();
			ch.sprite.centerEmitter().start(Speck.factory(Speck.NOTE), 0.3f, 3);
		}

		hero.spend(1f);
		hero.next();

		onSongCast(lute, hero);
	}

}
