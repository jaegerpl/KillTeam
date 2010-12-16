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

package pascal.goap.pathfinding;

import pascal.goap.AStar.IMap;
import pascal.goap.AStar.IMover;
import pascal.goap.AStar.Node;
import pascal.goap.AStar.heuristics.NavClosestHeuristic;
import pascal.goap.Scenario.TileProperty;

/**
 * A node in a navMesh. Is used in the A* search to find a valid navigation
 * path.
 * @author Kevin Glass
 * @edited Arne Klingenberg
 * http://www.cokeandcode.com/pathfinding
 */
public class NavNode extends Node{

    /** The x coordinate of the node */
    public int x;
    /** The y coordinate of the node */
    public int y;
    
    public boolean blocked = false;
    public TileProperty property = TileProperty.Normal;
    
    public NavNode(int x, int y)
    {     
        heuristic = new NavClosestHeuristic();
        this.x = x;
        this.y = y;
    }
    
    /**
     * Checks if the node is walkable
     * @param mover
     * @param map
     * @return
     */
    public boolean isOpen(IMover mover, IMap map)
    {
        return !blocked;
    }

    public NavNode clone()
    {
        return new NavNode(x,y);
    }

    /**
     * Checks if the node is the goal node
     * If it is the A* search is finished and a vaild path has been found
     */
    public boolean isFinished(IMover mover, Node o)
    {
        NavNode other = (NavNode)o;

        if(other.x == x && other.y == y)
        {
        	o.parent = this.parent;
            return true;
        }
        else
            return false;
    }
    
    public boolean equals(Object o)
    {
    	NavNode other = (NavNode)o;
    	
    	if(other.x == this.x && other.y == this.y)
    		return true;
    	
    	return false;
    }
    
    public String toString()
    {
    	return "x: " + x + ", y: " + y;
    }
}
