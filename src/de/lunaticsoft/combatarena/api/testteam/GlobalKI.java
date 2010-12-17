package de.lunaticsoft.combatarena.api.testteam;

import java.util.ArrayList;

import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.enumn.EColors;
import de.lunaticsoft.combatarena.api.interfaces.IPlayer;
import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;
import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;
import de.lunaticsoft.combatarena.objects.WorldObject;

import pascal.goap.Agent.GlobalKIAgent;
import pascal.goap.Agent.GlobalKIBlackboard;
import pascal.goap.Goap.Action;
import pascal.goap.Goap.Goal;
import pascal.goap.Goap.IGOAPListener;

public class GlobalKI  extends GlobalKIAgent implements IGOAPListener, IPlayer {
	
	private String name = "GlobaleKI";
	private IWorldInstance world;
	private float worldXDimension;
	private float worldYDimension;
	public boolean worldIsSet = false;
	public boolean worldScanned = false;
	
	
	public GlobalKI() {
		blackboard.name = name;
	}

	@Override
	public void actionChangedEvent(Object sender, Action oldAction,
			Action newAction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void goalChangedEvent(Object sender, Goal oldGoal, Goal newGoal) {
		// TODO Auto-generated method stub
		
	}
	public GlobalKIBlackboard getBlackBoard(){
		return blackboard;
	}
	
	/**
	 * Use world.getTerrainNormal(position) to scan the borders of the world
	 * using binary search.
	 */
	public void scanWorldSize(){
		if(!worldIsSet){
			System.err.println("World ist null. Kann world nicht scannen");
			return;
		}
		worldScanned = true;
		
		System.out.println("Start Scanning World....");
		float startXDimension = 100;
		float startZDimension = 100;
		boolean found = false;
		boolean foundX = false;
		boolean foundY = false;
		float distance;
		
		Vector3f sV = new Vector3f(startXDimension, 0, startZDimension); 	// searchVector
		Vector3f lV= new Vector3f(startXDimension, 0, startZDimension);		// latestValidVector
				
		while(!found){
			
			if (world == null) {
				System.err.println("World ist null");
			}
			
			// scan X Dimension
			while(world.getTerrainNormal(sV) != null){
				// if != null, we can move further in x Dimension
				lV = sV;
				sV.x *= 2;
			}
			
			// if null is returned, we moved too far, so the real border lies between the searchVector.X and the latestValidVector.X
			while(!foundX){
				distance = Math.abs(sV.x - lV.x);
				if(world.getTerrainNormal(sV) == null && lV.x < sV.x){
					sV.x -= (distance/2);
				}else if(world.getTerrainNormal(sV) != null && lV.x < sV.x){
					lV = sV;
					sV.x += (distance/2);
				} else if(world.getTerrainNormal(sV) != null && lV.x == sV.x){
					foundX = true;
					System.out.println("World X Dimension ist "+sV.x);
				} else if(world.getTerrainNormal(sV) == null && lV.x == sV.x){
					System.err.println("sV = lV und au§erhalb der Welt");
				}
			}
		}		
	}

	@Override
	public void attacked(IWorldObject competitor) { }

	@Override
	public void collected(IWorldObject worldObject) {	}

	@Override
	public void die() {	}

	@Override
	public void perceive(ArrayList<IWorldObject> worldObjects) {	}

	@Override
	public void setColor(EColors color) {	}

	@Override
	public void setWorldInstance(IWorldInstance world) {	
		this.world = world;
		worldIsSet = true;
		System.out.println("World Set");
	}

	@Override
	public void spawn() {	}

	@Override
	public void update(float interpolation) {	}
}
