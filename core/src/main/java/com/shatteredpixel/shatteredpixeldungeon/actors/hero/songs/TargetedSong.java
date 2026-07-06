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

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
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

	protected abstract void onTargetSelected(Lute lute, Hero hero, Integer target);

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
				echo.sprite.centerEmitter().start(Speck.factory(Speck.NOTE), 0.3f, 3);
				affectTarget(lute, hero, echo);
			}
		}
	}

}
