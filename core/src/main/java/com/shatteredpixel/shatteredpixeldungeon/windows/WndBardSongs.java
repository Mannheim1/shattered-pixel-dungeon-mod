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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.songs.Song;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.NinePatch;

import java.util.ArrayList;

//modeled on WndClericSpells
public class WndBardSongs extends Window {

	protected static final int WIDTH    = 120;

	public static int BTN_SIZE = 20;

	public WndBardSongs(Lute lute, Hero bard, boolean info){

		IconTitle title;
		if (!info){
			title = new IconTitle(new ItemSprite(lute), Messages.titleCase(Messages.get(this, "cast_title")));
		} else {
			title = new IconTitle(Icons.INFO.get(), Messages.titleCase(Messages.get(this, "info_title")));
		}

		title.setRect(0, 0, WIDTH, 0);
		add(title);

		IconButton btnInfo = new IconButton(info ? new ItemSprite(lute) : Icons.INFO.get()){
			@Override
			protected void onClick() {
				GameScene.show(new WndBardSongs(lute, bard, !info));
				hide();
			}
		};
		btnInfo.setRect(WIDTH-16, 0, 16, 16);
		add(btnInfo);

		RenderedTextBlock msg;
		if (info){
			msg = PixelScene.renderTextBlock( Messages.get( this, "info_desc"), 6);
		} else {
			msg = PixelScene.renderTextBlock( Messages.get( this, "cast_desc"), 6);
		}
		msg.maxWidth(WIDTH);
		msg.setPos(0, title.bottom()+4);
		add(msg);

		int top = (int)msg.bottom()+4;

		ArrayList<Song> songs = Song.getKnownSongs(bard);

		ArrayList<IconButton> songBtns = new ArrayList<>();
		for (Song song : songs) {
			IconButton songBtn = new SongButton(song, lute, info);
			add(songBtn);
			songBtns.add(songBtn);
		}

		//lay out song buttons in rows of up to 5
		int btnsPerRow = 5;
		int placed = 0;
		while (placed < songBtns.size()) {
			int rowCount = Math.min(btnsPerRow, songBtns.size() - placed);
			int left = 2 + (WIDTH - rowCount * (BTN_SIZE + 4)) / 2;
			for (int i = placed; i < placed + rowCount; i++) {
				songBtns.get(i).setRect(left, top, BTN_SIZE, BTN_SIZE);
				left += BTN_SIZE + 4;
			}
			placed += rowCount;
			top += BTN_SIZE + 2;
		}

		resize(WIDTH, top - 2);

		//if we are on mobile, offset the window down to just above the toolbar
		if (SPDSettings.interfaceSize() != 2){
			offset(0, (int) (GameScene.uiCamera.height/2 - 30 - height/2));
		}

	}

	public class SongButton extends IconButton {

		Song song;
		Lute lute;
		boolean info;

		NinePatch bg;

		public SongButton(Song song, Lute lute, boolean info){
			super(new HeroIcon(song));

			this.song = song;
			this.lute = lute;
			this.info = info;

			if (!lute.canPlay(Dungeon.hero, song)){
				icon.alpha( 0.3f );
			}

			bg = Chrome.get(Chrome.Type.TOAST);
			addToBack(bg);
		}

		@Override
		protected void onPointerUp() {
			super.onPointerUp();
			if (!lute.canPlay(Dungeon.hero, song)){
				icon.alpha( 0.3f );
			}
		}

		@Override
		protected void layout() {
			super.layout();

			if (bg != null) {
				bg.size(width, height);
				bg.x = x;
				bg.y = y;
			}
		}

		@Override
		protected void onClick() {
			if (info){
				GameScene.show(new WndTitledMessage(new HeroIcon(song), Messages.titleCase(song.name()), song.desc()));
			} else {
				hide();

				if (!lute.canPlay(Dungeon.hero, song)){
					GLog.w(Messages.get(Lute.class, "no_song"));
				} else {
					song.onCast(lute, Dungeon.hero);

					if (song.targetingFlags() != -1 && Dungeon.quickslot.contains(lute)){
						lute.targetingSong = song;
						QuickSlotButton.useTargeting(Dungeon.quickslot.getSlot(lute));
					}
				}

			}
		}

		@Override
		protected String hoverText() {
			return "_" + Messages.titleCase(song.name()) + "_\n" + song.shortDesc();
		}
	}

}
