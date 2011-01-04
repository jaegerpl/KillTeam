package goap.astar;

import java.util.ArrayList;

/**
 * The description for the data we're searching over.
 * 
 * @author Kevin Glass
 * http://www.cokeandcode.com/pathfinding
 * @edited Arne Klingenberg
 * 
 */
public interface IMap {

	/**
	 * Notification that the path finder visited a given tile. This is 
	 * used for debugging new heuristics.
     */
	public void pathFinderVisited(Node node);
	
	/**
	 * Get the cost of moving through the given tile. This can be used to 
	 * make certain areas more desirable. A simple and valid implementation
	 * of this method would be to return 1 in all cases.
     */
	public float getCost(IMover mover, Node current, Node neighbour);

    /**
     * Returns all neighbours to the given centre node
     * @param centre
     * @return
     */
    public ArrayList<Node> getNeighbours(IMover mover, Node centre);

}
