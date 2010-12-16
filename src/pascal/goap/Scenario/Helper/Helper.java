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

package pascal.goap.Scenario.Helper;

import pascal.goap.Scenario.BaseGame;
import pascal.goap.pathfinding.NavNode;
import pascal.goap.pathfinding.NavigationMap;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

/**
 * Helper class which provides methods to map 2d NavNode coordinates to
 * 3d World coordinates and vice versa.
 * @author Klinge
 *
 */
public class Helper {
	
	public static NavNode getNavNode(NavigationMap navMap, Vector3f pos)
	{
		return navMap.nodes[(int)FastMath.floor(pos.x / BaseGame.TILESIZE)]
				[(int)FastMath.floor(pos.z / BaseGame.TILESIZE)];
	}
	
	public static Vector3f navNodeToLocalCords(NavNode n)
	{
		return PointToLocalCords(n.x, n.y);
	}
	
	public static Vector3f PointToLocalCords(int x, int y)
	{
		return new Vector3f(x * BaseGame.TILESIZE + BaseGame.TILESIZE / 2, 0, y * BaseGame.TILESIZE + BaseGame.TILESIZE / 2);
	}

}
