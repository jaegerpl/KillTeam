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
import goap.goap.PropertyType;
import goap.goap.TankWorldProperty;
import goap.goap.WorldStateSymbol;
import goap.scenario.GoapActionSystem;

/**
 * Action to leave the hangar
 * preCond 
 * - inHangar = true
 * - hasDestination = true
 * Effect
 * - inHangar = false
 * 
 * @author Pascal Jaeger
 *
 */
public class LeaveHangar extends Action{

	protected GoapActionSystem as;

	public LeaveHangar(GoapActionSystem as, String name, float cost) {
	    super(1, null, null,null );


		this.as = as;
		effect.add(new WorldStateSymbol<Boolean>(TankWorldProperty.InHangar, false, PropertyType.Boolean));
		preCond.add(new WorldStateSymbol<Boolean>(TankWorldProperty.InHangar, true, PropertyType.Boolean));
		
	}

	public void performAction() {
	  // leaving the hangar is done here.
	  // set as.getBlackboard().inHangar = false when done
	}

	public boolean isFinished() {
		if(as.getBlackboard().inHangar == false){
			return true;
		}
		return false;
	}

	@Override
	public boolean isValid() {
		return !isFinished();
	}

	@Override
	public boolean contextPreconditionsMet() {
		return true;
	}
}
