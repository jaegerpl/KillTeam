/*
 * 'CombatArena' is a simple jME-Demo. For educational purpose only.
 * 
 * Copyright (C) 2009 Carsten Canow
 * E-Mail: games@lunatic-soft.de
 * 
 * 'CombatArena' is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 * 
 * 'CombatArena' is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 */

package de.lunaticsoft.combatarena.api.testteam;

import de.lunaticsoft.combatarena.api.interfaces.ILoadPlayerKI;
import de.lunaticsoft.combatarena.api.interfaces.IPlayer;

public class LoadPlayerKI implements ILoadPlayerKI {

	public IPlayer getKI(int index, String name) {
		switch (index) {
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		default:
			return new TestPlayer(name);
		}
	}
}
