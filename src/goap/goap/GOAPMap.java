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
package goap.goap;

import goap.astar.IMap;
import goap.astar.IMover;
import goap.astar.Node;

import java.util.ArrayList;


/**
 * The GOAPMap is the central class in the process of formulating a new sequence of actions
 * to fulfill a goal (plan).
 * It gets passed into the generic A*-machine and is responsible for finding all actions
 * that can satisfy currently unsatisfied worldstates (e.g neigbours in the A* graph)
 * @author Klinge
 *
 */
public class GOAPMap implements IMap{

	@Override
	public float getCost(IMover mover, Node current, Node neighbour) {
		
		GOAPNode gCurrent = (GOAPNode)current;
		GOAPNode gNeighbour = (GOAPNode)neighbour;
		
		return gNeighbour.getAction().getCost();
	}

	@Override
	/**
	 * Gets all valid neighbours to the given action (centre node)
	 * A neigbour is valid if it satisfies any of the currently unsatisfied worldstates.
	 */
	public ArrayList<Node> getNeighbours(IMover mover, Node centre) {

		ArrayList<Node> nodes = new ArrayList<Node>();
		GOAPNode parent = (GOAPNode)centre;
		GOAPManager manager = (GOAPManager)mover;
		
		for(Action a: manager.getActions())
		{
			for(WorldStateSymbol s : parent.unsatisfiedConditions)
				if(a.getEffect().contains(s))
				{
					boolean unsatisfiedCondition = false;
					WorldStateSymbol effectSymbol = a.getEffect().get(a.getEffect().indexOf(s));
					
					//If the action doesn't change the agents state in the desired way
					//skip this action
					if(!parent.goalState.isSatisfied(effectSymbol))
						break;			
					
					//If the action contains a precondition that is not satisfied yet and
					//that does not get satisfied through the action itself
					//this action is not suitable at the moment
					for(WorldStateSymbol unsatisfied : parent.unsatisfiedConditions)
						if(!a.getEffect().contains(unsatisfied) && a.getPreconditions().contains(unsatisfied) 
								|| !a.contextPreconditionsMet())
							unsatisfiedCondition = true;
					
					if(unsatisfiedCondition)
						break;
					
					GOAPNode neighbour = new GOAPNode(a);
					//1.Add the goal conditions of the parent node to the goal conditions of the new node
					neighbour.goalState = parent.goalState.clone();
					//TODO: hier muss vlt noch darauf geachtet werden, dass keine conditions doppelt eingetragen werden
					//2.Add all preconditions to goalConditions of the new node
					neighbour.goalState.addAll(a.getPreconditions());
					
					//3.Update the unsatisfiedConditions with the new preconditions and the value from the agents current state
					ArrayList<WorldStateSymbol> unsatisfiedConditions = new ArrayList<WorldStateSymbol>();
					
					//The action could solve more than one unsatisfied condition
					//therefore we need to check against all effects of the action
					for(WorldStateSymbol unsatisfied : parent.unsatisfiedConditions)
					{
						WorldStateSymbol effect;
						
						if(a.getEffect().contains(unsatisfied))
						{
							effect = a.getEffect().get(a.getEffect().indexOf(unsatisfied));
							
							if(!parent.goalState.isSatisfied(effect))
								unsatisfiedConditions.add(unsatisfied.clone());
						}
						else
							unsatisfiedConditions.add(unsatisfied.clone());
					}
					
					//Also add the the preconditions for this action
					//to the unsatisfied conditions of the new node if
					//they are not currently satisfied
					for(WorldStateSymbol preCond : a.getPreconditions())
					{
						//Some actions have preconditions that the condition they satisfy
						//has not been satisfied yet, so we need to check that so that the
						//condition the action has just setisfied does not get added again
						if(a.getEffect().contains(preCond) || unsatisfiedConditions.contains(preCond))
							unsatisfiedCondition = true;;
						
						if(!manager.currentWorldState.isSatisfied(preCond))
							unsatisfiedConditions.add(preCond.clone());
					}
					
					if(unsatisfiedCondition)
						break;
					
					neighbour.unsatisfiedConditions = unsatisfiedConditions;
					
					nodes.add(neighbour);
				}
		}
				
		return nodes;
	}

	@Override
	public void pathFinderVisited(Node node) {
		// TODO Auto-generated method stub
		
	}

}
