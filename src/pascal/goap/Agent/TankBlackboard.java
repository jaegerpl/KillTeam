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
package pascal.goap.Agent;

import java.util.ArrayList;

import pascal.goap.pathfinding.NavNode;
import pascal.goap.pathfinding.NavigationMap;

import com.jme.math.Vector3f;

/**
 * Class which holds all data that should be shared between different modules
 * @author Klinge
 *
 */
public class TankBlackboard {
	
	public enum AttackState{
		tank, hangar, nothing
	}
	
	
	public ArrayList<NavNode> path = new ArrayList<NavNode>();
	public Vector3f foodLocation = null;
	//public float hunger;		
	public NavNode destinationNode;
	public static NavigationMap navMap;
	public NavNode currentNode;
	public Vector3f exitLocation = null;
	public boolean wait = false;
	public float speed;
	public Vector3f entertainmentLoc = null;
	//public float boredom;
	public Vector3f currentTranslation;
	//public float exhaustion = 0.0f;	
	public ArrayList<Vector3f> visitedLocations = new ArrayList<Vector3f>();
	
	// new stuff
	public String name; // just for debugging
	public boolean underAttack;
	public AttackState attacking = AttackState.nothing;
	public boolean hasDestination;
	public boolean atDestination;
	public Vector3f currentPosition;
	public Vector3f direction;
}
