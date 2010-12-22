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

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import pascal.goap.Agent.Agent;
import pascal.goap.Agent.MemoryObject;
import pascal.goap.Agent.MemoryObjectType;
import pascal.goap.Goap.Action;
import pascal.goap.Goap.Goal;
import pascal.goap.Goap.IGOAPListener;
import pascal.goap.Scenario.GoapActionSystem;
import pascal.goap.Scenario.GoapController;
import pascal.map.Tile;
import pascal.map.WorldMap;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.enumn.EColors;
import de.lunaticsoft.combatarena.api.interfaces.IPlayer;
import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;
import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;

public class PascalPlayer extends Agent implements IGOAPListener, IPlayer {

	private IWorldInstance world;
	private Vector3f direction; // tanks direction
	private Vector3f pos;		// takns last updated position
	private Vector3f goalPosition; // tanks goal its heading to

	private Vector3f lastPos = null;
	private float lastDistance = 0;
	private EColors color;
	private String name;

	private boolean stop = false;
	
	
	// my variables
	private Vector3f startPos;
	private int scanCounter = 0;   // scan Sichtbereich every 10 update steps
	
	
	private static Random r = new Random();
		
	
	// GOAP STUFF
	private final GoapController gc = new GoapController((GoapActionSystem)actionSystem);
	private GlobalKI globalKI;
	private WorldMap worldMap;
	private Queue<Tile> worldMapQueue;
	private Map<Point, Boolean> localMap = new HashMap<Point, Boolean>();

	public PascalPlayer(String name, GlobalKI globalKI) {
		System.out.println("PascalPlayer "+name+" gestartet");
		this.name = name;
		
		// GOAP STUFF
		this.globalKI = globalKI;
		blackboard.name = name; // just for debugging
		actionSystem = new GoapActionSystem(this, blackboard,memory);	
		((GoapActionSystem)actionSystem).addGOAPListener(this);
		worldMap = globalKI.getWorldMap();
		worldMapQueue = worldMap.registerTank(this);
		
//		generateActions();
//		generateGoals();
//		generateRandomDesires();
	}

	@Override
	public void setColor(EColors color) {
		this.color = color;
	}

	@Override
	public void update(float interpolation) {
		// GOAP STUFF
//		gc.update(interpolation);
		scanViewRange();
		System.out.println("CurrentPosition ="+world.getMyPosition());
		System.out.println("GoalPosition ="+goalPosition);
		
		// current position
		pos = world.getMyPosition();
		if (!stop) {
			// move
			world.move(goalPosition);
			if(inRangeOfPosition(goalPosition)){
				stop = true;
				world.stop();
			}
		}		
	}
	

	@Override
	public void setWorldInstance(IWorldInstance world) {
		System.out.println("Panzer "+name+" hat World Instanz bekommen.");
		this.world = world;
		globalKI.setWorldInstance(world);
	}

	/**
	 * y = speed * time * sin(angle) - (gravity / 2) * time^2 y -> 0 <br>
	 * 0 = speed * time * sin(angle) - (gravity / 2) * time^2<br>
	 * <br>
	 * distance = speed * time * cos(angle)<br> 
	 * speed = distance / (time * cos(angle))<br>
	 * <br>
	 * einsetzen: <br>
	 * 0 = (distance / (time * cos(angle))) * time * sin(angle) - (gravity / 2) * time^2 <br>
	 * umstellen und 'time' kürzen: <br>
	 * 0 = (distance * (sin(angle) / cos(angle))) - (gravity / 2) * time^2 sin(angle) / cos(angle) <br>
	 * -> tan(angle) 0 = (distance * tan(angle)) - (gravity / 2)* time^2 | (gravity / 2) (gravity / 2) <br>
	 * -> 49.05f 0 = (distance * tan(angle)) - (gravity / 2) * time^2<br>
	 * <br>
	 * time^2 = (distance / 45.05f) * tan(angle) time = sqrt((distance / 45.05f) * tan(angle))<br>
	 * <br>
	 * distance = speed * time * cos(angle) <br>
	 * umstellen:<br> 
	 * speed = distance / (cos(angle) * time)<br>
	 */
	public float getSpeed(float angleDeg, float distance) {
		// Bogenmaß
		float angle = angleDeg / FastMath.RAD_TO_DEG;
		// gravity = 98.1f -> gravity/2 = 49.05f
		
		float time = FastMath.sqrt((distance / 49.05f) * FastMath.tan(angle));
		float speed = distance / (FastMath.cos(angle) * time);

		return speed;
	}

	@Override
	public void attacked(IWorldObject competitor) {
		Vector3f enemy = competitor.getPosition();
		String out = "Attacked by position " + enemy;
		Vector3f direction = world.getMyPosition().clone().subtract(enemy.clone()).negate();
		float distance = world.getMyPosition().distance(enemy);
		out += "\r\nDistance to enemy " + distance;

		float speed = getSpeed(30, distance);

		out += "\r\nSpeed " + speed;
		world.shoot(direction, speed, 30);

		// ACHTUNG: Keine Ausgaben in der Abgabe (Vorführung)! "Logger" benutzen
		System.out.println("================\r\n" + out + "\r\n================");
	}

