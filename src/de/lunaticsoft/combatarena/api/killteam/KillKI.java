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

package de.lunaticsoft.combatarena.api.killteam;

import goap.agent.Agent;
import goap.agent.MemoryObject;
import goap.agent.MemoryObjectType;
import goap.goap.Action;
import goap.goap.Goal;
import goap.goap.IGOAPListener;
import goap.scenario.GoapActionSystem;
import goap.scenario.GoapController;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import map.fastmap.FastRoutableWorldMap;
import map.fastmap.LinkedTile;
import memory.map.MemorizedMap;
import memory.objectStorage.MemorizedWorldObject;
import memory.objectStorage.ObjectStorage;
import memory.pathcalulation.Path;

import battle.Battle;
import battle.ShootTarget;

import com.jme.math.FastMath;
import com.jme.math.Matrix3f;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.enumn.EColors;
import de.lunaticsoft.combatarena.api.enumn.EObjectTypes;
import de.lunaticsoft.combatarena.api.interfaces.IPlayer;
import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;
import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;
import de.lunaticsoft.combatarena.objects.WorldObject;

public class KillKI extends Agent implements IGOAPListener, IPlayer {

	private IWorldInstance world;
	private Vector3f direction; // tanks direction
	private Vector3f pos;		// takns last updated position
	private Vector3f goalPosition; // tanks goal its heading to

	private Vector3f lastPos = null;
	private float lastDistance = 0;
	private EColors color;
	private String name;
	
	private LinkedTile moveTarget;
	private boolean imHangar = true;
	Path<LinkedTile> path = null;

	private boolean pathReset = false;
	
	private boolean stop = false;
	// my variables
	private Vector3f startPos;
	
	
	private static Random r = new Random();
		
	
	// GOAP STUFF
	private final GoapController gc = new GoapController((GoapActionSystem)actionSystem);
	private GlobalKI globalKI;
	private Map<Point, Boolean> localMap = new HashMap<Point, Boolean>();
	private MemorizedMap memoryMap;
	private ObjectStorage objectStorage = new ObjectStorage();

	public KillKI(String name, GlobalKI globalKI) {
		//System.out.println("KillKI "+name+" gestartet");
		this.name = name;
		
		this.memoryMap = globalKI.getWorldMap();
		this.globalKI = globalKI;
		// GOAP STUFF
		/*this.globalKI = globalKI;
		blackboard.name = name; // just for debugging
		actionSystem = new GoapActionSystem(this, blackboard,memory);	
		((GoapActionSystem)actionSystem).addGOAPListener(this);*/

		
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
		if(!stop){
			// GOAP STUFF
	//		gc.update(interpolation);
			//System.out.println("CurrentPosition ="+world.getMyPosition());
			//System.out.println("GoalPosition ="+goalPosition);
			
			// current position
			pos = world.getMyPosition();
			
			//scan unknown terrain
			scanTerrain();
			
			LinkedTile myPosTile = memoryMap.getTileAtCoordinate(pos);
	
			//Pr�fen ob durch neue Erkundung das Zwischenziel nicht mehr betretbar ist
			if(null != moveTarget && (!moveTarget.isPassable() || !myPosTile.isPassable())) {
				if(!pathReset) {
					path = null;
					moveTarget = null;
					pathReset = true;
				}
			} else {
				pathReset = false;
			}
			
			
			if(null == moveTarget || moveTarget.equals(myPosTile)) {
				explore();
			}
			
			
			if(imHangar || moveTarget == null)
				world.move(direction);
			else if(moveTarget != null){
				//System.out.println("bewege nach karte");
				//System.out.println("");
				//System.out.println("Panzer bei: " + pos + "(" + myPosTile + ")");
				//System.out.println("Ziel bei: " + moveTarget.getTileCenterCoordinates() + "(" + moveTarget + ")");
				Vector3f newDirection = moveTarget.getTileCenterCoordinates().subtract(pos);
				//System.out.println("Bewege Richtung " + newDirection);
				//System.out.println("");
				world.move(newDirection);
			}
		}
	}
	
