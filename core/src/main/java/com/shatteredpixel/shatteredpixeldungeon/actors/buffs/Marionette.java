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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.songs.MarionetteWaltzSong;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

//applied by the bard's marionette waltz. The target cannot move on its own,
// and instead mirrors the hero's movements step for step.
public class Marionette extends Buff {

	public static final float DURATION	= 5f;

	private int left = (int)DURATION;
	private int lastHeroPos = -1;

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	@Override
	public boolean attachTo( Char target ) {
		if (super.attachTo( target )) {
			target.rooted = true;
			//sleeping characters are woken by the compulsion to dance
			if (target instanceof Mob && ((Mob) target).state == ((Mob) target).SLEEPING) {
				((Mob) target).state = ((Mob) target).WANDERING;
			}
			if (Dungeon.hero != null) {
				lastHeroPos = Dungeon.hero.pos;
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void detach() {
		super.detach();
		//don't un-root the target if something else is also rooting it
		if (target.buff(Roots.class) == null
				&& target.buff(Dancing.class) == null
				&& target.buff(Marionette.class) == null) {
			target.rooted = false;
		}
	}

	public void set( int duration ) {
		left = duration;
	}

	//maestro finisher: the puppet also echoes the hero's attacks
	private boolean mirrorAttacks = false;

	public void setMirrorAttacks() {
		mirrorAttacks = true;
	}

	//called after the hero lands an attack. Every mirroring marionette that can reach
	// the hero's target attacks it too
	public static void mirrorAttack( Char enemy ) {
		if (enemy == null || !enemy.isAlive()) return;

		for (Char ch : Actor.chars().toArray(new Char[0])) {
			Marionette marionette = ch.buff(Marionette.class);
			if (marionette != null && marionette.mirrorAttacks
					&& ch != enemy && ch.isAlive()
					&& ch instanceof Mob && ((Mob) ch).canAttack(enemy)) {
				ch.sprite.emitter().start(MarionetteWaltzSong.StringParticle.FACTORY, 0.2f, 2);
				ch.attack(enemy);
				if (!enemy.isAlive()) {
					return;
				}
			}
		}
	}

	@Override
	public boolean act() {

		Hero hero = Dungeon.hero;
		if (hero != null && hero.isAlive()) {
			if (lastHeroPos == -1) {
				lastHeroPos = hero.pos;
			}
			if (hero.pos != lastHeroPos) {

				int width = Dungeon.level.width();
				int dx = (hero.pos % width) - (lastHeroPos % width);
				int dy = (hero.pos / width) - (lastHeroPos / width);
				//mirror at most one step, even if the hero moved further
				dx = Math.max(-1, Math.min(1, dx));
				dy = Math.max(-1, Math.min(1, dy));

				int newPos = target.pos + dx + dy * width;
				if ((Dungeon.level.passable[newPos] || Dungeon.level.avoid[newPos])
						&& (!Char.hasProp(target, Char.Property.LARGE) || Dungeon.level.openSpace[newPos])
						&& Actor.findChar(newPos) == null) {
					target.sprite.move(target.pos, newPos);
					target.move(newPos, false);
				}

				lastHeroPos = hero.pos;

				//the forced step may have killed the target (chasms, traps)
				if (!target.isAlive()) {
					return true;
				}
			}
		}

		left--;
		if (left <= 0) {
			detach();
			return true;
		}

		spend(TICK);
		return true;
	}

	private static final String LEFT            = "left";
	private static final String LAST_HERO_POS   = "last_hero_pos";
	private static final String MIRROR_ATTACKS  = "mirror_attacks";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LEFT, left);
		bundle.put(LAST_HERO_POS, lastHeroPos);
		bundle.put(MIRROR_ATTACKS, mirrorAttacks);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		left = bundle.getInt(LEFT);
		lastHeroPos = bundle.getInt(LAST_HERO_POS);
		mirrorAttacks = bundle.getBoolean(MIRROR_ATTACKS);
	}

	@Override
	public int icon() {
		return BuffIndicator.MARIONETTE;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", left);
	}

}
