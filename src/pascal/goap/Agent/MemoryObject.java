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

import com.jme.math.Vector3f;

/**
 * Everything the agent knows about the outside world is represented as a
 * MemoryObject 
 * MemoryObjects get stored in the WorkingMemory
 * @author Klinge
 *
 */
public class MemoryObject {
	
	public float beliefe;
	public String type;
	public Vector3f position;
	public boolean persistend = false;
	
	/**
	 * Creates a new MemoryObject
	 * @param beliefe: How sure is the agent about this memory
	 * @param type: What kind of memory
	 * @param position: The position the agent perceived this memory
	 */
	public MemoryObject(float beliefe, String type, Vector3f position)
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
