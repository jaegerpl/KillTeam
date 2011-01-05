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

import com.jme.math.Vector3f;

/**
 * Everything the agent knows about the outside world is represented as a MemoryObject 
 * MemoryObjects get stored in the WorkingMemory
 * @author Klinge
 * 
 * Changed type from String to new MemoryObjectType to store information better
 * @author Pascal J�ger
 *
 */
public class MemoryObject {
	
	public float beliefe;
	public MemoryObjectType type;
	public Vector3f position;
	public boolean persistend = false; //a persistend MemoryObjects believe will not be decreased by the WorkingMemory
	
	/**
	 * Creates a new MemoryObject
	 * @param beliefe: How sure is the agent about this memory
	 * @param type: What kind of memory
	 * @param position: The position the agent perceived this memory
	 */
	public MemoryObject(float beliefe, MemoryObjectType type, Vector3f position)
	{
		this.beliefe = beliefe;
		this.type = type;
		this.position = position;
	}
	
	public boolean equals(Object o)
	{
		MemoryObject other = (MemoryObject)o;
		
		if(this.type == other.type && other.position.x == this.position.x && other.position.z == position.z)
			return true;
		
		return false;
	}

}