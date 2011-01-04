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

import map.fastmap.LinkedTile;
import memory.pathcalulation.Path;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.enumn.EColors;
import de.lunaticsoft.combatarena.api.interfaces.IPlayer;
import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;
import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;

public class KillKI extends Agent implements IGOAPListener, IPlayer {

	private IWorldInstance world;
	private Vector3f direction; // tanks direction
	private Vector3f lastPos;		// tank position, last update
	private Vector3f goalPosition; // tanks goal its heading to
	private LinkedTile moveTarget;
	private boolean imHangar = true; 
	private String name;
	Path<LinkedTile> path = new Path();;

	private boolean stop = false;
	
	
	// my variables
	private Vector3f startPos;
	
	
		
	
	// GOAP STUFF
	private final GoapController gc = new GoapController((GoapActionSystem)actionSystem);
	private GlobalKI globalKI;
	private Map<Point, Boolean> localMap = new HashMap<Point, Boolean>();
	private EColors color;

	public KillKI(String name, GlobalKI globalKI) {
		this.name = name;
		
		// GOAP STUFF
		this.globalKI = globalKI;
		blackboard.name = name; // just for debugging
		actionSystem = new GoapActionSystem(this, blackboard,memory);	
		((GoapActionSystem)actionSystem).addGOAPListener(this);

		
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
		//System.out.println("CurrentPosition ="+world.getMyPosition());
		//System.out.println("GoalPosition ="+goalPosition);
		
		// current position
		
		
		//scan unknown terrain
		scanTerrain();
		explore();
		
		
		if(imHangar || moveTarget == null)
			world.move(direction);
		else if(moveTarget != null){
			System.out.println("bewege nach karte");
			world.move(moveTarget.getTileCenterCoordinates().subtract(world.getMyPosition()));
		}
	}
	
	private Vector3f rotateVector(Vector3f vec, float phi){
		Vector3f result = vec.clone();
		//result.x = FastMath.cos((float) (FastMath.atan2(vec.z, vec.x)+phi));
	//	result.z = FastMath.sin((float) (FastMath.atan2(vec.z, vec.x)+phi));
		
		result.x =vec.x * FastMath.cos(FastMath.DEG_TO_RAD*phi) - vec.z*FastMath.sin(FastMath.DEG_TO_RAD*phi);
		
		result.z =vec.z * FastMath.cos(FastMath.DEG_TO_RAD*phi) + vec.x*FastMath.sin(FastMath.DEG_TO_RAD*phi);
		
		return result;
	}
	
