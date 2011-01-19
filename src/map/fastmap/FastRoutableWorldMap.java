package map.fastmap;


import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.interfaces.IPlayer;

public class FastRoutableWorldMap{
	
	// Min-Max Positions of Map-Tiles
	int minXPos = Integer.MAX_VALUE;
	int maxXPos = Integer.MIN_VALUE;
	int minYPos = Integer.MAX_VALUE;
	int maxYPos = Integer.MIN_VALUE;
	
	public static final int tilesize = 10;
	
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
			Point tilePosition = new Point(x+1,y+1);
			if(map.containsKey(tilePosition)) {
				return map.get(tilePosition);
			} else {
				return createUnexploredTileAtPosition(tilePosition);
			}
		}
	}

	/**
	 * Creates an unepxlored tile and adds it to the map.
	 * Also keeps track of it in the unexploredTilesList.
	 * 
	 * @param tilePosition
	 * @return
	 */
	private LinkedTile createUnexploredTileAtPosition(Point tilePosition) {
		if(tilePosition.x > 60 || tilePosition.y > 60) {
			//System.out.println("Argh!");
		}
		LinkedTile unexploredTile = new LinkedTile(tilePosition, false, true, new Vector3f(0,0,0), false);
		addTile(unexploredTile);
		unexploredTiles.put(tilePosition, unexploredTile);
		return unexploredTile;
	}
	
	/**
	 * Marks a tile as explored and adds its specific features. Also removes this explored tile from
	 * the unexploredTilesList
	 * 
	 * @param tile
	 * @param isWater
	 * @param isPassable
	 * @param normalVector
	 */
	public void exploreTile(LinkedTile tile, boolean isWater, boolean isPassable, Vector3f normalVector) {
		//System.out.println("Tile erkundet: " + tile.mapIndex + " Passierbar: " + isPassable);		
		tile.exploreTile(isWater, isPassable, normalVector);
		if(unexploredTiles.containsKey(tile.getMapIndex())) {
			unexploredTiles.remove(tile.getMapIndex());
		}
	}
	
	/**
	 * Marks a tile as out of Map and removes it from the unexploredTilesList if necessary
	 * @param tile
	 */
	public void markTileAsOutOfMap(LinkedTile tile) {
		tile.markAsOutOfMap();
		if(unexploredTiles.containsKey(tile.getMapIndex())) {
			unexploredTiles.remove(tile.getMapIndex());
		}
	}
	
	synchronized public LinkedTile getTileAtMapIndex(Point point){
		return getTileAtCoordinate(getTileCenterCoordinates(point));
	}
	
	synchronized public List<LinkedTile> getTilesPossiblyInViewRange(Vector3f myPosition) {
		List<LinkedTile> list = new ArrayList<LinkedTile>();
		Point thisTile = getTileAtCoordinate(myPosition).getMapIndex();
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
	
//	synchronized public List<LinkedTile> getEmptyTilesPossiblyInViewRange(Vector3f pos){
//		List<LinkedTile> list = getTilesPossiblyInViewRange(pos);
//		
//		List<LinkedTile> retList = new ArrayList<LinkedTile>();
//		for(LinkedTile t : list){
//			if(!t.isExplored()){
//				retList.add(t);
//			}
//		}
//		return retList;
//	}
	
//	/**
//	 * Registers a tank at the WorldMap.<br>
//	 * A queue for the tank is created an a reference is returned.
//	 * 
//	 * @param tank 
//	 * @return the queue for the tank to write it's information into
//	 */
//	public ConcurrentLinkedQueue<LinkedTile> registerTank(IPlayer tank){
//		ConcurrentLinkedQueue<LinkedTile> list;
//		synchronized (queues) {
//			if(!queues.containsKey(tank.hashCode()+"")){
//				list = new ConcurrentLinkedQueue<LinkedTile>();
//				queues.put(tank.hashCode()+"", list);
//			}else {
//				list = (ConcurrentLinkedQueue<LinkedTile>) queues.get(tank.hashCode()+"");
//			}
//		}
//		return list;
//	}
	
	
	synchronized public void addTile(LinkedTile tile) {
		Point pos = tile.getMapIndex();
		if(map.containsKey(pos)) {
			if(!map.get(pos).isExplored() && tile.isExplored()) {
				map.get(pos).exploreTile(tile.isWater(), tile.isPassable(), tile.getNormalVector());
			}
		}
//System.out.println("Tile Hinzugef�gt: " + tile.mapIndex + " Passierbar: " + tile.isPassable);		
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
			if(tmpTile.isOutOfMap) {
				tile.isPassable = false;
			}
			tile.addNeighbour(tmpTile, LinkedTile.DIRECTION_NORTH);
		}
		
		tmpTile = map.get(neighbourSouth);
		if(null != tmpTile) {
			tmpTile.addNeighbour(tile, LinkedTile.DIRECTION_NORTH);
			if(tmpTile.isOutOfMap) {
				tile.isPassable = false;
			}
			tile.addNeighbour(tmpTile, LinkedTile.DIRECTION_SOUTH);
		}
		
		tmpTile = map.get(neighbourEast);
		if(null != tmpTile) {
			tmpTile.addNeighbour(tile, LinkedTile.DIRECTION_WEST);
			if(tmpTile.isOutOfMap) {
				tile.isPassable = false;
			}
			tile.addNeighbour(tmpTile, LinkedTile.DIRECTION_EAST);
		}
		
		tmpTile = map.get(neighbourWest);
		if(null != tmpTile) {
			tmpTile.addNeighbour(tile, LinkedTile.DIRECTION_EAST);
			if(tmpTile.isOutOfMap) {
				tile.isPassable = false;
			}
			tile.addNeighbour(tmpTile, LinkedTile.DIRECTION_WEST);
		}
		
		tmpTile = map.get(neighbourNortheast);
		if(null != tmpTile) {
			tmpTile.addNeighbour(tile, LinkedTile.DIRECTION_SOUTHWEST);
			if(tmpTile.isOutOfMap) {
				tile.isPassable = false;
			}
			tile.addNeighbour(tmpTile, LinkedTile.DIRECTION_NORTHEAST);
		}
		
		tmpTile = map.get(neighbourNorthwest);
		if(null != tmpTile) {
			tmpTile.addNeighbour(tile, LinkedTile.DIRECTION_SOUTHEAST);
			if(tmpTile.isOutOfMap) {
				tile.isPassable = false;
			}
			tile.addNeighbour(tmpTile, LinkedTile.DIRECTION_NORTHWEST);
		}
		
		tmpTile = map.get(neighbourSoutheast);
		if(null != tmpTile) {
			tmpTile.addNeighbour(tile, LinkedTile.DIRECTION_NORTHWEST);
			if(tmpTile.isOutOfMap) {
				tile.isPassable = false;
			}
			tile.addNeighbour(tmpTile, LinkedTile.DIRECTION_SOUTHEAST);
		}
		
		tmpTile = map.get(neighbourSouthwest);
		if(null != tmpTile) {
			tmpTile.addNeighbour(tile, LinkedTile.DIRECTION_NORTHEAST);
			if(tmpTile.isOutOfMap) {
				tile.isPassable = false;
			}
			tile.addNeighbour(tmpTile, LinkedTile.DIRECTION_SOUTHWEST);
		}
		
		map.put(tile.getMapIndex(), tile);
		
		// update Min-Max Map-Tile Values
		Point point = tile.getMapIndex();
		if(point.x > maxXPos){
			maxXPos = point.x;
		}
		if(point.x < minXPos){
			minXPos = point.x;
		}
		if(point.y > maxYPos){
			maxYPos = point.y;
		}
		if(point.y < minYPos){
			minYPos = point.y;
		}
	}
	
	public Map<Point, LinkedTile> getUnexploredTiles() {
		return unexploredTiles;
	}
	
