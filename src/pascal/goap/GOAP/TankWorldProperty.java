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

package pascal.goap.Goap;

/**
 * The world properties represent all properties a specific world may have
 * @author Klinge
 */
public enum TankWorldProperty {
		TankSpotted, // ein Tank ist Ÿber perceive reingekommen 
		HangarSpotted, // ein Hangar ist Ÿber perceive reingekommen
		ShotAtTank, // Schuss auf Tank abgegeben
		AtDestination, // Tank hat bestimmte Position erreicht
		HasDestination // Tank hat ein bestimmtes Ziel
}
