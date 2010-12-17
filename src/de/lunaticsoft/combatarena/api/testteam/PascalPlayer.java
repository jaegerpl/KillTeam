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

import java.util.ArrayList;
import java.util.Random;

import pascal.goap.Agent.Agent;
import pascal.goap.Agent.MemoryObject;
import pascal.goap.Agent.MemoryObjectType;
import pascal.goap.Goap.Action;
import pascal.goap.Goap.Goal;
import pascal.goap.Goap.IGOAPListener;
import pascal.goap.Scenario.GoapActionSystem;
import pascal.goap.Scenario.GoapController;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.enumn.EColors;
import de.lunaticsoft.combatarena.api.interfaces.IPlayer;
import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;
import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;

public class PascalPlayer extends Agent implements IGOAPListener, IPlayer {

	private IWorldInstance world;
	private Vector3f direction;
	private Vector3f pos;

	private Vector3f lastPos = null;
	private float lastDistance = 0;
	private EColors color;
	private String name;

	private boolean stop = false;
	
	
	// my variables
	private Vector3f startPos;
	private boolean turnPointReached= false;
	
	
	private static Random r = new Random();
		
	
	// GOAP STUFF
	private final GoapController gc = new GoapController((GoapActionSystem)actionSystem);
	private GlobalKI globalKI;

	public PascalPlayer(String name, GlobalKI globalKI) {
		System.out.println("PascalPlayer "+name+" gestartet");
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
		
		// current position
		pos = world.getMyPosition();
		if (!stop) {
			// move
			world.move(direction);
			// check distance to detect blocked-movement
			if (lastPos != null) {
				float distance = pos.distance(lastPos);
				if (distance < (lastDistance - 0.0005f)) {
					direction.negateLocal();
				}
				if(!turnPointReached && pos.distance(startPos) > 15f){
					turnPointReached = true;
					stop = true;
				}
				
				lastDistance = distance;
			}
			lastPos = pos;
		}
		
		world.getMyDirection();
		Vector3f pos = world.getMyPosition();
		world.getTerrainNormal(pos);
		world.isPassable(pos);
		world.isPassableNormal(pos);
		world.isWater(pos);
		world.stop(); //????
		
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
		globalKI.getBlackBoard().tanksAlive -= 1; // the global blackboard
	}

	@Override
	public void perceive(ArrayList<IWorldObject> worldObjects) {	
		// move WorldObjects into WorkingMemory
		for (IWorldObject wO : worldObjects) {
			// confidence of memoryObjects will decrease over time
			MemoryObject memo = new MemoryObject(1.0f, new MemoryObjectType(wO.getType(), wO.getColor()), wO.getPosition());
			memory.addMemory(memo);
		}
	}

	@Override
	public void spawn() {
		startPos = world.getMyPosition();
		direction = new Vector3f(25, 0, 25);
		stop = false;
		
		// GOAP STUFF
		globalKI.getBlackBoard().tanksAlive += 1;// the global blackboard
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
}