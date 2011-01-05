package de.lunaticsoft.combatarena.api.killteam.Movement;

import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;

public class StuckCheck {
	private final IWorldInstance world;
	private Vector3f curPos ;
	private Vector3f lastPos ;
	
	public final static float STUCKDIST = 0.06f;
	
	public StuckCheck(IWorldInstance world)
	{
		this.world = world;
		curPos = world.getMyPosition();
		lastPos = new Vector3f(-9999, -9999, -9999);
		
		
	
	}
	
	public boolean stuck(){
		if(curPos.distance(lastPos) <0.06f)
			return true;
		return false;
		
		
	}
	
	public void act()
	{
		lastPos = curPos;
		curPos = world.getMyPosition();
		
	}

}
