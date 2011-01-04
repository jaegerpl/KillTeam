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
package goap.agent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The WorkingMemory stores all MemoryObjects collected through sensors.
 * @author Klinge
 *
 */
public class WorkingMemory {
	
	public HashMap<MemoryObjectType, ArrayList<MemoryObject>> memoryObjects = new HashMap<MemoryObjectType, ArrayList<MemoryObject>>();
	
	/**
	 * The update method updates all MemoryObjects within the WorkingMemory
	 * If the belief of a MemoryObject is not persistent it is decreased every
	 * update and finally completely removed
	 */
	public void update()
	{
		ArrayList<MemoryObject> deleteList = new ArrayList<MemoryObject>();
		
		//the believe of each memorObject decreases over time if it is not renewed
		for(MemoryObjectType key : memoryObjects.keySet())
			for(MemoryObject m : memoryObjects.get(key))
			{
				//Persistent memories do not get weaker over time
				if(!m.persistend)
					m.beliefe -= 0.1f;
				
				if(m.beliefe <= 0.0f)
					deleteList.add(m);
			}
		
		//deletelist is needed because foreach loops do not allow to delete
		//files during the loop
		for(MemoryObject m : deleteList)
			memoryObjects.get(m.type).remove(m);
	}
	
	/**
	 * Adds a new MemoryObject to the WorkingMemory
	 * If a memory of the same object already exists, its believe gets updated.
	 * @param memory
	 */
	public void addMemory(MemoryObject memory)
	{
		if(memory != null)
		{
			if(memoryObjects.containsKey(memory.type))
			{
				ArrayList<MemoryObject> memories = memoryObjects.get(memory.type);
				
				//if the same memory has allready been stored
				//just renew its beliefe
				if(memories.contains(memory))
					memories.get(memories.indexOf(memory)).beliefe = memory.beliefe;
				else
					memoryObjects.get(memory.type).add(memory);
			}
			else
			{
				ArrayList<MemoryObject> memories = new ArrayList<MemoryObject>();
				memories.add(memory);
				memoryObjects.put(memory.type, memories);
			}
		}
	}
	
	/**
	 * Adds a list of new MemoryObjects
	 * @param memories
	 */
	public void addMemories(ArrayList<MemoryObject> memories)
	{
		if(memories != null)
		{
			for(MemoryObject memory : memories)
				addMemory(memory);
		}
	}
	
	/**
	 * Removes the given memory from the WorkingMemory
	 * @param memory
	 */
	public void removeMemory(MemoryObject memory)
	{
		if(memory != null)
		{
			if(memoryObjects.containsKey(memory.type))
				memoryObjects.get(memory.type).remove(memory);
		}
	}

}
