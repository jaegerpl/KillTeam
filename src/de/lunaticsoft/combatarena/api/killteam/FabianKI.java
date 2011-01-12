package de.lunaticsoft.combatarena.api.killteam;
//package de.lunaticsoft.combatarena.api.killteam;
//
//import goap.agent.Agent;
//import goap.agent.MemoryObject;
//import goap.agent.MemoryObjectType;
//import goap.goap.Action;
//import goap.goap.Goal;
//import goap.goap.IGOAPListener;
//import goap.scenario.GoapActionSystem;
//import goap.scenario.GoapController;
//
//import java.awt.Point;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//
//import map.fastmap.FastRoutableWorldMap;
//import map.fastmap.LinkedTile;
//import memory.map.MemorizedMap;
//import memory.objectStorage.MemorizedWorldObject;
//import memory.objectStorage.ObjectStorage;
//import memory.pathcalulation.Path;
//
//import battle.Battle;
//import battle.ShootTarget;
//
//import com.jme.math.FastMath;
//import com.jme.math.Matrix3f;
//import com.jme.math.Vector2f;
//import com.jme.math.Vector3f;
//
//import de.lunaticsoft.combatarena.api.enumn.EColors;
//import de.lunaticsoft.combatarena.api.enumn.EObjectTypes;
//import de.lunaticsoft.combatarena.api.interfaces.IPlayer;
//import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;
//import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;
//import de.lunaticsoft.combatarena.objects.WorldObject;
//
//public class FabianKI extends Agent implements IPlayer {
//	private IWorldInstance world;
//	
//	private EColors color;
//	private String name;
//	
//	private LinkedTile targetLocation;
//	Path<LinkedTile> path;
//	private Vector3f direction; // tanks direction
//
//	private boolean atHangar = true;
//	private Vector3f lastPos;
//	private Vector3f startPos;
//	
//	
//	// GOAP STUFF
//	private final GoapController gc = new GoapController((GoapActionSystem)actionSystem);
//	private GlobalKI globalKI;
//	private MemorizedMap memoryMap;
//	private ObjectStorage objectStorage = new ObjectStorage();
//
//	private Vector3f curPos;
//
//	public FabianKI(String name, GlobalKI globalKI) {
//		System.out.println("KillKI "+name+" gestartet");
//		this.name = name;
//		this.memoryMap = globalKI.getWorldMap();
//		this.globalKI = globalKI;
//	}
//
//	@Override
//	public void setColor(EColors color) {
//		this.color = color;
//	}
//	
//	public void moveOutdaHangar(int distance){
//		if(curPos.distance(startPos ) > distance)
//			return;
//		
//		
//	}
//	
//
//	@Override
//	public void update(float interpolation) {
//		lastPos = curPos;
//		curPos = world.getMyPosition();
//		
//		scanTerrain();
//		
//		LinkedTile myPosTile = memoryMap.getTileAtCoordinate(pos);
//		//Pr�fen ob durch neue Erkundung das Zwischenziel nicht mehr betretbar ist
//		if(null != moveTarget && !moveTarget.isPassable()) {
//			path = null;
//			moveTarget = null;
//		}
//		
//		if(null == moveTarget || moveTarget.equals(myPosTile)) {
//			explore();
//		}
//		
//		if(imHangar || moveTarget == null)
//			world.move(direction);
//		else if(moveTarget != null){
//			//System.out.println("bewege nach karte");
//			//System.out.println("");
//			//System.out.println("Panzer bei: " + pos + "(" + myPosTile + ")");
//			//System.out.println("Ziel bei: " + moveTarget.getTileCenterCoordinates() + "(" + moveTarget + ")");
//			Vector3f newDirection = moveTarget.getTileCenterCoordinates().subtract(pos);
//			//System.out.println("Bewege Richtung " + newDirection);
//			//System.out.println("");
//			world.move(newDirection);
//		}		
//	}
//	
//
//	
//	public void explore(){
//		//GOAP.getExploreDirection();
//		if(startPos.distance(world.getMyPosition()) > 15)
//			imHangar = false;
//		
//		if(lastPos == null){
//			direction = new Vector3f(FastMath.rand.nextInt(200),0, FastMath.rand.nextInt(200));
//		}
//		else if(!imHangar){
//			LinkedTile myPosTile = memoryMap.getTileAtCoordinate(pos);
//			//System.out.println("W�rde mich gern bewegen");
//			
//			if(null != path && !path.isEmpty()) {
//				//System.out.println("Path ist nicht NULL!");
//				if(myPosTile.equals(moveTarget)) {
//					System.out.println("Zwischenziel erreicht");
//					moveTarget = path.getNextWaypoint();
///*System.out.println();
//System.out.println("Aktuelle Position: " + myPosTile);
//System.out.println("Neues Zwischenziel: " + moveTarget);
//System.out.println("PassableTest: " +  world.isPassable(moveTarget.getTileCenterCoordinates()));*/
//				}
//			} else {
//				//Neuen Pfad berechnen
//				System.out.println("Neuen Pfad berechnen.");
//				//LinkedTile targetTile = memoryMap.getNearestUnexploredTile(pos);
//				Vector3f targetPos = this.pos.add(direction.normalize().mult(60));
//				LinkedTile targetTile = memoryMap.getTileAtCoordinate(targetPos);
//				if(targetTile.isPassable()) {
//					path = memoryMap.calculatePath(myPosTile, targetTile);
//					if(path.isEmpty()) {
//						this.direction = rotateVector(this.direction, 10);
//						moveTarget = null;
//					} else {
//					//	System.out.println("########### Pfad gefunden ###########");
//						moveTarget = path.getNextWaypoint();
//					//	System.out.println("Pfad: " + path);
//					}
//				} else {
//					//Rotieren und weitersuchen
//					this.direction = rotateVector(this.direction, 10);
//					moveTarget = null;
//				}
//			}
//			
//			/*
//			//neues ziel berechnen wenn ziel erreicht wurde
//			
//			
//			//n�chsten Wegpunkt als Ziel anvisieren
//			if(myPosTile.equals(moveTarget) || moveTarget == null || path.isEmpty())
//			{
//				//neues Ziel berechnen
//				if(path.isEmpty()){
//					Vector3f eov = new Vector3f();
//					
//					eov = world.getMyPosition().clone().add(direction.clone().normalize().mult(20));
//					LinkedTile targetTile = this.globalKI.getWorldMap().getTileAtCoordinate(eov);
//					if(!targetTile.isPassable()) {
//						targetTile = memoryMap.getNearestUnexploredTile(pos);
//					}
//					path = memoryMap.calculatePath(myPosTile, targetTile);
//					if(path.isEmpty()) {
//						//Kein Pfad in aktueller Richtung gefunden
//						direction = rotateVector(this.direction, 45);
//						eov = world.getMyPosition().clone().add(direction.clone().normalize().mult(40));
//						targetTile = this.globalKI.getWorldMap().getTileAtCoordinate(eov);
//					}*/
//					
//					/*while(path.isEmpty() || !targetTile.isPassable() || !targetTile.isExplored()){
//						System.out.println("Berechne n�chstes Ziel, eov:"+eov);
//						direction = rotateVector(direction,10);
//						eov = world.getMyPosition().clone().add(direction.clone().normalize().mult(20));
//					//	System.out.println("vor drehen: "+direction);
//					//	System.out.println("nach drehen: "+rotateVector(direction, 360));
//						this.
//						//TODO pfad nur berechnen wenn tile passable und explored ist
//						path = this.globalKI.getWorldMap().calculatePath(this.globalKI.getWorldMap().getTileAtCoordinate(world.getMyPosition()), this.globalKI.getWorldMap().getTileAtCoordinate(eov));
//						
//						System.out.println("is eov passable: "+this.globalKI.getWorldMap().getTileAtCoordinate(eov).isPassable());
//						System.out.println("empty: "+path.isEmpty());
//
//					}*/
//		/*			System.out.println("Wegpunkte: "+path.waypointCount());
//				}
//				System.out.println("Wegpunkt erreicht, lese n�chsten Wegpunkt, Wegpunkte im Pfad"+path.waypointCount());
//				moveTarget = path.getNextWaypoint();
//			}*/
//			 
//		}
//		else if(lastPos.distance(world.getMyPosition()) < 0.06f){
//			//world.isWater(world.getMyPosition().add(world.getMyDirection().normalize().mult(3)));
//			System.out.println("STUCK");
//			//System.out.println("olddirection: "+direction);
//			//direction = rotateVector(direction, 45);
//			//System.out.println("newdirection: "+direction);
//		}
//		lastPos = world.getMyPosition();
//			
//		
//	}
//	
//
//	@Override
//	public void setWorldInstance(IWorldInstance world) {
//		System.out.println("Panzer "+name+" hat World Instanz bekommen.");
//		this.world = world;
//		globalKI.setWorldInstance(world);
//	}
//
//	/**
//	 * y = speed * time * sin(angle) - (gravity / 2) * time^2 y -> 0 <br>
//	 * 0 = speed * time * sin(angle) - (gravity / 2) * time^2<br>
//	 * <br>
//	 * distance = speed * time * cos(angle)<br> 
//	 * speed = distance / (time * cos(angle))<br>
//	 * <br>
//	 * einsetzen: <br>
//	 * 0 = (distance / (time * cos(angle))) * time * sin(angle) - (gravity / 2) * time^2 <br>
//	 * umstellen und 'time' kürzen: <br>
//	 * 0 = (distance * (sin(angle) / cos(angle))) - (gravity / 2) * time^2 sin(angle) / cos(angle) <br>
//	 * -> tan(angle) 0 = (distance * tan(angle)) - (gravity / 2)* time^2 | (gravity / 2) (gravity / 2) <br>
//	 * -> 49.05f 0 = (distance * tan(angle)) - (gravity / 2) * time^2<br>
//	 * <br>
//	 * time^2 = (distance / 45.05f) * tan(angle) time = sqrt((distance / 45.05f) * tan(angle))<br>
//	 * <br>
//	 * distance = speed * time * cos(angle) <br>
//	 * umstellen:<br> 
//	 * speed = distance / (cos(angle) * time)<br>
//	 */
//	public float getSpeed(float angleDeg, float distance) {
//		// Bogenmaß
//		float angle = angleDeg / FastMath.RAD_TO_DEG;
//		// gravity = 98.1f -> gravity/2 = 49.05f
//		
//		float time = FastMath.sqrt((distance / 49.05f) * FastMath.tan(angle));
//		float speed = distance / (FastMath.cos(angle) * time);
//
//		return speed;
//	}
//
//	@Override
//	public void attacked(IWorldObject competitor) {
//		Vector3f enemy = competitor.getPosition();
//		String out = "Attacked by position " + enemy;
//		Vector3f direction = world.getMyPosition().clone().subtract(enemy.clone()).negate();
//		float distance = world.getMyPosition().distance(enemy);
//		out += "\r\nDistance to enemy " + distance;
//
//		float speed = getSpeed(30, distance);
//
//		out += "\r\nSpeed " + speed;
//		world.shoot(direction, speed, 30);
//
//		// ACHTUNG: Keine Ausgaben in der Abgabe (Vorführung)! "Logger" benutzen
//		System.out.println("================\r\n" + out + "\r\n================");
//	}
//
//	@Override
//	public void collected(IWorldObject worldObject) {
//		switch (worldObject.getType()) {
//		case Item:
//			// ITEM COLLECTED
//			break;
//		default:
//			// DO NOTHING
//			break;
//		}
//	}
//
//	@Override
//	public void die() {
//		
//		WorldObject wO = new WorldObject(null, color, this.pos, EObjectTypes.Item);
//		this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
//		// GOAP STUFF
//		globalKI.getBlackBoard().tanksAlive -= 1; // tell the GlobalKI about death of tank
//	}
//
//	@Override
//	public void perceive(ArrayList<IWorldObject> worldObjects) {	
//		
//		// move WorldObjects into WorkingMemory
//		for (IWorldObject wO : worldObjects) {
//			// confidence of memoryObjects will decrease over time
//			MemoryObject memo = new MemoryObject(1.0f, new MemoryObjectType(wO.getType(), 
//																			wO.getColor()), 
//																			wO.getPosition());
//			memory.addMemory(memo);
//			
//			switch(wO.getType()){
//				case Competitor:
//					if(wO.getColor() != this.color){
//						this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
//						System.out.println("Panzer gefunden: " + wO.hashCode());
//						ShootTarget target = Battle.getShootTarget(wO.getPosition(), this.pos);
//						world.shoot(target.direction, target.force, target.angle);
//						System.out.println("Feind entdeckt");
//					}
//					break;
//				case Hangar:
//					if(wO.getColor() != this.color){
//						this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
//						ShootTarget target = Battle.getShootTarget(wO.getPosition(), this.pos);
//						world.shoot(target.direction, target.force, target.angle);
//						System.out.println("feindlichen Hangar entdeckt");
//					}
//					break;
//				case Item:
//						this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
//						System.out.println("Item entdeckt");
//					break;
//				default: 
//					System.out.println("Kein WO");
//			}
//		}
//		
//		//ShootTarget target = Battle.getShootTarget(worldObject.getPosition(), world.getMyPosition());
//		//world.shoot(target.direction, target.force, target.angle);*/		
//	}
//
//	@Override
//	public void spawn() {
//		startPos = world.getMyPosition();
//		System.out.println("StartPos: "+startPos);
//		direction = world.getMyDirection();
//		goalPosition = startPos.add(new Vector3f(1,0,1));
//		goalPosition.x = (int)goalPosition.x;
//		goalPosition.z = (int)goalPosition.z;
////		goalPosition = new Vector3f(300f,18f,300f);
//		stop = false;
//		System.out.println("goalPosition: "+goalPosition);
//		
//		// GOAP STUFF
//		globalKI.getBlackBoard().tanksAlive += 1;// tell GlobalKI about rebirth of tank
//		blackboard.direction = direction;
//	}
//
//	public String getName() {
//		return name;
//	}
//
//
//	
//	private void scanTerrain(){
//		boolean tileWithWater = false;
//		List<LinkedTile> tiles = this.globalKI.getWorldMap().getEmptyTilesPossiblyInViewRange(curPos);
//		for(LinkedTile tile : tiles){
//			boolean isWater = false;
//			boolean isPassable  = true;
//			Vector3f terrain = world.getTerrainNormal(tile.getTileCenterCoordinates());
//			if(terrain != null){
//				if(!world.isPassable(tile.getTileCenterCoordinates())){
//					isPassable = false;
//				}
//
//				if(world.isWater(tile.getTileCenterCoordinates())){
//					isWater = true;
//					tileWithWater = true;
//				}
//				
//				memoryMap.exploreTile(tile, isWater, isPassable, terrain);
//			} else {
//				if(memoryMap.tileIsInViewRange(curPos, world.getMyDirection(), tile)) {
//					//NULL obwohl in Sichtweite
//					memoryMap.exploreTile(tile, false, false, new Vector3f(0,0,0));
//				}
//			}
//		}
//		if(tileWithWater)
//			this.direction  = this.direction.negate();
//
//		
//	}
//
//	@Override
//	public IWorldInstance getWorld() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public GlobalKI getGlobalKi() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//
//
//}