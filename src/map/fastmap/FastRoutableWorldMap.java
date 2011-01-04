package map.fastmap;


import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.interfaces.IPlayer;

public class FastRoutableWorldMap{
	
	public static final int tilesize = 20;
	
	// a list of queues for the tanks
	protected Map<String, Queue<LinkedTile>> queues = Collections.synchronizedMap(new HashMap<String, Queue<LinkedTile>>());
	
	
	public static final int viewRangeRadius = 50;
	public static final int viewRangeOffset = 30;
	
	protected Map<Point,LinkedTile> map = Collections.synchronizedMap(new HashMap<Point, LinkedTile>());
	protected Map<Point,LinkedTile> unexploredTiles = Collections.synchronizedMap(new HashMap<Point, LinkedTile>());
	
	public FastRoutableWorldMap(){
		super();
	}
	
	synchronized public LinkedTile getTileAtCoordinate(Vector3f position){
		int x = (int)position.x / tilesize;
		int y = (int)position.z / tilesize;
		synchronized (map) {
			/*LinkedTile t = map.get(new Point(x+1,y+1));
			if(t != null){
				return t.deepCopy();
			} else {
				EmptyLinkedTile et = new EmptyLinkedTile(new Point(x+1,y+1));
				map.put(new Point(x+1,y+1), et );
				return et.deepCopy();
			}*/
			
			Point tilePosition = new Point(x+1,y+1);
			if(map.containsKey(tilePosition)) {
				return map.get(tilePosition);
			} else {
				return createUnexploredTileAtPosition(tilePosition);
			}
		}
	}

	private LinkedTile createUnexploredTileAtPosition(Point tilePosition) {
		LinkedTile unexploredTile = new LinkedTile(tilePosition, false, false, new Vector3f(0,0,0), false);
		map.put(tilePosition, unexploredTile);
		unexploredTiles.put(tilePosition, unexploredTile);
		return unexploredTile;
	}
	
	public void exploreTile(LinkedTile tile, boolean isWater, boolean isPassable, Vector3f normalVector) {
		tile.exploreTile(isWater, isPassable, normalVector);
		if(unexploredTiles.containsKey(tile.getMapIndex())) {
			unexploredTiles.remove(tile.getMapIndex());
		}
	}
	
	synchronized public LinkedTile getTileAtMapIndex(Point point){
		return getTileAtCoordinate(getTileCenterCoordinates(point));
	}
	
	synchronized public List<LinkedTile> getTilesPossiblyInViewRange(Vector3f myPosition) {
		List<LinkedTile> list = new ArrayList<LinkedTile>();
		// get tile center for current position
		Vector3f tileCenter = getTileCenterCoordinates(myPosition);
		Point thisTile = getTileAtCoordinate(myPosition).mapIndex;
		int x = thisTile.x;
		int y = thisTile.y;
		
		int minX = x - ((viewRangeOffset + viewRangeRadius) / tilesize);
		if(minX < 1) {
			minX = 1;
		}
		int maxX = x + ((viewRangeOffset + viewRangeRadius) / tilesize);
		
		int minY = y - ((viewRangeOffset + viewRangeRadius) / tilesize);
		if(minY < 1) {
			minY = 1;
		}
		int maxY = y + ((viewRangeOffset + viewRangeRadius) / tilesize);
		
		for(int curX = minX; curX <= maxX; curX++){
			for(int curY = minY; curY <= maxY; curY++){
				list.add(getTileAtMapIndex(new Point(curX,curY)));
			}
		}
		
		return list;
	}
	
	synchronized public List<LinkedTile> getEmptyTilesPossiblyInViewRange(Vector3f pos){
		List<LinkedTile> list = getTilesPossiblyInViewRange(pos);
		
		List<LinkedTile> retList = new ArrayList<LinkedTile>();
		for(LinkedTile t : list){
			if(!t.isExplored()){
				retList.add(t);
			}
		}
		return retList;
	}
	
