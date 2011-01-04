/*
 * Copyright (C) 2009 Arne Klingenberg
 * E-Mail: klingenberg.a@googlemail.com
 * 
 * This software is free; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 */

package goap.goap;

/**
 * The world properties represent all properties GlobalKI's world may have
 * @author Pascal Jaeger
 */
public enum GlobalKIWorldProperty {
	HangarUnderAttack, 		// Hangar wird angegriffen
	MapExplored, 			// gibt an ob eine Karte vollstaendig aufgaut wurde
	TankUnderAttack, 		// zeigt an, dass ein Tank angegriffen wird
	EnemieHangarSpotted, 	// zaehlt wieiviele Hangars gefunden wurden
	EnemieHangarsDestroyed 	// z�hlt wieviele Hangars zerstoert wurden
}
			