	public void explore(){
		//GOAP.getExploreDirection();
		if(startPos.distance(world.getMyPosition()) > 15)
			imHangar = false;
		
		if(lastPos == null){
			direction = new Vector3f(FastMath.rand.nextInt(200),0, FastMath.rand.nextInt(200));
		}
		else if(!imHangar){
			System.out.println("hangar verlassen");
			//neues ziel berechnen wenn ziel erreicht wurde
			
			//neues Ziel berechnen
			float drehung = 10;
			float bereitsGedreht = 0;
			if(path.isEmpty()){
				Vector3f eov = world.getMyPosition().clone().add(direction.clone().normalize().mult(30));
				
				while(path.isEmpty() || (!this.globalKI.getWorldMap().getTileAtCoordinate(eov).isPassable() ||! this.globalKI.getWorldMap().getTileAtCoordinate(eov).isExplored())){
					if(bereitsGedreht >= 360){
						System.out.println("KEIN NEUER PFAD");
						world.move(world.getMyPosition().add(rotateVector(direction, 45).normalize()));
						moveTarget = null;
						break;
						
					}
					System.out.println("Berechne n‰chstes Ziel, eov:"+eov);
					eov = world.getMyPosition().clone().add(direction.clone().normalize().mult(30));
			
					//TODO pfad nur berechnen wenn tile passable und explored ist
					path = this.globalKI.getWorldMap().calculatePath(this.globalKI.getWorldMap().getTileAtCoordinate(world.getMyPosition()), this.globalKI.getWorldMap().getTileAtCoordinate(eov));
					direction = rotateVector(direction, bereitsGedreht + drehung );
				//	world.move(world.getMyPosition().add(direction.clone().normalize().mult(2)));
					System.out.println("is eov passable: "+this.globalKI.getWorldMap().getTileAtCoordinate(eov).isPassable);
					System.out.println("empty: "+path.isEmpty());
					bereitsGedreht += drehung;
				}
				
			}
			//n‰chsten Wegpunkt als Ziel anvisieren
			
			System.out.println("Myposition tile: "+this.globalKI.getWorldMap().getTileAtCoordinate(world.getMyPosition()));
			System.out.println("target tile: "+moveTarget);
			System.out.println("world.pos: "+world.getMyPosition());
			if(moveTarget != null)
				System.out.println("world.move target: "+ moveTarget.getTileCenterCoordinates());
		//	if(this.globalKI.getWorldMap().getTileAtCoordinate(world.getMyPosition()).equals(moveTarget) || moveTarget == null)
			if(moveTarget != null && (world.getMyPosition().distance(moveTarget.getTileCenterCoordinates())< 0.1f))
			{
				System.out.println("Wegpunkt erreicht, lese n‰chsten Wegpunkt, Wegpunkte im Pfad "+path.waypointCount());
				moveTarget = path.getNextWaypoint();
			}
		}
		else if(imHangar && lastPos.distance(world.getMyPosition()) < 0.06f){
			//world.isWater(world.getMyPosition().add(world.getMyDirection().normalize().mult(3)));
			System.out.println("STUCK");
			System.out.println("olddirection: "+direction);
			direction = rotateVector(direction, 45);
			System.out.println("newdirection: "+direction);
		}
		lastPos = world.getMyPosition();
			
		
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
	 * umstellen und 'time' k√ºrzen: <br>
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
		// Bogenma√ü
		float angle = angleDeg / FastMath.RAD_TO_DEG;
		// gravity = 98.1f -> gravity/2 = 49.05f
		
		float time = FastMath.sqrt((distance / 49.05f) * FastMath.tan(angle));
		float speed = distance / (FastMath.cos(angle) * time);

		return speed;
	}

	@Override
	public void attacked(IWorldObject competitor) {
		/*
		Vector3f enemy = competitor.getPosition();
		String out = "Attacked by position " + enemy;
		Vector3f direction = world.getMyPosition().clone().subtract(enemy.clone()).negate();
		float distance = world.getMyPosition().distance(enemy);
		out += "\r\nDistance to enemy " + distance;

		float speed = getSpeed(30, distance);

		out += "\r\nSpeed " + speed;
		world.shoot(direction, speed, 30);

		// ACHTUNG: Keine Ausgaben in der Abgabe (Vorf√ºhrung)! "Logger" benutzen
		System.out.println("================\r\n" + out + "\r\n================");
		*/
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
		
		//ShootTarget target = Battle.getShootTarget(worldObject.getPosition(), world.getMyPosition());
		//world.shoot(target.direction, target.force, target.angle);
		
	}

	@Override
	public void spawn() {
		startPos = world.getMyPosition();
		direction = world.getMyDirection();
		goalPosition = startPos.add(new Vector3f(1,0,1));
		goalPosition.x = (int)goalPosition.x;
		goalPosition.z = (int)goalPosition.z;
//		goalPosition = new Vector3f(300f,18f,300f);
		stop = false;
		
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
		List<LinkedTile> tiles = this.globalKI.getWorldMap().getEmptyTilesPossiblyInViewRange(world.getMyPosition());
		for(LinkedTile tile : tiles){
			boolean isWater = false;
			boolean isPassable  = true;
			
			Vector3f terrain = world.getTerrainNormal(tile.getTileCenterCoordinates());
			if(terrain != null){
				if(!world.isPassable(tile.getTileCenterCoordinates())){
					//System.out.println("nicht passierbar "+ tile.tileCenterCoordinates);
					isPassable = false;
				}
				
				if(world.isWater(tile.getTileCenterCoordinates())){
					isWater = true;
					//System.out.println("wasser");
					tileWithWater = true;
				}
				
				tile.exploreTile(isWater, isPassable, terrain);
					
				//this.map.addTile(new LinkedTile(FastRoutableWorldMap.coordinates2MapIndex(tile.getTileCenterCoordinates()),
				//		isWater, isPassable, terrain, true));
			}
		}
		//if(tileWithWater)
			//this.direction  = this.direction.negate();

		
	}


}