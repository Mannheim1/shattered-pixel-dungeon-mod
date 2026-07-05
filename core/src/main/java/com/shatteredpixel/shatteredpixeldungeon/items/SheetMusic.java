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

			if (unknown.size() == 1) {
				learn(hero, lute, unknown.get(0));
			} else {
				Song first = unknown.get(0);
				Song second = unknown.get(1);
				GameScene.show(new WndOptions(new ItemSprite(this),
						Messages.titleCase(name()),
						Messages.get(SheetMusic.this, "choose_desc",
								Messages.titleCase(first.name()), first.shortDesc(),
								Messages.titleCase(second.name()), second.shortDesc()),
						Messages.titleCase(first.name()),
						Messages.titleCase(second.name())) {
					@Override
					protected void onSelect(int index) {
						learn(hero, lute, index == 0 ? first : second);
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
