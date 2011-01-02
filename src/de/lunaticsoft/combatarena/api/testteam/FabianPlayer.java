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

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.enumn.EColors;
import de.lunaticsoft.combatarena.api.interfaces.IPlayer;
import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;
import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;
import fabian.Battle;
import fabian.ShootTarget;

public class FabianPlayer implements IPlayer {

	private IWorldInstance world;
	private Vector3f direction;
	private Vector3f pos;

	private Vector3f lastPos = null;
	private float lastDistance = 0;
	private EColors color;
	private String name;

	private boolean stop = false;
	private Vector3f startpos;
	private boolean umgedreht=false;

	public FabianPlayer(String name) {
		this.name = name;
	}

	@Override
	public void setColor(EColors color) {
		this.color = color;
	}

	@Override
	public void update(float interpolation) {
		// current position
		pos = world.getMyPosition();
		if(world.getMyPosition().distance(startpos) >40)
			stop=true;
		if (!stop ) {
			// move
			world.move(direction);
			// check distance to detect blocked-movement
			if (lastPos != null) {
				float distance = pos.distance(lastPos);
				if (distance < 0.01f) {
					//direction.x *= 1.25f;
					direction = direction.negateLocal();
					//direction.x++;
				}
				lastDistance = distance;
			}
			lastPos = pos;
		}
		else{
			//world.stop();
			if( umgedreht){
			world.stop();
			}
			else{
				world.move(world.getMyDirection().negate());
				umgedreht = true;
			}
			
		}
	}

	@Override
	public void setWorldInstance(IWorldInstance world) {
		this.world = world;
	}



	@Override
	public void attacked(IWorldObject competitor) {
		
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
		// damn i died....
	}

	@Override
	public void perceive(ArrayList<IWorldObject> worldObjects) {
	
		for (IWorldObject worldObject : worldObjects) {
			switch (worldObject.getType()) {
			case Competitor:
				// ATTACK
				if(stop){	
					/*
					Vector3f direction = world.getMyPosition().clone().subtractLocal(worldObject.getPosition().clone());
					float distance = world.getMyPosition().distance(worldObject.getPosition());
					
						world.stop();
						stop = true;
						world.shoot(direction.negateLocal(), distance, 1f);
						*/
				//	for(int angle =0; angle<135;angle+=10){
			//			System.out.println(angle);
					
				//	}
					
				}
				break;
			case Hangar:
				if(stop){

				//	ShootTarget target = Battle.getShootTarget(worldObject.getPosition(), world.getMyPosition());
				//	world.shoot(target.direction, target.force, target.angle);
					
				}
			
				break;
			case Item:
				// COLLECT
				break;
			default:
				stop = false;
				break;
			}
		}
	}

	@Override
	public void spawn() {
		System.out.println("max reichtweite: "+Battle.getMaxDistance());
		direction = world.getMyDirection();
		stop = false;
		startpos = world.getMyPosition();
	}

	public String getTeamName() {
		return name;
	}
}