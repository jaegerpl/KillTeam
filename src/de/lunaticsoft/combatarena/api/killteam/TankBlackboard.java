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
package de.lunaticsoft.combatarena.api.killteam;

import map.fastmap.LinkedTile;
import memory.objectStorage.MemorizedWorldObject;

import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.enumn.EColors;
import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;

/**
 * Class which holds all data that should be shared between different modules
 * 
 * Holds state information of a tank.
 * 
 * @author Pascal Jaeger
 *
 */
public class TankBlackboard {
	
	public String name; 				// just for debugging
	
	// BATTLE STUFF
	public int hitsTaken;				// counts how often the tank has been shot at (indiciating that fitness of tank is NOT 100%) is set to 0 when collecting a toolbox
	public IWorldObject spottedTank;	// the tank in view range
	public MemorizedWorldObject spottedHangar;	// the hangar in view range
	public EColors spottedHangarsColor; // the color the spotted hangar should have
	
	// TOOLBOX STUFF
	public IWorldObject spottedToolBox; // IWorldObject of type Item
	public boolean toolBoxCollected;	// true after picking up the specified toolbox
	public boolean toolBoxSpotted;		// true after picking up the specified toolbox
	public LinkedTile oldMoveTarget; 	// das target bevor das item gesehen wurde, muss später wieder hergestellt werden
	public Task oldTask;				// der alte Task muss wieder hergestellt werden nach dem einsammeln
	
	// CAPTURE THE FLAG STUFF
	public boolean hasFlag;				// true if tank owns the flag
	public boolean flagSpotted;			// true if flag is in view range
	
	// MOVEMENT STUFF
	public boolean inHangar = false;	// indicating that tank has respawned and is in the hangar
	public boolean hasDestination;		// tank has a goal to move to
	public boolean atDestination;		// tank is at goal position
	public Vector3f destination;		// the goal position the tank has to move to
	public Vector3f currentPosition;	// tanks current position
	public Vector3f direction;			// tanks direction in the world
	
	public Task curTask = Task.EXPLORE;


}
