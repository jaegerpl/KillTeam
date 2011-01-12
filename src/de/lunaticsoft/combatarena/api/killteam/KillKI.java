package de.lunaticsoft.combatarena.api.killteam;



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
import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.enumn.EColors;
import de.lunaticsoft.combatarena.api.enumn.EObjectTypes;
import de.lunaticsoft.combatarena.api.interfaces.IPlayer;
import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;
import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;
import de.lunaticsoft.combatarena.api.killteam.globalKI.GlobalKI;
import de.lunaticsoft.combatarena.objects.WorldObject;

public class KillKI implements IPlayer {

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
	
	Path<LinkedTile> path = null;
	LinkedTile lastPathDest = null;

	private boolean pathReset = false;
	
	private boolean stop = false;
	private boolean calibrated = false;
	int viewRangeRadius = 0;
	int viewRangeOffset = 0;
	// my variables
	private Vector3f startPos;
	private Vector3f flagPos;
	private Vector3f flagPosPath; //flaPos für die der Pfad berechnet wurde
	private Task lastTask;
	
	private static Random r = new Random();
	
		
	
	// GOAP STUFF
	private GlobalKI globalKI;
	private Map<Point, Boolean> localMap = new HashMap<Point, Boolean>();
	private MemorizedMap memoryMap;
	private ObjectStorage objectStorage;
	protected TankBlackboard blackboard;
	private float inHangarThreshold = 15;
	private boolean calcNewDefendPath = true;
	
	//should be true if we play CTF
	private boolean CTFmode = false;
	private boolean iHaveTheFlag = false;
	private boolean flagCollectet = false;

	public KillKI(String name, GlobalKI globalKI, Task task) {
		//System.out.println("KillKI "+name+" gestartet");
		this.blackboard = new TankBlackboard();
		this.name = name;
		
		this.memoryMap = globalKI.getWorldMap();
		this.globalKI = globalKI;
		this.objectStorage = globalKI.getObjectStorage();
		
		
		lastPositions = new LinkedBlockingQueue<Vector3f>(2);
		this.blackboard.curTask = task;
	}

