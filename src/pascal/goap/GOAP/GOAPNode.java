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

package pascal.goap.GOAP;

import java.util.ArrayList;

import pascal.goap.AStar.IMover;
import pascal.goap.AStar.Node;

/**
 * A node to represent the actions in a form the A*-machine is able to understand
 * @author Klinge
 */
public class GOAPNode extends Node {

	public WorldState goalState = new WorldState();
	public ArrayList<WorldStateSymbol> unsatisfiedConditions;
	private Action action;
	
	/**
	 * Creates a new Node for the AStarMachine
	 * @param action : The action that led to the Node (e.g the worldstate after the execution of the action)
	 */
	public GOAPNode(Action action)
	{
		this.action = action;
		heuristic = new UnsatisfiedWorldStatesHeuristic();
		unsatisfiedConditions = new ArrayList<WorldStateSymbol>();
	}
	
	/**
	 * Gets the action that led to this node
	 * @return
	 */
	public Action getAction()
	{
		return action;
	}


	/**
	 * Checks if the A*-search is finished
	 * The search is finished if there are no more unsatisfied conditions
	 */
	public boolean isFinished(IMover mover, Node o) {
		
		if(unsatisfiedConditions != null && unsatisfiedConditions.size() == 0)
		{		
			GOAPNode endNode = (GOAPNode)o;
			//since we do not know the end of the path when we start the search for a plan
			//we need to set this node as the end node otherwise
			//the AStarMachine can not trace back to the beginning
			endNode.parent = this.parent;
			endNode.goalState = this.goalState;
			endNode.unsatisfiedConditions = this.unsatisfiedConditions;
			endNode.action = this.action;
			
			return true;
		}
		else
			return false;
	}

	@Override
	public Node clone() {
		GOAPNode node = new GOAPNode(action);
		node.unsatisfiedConditions = new ArrayList<WorldStateSymbol>();
		
		for(WorldStateSymbol s : unsatisfiedConditions)
			node.unsatisfiedConditions.add(s.clone());
		
		node.goalState = goalState.clone();
		
		node.parent = parent;
		
		return node;
	}
}
