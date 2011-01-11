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
import goap.scenario.actions.CollectFlag;
import goap.scenario.actions.CollectToolBox;
import goap.scenario.actions.DestroyHangar;
import goap.scenario.actions.DestroyTank;
import goap.scenario.actions.DestroyTankColor;
//import goap.scenario.actions.GoToLocation;
import goap.scenario.actions.LeaveHangar;
import goap.scenario.goals.CollectToolBoxGOAL;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

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
import de.lunaticsoft.combatarena.api.killteam.globalKI.GlobalKI;
import de.lunaticsoft.combatarena.api.killteam.globalKI.StatusType;
import de.lunaticsoft.combatarena.objects.WorldObject;
import debug.MapServer;

public class KillKI implements IGOAPListener, IPlayer {

	private IWorldInstance world;
	private Vector3f direction; // tanks direction
	private Vector3f pos;		// takns last updated position
	private Vector3f goalPosition; // tanks goal its heading to
	
	private Vector3f currentDirection;
	
	private Queue<Vector3f> lastPositions;
	
	private float lastDistance = 0;
	private EColors color;
	private String name;
	
	private LinkedTile moveTarget;
	private boolean imHangar = true;
	Path<LinkedTile> path = null;

	private boolean pathReset = false;
	
	private boolean stop = false;
	private boolean calibrated = false;
	int viewRangeRadius = 0;
	int viewRangeOffset = 0;
	// my variables
	private Vector3f startPos;
	
	
	private static Random r = new Random();
		
	
	// GOAP STUFF
	//private final GoapController gc = new GoapController((GoapActionSystem)actionSystem);
	private GlobalKI globalKI;
	private Map<Point, Boolean> localMap = new HashMap<Point, Boolean>();
	private MemorizedMap memoryMap;
	private ObjectStorage objectStorage;