	private Vector3f rotateVector(Vector3f vec, float phi){
		Vector3f result = vec.clone();		
		result.x =vec.x * FastMath.cos(FastMath.DEG_TO_RAD*phi) - vec.z*FastMath.sin(FastMath.DEG_TO_RAD*phi);		
		result.z =vec.z * FastMath.cos(FastMath.DEG_TO_RAD*phi) + vec.x*FastMath.sin(FastMath.DEG_TO_RAD*phi);	
		return result;
	}
	
	public void explore(){
		//GOAP.getExploreDirection();
		if(startPos.distance(world.getMyPosition()) > 15)
			imHangar = false;
		
		//if(lastPos == null){
		//	direction = new Vector3f(FastMath.rand.nextInt(200),0, FastMath.rand.nextInt(200));
		//}
		//else 
			if(!imHangar){
			
			if(lastPos != null && lastPos.distance(world.getMyPosition()) < 0.06f){
				//System.out.println("STUCK");
			}
			
			LinkedTile myPosTile = memoryMap.getTileAtCoordinate(pos);
			//System.out.println("W�rde mich gern bewegen");
			
			if(null != path && !path.isEmpty()) {
				//System.out.println("Path ist nicht NULL!");
				if(myPosTile.equals(moveTarget)) {
					//System.out.println("Zwischenziel erreicht");
					moveTarget = path.getNextWaypoint();
				}
			} else {
				//Neuen Pfad berechnen
				//System.out.println("Neuen Pfad berechnen.");
				//LinkedTile targetTile = memoryMap.getNearestUnexploredTile(pos);
				Vector3f targetPos = this.pos.add(direction.normalize().mult(60));
				LinkedTile targetTile = memoryMap.getTileAtCoordinate(targetPos);
				if(!targetTile.isExplored()){
					if(targetTile.isPassable()) {
						path = memoryMap.calculatePath(myPosTile, targetTile);
						if(path.isEmpty()) {
							this.direction = rotateVector(this.direction, 10);
							moveTarget = null;
						} else {
	//System.out.println("########### Pfad gefunden ###########");
							moveTarget = path.getNextWaypoint();
	//System.out.println("Meine Position: " + myPosTile.getMapIndex());
	//System.out.println("Pfad: " + path);
						}
					} else {
						//Rotieren und weitersuchen
						this.direction = rotateVector(this.direction, 10);
						moveTarget = null;
					}
				} else {
					TreeMap<Integer, LinkedTile> sortedTiles = memoryMap.getUnexploredTilesSortedByDistance(pos);
					for(LinkedTile tile : sortedTiles.values()) {
						path = memoryMap.calculatePath(myPosTile, tile);
						if(!path.isEmpty()) {
							moveTarget = path.getNextWaypoint();
							break;
						}
					}
				}
			}
			
			
			 
		}	
		lastPos = world.getMyPosition();		
	}
	

	@Override
	public void setWorldInstance(IWorldInstance world) {
		//System.out.println("Panzer "+name+" hat World Instanz bekommen.");
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
		//System.out.println("================\r\n" + out + "\r\n================");
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
		
		WorldObject wO = new WorldObject(null, color, this.pos, EObjectTypes.Item);
		this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
		// GOAP STUFF
		globalKI.getBlackBoard().tanksAlive -= 1; // tell the GlobalKI about death of tank
	}

