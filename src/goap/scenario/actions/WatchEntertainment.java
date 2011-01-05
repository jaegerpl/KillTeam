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
package goap.scenario.actions;

import goap.goap.Action;
import goap.scenario.GoapActionSystem;

/**
 * Action which entertains the agent.
 * 
 * preCondtion: WorldProperty.AtDestination, true
 * effect: WorldProperty.Entertained, true
 * @author Klinge
 *
 */
public class WatchEntertainment extends Action{

	private GoapActionSystem as;
	
	public WatchEntertainment(GoapActionSystem as, String name, float cost) {

	    super(1, null, null,null );

		this.as = as;
//		effect.add(new WorldStateSymbol<Boolean>(TankWorldProperty.Entertained, true, PropertyType.Boolean));
//		preCond.add(new WorldStateSymbol<Boolean>(TankWorldProperty.AtDestination, true, PropertyType.Boolean));
	}
	
	@Override
	public boolean contextPreconditionsMet() {
		return true;
	}

	@Override
	public boolean isFinished() {
//		if((Float)as.currentWorldState.getValue(TankWorldProperty.Boredom) <= 0.0f)
//		{
//			//boredem should not be negativ
//			as.currentWorldState.setValue(TankWorldProperty.Boredom, 0.0f) ;
//			as.getBlackboard().destinationNode = null;
//			return true;
//		}
		
		return false;
	}

	@Override
	public boolean isValid() {
		return !isFinished();
	}

	@Override
	public void performAction() {
//		as.currentWorldState.setValue(TankWorldProperty.Boredom, 
//				((Float)as.currentWorldState.getValue(TankWorldProperty.Boredom)) - 0.001f) ;		
	}
}
