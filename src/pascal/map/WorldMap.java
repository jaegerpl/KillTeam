package pascal.map;

import java.awt.Point;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;
import de.lunaticsoft.combatarena.api.testteam.PascalPlayer;

/**
 * A tank registers at the map and is given a queue, where it can place information about the world it perceives.
 * The map cyclically crawls the queues and puts the information in the central map.
 * 
 * @author Pascal Jaeger
 *
 */
public class WorldMap {
	
	// a list of queues for the tanks
	private Map<String, Queue<IWorldObject>> queues = Collections.synchronizedMap(new HashMap<String, Queue<IWorldObject>>());
	
	// the central map
	Map<Point,Tile> map = new HashMap<Point, Tile>();
	
	public WorldMap() {
		// TODO starte einen Thread, der zyklisch aus den Queues immer ein Element liest und in die Map schreibt.
	}
	
	/**
	 * Registers a tank at the WorldMap.<br>
	 * A queue for the tank is created an a reference is returned.
	 * 
	 * @param tank 
	 * @return the queue for the tank to write it's information into
	 */
	public ConcurrentLinkedQueue<IWorldObject> registerTank(PascalPlayer tank){
		ConcurrentLinkedQueue<IWorldObject> list;
		synchronized (queues) {
			if(!queues.containsKey(tank.getName())){
				list = new ConcurrentLinkedQueue<IWorldObject>();
				queues.put(tank.getName(), list);
			}else {
				list = (ConcurrentLinkedQueue<IWorldObject>) queues.get(tank.getName());
			}
		}
		return list;
	}
}