	@Override
	public void perceive(ArrayList<IWorldObject> worldObjects) {	
		boolean hangarDiscovered = false;
		
		// move WorldObjects into WorkingMemory
		for (IWorldObject wO : worldObjects) {
			// confidence of memoryObjects will decrease over time
			MemoryObject memo = new MemoryObject(1.0f, new MemoryObjectType(wO.getType(), 
																			wO.getColor()), 
																			wO.getPosition());
			memory.addMemory(memo);
			
			switch(wO.getType()){
				case Competitor:
					if(wO.getColor() != this.color){
						this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
						//System.out.println("Panzer gefunden: " + wO.hashCode());
						ShootTarget target = Battle.getShootTarget(wO.getPosition(), this.pos);
						world.shoot(target.direction, target.force, target.angle);
						//System.out.println("Feind entdeckt");
					}
					break;
				case Hangar:
					if(wO.getColor() != this.color){
						this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
						ShootTarget target = Battle.getShootTarget(wO.getPosition(), this.pos);
						world.shoot(target.direction, target.force, target.angle);
						//System.out.println("feindlichen Hangar entdeckt");
						hangarDiscovered = true;
					}
					break;
				case Item:
						this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
						//System.out.println("Item entdeckt");
					break;
				default: 
					//System.out.println("Kein WO");
			}
		}
		if(hangarDiscovered)
			stop = true;
		else
			stop = false;
		
		//ShootTarget target = Battle.getShootTarget(worldObject.getPosition(), world.getMyPosition());
		//world.shoot(target.direction, target.force, target.angle);*/		
	}

	@Override
	public void spawn() {
		startPos = world.getMyPosition();
		//System.out.println("StartPos: "+startPos);
		direction = world.getMyDirection();
		goalPosition = startPos.add(new Vector3f(1,0,1));
		goalPosition.x = (int)goalPosition.x;
		goalPosition.z = (int)goalPosition.z;
//		goalPosition = new Vector3f(300f,18f,300f);
		//System.out.println("goalPosition: "+goalPosition);
		
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
	
	
	private void scanTerrain(){
		boolean tileWithWater = false;
		List<LinkedTile> tiles = this.globalKI.getWorldMap().getEmptyTilesPossiblyInViewRange(this.pos);
		for(LinkedTile tile : tiles){
			boolean isWater = false;
			boolean isPassable  = true;
			Vector3f terrain = world.getTerrainNormal(tile.getTileCenterCoordinates());
			if(terrain != null){
				if(!world.isPassable(tile.getTileCenterCoordinates())){
					isPassable = false;
				}

				if(world.isWater(tile.getTileCenterCoordinates())){
					isWater = true;
					tileWithWater = true;
				}
				
				memoryMap.exploreTile(tile, isWater, isPassable, terrain);
			} else {
				if(memoryMap.tileIsInViewRange(pos, world.getMyDirection(), tile)) {
					//NULL obwohl in Sichtweite
					//memoryMap.exploreTile(tile, false, false, new Vector3f(0,0,0));
					memoryMap.markTileAsOutOfMap(tile);
				}
			}
		}
		//Direkt voraus gucken
		Vector3f voraus = pos.add(world.getMyDirection().normalize().mult(2));
		if(!world.isPassable(voraus)) {
			LinkedTile tileVoraus = memoryMap.getTileAtCoordinate(voraus);
			memoryMap.exploreTile(tileVoraus, tileVoraus.isWater(), false, tileVoraus.getNormalVector());
		}

		
	}
	
	/**
	 * Turns the direction of the tank by the given degree alpha.
	 * If alpha is positive the tank turns left, tank turns right for negative alpha.
	 * 
	 * @param alpha the degree by which the tank turns its direction.
	 */
	public void turn(double alpha){
		Vector3f dir = world.getMyDirection();
		Vector3f newdir = new Vector3f();
		
		// Drehmatrix um die y-Achse (wikipedia)
		Matrix3f mat = new Matrix3f(
				(float)Math.cos(alpha),      0f, (float)Math.sin(alpha),
				0f,                          1f, 0f,
				(float)(-1*Math.sin(alpha)), 0f, (float)Math.cos(alpha)
		);

		// Vektor mit Drehmatrix drehen
		newdir = mat.mult(dir, newdir);
		
		world.move(newdir);
	}


}