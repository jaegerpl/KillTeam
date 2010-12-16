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
 * The SensorySystem updates every ISensor that is registered with it and
 * puts the collected MemoryObjects into the given WorkingMemory
 * @author Klinge
 *
 */
public class SensorySystem {

	private ArrayList<ISensor> sensors = new ArrayList<ISensor>();
	private WorkingMemory memory;
	
	/**
	 * Creates a new SensorySystem
	 * @param memory: Working memory in which the collected MemoryObjects should
	 * be stored
	 */
	public SensorySystem(WorkingMemory memory){
		this.memory = memory;
	}
	
	/**
	 * Adds a new sensor to the SensorySystem.
	 * Every sensor added here gets updated automatically
	 * @param sensor
	 */
	public void addSensor(ISensor sensor){
		this.sensors.add(sensor);
	}
	
	/**
	 * Updates all registered sensors and collects new MemoryObjects in the process
	 */
	public void update(){
		for(ISensor sensor: sensors)
			sensor.update(memory);
	}
}
