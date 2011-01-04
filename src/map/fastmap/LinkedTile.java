package map.fastmap;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import map.memory.pathcalulation.IPathNode;

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
	
	protected boolean isWater;
	protected boolean isPassable;
	protected Vector3f normalVector;
	protected Point mapIndex;
	protected Vector3f tileCenterCoordinates;
	
	protected boolean isExplored;
	
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
	
	protected void exploreTile(boolean isWater, boolean isPassable, Vector3f normalVector) {
		this.isWater = isWater;
		this.isPassable = isPassable;
		this.normalVector = normalVector;
		
		this.isExplored = true;
	}

}