	@Override
	public void collected(IWorldObject worldObject) {
		switch (worldObject.getType()) {
		case Item:
			// ITEM COLLECTED
			break;
		default:
			// DO NOTHING
			break;
		}
	}

	@Override
	public void die() {
		// GOAP STUFF
		globalKI.getBlackBoard().tanksAlive -= 1; // tell the GlobalKI about death of tank
	}

	@Override
	public void perceive(ArrayList<IWorldObject> worldObjects) {	
		// move WorldObjects into WorkingMemory
		for (IWorldObject wO : worldObjects) {
			// confidence of memoryObjects will decrease over time
			MemoryObject memo = new MemoryObject(1.0f, new MemoryObjectType(wO.getType(), 
																			wO.getColor()), 
																			wO.getPosition());
			memory.addMemory(memo);
		}
		
	}

	@Override
	public void spawn() {
		startPos = world.getMyPosition();
		System.out.println("StartPos: "+startPos);
		direction = world.getMyDirection();
		goalPosition = startPos.add(new Vector3f(1,0,1));
		goalPosition.x = (int)goalPosition.x;
		goalPosition.z = (int)goalPosition.z;
//		goalPosition = new Vector3f(300f,18f,300f);
		stop = false;
		System.out.println("goalPosition: "+goalPosition);
		
		// GOAP STUFF
		globalKI.getBlackBoard().tanksAlive += 1;// tell GlobalKI about rebirth of tank
		blackboard.direction = direction;
	}

	public String getName() {
		return name;
	}

	@Override
	public void actionChangedEvent(Object sender, Action oldAction,	Action newAction) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void goalChangedEvent(Object sender, Goal oldGoal, Goal newGoal) {
		// TODO Auto-generated method stub		
	}
	
	private void generateGoals(){
//		((GoapActionSystem)actionSystem).addGoal(new Explore("Explore",0.6f, (GoapActionSystem) actionSystem));
	}
	
	private void generateActions(){			
//		((GoapActionSystem)actionSystem).addAction(new GotoLocation((GoapActionSystem) this.actionSystem,"GoToLocation",1.0f));
//		((GoapActionSystem)actionSystem).addAction(new WatchEntertainment((GoapActionSystem) this.actionSystem,"WatchEntertianment",1.0f));
	}
	
	private void generateRandomDesires(){
//		((GoapActionSystem)actionSystem).currentWorldState.add(new WorldStateSymbol<Float>(TankWorldProperty.Boredom, r.nextFloat() % 1.0f, PropertyType.Float));
	}

	/**
	 * Scans the view range of tank for new information to build up a global map
	 */
	private void scanViewRange(){
		if(scanCounter < 40){
			scanCounter++;
		} else {
			scanCounter = 0;
			/*
			 * Sichtbereich hat einen max Radius von 50
			 */
			Vector3f viewRangeCenter = getViewRangeCenter();
			List<Tile> tiles = worldMap.getEmptyTilesInViewRange(viewRangeCenter);
			
			Vector3f normalVec;
			boolean isWater = false;
			boolean isPassable = false;
			
			for(Tile t : tiles){
				normalVec = world.getTerrainNormal(t.tileCenterCoordinates);
				isPassable = world.isPassable(t.tileCenterCoordinates);
				isWater = tileContainsWater(t);
				Tile tile = new Tile(t.mapIndex, isWater, isPassable, normalVec);				
				localMap.put(t.mapIndex, true);
				worldMapQueue.add(tile);
			}
		}			
	}
	
	/**
	 * Returns true if the tank is in an area of 2 units around the given goal
	 * 
	 * @param goal
	 * 
	 * @return true if tank is near the goal position, false otherwise
	 */
	private boolean inRangeOfPosition(Vector3f goal){
		Vector3f tankPos = world.getMyPosition();
		if(goal.x-2 <= tankPos.x && tankPos.x <= goal.x+2 ){
			if(goal.z-2 <= tankPos.z && tankPos.z <= goal.z+2 ){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the world position of the tanks view range center.
	 * It is placed 30 units in front of the tanks position
	 * 
	 * @return world position of view range center
	 */
	private Vector3f getViewRangeCenter(){
		Vector3f tankPos = world.getMyPosition();
		Vector3f direction = world.getMyDirection();
		direction.multLocal(-direction.length()); // einheitsvektor
		return tankPos.add(direction.mult(30));
	}
	
	/**
	 * Returns true if one unit of tile is water.
	 * If water is found, the other units of the tile won't be checked further.
	 * @param t The tile to check
	 * @return true if water is on this tile, false otherwise.
	 */
	private boolean tileContainsWater(Tile t){
		int halfsize = WorldMap.tilesize;
		int x = (int)t.tileCenterCoordinates.x;
		int y = (int)t.tileCenterCoordinates.z;	
		for(int i = x-halfsize; i < x+halfsize; i++){
			for(int j = y-halfsize; j < y+halfsize; j++){
				if(world.isWater(new Vector3f(i,0,j))){
					return true;
				}
			}
		}
		return false;
	}
}