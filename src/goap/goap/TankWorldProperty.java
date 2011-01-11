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
 * The world properties represent all properties a specific world may have
 * @author Pascal Jaeger
 */
public enum TankWorldProperty {
		TankSpotted, 		// ein Tank ist ï¿½ber perceive reingekommen 
		HangarSpotted, 		// ein Hangar ist ï¿½ber perceive reingekommen
		ShotAtTank, 		// Schuss auf Tank abgegeben
		AtDestination, 		// Tank hat bestimmte Position erreicht
		HasDestination, 	// Tank hat ein bestimmtes Ziel
		InHangar,			// gibt an, ob der Tank im Hangar ist (nach einem Spawn)	
		ToolBoxSpotted,		// gibt an, ob eine ToolBox im Sichtbereich ist.
		ToolBoxCollected,	// gibt an, dass die gewŸnschte ToolBox eingesammelt wurde
		HasFlag, 			// gibt ab, das der Tank die Flag hat
		FlagSpotted, 		// gibt an, dass eine Flag im Sichtbereich ist
		SpottedTanksColor 	// of type EColors
}
