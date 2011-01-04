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

package goap.scenario;

import goap.pathfinding.NavNode;
import goap.pathfinding.NavigationMap;
import goap.scenario.helper.Helper;

import java.util.ArrayList;


import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;


/**
 * A Marketstand which provides food
 * @author Klinge
 *
 */
public class MarketStand {
	
	protected Box stand;
	protected static int number = 1;
	protected static Node stands;
	protected ArrayList<NavNode> freeSpots;
	protected int greatestX = Integer.MIN_VALUE;
	protected int greatestY = Integer.MIN_VALUE;
	protected int lowestX = Integer.MAX_VALUE;
	protected int lowestY = Integer.MAX_VALUE;
	protected Node parent;

	public MarketStand(float centreX, float centreY, Node node, NavigationMap navMap, Vector3f size) {
		
		parent = node;
		node.attachChild(stand);	
		
		blockNavPoints(node,navMap);
		generateFreeSpots(navMap);
	}
	
	/**
	 * Block the navPoints under the marketStand so that agents do not run into it
	 * @param node
	 * @param navMap
	 */
	private void blockNavPoints(Node node, NavigationMap navMap)
	{
		Box bounds = new Box("boundingBOx",Vector3f.ZERO,0.5f,0.5f,0.5f);
		bounds.setModelBound(new BoundingBox());
		bounds.updateModelBound();
		node.attachChild(bounds);
			
		for(int x = 0; x < navMap.width; x++)
			for(int y = 0; y < navMap.height; y++)
			{
				bounds.setLocalTranslation(Helper.navNodeToLocalCords(navMap.nodes[x][y]));
				bounds.updateModelBound();
				
				if(bounds.hasCollision(stand, false))
				{
					navMap.nodes[x][y].blocked = true;
					
					if(x > greatestX)
						greatestX = x;
					if(y > greatestY)
						greatestY = y;
					if(y < lowestY)
						lowestY = y;
					if(x < lowestX)
						lowestX = x;			
				}
			}
		
		bounds.removeFromParent();
	}
	
	/**
	 * Generates a list of locations where agents are able to get food
	 */
	protected void generateFreeSpots(NavigationMap navMap)
	{
		freeSpots = new ArrayList<NavNode>();
		
		for(int i = lowestX -1; i <= greatestX + 1; i++)
		{
			freeSpots.add(navMap.nodes[i][lowestY -1]);
			freeSpots.add(navMap.nodes[i][greatestY + 1]);
		}
		
		for(int i = lowestY -1; i <= greatestY + 1; i++)
		{
			freeSpots.add(navMap.nodes[lowestX -1][i]);
			freeSpots.add(navMap.nodes[greatestX + 1][i]);
		}		
	}
	
	/**
	 * Returns all free locations where agents currently can get food
	 * @return
	 */
	public ArrayList<NavNode> getFreeSpots(){
		return freeSpots;
	}
}