	/**
	 * Registers a tank at the WorldMap.<br>
	 * A queue for the tank is created an a reference is returned.
	 * 
	 * @param tank 
	 * @return the queue for the tank to write it's information into
	 */
	public ConcurrentLinkedQueue<LinkedTile> registerTank(IPlayer tank){
		ConcurrentLinkedQueue<LinkedTile> list;
		synchronized (queues) {
			if(!queues.containsKey(tank.hashCode()+"")){
				list = new ConcurrentLinkedQueue<LinkedTile>();
				queues.put(tank.hashCode()+"", list);
			}else {
				list = (ConcurrentLinkedQueue<LinkedTile>) queues.get(tank.hashCode()+"");
			}
		}
		return list;
	}
	
	
	synchronized public void addTile(LinkedTile tile) {
		Point pos = tile.mapIndex;
		if(map.containsKey(pos)) {
			if(!map.get(pos).isExplored() && tile.isExplored()) {
				map.get(pos).exploreTile(tile.isWater(), tile.isPassable(), tile.getNormalVector());
			}
		}
		Point neighbourNorth = new Point(pos.x, pos.y+1);
		Point neighbourSouth = new Point(pos.x, pos.y-1);
		Point neighbourEast = new Point(pos.x-1, pos.y);
		Point neighbourWest = new Point(pos.x+1, pos.y);
		Point neighbourNortheast = new Point(pos.x-1, pos.y+1);
		Point neighbourNorthwest = new Point(pos.x+1, pos.y+1);
		Point neighbourSoutheast = new Point(pos.x-1, pos.y-1);
		Point neighbourSouthwest = new Point(pos.x+1, pos.y-1);
		
		LinkedTile tmpTile = map.get(neighbourNorth);
		if(null != tmpTile) {
			tmpTile.addNeighbour(tile, LinkedTile.DIRECTION_SOUTH);
		}
		
		tmpTile = map.get(neighbourSouth);
		if(null != tmpTile) {
			tmpTile.addNeighbour(tile, LinkedTile.DIRECTION_NORTH);
		}
		
		tmpTile = map.get(neighbourEast);
		if(null != tmpTile) {
			tmpTile.addNeighbour(tile, LinkedTile.DIRECTION_WEST);
		}
		
		tmpTile = map.get(neighbourWest);
		if(null != tmpTile) {
			tmpTile.addNeighbour(tile, LinkedTile.DIRECTION_EAST);
		}
		
		tmpTile = map.get(neighbourNortheast);
		if(null != tmpTile) {
			tmpTile.addNeighbour(tile, LinkedTile.DIRECTION_SOUTHWEST);
		}
		
		tmpTile = map.get(neighbourNorthwest);
		if(null != tmpTile) {
			tmpTile.addNeighbour(tile, LinkedTile.DIRECTION_SOUTHEAST);
		}
		
		tmpTile = map.get(neighbourSoutheast);
		if(null != tmpTile) {
			tmpTile.addNeighbour(tile, LinkedTile.DIRECTION_NORTHWEST);
		}
		
		tmpTile = map.get(neighbourSouthwest);
		if(null != tmpTile) {
			tmpTile.addNeighbour(tile, LinkedTile.DIRECTION_NORTHEAST);
		}
		
		map.put(tile.mapIndex, tile);
	}
	
	public Map<Point, LinkedTile> getUnexploredTiles() {
		return unexploredTiles;
	}
	
	//===================================================
	// STATIC METHODS
	//===================================================

	/**
	 * Turns world coordinates into gird indices
	 */
	public static Point coordinates2MapIndex(Vector3f position){
		int x = (int)position.x / tilesize;
		int y = (int)position.z / tilesize;
		return new Point(x+1,y+1);
	}
	
	/**
	 * Calculates the Tile center of the Tile the given position belongs to.
	 * 
	 * @param position
	 * @return
	 */
	public static Vector3f getTileCenterCoordinates(Vector3f position){
		int x = (int)position.x / tilesize;
		int y = (int)position.z / tilesize;
		int tHalb = tilesize/2;
		return new Vector3f(x*tilesize+tHalb,0,y*tilesize+tHalb);
	}
	
	/**
	 * Returns the center coordinates of the Tile
	 * 
	 * @param position the Tile indices
	 * @return the world coordinates of the Tile center
	 */
	public static Vector3f getTileCenterCoordinates(Point position){
		return new Vector3f(position.x*tilesize+tilesize/2,0,position.y*tilesize+tilesize/2);
	}

	

}
