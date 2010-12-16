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

import pascal.goap.AStar.AStarHeuristic;
import pascal.goap.AStar.IMap;
import pascal.goap.AStar.Node;

public class UnsatisfiedWorldStatesHeuristic implements AStarHeuristic{

	@Override
	public float getCost(IMap map, Node start, Node end) {
			
		return ((GOAPNode)start).unsatisfiedConditions.size();	
	}

}