	public KillKI(String name, GlobalKI globalKI) {
		//System.out.println("KillKI "+name+" gestartet");
		this.name = name;
		
		this.memoryMap = globalKI.getWorldMap();
		this.globalKI = globalKI;
		this.objectStorage = globalKI.getObjectStorage();
		
		
		lastPositions = new LinkedBlockingQueue<Vector3f>(2);
		// GOAP STUFF
		/*
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
		if(!calibrated) {
			calibrate();
		}
		if(!stop){
			// GOAP STUFF
	//		gc.update(interpolation);
			//System.out.println("CurrentPosition ="+world.getMyPosition());
			//System.out.println("GoalPosition ="+goalPosition);
			
			// current position
			pos = world.getMyPosition();
			currentDirection = world.getMyDirection();
			
			//scan unknown terrain
			scanTerrain();
			
			if(stuck()) {
				System.out.println(name + " steckt fest. Sein Ziel ist " + moveTarget + "und er befindet sich an Position " + memoryMap.getTileAtCoordinate(pos));
			}
			
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
			
			
			if(blackboard.inHangar || moveTarget == null)
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
	
	private void calibrate() {
		Vector3f myPosition = world.getMyPosition();
		Vector3f myDirection = world.getMyDirection();
		Vector3f incrementor = myDirection.normalize();
		
		Vector3f currPos = myPosition;
		int counterFront = 0;
		do {
			currPos = currPos.add(incrementor);
			counterFront++;
		} while(world.getTerrainNormal(currPos) != null);
		counterFront--;
		
		currPos = myPosition;
		int counterBack = 0;
		do {
			currPos = currPos.subtract(incrementor);
			counterBack++;
		} while(world.getTerrainNormal(currPos) != null);
		counterBack--;
		
		this.viewRangeRadius = (counterFront + counterBack) / 2;
		this.viewRangeOffset = this.viewRangeRadius - counterBack;
		
		System.out.println("Radius: " +  viewRangeRadius);
		System.out.println("Offset: " +  viewRangeOffset);
		System.out.println();
		System.out.println("Front: " + counterFront);
		System.out.println("Back: " + counterBack);
		
		this.calibrated = true;
	}
	
	private Vector3f rotateVector(Vector3f vec, float phi){
		Vector3f result = vec.clone();		
		result.x =vec.x * FastMath.cos(FastMath.DEG_TO_RAD*phi) - vec.z*FastMath.sin(FastMath.DEG_TO_RAD*phi);		
		result.z =vec.z * FastMath.cos(FastMath.DEG_TO_RAD*phi) + vec.x*FastMath.sin(FastMath.DEG_TO_RAD*phi);	
		return result;
	}
	
	private boolean stuck() {
		boolean stuck = false;
		if(lastPositions.size() > 1) {
			Vector3f comparisonPosition = this.lastPositions.poll();
			
			if(comparisonPosition != null && Math.abs(comparisonPosition.distance(pos)) < 0.01f){
				stuck = true;
			}
		}
		this.lastPositions.add(pos);
		return stuck;
	}
	
	public void explore(){
		//GOAP.getExploreDirection();
		if(startPos.distance(world.getMyPosition()) > 15)
			blackboard.inHangar = false;
		
		//if(lastPos == null){
		//	direction = new Vector3f(FastMath.rand.nextInt(200),0, FastMath.rand.nextInt(200));
		//}
		//else 
			if(!blackboard.inHangar){
			
			
			
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
				if(targetPos.x > 10000 || targetPos.z > 10000) {
					System.out.println("Alerm!");
				}
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
				
	}
	

	@Override
	public void setWorldInstance(IWorldInstance world) {
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
		
		// GOAP STUFF
		blackboard.hitsTaken++;
	}

	@Override
	public void collected(IWorldObject worldObject) {
		switch (worldObject.getType()) {
		case Item:
			if(blackboard.spottedToolBox != null){
				if(blackboard.spottedToolBox.getPosition() == worldObject.getPosition()){
					blackboard.spottedToolBox = null;
					blackboard.toolBoxCollected = true;
					// TODO update object storage
				}
			}
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
		globalKI.removeTank(this);					// deregister tank in globalKI
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
			//memory.addMemory(memo);
			
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
		globalKI.registerTank(this); 				// register tank in globalKI
		//blackboard.direction = direction;
		blackboard.inHangar = true;
	}

	public String getName() {
		return name;
	}

	@Override
	public void actionChangedEvent(Object sender, Action oldAction,	Action newAction) {
		// we are going to tell the globalki about our status change
		globalKI.tankStatusChanged(this, newAction, StatusType.Action);
	}

	@Override
	public void goalChangedEvent(Object sender, Goal oldGoal, Goal newGoal) {
		// we are going to tell the globalki about our status change	
		globalKI.tankStatusChanged(this, newGoal, StatusType.Goal);
	}
	
	private void generateGoals(){
		((GoapActionSystem)actionSystem).addGoal(new CollectToolBoxGOAL("CollectToolBoxGOAL",0.1f, (GoapActionSystem) actionSystem));
	}
	
	private void generateActions(){			
		((GoapActionSystem)actionSystem).addAction(new CollectFlag((GoapActionSystem) this.actionSystem,"CollectFlag",1.0f));
		((GoapActionSystem)actionSystem).addAction(new CollectToolBox((GoapActionSystem) this.actionSystem,"CollectToolBox",1.0f));
		((GoapActionSystem)actionSystem).addAction(new DestroyHangar((GoapActionSystem) this.actionSystem,"DestroyHangar",1.0f));
		((GoapActionSystem)actionSystem).addAction(new DestroyTank((GoapActionSystem) this.actionSystem,"DestroyTank",1.0f));
	//	((GoapActionSystem)actionSystem).addAction(new GoToLocation((GoapActionSystem) this.actionSystem,"GoToLocation",1.0f));
		((GoapActionSystem)actionSystem).addAction(new LeaveHangar((GoapActionSystem) this.actionSystem,"LeaveHangar",1.0f));

		//DestroyTankColor needs to be added, when we know which colors are in the game => extra method
	}
	
	
	private void scanTile(LinkedTile tile) {
		if(!tile.isExplored()) {
if(tile.mapIndex.x > 60 || tile.mapIndex.y > 60 || tile.mapIndex.x < 0 || tile.mapIndex.y < 0) {
	System.out.println("Debug mich");
}
			boolean isPassable = true;
			boolean isWater = false;			
			
			Vector3f tileCenter = tile.getTileCenterCoordinates();
			int increment = (int) FastMath.ceil(FastRoutableWorldMap.tilesize / 4.0f);
			
			List<Vector3f> scanPositions = new ArrayList<Vector3f>();
			scanPositions.add(tileCenter);
			scanPositions.add(tileCenter.add(new Vector3f(increment, 0, increment)));
			scanPositions.add(tileCenter.add(new Vector3f(-increment, 0, increment)));
			scanPositions.add(tileCenter.add(new Vector3f(increment, 0, -increment)));
			scanPositions.add(tileCenter.add(new Vector3f(-increment, 0, -increment)));
			
			for(Vector3f scanPosition : scanPositions) {
				//if(memoryMap.positionIsInViewRange(pos, currentDirection, position) && world);
				Vector3f terrainNormal = world.getTerrainNormal(scanPosition);
				if(null == terrainNormal) {
					if(memoryMap.positionIsInViewRange(pos, currentDirection, scanPosition)) {
						//Ausserhalb der Map
						memoryMap.markTileAsOutOfMap(tile);
						return;
					} else {
						//nicht in Sichtweite
						return;
					}
				}
				if(world.isWater(scanPosition)) {
					isWater = true;
					isPassable = false;
					memoryMap.exploreTile(tile, isWater, isPassable, tileCenter);
					return;
				}
				if(!world.isPassable(scanPosition)){
					isPassable = false;
				}
			}
			memoryMap.exploreTile(tile, isWater, isPassable, tileCenter);
			return;
		}
	}
	
	private void scanTerrain(){
		List<LinkedTile> tiles = this.globalKI.getWorldMap().getTilesPossiblyInViewRange(this.pos);
		for(LinkedTile tile : tiles){
			scanTile(tile);
		}
		//Direkt voraus gucken
		Vector3f voraus = pos.add(world.getMyDirection().normalize().mult(2));
if(null == voraus) {
	System.out.println("Voraus ist NULL!!!");
}
if(voraus.x > 10000 || voraus.z > 10000) {
	System.out.println("Alerm!");
}
		if(null != world.getTerrainNormal(voraus) && !world.isPassable(voraus)) {
			LinkedTile tileVoraus = memoryMap.getTileAtCoordinate(voraus);
			memoryMap.exploreTile(tileVoraus, tileVoraus.isWater(), false, tileVoraus.getNormalVector());
		}	
	}

	@Override
	public GlobalKI getGlobalKi() {
		return globalKI;
	}

	@Override
	public IWorldInstance getWorld() {
		return world;
	}
}