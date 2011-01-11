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
 * Goal to let the agent capture the flag and move it back to his base. 
 * 
 * @author Pascal Jaeger
 *
 */
public class CaptureTheFlagGOAL extends Goal implements ITankGoal{

	/**
	 * Goal-conditions<br>
	 * - tank has flag<br>
	 * - tank is in hangar<br>
	 * @param name
	 * @param relevancy default value 0.1f
	 * @param as
	 */
	public CaptureTheFlagGOAL(GoapActionSystem as) {
		super("CaptureTheFlag", 1, as);
	}

	@Override
	public GOAPNode getNode() {
		GOAPNode goal = new GOAPNode(null);
		goal.goalState = new WorldState();
		goal.goalState.add(new WorldStateSymbol<Boolean>(TankWorldProperty.HasFlag, true, PropertyType.Boolean));
		goal.goalState.add(new WorldStateSymbol<Boolean>(TankWorldProperty.InHangar, true, PropertyType.Boolean));
		
		goal.unsatisfiedConditions = new ArrayList<WorldStateSymbol>();
		goal.unsatisfiedConditions.add(new WorldStateSymbol<Boolean>(TankWorldProperty.HasFlag, true, PropertyType.Boolean));
		goal.unsatisfiedConditions.add(new WorldStateSymbol<Boolean>(TankWorldProperty.InHangar, true, PropertyType.Boolean));
		
		return goal;
	}

	@Override
	public boolean isFullfilled() {
		if(as.currentWorldState.isSatisfied(new WorldStateSymbol<Boolean>(TankWorldProperty.HasFlag, true, PropertyType.Boolean)) &&
		   as.currentWorldState.isSatisfied(new WorldStateSymbol<Boolean>(TankWorldProperty.InHangar, true, PropertyType.Boolean)) ){
			return true;
		}
		return false;
	}

	@Override
	public void updateRelevance() {
		relevancy = 1f; // always top goal during capture the flag game
	}
}