//	public boolean tileIsInViewRange(Vector3f myPosition, Vector3f viewDirection, LinkedTile tile) {
//		return positionIsInViewRange(myPosition, viewDirection, tile.getTileCenterCoordinates());
//	}
	
	public boolean positionIsInViewRange(Vector3f myPosition, Vector3f viewDirection, Vector3f position) {
		Vector3f test = viewDirection.normalize();
		test = test.mult(30);
		float bar = test.length();
		test = myPosition.add(test);
		float foo = myPosition.add(viewDirection.normalize().mult(30)).distance(position);
		
		if(40 > myPosition.add(viewDirection.normalize().mult(30)).distance(position)) {
			return true;
		}
		return false;
	}
	
	public void markAllUnexploredAsOutOfMap() {
		Collection<LinkedTile> tiles = unexploredTiles.values();
		List<LinkedTile> tilesToMark = new ArrayList<LinkedTile>();
		
		for(LinkedTile tile : tiles) {
			//this.markTileAsOutOfMap(tile);
			tilesToMark.add(tile);
		}
		
		for(LinkedTile tileToMark : tilesToMark) {
			this.markTileAsOutOfMap(tileToMark);
		}
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
		return new Vector3f((position.x - 1)*tilesize + tilesize/2, 0, (position.y - 1)*tilesize + tilesize/2);
	}

	public Map<Point,LinkedTile> getMap(){
		return this.map;
	}
	
	/**
	 * Generates a random passable target in the map
	 * 
	 * @return
	 */
	public LinkedTile getRandomTarget(){
		Random r = new Random();
		LinkedTile tile;
		do {
			int randX = r.nextInt(maxXPos - minXPos) - minXPos;
			int randY = r.nextInt(maxYPos - minYPos) - minYPos;
			tile = getTileAtMapIndex(new Point(randX, randY));
		} while (!tile.isPassable);
		return tile;
	}

}
