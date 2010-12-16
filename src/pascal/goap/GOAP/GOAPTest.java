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

//package GOAP;
//
//import java.util.ArrayList;
//
//import AStar.AStarMachine;
//import Agent.Agent;
//
//public class GOAPTest {
//
//	public static ArrayList<Action> availableActions = new ArrayList<Action>();
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		
//		ArrayList<WorldStateSymbol> preCond;
//		ArrayList<WorldStateSymbol> effects;
//		
//		Agent asi = new Agent();
//		
//		preCond = new ArrayList<WorldStateSymbol>();
//		effects = new ArrayList<WorldStateSymbol>();
//		preCond.add(new WorldStateSymbol(WorldProperty.HasRangedWeapon, true));
//	    effects.add(new WorldStateSymbol(WorldProperty.WeaponLoaded, true));
//		
//		availableActions.add(new Action("Reload",3.0f,preCond,effects));
//		
//		preCond = new ArrayList<WorldStateSymbol>();
//		effects = new ArrayList<WorldStateSymbol>();
//		effects.add(new WorldStateSymbol(WorldProperty.WeaponLoaded, false));
//	    effects.add(new WorldStateSymbol(WorldProperty.HasRangedWeapon, true));
//		
//		availableActions.add(new Action("PickUpWeapon",1.0f,preCond,effects));
//		
//		preCond = new ArrayList<WorldStateSymbol>();
//		effects = new ArrayList<WorldStateSymbol>();
//		preCond.add(new WorldStateSymbol(WorldProperty.HasRangedWeapon, true));
//		preCond.add(new WorldStateSymbol(WorldProperty.WeaponLoaded, true));
//	    effects.add(new WorldStateSymbol(WorldProperty.TargetDead, true));
//		
//		availableActions.add(new Action("Shoot",1.0f,preCond,effects));
//		
//		preCond = new ArrayList<WorldStateSymbol>();
//		effects = new ArrayList<WorldStateSymbol>();
//
//	    effects.add(new WorldStateSymbol(WorldProperty.InRange, true));
//		
//		availableActions.add(new Action("GetInRange",2.0f,preCond,effects));
//		
//		preCond = new ArrayList<WorldStateSymbol>();
//		effects = new ArrayList<WorldStateSymbol>();
//	    effects.add(new WorldStateSymbol(WorldProperty.TargetDead, true));
//	    preCond.add(new WorldStateSymbol(WorldProperty.HasMeleWeapon, true));
//		availableActions.add(new Action("AttackMele",4.0f,preCond,effects));
//		
//		WorldState state = new WorldState();
//		
//		state.add(new WorldStateSymbol(WorldProperty.TargetDead, false));
//		
//		//state.applyAction(availableActions.get(availableActions.size() -1));
//		
//		asi.currentWorldState = state;
//		
//		AStarMachine<GOAPNode> aStar = new AStarMachine<GOAPNode>(new GOAPMap(), 100);
//		
//		GOAPNode goal = new GOAPNode(null);
//		goal.goalState = new WorldState();
//		goal.goalState.add(new WorldStateSymbol(WorldProperty.TargetDead, true));
//		goal.unsatisfiedConditions = new ArrayList<WorldStateSymbol>();
//		goal.unsatisfiedConditions.add(new WorldStateSymbol(WorldProperty.TargetDead, true));
//		
//		asi.getBlackboard().availableActions = availableActions;
//		
//		aStar.findPath(asi, goal, new GOAPNode(null));
//
//	}
//
//}
