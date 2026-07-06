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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dancing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class DanceSong extends TargetedSong {

	public static final DanceSong INSTANCE = new DanceSong();

	@Override
	public int icon() {
		return HeroIcon.DANCE_SONG;
	}

	@Override
	protected void onTargetSelected(Lute lute, Hero hero, Integer target) {
		if (target == null) {
			return;
		}

		Ballistica aim = new Ballistica(hero.pos, target, targetingFlags());
		Char ch = Actor.findChar(aim.collisionPos);

		if (ch == hero) {
			GLog.i(Messages.get(Wand.class, "self_target"));
			return;
		} else if (ch == null) {
			GLog.w(Messages.get(this, "no_target"));
			return;
		}

		QuickSlotButton.target(ch);

		hero.sprite.operate(hero.pos);
		Sample.INSTANCE.play(Assets.Sounds.CHARMS);
		hero.sprite.centerEmitter().start(Speck.factory(Speck.NOTE), 0.3f, 5);

		affectTarget(lute, hero, ch);
		maybeReverb(lute, hero, ch);

		hero.spend(1f);
		hero.next();

		onSongCast(lute, hero);
	}

	@Override
	protected void affectTarget(Lute lute, Hero hero, Char ch) {
		ch.sprite.centerEmitter().start(Speck.factory(Speck.NOTE), 0.3f, 5);
		Buff.prolong(ch, Dancing.class, modifyDuration(Dancing.DURATION));
	}

}
