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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.songs.Song;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

//teaches the bard a new song, chosen from two random ones he doesn't know
public class SheetMusic extends Item {

	public static final String AC_STUDY = "STUDY";

	{
		image = ItemSpriteSheet.SOMETHING; //placeholder, no sheet music sprite yet

		defaultAction = AC_STUDY;

		stackable = true;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_STUDY);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {

		super.execute(hero, action);

		if (action.equals(AC_STUDY)) {

			Lute lute = hero.belongings.getItem(Lute.class);
			if (lute == null) {
				GLog.w(Messages.get(this, "no_lute"));
				return;
			}

			ArrayList<Song> unknown = new ArrayList<>();
			for (Song song : Song.getAllSongs()) {
				if (!lute.knowsSong(song)) {
					unknown.add(song);
				}
			}

			if (unknown.isEmpty()) {
				GLog.w(Messages.get(this, "all_known"));
				return;
			}

			Random.shuffle(unknown);

			//sight reading grants a third song to choose from
			int numChoices = 2 + (hero.hasTalent(Talent.SIGHT_READING) ? 1 : 0);
			numChoices = Math.min(numChoices, unknown.size());

			if (numChoices == 1) {
				learn(hero, lute, unknown.get(0));
			} else {
				ArrayList<Song> choices = new ArrayList<>(unknown.subList(0, numChoices));

				StringBuilder message = new StringBuilder(Messages.get(SheetMusic.this, "choose_desc"));
				String[] options = new String[choices.size()];
				for (int i = 0; i < choices.size(); i++) {
					Song song = choices.get(i);
					message.append("\n\n_").append(Messages.titleCase(song.name())).append(":_ ").append(song.shortDesc());
					options[i] = Messages.titleCase(song.name());
				}

				GameScene.show(new WndOptions(new ItemSprite(this),
						Messages.titleCase(name()),
						message.toString(),
						options) {
					@Override
					protected void onSelect(int index) {
						learn(hero, lute, choices.get(index));
					}
				});
			}

		}
	}

	private void learn(Hero hero, Lute lute, Song song) {
		detach(hero.belongings.backpack);
		lute.learnSong(song);

		GLog.p(Messages.get(this, "learned", Messages.titleCase(song.name())));
		Sample.INSTANCE.play(Assets.Sounds.READ);

		//sight reading +2: studying sheet music also fully recharges the lute
		if (hero.pointsInTalent(Talent.SIGHT_READING) == 2) {
			lute.fullCharge();
		}

		hero.sprite.operate(hero.pos);
		hero.spendAndNext(1f);
		updateQuickslot();
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

}
