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

package goap.scenario;


import goap.agent.Agent;
import goap.agent.IActionSystem;
import goap.agent.MemoryObject;
import goap.agent.TankBlackboard;
import goap.agent.WorkingMemory;
import goap.goap.GOAPManager;

import java.util.ArrayList;


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
