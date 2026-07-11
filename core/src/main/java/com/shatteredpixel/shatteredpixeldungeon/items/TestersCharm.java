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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.songs.Song;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.LeatherArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ScaleArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Longsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shortsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

//TEMPORARY dev item: makes the hero invulnerable while it is in their inventory,
// and provides cheats for playtesting the bard. Remove before any release
public class TestersCharm extends Item {

	public static final String AC_SONGS     = "SONGS";
	public static final String AC_RECHARGE  = "RECHARGE";
	public static final String AC_MAP       = "MAP";
	public static final String AC_LEVELUP   = "LEVELUP";
	public static final String AC_LASER     = "LASER";

	{
		image = ItemSpriteSheet.SOMETHING;

		unique = true;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		//ordered so the buttons lay out as: DROP THROW LASER / MAP FLOOR LEVEL UP / RECHARGE ALL SONGS
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_LASER);
		actions.add(AC_MAP);
		actions.add(AC_LEVELUP);
		actions.add(AC_RECHARGE);
		actions.add(AC_SONGS);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {

		super.execute(hero, action);

		if (action.equals(AC_SONGS)) {

			Lute lute = hero.belongings.getItem(Lute.class);
			if (lute == null) {
				GLog.w(Messages.get(this, "no_lute"));
			} else {
				for (Song song : Song.getAllSongs()) {
					lute.learnSong(song);
				}
				GLog.p(Messages.get(this, "songs_unlocked"));
			}

		} else if (action.equals(AC_RECHARGE)) {

			Lute lute = hero.belongings.getItem(Lute.class);
			if (lute != null) {
				lute.fullCharge();
			}
			//covers any other artifacts the hero is testing with
			Buff.affect(hero, ArtifactRecharge.class).set(50);
			GLog.p(Messages.get(this, "recharged"));

		} else if (action.equals(AC_MAP)) {

			//mirrors the effect of a scroll of magic mapping
			int length = Dungeon.level.length();
			int[] map = Dungeon.level.map;
			boolean[] mapped = Dungeon.level.mapped;
			boolean[] discoverable = Dungeon.level.discoverable;

			for (int i=0; i < length; i++) {

				int terr = map[i];

				if (discoverable[i]) {

					mapped[i] = true;
					if ((Terrain.flags[terr] & Terrain.SECRET) != 0) {

						Dungeon.level.discover( i );

						if (Dungeon.level.heroFOV[i]) {
							GameScene.discoverTile( i, terr );
							ScrollOfMagicMapping.discover( i );
						}
					}
				}
			}
			GameScene.updateFog();

			SpellSprite.show( hero, SpellSprite.MAP );
			GLog.p(Messages.get(this, "mapped"));

		} else if (action.equals(AC_LEVELUP)) {

			//level the hero up to the current depth
			int targetLvl = Math.min(Dungeon.depth, Hero.MAX_LEVEL);
			while (hero.lvl < targetLvl) {
				hero.earnExp(hero.maxExp() - hero.exp, TestersCharm.class);
			}

			//give a +3 armor and weapon matching the current region's tier
			int tier = Math.min(1 + (Dungeon.depth - 1) / 5, 5);
			Armor armor;
			MeleeWeapon weapon;
			switch (tier) {
				case 1: default:
					armor = new ClothArmor();   weapon = new WornShortsword();  break;
				case 2:
					armor = new LeatherArmor(); weapon = new Shortsword();      break;
				case 3:
					armor = new MailArmor();    weapon = new Sword();           break;
				case 4:
					armor = new ScaleArmor();   weapon = new Longsword();       break;
				case 5:
					armor = new PlateArmor();   weapon = new Greatsword();      break;
			}
			armor.upgrade(3).identify();
			weapon.upgrade(3).identify();
			if (!armor.collect())   Dungeon.level.drop(armor, hero.pos).sprite.drop();
			if (!weapon.collect())  Dungeon.level.drop(weapon, hero.pos).sprite.drop();

			//bring the lute up to 1/3 of the hero's level
			Lute lute = hero.belongings.getItem(Lute.class);
			if (lute != null) {
				while (lute.level() < hero.lvl / 3) {
					lute.upgrade();
				}
			}

			//give 8 potions of healing and 4 rations
			Item potions = new PotionOfHealing().quantity(8).identify();
			if (!potions.collect()) Dungeon.level.drop(potions, hero.pos).sprite.drop();
			Item food = new Food().quantity(4);
			if (!food.collect())    Dungeon.level.drop(food, hero.pos).sprite.drop();

			//give one sheet music per region (1 in sewers, up to 5 in halls)
			Item music = new SheetMusic().quantity(tier);
			if (!music.collect())   Dungeon.level.drop(music, hero.pos).sprite.drop();

			//ensure the hero can use the given equipment without penalty
			hero.STR = Math.max(hero.STR, Math.max(armor.STRReq(), weapon.STRReq()));

			updateQuickslot();
			GLog.p(Messages.get(this, "leveled"));

		} else if (action.equals(AC_LASER)) {

			GameScene.selectCell(laser);

		}
	}

	//a magic missile bolt that does 999 damage, modeled on the dissonant chord song
	private final CellSelector.Listener laser = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			if (target == null) {
				return;
			}
			Hero hero = curUser;

			Ballistica aim = new Ballistica(hero.pos, target, Ballistica.MAGIC_BOLT);

			if (Actor.findChar(aim.collisionPos) == hero) {
				GLog.i(Messages.get(Wand.class, "self_target"));
				return;
			}

			if (Actor.findChar(aim.collisionPos) != null) {
				QuickSlotButton.target(Actor.findChar(aim.collisionPos));
			}

			hero.busy();
			Sample.INSTANCE.play(Assets.Sounds.ZAP);
			hero.sprite.zap(target);
			MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.MAGIC_MISSILE, hero.sprite, aim.collisionPos, new Callback() {
				@Override
				public void call() {

					Char ch = Actor.findChar(aim.collisionPos);
					if (ch != null) {
						ch.damage(999, TestersCharm.this);
						Sample.INSTANCE.play(Assets.Sounds.HIT_MAGIC, 1, Random.Float(0.87f, 1.15f));
						if (ch.sprite != null) {
							ch.sprite.burst(0xFFFFFF44, 3);
						}
					} else {
						Dungeon.level.pressCell(aim.collisionPos);
					}

					hero.spend(1f);
					hero.next();

				}
			});
		}

		@Override
		public String prompt() {
			return Messages.get(TestersCharm.class, "laser_prompt");
		}
	};

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

}
