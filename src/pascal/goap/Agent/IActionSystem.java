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

import java.util.ArrayList;

/**
 * Interface for all ActionSystems
 * The ActionSystem is the module responsible for decision making in the C4 model
 * @author Klinge
 *
 */
public interface IActionSystem {

	/**
	 * Choose a new action for the agent to perform
	 */
	public void selectAction();
	
	/**
	 * Gets all memoryFacts from the workingMemory from the given type
	 * @param key
	 * @return
	 */
	public ArrayList<MemoryObject> getMemoryFacts(String key);
	
	/**
	 * Gets the Blackboard which holds all data that should be shared between
	 * the different modules
	 * @return
	 */
	public TankBlackboard getBlackboard();
}
