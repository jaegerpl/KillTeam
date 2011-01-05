



/**
 * DIENT ALS BEISPIEL GOAL
 */







///*
// * Copyright (C) 2009 Arne Klingenberg
// * E-Mail: klingenberg.a@googlemail.com
// * 
// * This software is free; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License version 2 as published
// * by the Free Software Foundation.
// * 
// * You should have received a copy of the GNU General Public License along with
// * this program; if not, see <http://www.gnu.org/licenses/>.
// */
//
//package goap.scenario.goals;
//
//import goap.goap.GOAPNode;
//import goap.goap.Goal;
//import goap.goap.PropertyType;
//import goap.goap.TankWorldProperty;
//import goap.goap.WorldState;
//import goap.goap.WorldStateSymbol;
//import goap.scenario.GoapActionSystem;
//import goap.scenario.goals.interfaces.ITankGoal;
//
//import java.util.ArrayList;
//
//
///**
// * Goal to let the agent explore its environment
// * GoalState: WorldProperty.AtDestination, true
// * @author Klinge
// *
// */
//public class Explore extends Goal implements ITankGoal{
//
//	public Explore(String name, float relevancy, GoapActionSystem as) {
//		super(name, relevancy, as);
//	}
//
//	@Override
//	public GOAPNode getNode() {
//		GOAPNode goal = new GOAPNode(null);
//		goal.goalState = new WorldState();
//		goal.goalState.add(new WorldStateSymbol<Boolean>(TankWorldProperty.AtDestination, true, PropertyType.Boolean));
//		goal.unsatisfiedConditions = new ArrayList<WorldStateSymbol>();
//		goal.unsatisfiedConditions.add(new WorldStateSymbol<Boolean>(TankWorldProperty.AtDestination, true, PropertyType.Boolean));
//		
//		return goal;
//	}
//
//	@Override
//	public boolean isFullfilled() {
//		
//		return as.currentWorldState.isSatisfied(new WorldStateSymbol<Boolean>(TankWorldProperty.AtDestination, true, PropertyType.Boolean));
//	}
//
//	@Override
//	public void updateRelevance() {
//		//TODO: sollte nicht hart eingecoded sein
//		relevancy = 0.2f;	
//	}
//}
