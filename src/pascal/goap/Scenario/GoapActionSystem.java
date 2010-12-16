/*
 * Copyright (C) 2009 Arne Klingenberg
 * E-Mail: klingenberg.a@googlemail.com
 * 
 * This software is free; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 */

package pascal.goap.Scenario;


import java.util.ArrayList;

import pascal.goap.Agent.Agent;
import pascal.goap.Agent.TankBlackboard;
import pascal.goap.Agent.IActionSystem;
import pascal.goap.Agent.MemoryObject;
import pascal.goap.Agent.WorkingMemory;
import pascal.goap.GOAP.GOAPManager;

/**
 * GOAPManager with some extra getters and setters.
 * @author Klinge
 *
 */
public class GoapActionSystem extends GOAPManager implements IActionSystem{

	private Agent owner;
	private TankBlackboard blackboard;
	private WorkingMemory memory;
	
	public GoapActionSystem(Agent owner, TankBlackboard blackboard, WorkingMemory memory)
	{
		this.blackboard = blackboard;
		this.memory = memory;
		this.owner = owner;
	}
	
	public Agent getOwner()
	{
		return owner;
	}
	
	public ArrayList<MemoryObject> getMemoryFacts(String key)
	{
		return memory.memoryObjects.get(key);
	}
	
	public TankBlackboard getBlackboard()
	{
		return blackboard;
	}
	
	public WorkingMemory getMemory()
	{
		return memory;
	}

	@Override
	public void selectAction() {
		
		super.update();
		
	}
}
