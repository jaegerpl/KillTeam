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

package pascal.goap.Scenario.Sensors;

import java.awt.Point;

import pascal.goap.Scenario.BaseGame;
import pascal.goap.Scenario.MarketStand;
import pascal.goap.Scenario.Pedestrian;
import pascal.goap.Scenario.Helper.Helper;

import pascal.goap.pathfinding.NavNode;

import pascal.goap.Agent.Agent;
import pascal.goap.Agent.ISensor;
import pascal.goap.Agent.MemoryObject;
import pascal.goap.Agent.WorkingMemory;

/**
 * Sensor which searches the Agents visualRange for stands which could provide food
 * @author Klinge
 *
 */
public class FoodSensor implements ISensor {

	Pedestrian owner;
	
	public FoodSensor(Pedestrian owner)
	{
		this.owner =owner;
	}
	
	@Override
	public void update(WorkingMemory memory) {
		
		for(MarketStand foodStand :BaseGame.foodStands){
//			if(owner.inVisualRange(foodStand.getShape())){
//				for(NavNode n: foodStand.getFreeSpots()){
//					MemoryObject m = new MemoryObject(1.0f, "Food", Helper.navNodeToLocalCords(n));
//					
//					if(!n.blocked)		
//						memory.addMemory(m);
//					else
//						memory.removeMemory(m);
//				}
//			}
		}
	}

}
