package pascal.map;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.testteam.PascalPlayer;

/**
 * A tank registers at the map and is given a queue, where it can place 
 * information about the world it perceives.<br>
 * The map cyclically crawls the queues and puts the information in the 
 * central map.<br>
 * <br>
 * The map is build up from {@link Tile}s and a map index can have three different states
 * <li> the tile is null:this means, that no tank has visited or even asked for a tile at that posiiton
 * <li> the tile is an {@link EmptyTile}: a tank has asked for a tile at that map index and it has been null, so an EmptyTile
 * has been inserted, so the tank can operate upon it
 * <li> the tile is of type {@link Tile}: all world information has been collected by the tank and the information don't change anymore
 * <br><br>
 * Tiles can be received from the view range to navigate, or EmptyTiles in view range
 * to scan unknown tiles.
 * 
 * @author Pascal Jaeger
 *
 */
public class WorldMap {
	
	public static final int tilesize = 20;
	
	// a list of queues for the tanks
	private Map<String, Queue<Tile>> queues = Collections.synchronizedMap(new HashMap<String, Queue<Tile>>());
	
	// the central map
	Map<Point,Tile> map = new HashMap<Point, Tile>();
	
	public WorldMap() {
		Thread queueCrawler = new Thread(new QueueCrawler());
		queueCrawler.start();
	}
	
	/**
	 * Registers a tank at the WorldMap.<br>
	 * A queue for the tank is created an a reference is returned.
	 * 
	 * @param tank 
	 * @return the queue for the tank to write it's information into
	 */
	public ConcurrentLinkedQueue<Tile> registerTank(PascalPlayer tank){
		ConcurrentLinkedQueue<Tile> list;
		synchronized (queues) {
			if(!queues.containsKey(tank.getName())){
				list = new ConcurrentLinkedQueue<Tile>();
				queues.put(tank.getName(), list);
			}else {
				list = (ConcurrentLinkedQueue<Tile>) queues.get(tank.getName());
			}
		}
		return list;
	}
	
	/**
	 * Returns the Tile at the given world position
	 * 
	 * @param vec the world position
	 * @return the tile at the given position or an EmptyTile if no Tile has been inserted yet.
	 */
	public Tile getTileAtCoordinate(Vector3f position){
		int x = (int)position.x / tilesize;
		int y = (int)position.z / tilesize;
		synchronized (map) {
			Tile t = map.get(new Point(x+1,y+1));
			if(t != null){
				return t.deepCopy();
			} else {
				EmptyTile et = new EmptyTile(new Point(x+1,y+1));
				map.put(new Point(x+1,y+1), et );
				return et.deepCopy();
			}
		}
	}
	
	public Tile getTileAtMapIndex(Point point){
		return getTileAtCoordinate(getTileCenterCoordinates(point));
	}
	
//	/**
//	 * Returns the direct neighbors of the tile
//	 * 
//	 * @param position
//	 * @return the neighbors around the given position
//	 */
//	public List<Tile> getNeigbors(Point position){
//		List<Tile> list = new ArrayList<Tile>();
//		for(int x = position.x-1;x<position.x+1;x++){
//			for(int y = position.y-1;y<position.y-1;y++){
//				Tile t = map.get(new Point(x,y));
//				if(t != null){
//					list.add(t);	
//				}
//			}
//		}
//		return list;
//	}
	
