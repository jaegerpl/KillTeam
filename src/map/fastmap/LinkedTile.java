package map.fastmap;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import memory.pathcalulation.IPathNode;

import com.jme.math.Vector3f;


public class LinkedTile implements IPathNode {
	
	public final static int DIRECTION_NORTH = 1;
	public final static int DIRECTION_SOUTH = 2;
	public final static int DIRECTION_EAST = 3;
	public final static int DIRECTION_WEST = 4;
	public final static int DIRECTION_NORTHEAST = 5;
	public final static int DIRECTION_NORTHWEST = 6;
	public final static int DIRECTION_SOUTHEAST = 7;
	public final static int DIRECTION_SOUTHWEST = 8;
	
	protected Map<Integer, LinkedTile> linkedTiles;
	protected int precisionLevel;
	
	public boolean isWater;



	public boolean isPassable;
	protected Vector3f normalVector;
	public Point mapIndex;
	protected Vector3f tileCenterCoordinates;
	
	protected boolean isExplored;
	protected boolean isOutOfMap = false;
	
	public LinkedTile(Point position, boolean isWater, boolean isPassable,
			Vector3f normalVector, boolean isExplored) {
		
		this.mapIndex = position;
		this.isWater = isWater;
		this.isPassable = isPassable;
		this.normalVector = normalVector;
		this.tileCenterCoordinates = FastRoutableWorldMap.getTileCenterCoordinates(mapIndex);

		linkedTiles = new HashMap<Integer, LinkedTile>();
		precisionLevel = 0;
		this.isExplored = isExplored;
	}
	
	public LinkedTile(Point position, boolean isWater, boolean isPassable,
			Vector3f normalVector, Map<Integer, LinkedTile> linkedTiles, boolean isExplored, int precisionLevel) {
		
		this.mapIndex = position;
		this.isWater = isWater;
		this.isPassable = isPassable;
		this.normalVector = normalVector;
		this.tileCenterCoordinates = FastRoutableWorldMap.getTileCenterCoordinates(mapIndex);

		this.linkedTiles = linkedTiles;
		this.precisionLevel = precisionLevel;
		this.isExplored = isExplored;
	}

	public LinkedTile(Point position, boolean isWater, boolean isPassable,
			Vector3f normalVector, boolean isExplored, int precisionLevel) {
		
		this.mapIndex = position;
		this.isWater = isWater;
		this.isPassable = isPassable;
		this.normalVector = normalVector;
		this.tileCenterCoordinates = FastRoutableWorldMap.getTileCenterCoordinates(mapIndex);

		linkedTiles = new HashMap<Integer, LinkedTile>();
		this.precisionLevel = precisionLevel;
		this.isExplored = isExplored;
	}
	
	public boolean isExplored() {
		return isExplored;
	}

	public boolean isWater() {
		return isWater;
	}

	public boolean isPassable() {
		return isPassable;
	}

	public Vector3f getNormalVector() {
		return normalVector;
	}

	public Point getMapIndex() {
		return mapIndex;
	}

	public Vector3f getTileCenterCoordinates() {
		return tileCenterCoordinates;
	}
	
	public void addNeighbour(LinkedTile tile, Integer direction) {
		linkedTiles.put(direction, tile);
	}
	
	public Map<Integer, LinkedTile> getNeighbours() {
		return linkedTiles;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof LinkedTile) {
			LinkedTile other = (LinkedTile) obj;
			if(other.mapIndex.equals(this.mapIndex)) {
				return true;
			}
		}
		return false;
	}

	public int getPrecisionLevel() {
		return precisionLevel;
	}
	
	public LinkedTile deepCopy(){
		return new LinkedTile(mapIndex, isWater, isPassable, normalVector, linkedTiles, isExplored, precisionLevel);
	}
	
	public void exploreTile(boolean isWater, boolean isPassable, Vector3f normalVector) {
		this.isWater = isWater;
		this.isPassable = isPassable;
		this.normalVector = normalVector;
		
		this.isExplored = true;
	}
	
	protected void markAsOutOfMap() {
		this.isWater = false;
		this.isPassable = false;
		this.normalVector = new Vector3f(0,0,0);	
		this.isExplored = true;
		this.isOutOfMap = true;
		//Nachbarn sind nicht betretbar
		for(LinkedTile neighbour : linkedTiles.values()) {
			neighbour.isPassable = false;
		}
	}

	@Override
	public String toString() {
		return "[" + this.mapIndex.x + "," + this.mapIndex.y + "] Passierbar:" + isPassable + " isWater: " + isWater + " isExplored: " + isExplored + " isOutOfMap: " + isOutOfMap;
	}

	
}