	@Override
	public void setColor(EColors color) {
		this.color = color;
	}

/**
 * Berechnet einen Weg zum Zielhangar, bewegt den Tank zum Zielhangar
 * und aktuallisiert die Hangars in der Karte
 */
public void goToHangar(){
	Vector3f goalPos = blackboard.spottedHangar.getPosition();
	Vector3f myPos = world.getMyPosition();
	LinkedTile targetTile = memoryMap.getTileAtCoordinate(goalPos);
	LinkedTile currentTile = memoryMap.getTileAtCoordinate(myPos);
	
	// berechne Weg zum Hangar
	if(path == null){	
		path = memoryMap.calculatePath(currentTile, targetTile);
	}
	
	// fahre den Pfad entlang
	if(null != path && !path.isEmpty()) {
		//System.out.println("Path ist nicht NULL!");
		if(currentTile.equals(moveTarget)) {
			//System.out.println("Zwischenziel erreicht");
			moveTarget = path.getNextWaypoint();
		}
	}
	
	// prŸfe ob du am Hangar bist und ob er noch existiert
	if(currentTile.equals(targetTile)){
		checkHangarExistance();
	}
	
}

/**
 * Prueft am Zielort, welche Hangars der Tank sehen sollte und welche er sieht 
 * und loescht die Hangars, die nicht mehr da sind.
 */
private void checkHangarExistance(){
	IWorldObject spottedHangar = blackboard.spottedHangar;
	Map<Point, MemorizedWorldObject> hangars = objectStorage.getEnemyHangarsOfPlayer(spottedHangar.getColor());
	
	// tiles auf denen ein hangar sein sollte
	HashMap<LinkedTile, List<MemorizedWorldObject>> hangarTiles = new HashMap<LinkedTile, List<MemorizedWorldObject>>(); 
	for(MemorizedWorldObject obj : hangars.values()){
		LinkedTile tile = memoryMap.getTileAtCoordinate(obj.getPosition());
		if(hangarTiles.containsKey(tile)){
			hangarTiles.get(tile).add(obj);
		} else {
			hangarTiles.put(tile, new ArrayList<MemorizedWorldObject>());
			hangarTiles.get(tile).add(obj);
		}
	}
}
	@Override
	public void update(float interpolation) {
		if(!calibrated) {
			calibrate();
		}
		evalNextTask(); //update Mission/Task of the tank
		if(!stop){
			// GOAP STUFF
			//System.out.println("CurrentPosition ="+world.getMyPosition());
			//System.out.println("GoalPosition ="+goalPosition);
			
			// current position
			pos = world.getMyPosition();
			currentDirection = world.getMyDirection();
			
			//scan unknown terrain
			scanTerrain();
			
	
			if(stuck()) {
				System.out.println(name + " steckt fest. Sein Ziel ist " + moveTarget + 
						"und er befindet sich an Position " + memoryMap.getTileAtCoordinate(pos));
				int faktor = 1;
				if(Math.random() > 0.5){
					faktor = -1;
				}
			LinkedTile myPosTile = memoryMap.getTileAtCoordinate(pos);
			//Pruefen ob durch neue Erkundung das Zwischenziel nicht mehr betretbar ist
			if(null != moveTarget && (!moveTarget.isPassable() || !myPosTile.isPassable()) || lastTask != blackboard.curTask) {
				if(!pathReset) {
					path = null;
					moveTarget = null;
					pathReset = true;
				}
			} else {
				pathReset = false;
			}
			
			
			if( moveTarget == null || moveTarget.equals(myPosTile)) {
				if(this.blackboard.curTask == Task.DEFEND)
					defend();
				else if(this.blackboard.curTask == Task.EXPLORE)
					explore();
				else if(this.blackboard.curTask == Task.CTF)
				{
					if(flagPos != flagPosPath)
						pathReset = true;
						goToTarget(memoryMap.getTileAtCoordinate(flagPos));
					    
				}
					else if(this.blackboard.curTask == Task.RAPEaHANGAR)
						rapeHangar();
					else if(iHaveTheFlag && flagCollectet)
						goToTarget(memoryMap.getTileAtCoordinate(startPos));
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
		lastTask = blackboard.curTask;
	}
	
	private void evalNextTask(){
		if(blackboard.curTask == Task.DEFEND)
			return;
		else if(iHaveTheFlag && flagCollectet)
			blackboard.curTask = Task.GoToBase;
		/*
		else if(blackboard.curTask == Task.EXPLORE){
			if(CTFmode){
				if(pathToFlagKnown()){
					blackboard.curTask = Task.CTF;
				}
					blackboard.curTask = Task.EXPLORE;
			}
			else
			}
			*/
			
		}
		
	

	
	private void goToTarget(LinkedTile target) {
		if(pathReset){
			flagPosPath = flagPos;
			path = memoryMap.calculatePath(memoryMap.getTileAtCoordinate(pos), target);
		}
		moveTarget = path.getNextWaypoint();		
	}

	private boolean pathToFlagKnown()
{
		if(memoryMap.calculatePath(memoryMap.getTileAtCoordinate(pos), memoryMap.getTileAtCoordinate(flagPos)).isEmpty())
			return false;
		return true;
	}
	
	private void rapeHangar() {
		
		
	}



	/**
	 * Pruefe den Sichtbereich  um daraus den Mittelpunkt und die 
	 * Position des Tanks zum Mittelpunkt zu berechnen
	 */
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
	
	/**
	 * Prueft, ob sich der Panzer zu wenig bewegt hat.
	 * @return
	 */
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
	
	public void defend(){
		if(world.getMyPosition().distance(startPos) > 25){
			blackboard.inHangar = false;
		}
		
		if(!blackboard.inHangar){
			if(calcNewDefendPath){
			float distance = world.getMyPosition().distance(startPos);
			//float distance = 10;
			double x = 0d; // real part
			double z = 0d; // imaginary part
			
			path = new Path<LinkedTile>();
			path.setCircleCourse(true);

				for (int angle = 0; angle < 360; angle += 15) {
					x = distance * Math.cos(angle);
					z = distance * Math.sin(angle);

					Vector3f d = startPos.clone();
					d.x += x;
					d.z += z;

					path.addWaypoint(memoryMap.getTileAtCoordinate(d));
					
				}
				
				calcNewDefendPath = false;
			}
			
				moveTarget = path.getNextWaypoint();
			}
	else
		System.out.println("abstand zum hangar zu klein");
	}
	
	
	
	
	public void explore(){
		if(startPos.distance(world.getMyPosition()) > inHangarThreshold )
			blackboard.inHangar = false;
		//GOAP.getExploreDirection();
		
		//if(lastPos == null){
		//	direction = new Vector3f(FastMath.rand.nextInt(200),0, FastMath.rand.nextInt(200));
		//}
		//else 
			if(!blackboard.inHangar){
				Vector3f targetPos = this.pos.add(direction.normalize().mult(30));

			LinkedTile myPosTile = memoryMap.getTileAtCoordinate(pos);
			//System.out.println("Wï¿½rde mich gern bewegen");
			
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
				 targetPos = this.pos.add(direction.normalize().mult(60));
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
	 * umstellen und 'time' kÃ¼rzen: <br>
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
		// BogenmaÃŸ
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
					/*
				case Flag:
					iHaveTheFlag = true;
					break;
					*/
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
		blackboard.direction = direction;
		blackboard.inHangar = true;
	}

	public String getName() {
		return name;
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

	public GlobalKI getGlobalKi() {
		return globalKI;
	}

	public IWorldInstance getWorld() {
		return world;
	}
}
