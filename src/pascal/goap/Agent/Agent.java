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
package pascal.goap.Agent;

/**
 * Base class for all Agents based on the C4 Architecture
 * @author Klinge
 *
 */
public abstract class Agent{
	
	protected SensorySystem sensorySystem;
	protected WorkingMemory memory;
	protected TankBlackboard blackboard;
	protected IActionSystem actionSystem;
	
	public Agent()
	{
		memory = new WorkingMemory();
		sensorySystem = new SensorySystem(memory);
		blackboard = new TankBlackboard();
		
	}
	
	/**
	 * Gets the Blackboard which holds all data that should be shared between
	 * the different modules
	 * @return
	 */
	public TankBlackboard getBlackboard(){
		return blackboard;
	}
	
	/**
	 * Updates the agents memory and sensory system
	 */
	public void update(){
		sensorySystem.update();
		memory.update();
		//actionSystem.selectAction();
	}

}
