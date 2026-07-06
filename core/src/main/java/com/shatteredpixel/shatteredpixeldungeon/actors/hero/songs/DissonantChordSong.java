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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class DissonantChordSong extends TargetedSong {

	public static final DissonantChordSong INSTANCE = new DissonantChordSong();

	@Override
	public int icon() {
		//placeholder, no song icons exist yet
		return HeroIcon.GUIDING_LIGHT;
	}

	@Override
	protected void onTargetSelected(Lute lute, Hero hero, Integer target) {
		if (target == null) {
			return;
		}

		Ballistica aim = new Ballistica(hero.pos, target, targetingFlags());

		if (Actor.findChar(aim.collisionPos) == hero) {
			GLog.i(Messages.get(Wand.class, "self_target"));
			return;
		}

		if (Actor.findChar(aim.collisionPos) != null) {
			QuickSlotButton.target(Actor.findChar(aim.collisionPos));
		} else {
			QuickSlotButton.target(Actor.findChar(target));
		}

		hero.busy();
		Sample.INSTANCE.play(Assets.Sounds.ZAP);
		hero.sprite.zap(target);
		MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.MAGIC_MISSILE, hero.sprite, aim.collisionPos, new Callback() {
			@Override
			public void call() {

				Char ch = Actor.findChar(aim.collisionPos);
				if (ch != null) {
					affectTarget(lute, hero, ch);
					maybeReverb(lute, hero, ch);
				} else {
					Dungeon.level.pressCell(aim.collisionPos);
				}

				hero.spend(1f);
				hero.next();

				onSongCast(lute, hero);

			}
		});
	}

	@Override
	protected void affectTarget(Lute lute, Hero hero, Char ch) {
		ch.damage(modifyDamage(damageRoll(lute)), this);
		Sample.INSTANCE.play(Assets.Sounds.HIT_MAGIC, 1, Random.Float(0.87f, 1.15f));
		if (ch.sprite != null) {
			ch.sprite.burst(0xFFFFFF44, 3);
		}
	}

	public int damageRoll(Lute lute) {
		return Hero.heroDamageIntRange(2 + lute.level() / 2, 8 + lute.level());
	}

	@Override
	public String desc() {
		Lute lute = Dungeon.hero.belongings.getItem(Lute.class);
		int lvl = lute != null ? lute.level() : 0;
		return Messages.get(this, "desc", 2 + lvl / 2, 8 + lvl)
				+ "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
	}

}
