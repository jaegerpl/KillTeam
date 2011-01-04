package de.lunaticsoft.combatarena.api.killteam;

import map.memory.map.MemorizedMap;
import goap.agent.GlobalKIAgent;
import goap.agent.GlobalKIBlackboard;
import goap.goap.Action;
import goap.goap.Goal;
import goap.goap.IGOAPListener;
import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;

/**
 * Stellt die gobale KI dar. Hier soll
 * <li> die Karte aufgebaut werden
 * <li> die globale Strategie entschieden werden
 * <li> etc
 * 
 * Damit die GlobalKI mit der Welt interagieren kann
 * 
 * <br><br>
 * @author pascal
 *
 */
public class GlobalKI  extends GlobalKIAgent implements IGOAPListener {
	
	private String name = "GlobaleKI";
	private IWorldInstance world;	
	private MemorizedMap map;
	
	public GlobalKI() {
		blackboard.name = name;
		map = new MemorizedMap();
	}

	@Override
	public void actionChangedEvent(Object sender, Action oldAction,
			Action newAction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void goalChangedEvent(Object sender, Goal oldGoal, Goal newGoal) {
		// TODO Auto-generated method stub
		
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
}
