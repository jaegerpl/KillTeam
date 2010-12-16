package de.lunaticsoft.combatarena.api.testteam;

import pascal.goap.Agent.GlobalKIAgent;
import pascal.goap.Agent.GlobalKIBlackboard;
import pascal.goap.GOAP.Action;
import pascal.goap.GOAP.Goal;
import pascal.goap.GOAP.IGOAPListener;

public class GlobalKI  extends GlobalKIAgent implements IGOAPListener {
	
	private String name = "GlobaleKI";
	
	public GlobalKI() {
		blackboard.name = name;
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

}
