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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.NoteParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

//modeled on TargetedClericSpell
public abstract class TargetedSong extends Song {

	@Override
	public void onCast(Lute lute, Hero hero) {
		GameScene.selectCell(new CellSelector.Listener() {
			@Override
			public void onSelect(Integer cell) {
				onTargetSelected(lute, hero, cell);
			}

			@Override
			public String prompt() {
				return targetingPrompt();
			}
		});
	}

	@Override
	public int targetingFlags(){
		return Ballistica.MAGIC_BOLT;
	}

	protected String targetingPrompt(){
		return Messages.get(this, "prompt");
	}

	//the sound played when this song is cast
	protected String castSound(){
		return Assets.Sounds.LULLABY;
	}

	//targeted songs aim at a tile like wands do, sending a stream of notes at the
	// target. If there is no one at the aimed tile, the song still plays and the
	// charge is still spent
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
		}

		hero.busy();
		hero.sprite.zap(aim.collisionPos);
		Sample.INSTANCE.play(castSound());
		hero.sprite.centerEmitter().start(noteFactory(), 0.3f, 5);

		MagicMissile missile = MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.MAGIC_MISSILE,
				hero.sprite, aim.collisionPos, new Callback() {
			@Override
			public void call() {

				Char ch = Actor.findChar(aim.collisionPos);
				if (ch != null) {
					affectTarget(lute, hero, ch);
					maybeReverb(lute, hero, ch);
				} else {
					CellEmitter.get(aim.collisionPos).start(noteFactory(), 0.3f, 3);
					GLog.i(Messages.get(TargetedSong.class, "no_target"));
				}

				hero.spend(1f);
				hero.next();

				onSongCast(lute, hero);

			}
		});
		//the missile is made of the song's notes, rather than its usual particles
		missile.pour(noteFactory(), 0.03f);
	}

	//applies the song's effect to a character. Used for the primary target and reverb echoes
	protected abstract void affectTarget(Lute lute, Hero hero, Char ch);

	//reverb talent: chance for a targeted song to echo onto an enemy adjacent to the target
	protected void maybeReverb(Lute lute, Hero hero, Char primary) {
		if (hero.hasTalent(Talent.REVERB)
				&& Random.Float() < 0.25f * hero.pointsInTalent(Talent.REVERB)) {

			ArrayList<Char> candidates = new ArrayList<>();
			for (int offset : PathFinder.NEIGHBOURS8) {
				Char ch = Actor.findChar(primary.pos + offset);
				if (ch != null && ch != hero && ch.alignment == Char.Alignment.ENEMY) {
					candidates.add(ch);
				}
			}

			if (!candidates.isEmpty()) {
				Char echo = Random.element(candidates);
				echo.sprite.centerEmitter().start(noteFactory(), 0.3f, 3);
				affectTarget(lute, hero, echo);
			}
		}
	}

}
