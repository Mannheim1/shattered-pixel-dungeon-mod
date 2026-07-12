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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Trance;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.NoteParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Lute;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

public class DirgeSong extends TargetedSong {

	public static final DirgeSong INSTANCE = new DirgeSong();

	public static final float BASE_DURATION = 5f;
	//the song casts at +4 lute levels against an entranced target
	public static final int TRANCE_BONUS_LVLS = 4;

	@Override
	public int icon() {
		return HeroIcon.DIRGE;
	}

	@Override
	protected String castSound() {
		return Assets.Sounds.CURSED;
	}

	@Override
	public int noteColor() {
		return 0x000000;
	}

	public static float duration(int lvl) {
		return BASE_DURATION + lvl;
	}

	@Override
	protected void affectTarget(Lute lute, Hero hero, Char ch) {
		//against an entranced target the dirge casts at bonus levels, consuming the trance
		int lvl = lute.buffedLvl();
		Trance trance = ch.buff(Trance.class);
		if (trance != null) {
			lvl += TRANCE_BONUS_LVLS;
			trance.detach();
		}
		float duration = modifyDuration(duration(lvl));

		ch.sprite.centerEmitter().start(noteFactory(), 0.3f, 5);
		Buff.prolong(ch, Terror.class, duration).object = hero.id();
		Buff.prolong(ch, BardTerrorTracker.class, duration);
	}

	@Override
	protected Object[] descArgs() {
		int lvl = luteLvl();
		return new Object[]{ (int)duration(lvl), (int)duration(lvl + TRANCE_BONUS_LVLS) };
	}

	//invisible tracker so effects like grand finale can tell bard-sourced terror apart
	public static class BardTerrorTracker extends FlavourBuff {}

}
