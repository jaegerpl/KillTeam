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
 * The world properties represent all properties the tank's world may have
 * @author Pascal Jaeger
 */
public enum TankWorldProperty {
		TankSpotted, 	// ein Tank ist �ber perceive reingekommen 
		HangarSpotted, 	// ein Hangar ist �ber perceive reingekommen
		ShotAtTank, 	// Schuss auf Tank abgegeben
		AtDestination, 	// Tank hat bestimmte Position erreicht
		HasDestination, // Tank hat ein bestimmtes Ziel
		UnderAttack 	// Tank has been shot at in the last some seconds.
}
