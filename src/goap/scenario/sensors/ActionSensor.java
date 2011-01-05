/*
 * Copyright (C) 2010 Arne Klingenberg
 * E-Mail: klingenberg.a@googlemail.com
 * 
 * This software is free; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 */
package goap.scenario.sensors;

import goap.agent.ISensor;
import goap.agent.WorkingMemory;
import goap.scenario.BaseGame;
import goap.scenario.Pedestrian;

/**
 * Sensor which searches for other Agents within an certain range in order to be able to be informed
 * over all the actions these agents perform.
 * @author Klinge
 *
 */
public class ActionSensor implements ISensor{

	private Pedestrian owner;
	
	public ActionSensor(Pedestrian owner){
		this.owner = owner;
	}
	
	@Override
	public void update(WorkingMemory memory) {
		for(Pedestrian pedestrian :BaseGame.pedestrians)
		{
			if(pedestrian != owner){
//				if(owner.inVisualRange(pedestrian.getModel()))
//					owner.getBlackboard().actionProvider.addActionListener(pedestrian);
//				else
//					owner.getBlackboard().actionProvider.removeActionListener(pedestrian);
			}
		}
		
	}

}
