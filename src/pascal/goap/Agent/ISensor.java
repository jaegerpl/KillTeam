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
 * Interface for all Sensors.
 * A sensor is responsible for collecting data from the world the agent is in
 * @author Klinge
 *
 */
public interface ISensor {
	
	/**
	 * A update collects new data from the world and stores it in the given
	 * WorkingMemory
	 * @param memory
	 */
	public void update(WorkingMemory memory);

}