	/**
	 * Returns the tiles that totally fit into the view range.
	 * Tiles can be EmptyTiles or already checked tiles.<br>
	 * <br>
	 * To get only EmptyTiles in view range call {@link getEmptyTilesInViewRange}
	 * @param pos the center coordinates of the view range
	 * 
	 * @return a list with tiles in the view range
	 */
	public List<Tile> getTilesInViewRange(Vector3f pos){
		
		/**
		 *  _____________		____________		_____________
		 *  | (2) | (3) |		|XXXXX|    |		|XX|  |     |
		 *  |  -------  |		|--------  |		|XX-------  |
		 *  |  |     |  |		|  |				|XX|     |  |
		 *  |--| (1) |--|		|--|				|--|     |--|
		 *  |  |     |  |
		 *  |  -------  |       oberes				linkes
		 *  | (4) | (5) |
		 *  |___________|
		 */
		List<Tile> list = new ArrayList<Tile>();
		
		// get tile center for current position
		Vector3f tileCenter = getTileCenterCoordinates(pos);
		Point thisTile = getTileAtCoordinate(pos).mapIndex;
		int x = thisTile.x;
		int y = thisTile.y;
		
		// (1)   im inneren Viertel
		// jeweils der direkte Nachbar in jede Richtung (auch diagonal)
		if( (tileCenter.x - tilesize/2) < pos.x && (tileCenter.x + tilesize/2) > pos.x){
			if( (tileCenter.z - tilesize/2) < pos.z && (tileCenter.z + tilesize/2) > pos.z){		
				for(int i = x-1; i < x+1; i++){
					for(int j = y-1; j < y+1; j++){
						list.add(getTileAtMapIndex(new Point(i,j)));
					}
				}
			}
			
		}
		// (2) oben links im Tile
		else if( ( (tileCenter.x - tilesize)   <= pos.x && pos.x <= (tileCenter.x + tilesize/2) &&		// linkes
				   (tileCenter.y)              <= pos.z && pos.z <= (tileCenter.y + tilesize)      ) ||
				 ( (tileCenter.x - tilesize)   <= pos.x && pos.x <= (tileCenter.x) 			  &&		// oberes	
				   (tileCenter.y + tilesize/2) <= pos.z && pos.z <= (tileCenter.y + tilesize)    )
			   ){
				for(int i = x-1; i < x+1; i++){
					for(int j = y-1; j < y+1; j++){
						list.add(getTileAtMapIndex(new Point(i,j)));
					}
				}
				// remove the tile that does not fit, in this case, the last one added
				list.remove(list.size()-1);
				// adding two additional tiles
				list.add(getTileAtMapIndex(new Point(thisTile.x-2,thisTile.y)));
				list.add(getTileAtMapIndex(new Point(thisTile.x,thisTile.y+2)));			
		} 
		// (3) oben rechts im Tile
		else if( ( (tileCenter.x)              <= pos.x && pos.x <= (tileCenter.x + tilesize) &&		// oberes
				   (tileCenter.y + tilesize/2) <= pos.z && pos.z <= (tileCenter.y + tilesize)    ) ||
				 ( (tileCenter.x + tilesize/2) <= pos.x && pos.x <= (tileCenter.x + tilesize) &&		// rechtes
				   (tileCenter.y)              <= pos.z && pos.z <= (tileCenter.y + tilesize)    )
			   ){
				for(int i = x-1; i < x+1; i++){
					for(int j = y-1; j < y+1; j++){
						list.add(getTileAtMapIndex(new Point(i,j)));
					}
				}
				// remove the tile that does not fit, in this case, the last one added
				list.remove(5); // should be tile down left of center tile
				// adding two additional tiles
				list.add(getTileAtMapIndex(new Point(thisTile.x+2,thisTile.y)));
				list.add(getTileAtMapIndex(new Point(thisTile.x,thisTile.y+2)));			
		} 
		// (4) unten links im Tile
		else if( ( (tileCenter.x - tilesize) <= pos.x && pos.x <= (tileCenter.x - tilesize/2) &&	// unteres
				   (tileCenter.y - tilesize) <= pos.z && pos.z <= (tileCenter.y)      ) ||
				 ( (tileCenter.x - tilesize) <= pos.x && pos.x <= (tileCenter.x) 			  &&	// linkes
				   (tileCenter.y - tilesize) <= pos.z && pos.z <= (tileCenter.y)    			 )
			   ){
				for(int i = x-1; i < x+1; i++){
					for(int j = y-1; j < y+1; j++){
						list.add(getTileAtMapIndex(new Point(i,j)));
					}
				}
				// remove the tile that does not fit, in this case, the last one added
				list.remove(2); // should be tile up right of center tile
				// adding two additional tiles
				list.add(getTileAtMapIndex(new Point(thisTile.x-2,thisTile.y)));
				list.add(getTileAtMapIndex(new Point(thisTile.x,thisTile.y-2)));			
		} 
		// (5) unten rechts im Tile
		else if( ( (tileCenter.x + tilesize/2) <= pos.x && pos.x <= (tileCenter.x + tilesize) &&		// rechtes
				   (tileCenter.y - tilesize)   <= pos.z && pos.z <= (tileCenter.y) ) ||
			     ( (tileCenter.x) 			   <= pos.x && pos.x <= (tileCenter.x + tilesize)  &&		// unteres
				   (tileCenter.y - tilesize)   <= pos.z && pos.z <= (tileCenter.y - tilesize/2)    )
			   ){
				for(int i = x-1; i < x+1; i++){
					for(int j = y-1; j < y+1; j++){
						list.add(getTileAtMapIndex(new Point(i,j)));
					}
				}
				// remove the tile that does not fit, in this case, the last one added
				list.remove(0); // should be tile up left of center tile
				// adding two additional tiles
				list.add(getTileAtMapIndex(new Point(thisTile.x+2,thisTile.y)));
				list.add(getTileAtMapIndex(new Point(thisTile.x,thisTile.y-2)));			
		}
		return list;
	}
	
	/**
	 * Only returns EmptyTiles in view range to avoid double checking known tiles
	 * 
	 * @param pos the center coordinates of the tanks view range
	 * 
	 * @return a list of EmptyTiles in view range
	 */
	public List<Tile> getEmptyTilesInViewRange(Vector3f pos){
		List<Tile> list = getTilesInViewRange(pos);
		for(Tile t : list){
			if( !(t instanceof EmptyTile) ){
				list.remove(t);
			}
		}
		return list;
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
		return new Vector3f(x*2+tHalb,0,y*2+tHalb);
	}
	
	/**
	 * Returns the center coordinates of the Tile
	 * 
	 * @param position the Tile indices
	 * @return the world coordinates of the Tile center
	 */
	public static Vector3f getTileCenterCoordinates(Point position){
		return new Vector3f(position.x*2+10,0,position.y*2+10);
	}

	
	//===================================================
	// PRIVATE CLASS
	//===================================================
	
	/**
	 * Cyclically collects world information from each queue.
	 * 
	 * @author Pascal Jaeger
	 */
	private class QueueCrawler implements Runnable{

		private boolean stop = false;
		
		@Override
		public void run() {
			while(!stop){
				synchronized (map) {
					Tile tile;
					for(Queue<Tile> e : queues.values()){
						if((tile = e.poll()) != null){
							if(tile != null){
								map.put(tile.mapIndex, tile);	
							}							
						}
					}
				}
			}
		}
		
		public void stopCrawling(){
			stop = true;
		}
		
		public void startCrawling(){
			stop = false;
		}
	}
	
	//===================================================
	// END OF PRIVATE CLASS
	//===================================================
}
