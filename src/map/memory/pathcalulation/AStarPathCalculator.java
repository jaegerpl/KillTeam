package map.memory.pathcalulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import map.fastmap.LinkedTile;
import map.memory.map.MemorizedMap;


public class AStarPathCalculator {
	
	protected MemorizedMap map;

	public AStarPathCalculator(MemorizedMap map) {
		this.map =map;
	}

	public Path<LinkedTile> calculatePath(LinkedTile start, LinkedTile destination) {
		SortedList<PathCalculatorNode> openList = new SortedList<PathCalculatorNode>();
		ArrayList<PathCalculatorNode> closedList = new ArrayList<PathCalculatorNode>();
		
		openList.add(new PathCalculatorNode(start, calculateApproximatedDistance(start, destination), 0));
		
		do {
			PathCalculatorNode currentNode = openList.remove();
			if(currentNode.getCoordinate().equals(destination)) {
				//Pfad gefunden
				return extractPath(currentNode);
			}
			expandNode(currentNode, openList, closedList);
			closedList.add(currentNode);
			
		} while(0 < openList.size());
		//Kein Pfad gefunden
		return new Path<LinkedTile>(this, map);
	}
	
	protected Path<LinkedTile> extractPath(PathCalculatorNode destinationNode) {
		Path<LinkedTile> resultingPath = new Path<LinkedTile>(this, map);
		
		PathCalculatorNode current = destinationNode;
		while(null != current) {
			resultingPath.addWaypointToFront(current.getCoordinate());
			current = current.getPredesessor();
		}
		
		return resultingPath;
	}
	
	protected float getCost(PathCalculatorNode from, PathCalculatorNode to) {
		LinkedTile fromCoord = from.getCoordinate();
		LinkedTile toCoord = to.getCoordinate();
		
		float cost = 1f;
		
		if(fromCoord.getMapIndex().x != toCoord.getMapIndex().x && fromCoord.getMapIndex().y != toCoord.getMapIndex().y) {
			cost = 1.4f;
		}
		if(!toCoord.isPassable() || toCoord.isWater()) {
			cost = 10000000000000000000000000f;
		}
		return cost;
	}
	
	protected void expandNode(PathCalculatorNode predesessor, SortedList<PathCalculatorNode> openList, List<PathCalculatorNode> closedList) {
		List<PathCalculatorNode> neighbours = getNeighbourNodes(predesessor);
		Iterator<PathCalculatorNode> it = neighbours.iterator();
		while(it.hasNext()) {
			PathCalculatorNode currentNode = it.next();
			if(!closedList.contains(currentNode)) {
				
			}
			currentNode.setG(predesessor.getG() + getCost(currentNode, predesessor));
			
			//check whether node already in openList
			PathCalculatorNode nodeInList = openList.getElementEqualTo(currentNode);
			if(null != nodeInList) {
				//check whether better way to node is known
				if(nodeInList.getG() <= currentNode.getG()) {
					continue;
				}
				nodeInList.setPredesessor(predesessor);
				nodeInList.setG(currentNode.getG());
			} else {
				currentNode.setPredesessor(predesessor);
				openList.add(currentNode);
			}
		}
	}
	
	protected List<PathCalculatorNode> getNeighbourNodes(PathCalculatorNode node) {
		List<PathCalculatorNode> neighbourNodes = new ArrayList<PathCalculatorNode>();
		
		Collection<LinkedTile> neighbours = node.getCoordinate().getNeighbours().values();
		
		Iterator<LinkedTile> it = neighbours.iterator();
		while(it.hasNext()) {
			LinkedTile curCoord = it.next();
			neighbourNodes.add(new PathCalculatorNode(curCoord, 0, 0));
		}
		
		return neighbourNodes;
	}
	
	/*
	 * Berechnet die Manhattan Distanz
	 */
	public int calculateApproximatedDistance(LinkedTile start, LinkedTile destination) {
		return Math.abs(start.getMapIndex().x - destination.getMapIndex().x) + Math.abs(start.getMapIndex().y - destination.getMapIndex().y);
	}
}
