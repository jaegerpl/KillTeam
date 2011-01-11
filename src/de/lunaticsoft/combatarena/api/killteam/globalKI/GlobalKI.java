package de.lunaticsoft.combatarena.api.killteam.globalKI;

import java.util.HashMap;
import java.util.Map;

import com.jme.math.Vector3f;

import memory.map.MemorizedMap;
import memory.objectStorage.ObjectStorage;
import goap.agent.GlobalKIAgent;
import goap.agent.GlobalKIBlackboard;
import goap.goap.Action;
import goap.goap.Goal;
import de.lunaticsoft.combatarena.api.interfaces.IPlayer;
import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;
import debug.MapServer;

/**
 * Stellt die gobale KI dar. Hier soll
 * <li> die Karte aufgebaut werden
 * <li> die globale Strategie entschieden werden
 * <li> etc
 * <br>
 * damit die GlobalKI mit der Welt interagieren kann
 * 
 * <br><br>
 * @author pascal
 *
 */
public class GlobalKI  extends GlobalKIAgent{
	
	private String name = "GlobaleKI";
	private IWorldInstance world;	
	private MemorizedMap map;
	private ObjectStorage objectStorage;
	private Map<IPlayer, PlayerData> players;
	
	public GlobalKI() {
		blackboard.name = name;
		map = new MemorizedMap();
		objectStorage = new ObjectStorage();
		players = new HashMap<IPlayer, PlayerData>();
		
		MapServer srv = new MapServer(map, this.objectStorage);

		Thread t = new Thread(srv);
		t.start();
	}

	public GlobalKIBlackboard getBlackBoard(){
		return blackboard;
	}

	/**
	 * Is called by one of the tanks, since only IPlayer classes get an world reference 
	 * from the TankArenaServer
	 * 
	 * @param world
	 */
	public void setWorldInstance(IWorldInstance world) {
		this.world = world;	
	}
	
	public MemorizedMap getWorldMap(){
		return map;
	}
	
	public ObjectStorage getObjectStorage() {
		return this.objectStorage;
	}
	
	/**
	 * Registers a tank in global KI after it has spawned and creates the players status object
	 * @param tank
	 */
	public void registerTank(IPlayer tank){
		PlayerData data = new PlayerData();
		players.put(tank, data);
		blackboard.tanksAlive++;
	}
	
	/**
	 * Removes a registered tank when it dies
	 * @param tank
	 */
	public void removeTank(IPlayer tank){
		players.remove(tank);
		blackboard.tanksAlive--;
	}
	
	/**
	 * Updates a specific status item of a registered tank
	 * 
	 * @param tank
	 * @param statusItem
	 */
	public void tankStatusChanged(IPlayer tank, Object statusItem, StatusType type){
		PlayerData data = players.get(tank); 
		switch (type) {
		case Goal:
			if(statusItem instanceof Goal){
				Goal goal = (Goal)statusItem;
				data.oldGoal = data.goal;
				data.goal = goal;
			}			
			break;
		case Action:
			if(statusItem instanceof Action){
				Action action = (Action)statusItem;
				data.oldAction = data.action;
				data.action = action;
			}	
			break;
		case Position:
			if(statusItem instanceof Vector3f){
				Vector3f pos = (Vector3f)statusItem;
				data.lastPosition = data.Position;
				data.Position = pos;
			}	
			break;
		case GoalPosition:
			if(statusItem instanceof Vector3f){
				data.GoalPosition = (Vector3f)statusItem;
			}	
			break;
		case Attacked:
			if(statusItem instanceof Integer){
				data.attacked = (Integer)statusItem;
			}
			break;			
		default:
			break;
		}
		
	}
}
