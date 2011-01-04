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

package goap.pathfinding;

import goap.astar.IMap;
import goap.astar.IMover;
import goap.astar.Node;

import java.util.ArrayList;

;

/**
 * The data map from our example game. This holds the state and context of each tile
 * on the map. It also implements the interface required by the path finder. It's implementation
 * of the path finder related methods add specific handling for the types of units
 * and terrain in the example game.
 * 
 * @author Kevin Glass
 * @edited Arne Klingenberg
 * http://www.cokeandcode.com/pathfinding
 */
public class NavigationMap implements IMap {
	/** The map width in tiles */
	public int width;
	/** The map height in tiles */
	public int height;


    public NavNode[][] nodes;
	
	/**
	 * Create a new test map with some default configuration
	 */
	public NavigationMap(int width, int height) {

		this.width = width;
		this.height = height;
		
        nodes = new NavNode[width][];

        for(int x = 0; x < width; x++)
        {
            nodes[x] = new NavNode[height];

            for(int y = 0; y < height; y++)
            {
                nodes[x][y] = new NavNode(x,y);
            }
        }
	}

      public ArrayList<Node> getNeighbours(IMover mover, Node centre) {
         ArrayList<Node> neighbours = new ArrayList<Node>();

         NavNode temp = (NavNode)centre;

        if (temp.x - 1 >= 0 && temp.x - 1 < getWidth() && temp.y - 1 >= 0 && temp.y - 1 < getHeight() && nodes[temp.x-1][temp.y-1].isOpen(mover, this)) {
            neighbours.add(nodes[temp.x-1][temp.y-1]);
        }
        if (temp.x >= 0 && temp.x < getWidth() && temp.y - 1 >= 0 && temp.y - 1 < getHeight() && nodes[temp.x][temp.y-1].isOpen(mover, this)) {
            neighbours.add(nodes[temp.x][temp.y-1]);
        }
        if (temp.x + 1>= 0 && temp.x + 1 < getWidth() && temp.y - 1 >= 0 && temp.y - 1 < getHeight() && nodes[temp.x+1][temp.y-1].isOpen(mover, this)) {
            neighbours.add(nodes[temp.x+1][temp.y-1]);
        }
        if (temp.x - 1 >= 0 && temp.x - 1 < getWidth() && nodes[temp.x-1][temp.y].isOpen(mover, this)) {
            neighbours.add(nodes[temp.x-1][temp.y]);
        }
        if (temp.x + 1>= 0 && temp.x + 1 < getWidth() && nodes[temp.x+1][temp.y].isOpen(mover, this)) {
            neighbours.add(nodes[temp.x+1][temp.y]);
        }
        if (temp.x - 1 >= 0 && temp.x - 1 < getWidth() && temp.y + 1 >= 0 && temp.y + 1 < getHeight() && nodes[temp.x-1][temp.y+1].isOpen(mover, this)) {
            neighbours.add(nodes[temp.x-1][temp.y+1]);
        }
        if (temp.x >= 0 && temp.x < getWidth() && temp.y + 1 >= 0 && temp.y + 1 < getHeight() && nodes[temp.x][temp.y+1].isOpen(mover, this)) {
            neighbours.add(nodes[temp.x][temp.y+1]);
        }
        if (temp.x + 1>= 0 && temp.x + 1 < getWidth() && temp.y + 1 >= 0 && temp.y + 1 < getHeight() && nodes[temp.x+1][temp.y+1].isOpen(mover, this)) {
            neighbours.add(nodes[temp.x+1][temp.y+1]);
        }

        return neighbours;
    }

	public float getCost(IMover mover, Node current, Node neighbour) {
            if(((NavNode)current).x != ((NavNode)neighbour).x && ((NavNode)current).y != ((NavNode)neighbour).y)
		return 2;
            else
                return 1;
	}


	/**
	 * @see TileBasedMap#getHeightInTiles()
	 */
	public int getHeight() {
		return width;
	}

	/**
	 * @see TileBasedMap#getWidthInTiles()
	 */
	public int getWidth() {
		return height;
	}


	@Override
	public void pathFinderVisited(Node node) {
		// TODO Auto-generated method stub
		
	}	
	
}
