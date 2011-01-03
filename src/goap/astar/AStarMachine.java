package goap.astar;

import java.util.ArrayList;

/**
 * A path finder implementation that uses the AStar heuristic based algorithm to
 * determine a path.
 * Modified to be generic to be able to use the algorithm for more than just
 * pathfinding.
 * 
 * @author Kevin Glass  http://www.cokeandcode.com/pathfinding
 * @modified Arne Klingenberg
 */
public class AStarMachine<T extends Node> {
	/** The set of nodes that have been searched through */
	private ArrayList<T> closed = new ArrayList<T>();
	/** The set of nodes that we do not yet consider fully searched */
	private SortedList open = new SortedList();

	/** The map being searched */
	private IMap map;
	/** The maximum depth of search we're willing to accept before giving up */
	private int maxSearchDistance;

	private ArrayList<Node> neighbours;

	/**
	 * Create a path finder
	 * 
	 * @param heuristic
	 *            The heuristic used to determine the search order of the map
	 * @param map
	 *            The map to be searched
	 * @param maxSearchDistance
	 *            The maximum depth we'll search before giving up
	 * @param allowDiagMovement
	 *            True if the search should try diaganol movement
	 */
	public AStarMachine(IMap map, int maxSearchDistance) {
		this.map = map;
		this.maxSearchDistance = maxSearchDistance;
	}

	/**
	 * @see PathFinder#findPath(IMover, int, int, int, int)
	 */
	public ArrayList<T> findPath(IMover mover, Node start, Node end) {
		// initial state for A*. The closed group is empty. Only the starting
		// tile is in the open list and it's cost is zero, i.e. we're already
		// there
		start.g = 0;
		start.depth = 0;
		start.h = start.calculateHeuristics(map, end);
		start.f = start.h;

		cleanUp();

		open.add(start);

		if(end != null)
			end.parent = null;

		// while we haven't found the goal and haven't exceeded our max search
		// depth
		int maxDepth = 0;
		while ((maxDepth < maxSearchDistance) && (open.size() != 0)) {
			// pull out the first node in our open list, this is determined to
			// be the most likely to be the next step based on our heuristic
			Node current = getFirstInOpen();

			if (current.isFinished(mover, end)) {
				break;
			}

			removeFromOpen(current);
			addToClosed(current);

			neighbours = map.getNeighbours(mover, current);

			for (int i = 0; i < neighbours.size(); i++) {
				// the cost to get to this node is cost the current plus the
				// movement
				// cost to reach this node. Note that the heursitic value is
				// only used
				// in the sorted open list
				Node neighbour = neighbours.get(i);
				
				float g = current.g
						+ map.getCost(mover, current, neighbour);
				
				map.pathFinderVisited(neighbour);

				// if the new cost we've determined for this node is lower than
				// it has been previously makes sure the node hasn't been
				// discarded. We've
				// determined that there might have been a better path to get to
				// this node so it needs to be re-evaluated
				if (g < neighbour.g) {
					if (inOpenList(neighbour)) {
						removeFromOpen(neighbour);
					}
					if (inClosedList(neighbour)) {
						removeFromClosed(neighbour);
					}
				}

				// if the node hasn't already been processed and discarded then
				// reset it's cost to our current cost and add it as a next
				// possible
				// step (i.e. to the open list)
				if (!inOpenList(neighbour) && !(inClosedList(neighbour))) {
					neighbour.g = g;
					neighbour.h = neighbour.calculateHeuristics(map, end);
					neighbour.f = neighbour.g + neighbour.h;
					maxDepth = Math.max(maxDepth, neighbour.setParent(current));
					addToOpen(neighbour);

				}
			}
		}

		// since we've got an empty open list or we've run out of search
		// there was no path. Just return null
		if (end.parent == null) {
			return null;
		}

		// At this point we've definitely found a path so we can use the parent
		// references of the nodes to find out way from the target location back
		// to the start recording the nodes on the way.
		//TODO: Hier nochmal nachgucken ob �berhaupt target.clone()
		//notwendig ist, da die map ja eigentlich schon jedes node cloned
		ArrayList<T> path = new ArrayList<T>();
		Node target = end;
		while (target != start) {
			path.add(0, (T) target.clone());
			target = target.parent;
		}
		//path.add(0, (T) start);

		// thats it, we have our path
		return path;

	}

	/**
	 * Removes the nodes and values from the previous search
	 */
	private void cleanUp() {
		for (Node n : closed) {
			n.f = 0;
			n.h = 0;
			n.g = 0;
			n.parent = null;
		}

		for (int i = 0; i < open.size(); i++) {
			((Node) open.get(i)).f = 0;
			((Node) open.get(i)).g = 0;
			((Node) open.get(i)).h = 0;
			((Node) open.get(i)).parent = null;
		}
		closed.clear();
		open.clear();
	}

	/**
	 * Get the first element from the open list. This is the next one to be
	 * searched.
	 * 
	 * @return The first element in the open list
	 */
	protected Node getFirstInOpen() {
		return (Node) open.first();
	}

	/**
	 * Add a node to the open list
	 * 
	 * @param node
	 *            The node to be added to the open list
	 */
	protected void addToOpen(Node node) {
		open.add(node);
	}

	/**
	 * Check if a node is in the open list
	 * 
	 * @param node
	 *            The node to check for
	 * @return True if the node given is in the open list
	 */
	protected boolean inOpenList(Node node) {
		return open.contains(node);
	}

	/**
	 * Remove a node from the open list
	 * 
	 * @param node
	 *            The node to remove from the open list
	 */
	protected void removeFromOpen(Node node) {
		open.remove(node);
	}

	/**
	 * Add a node to the closed list
	 * 
	 * @param node
	 *            The node to add to the closed list
	 */
	protected void addToClosed(Node node) {
		closed.add((T) node);
	}

	/**
	 * Check if the node supplied is in the closed list
	 * 
	 * @param node
	 *            The node to search for
	 * @return True if the node specified is in the closed list
	 */
	protected boolean inClosedList(Node node) {
		return closed.contains(node);
	}

	/**
	 * Remove a node from the closed list
	 * 
	 * @param node
	 *            The node to remove from the closed list
	 */
	protected void removeFromClosed(Node node) {
		closed.remove(node);
	}
}
