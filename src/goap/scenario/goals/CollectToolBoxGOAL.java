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

package goap.scenario.goals;

import goap.goap.GOAPNode;
import goap.goap.Goal;
import goap.goap.PropertyType;
import goap.goap.TankWorldProperty;
import goap.goap.WorldState;
import goap.goap.WorldStateSymbol;
import goap.scenario.GoapActionSystem;
import goap.scenario.goals.interfaces.ITankGoal;

import java.util.ArrayList;


/**
 * Goal to let the agent collect a toolbox, to regain complete health. 
 * The more the tanks has been hit, the higher the relevancy of this goal is.
 * 
 * @author Pascal Jaeger
 *
 */
public class CollectToolBoxGOAL extends Goal implements ITankGoal{

	/**
	 * @param name
	 * @param relevancy default value 0.1f
	 * @param as
	 */
	public CollectToolBoxGOAL(String name, float relevancy, GoapActionSystem as) {
		super(name, relevancy, as);
	}

	@Override
	public GOAPNode getNode() {
		GOAPNode goal = new GOAPNode(null);
		goal.goalState = new WorldState();
		goal.goalState.add(new WorldStateSymbol<Boolean>(TankWorldProperty.ToolBoxSpotted, false, PropertyType.Boolean));
		goal.goalState.add(new WorldStateSymbol<Boolean>(TankWorldProperty.ToolBoxCollected, true, PropertyType.Boolean));
		
		goal.unsatisfiedConditions = new ArrayList<WorldStateSymbol>();
		goal.unsatisfiedConditions.add(new WorldStateSymbol<Boolean>(TankWorldProperty.ToolBoxSpotted, false, PropertyType.Boolean));
		goal.unsatisfiedConditions.add(new WorldStateSymbol<Boolean>(TankWorldProperty.ToolBoxCollected, true, PropertyType.Boolean));
		
		return goal;
	}

	@Override
	public boolean isFullfilled() {
		if(as.currentWorldState.isSatisfied(new WorldStateSymbol<Boolean>(TankWorldProperty.ToolBoxSpotted, false, PropertyType.Boolean)) &&
		   as.currentWorldState.isSatisfied(new WorldStateSymbol<Boolean>(TankWorldProperty.ToolBoxCollected, true, PropertyType.Boolean)) ){
			return true;
		}
		return false;
	}

	@Override
	public void updateRelevance() {
		int hits = as.getBlackboard().hitsTaken;
		if (hits == 0){
			relevancy = 0.1f;  // collecting toolboxes gives points
		} 
		else if(hits == 1){
			relevancy = 0.5f;
		} 
		else if(hits > 1){
			relevancy = 0.9f;
		}
		
	}
}
