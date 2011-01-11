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
 * Action to move the tank to a specified location in the map.
 * 
 * preCond
 *  - inHangar = false
 *  - hasDestination = true
 *  Effect
 *  - atDestination
 *  - hasDestination = false
 *  
 * @author Pascal Jaeger
 *
 */
public class GotoLocation extends Action{

	protected GoapActionSystem as;

	public GotoLocation(GoapActionSystem as, String name, float cost) {
	    super(1, null, null,null );


		this.as = as;
		effect.add(new WorldStateSymbol<Boolean>(TankWorldProperty.HasDestination, false, PropertyType.Boolean));
		effect.add(new WorldStateSymbol<Boolean>(TankWorldProperty.AtDestination, true, PropertyType.Boolean));
		preCond.add(new WorldStateSymbol<Boolean>(TankWorldProperty.HasDestination, true, PropertyType.Boolean));
		preCond.add(new WorldStateSymbol<Boolean>(TankWorldProperty.InHangar, true, PropertyType.Boolean));
		
	}

	public void performAction() {
		// movement goes here
	}

	public boolean isFinished() {
		if(as.getBlackboard().hasDestination == true && as.getBlackboard().atDestination == true){
			return true;
		}
		return false;
	}

	@Override
	public boolean isValid() {
		// Gruende, warum diese Aktion nicht mehr ausgef�hrt werden koennte?
		return true;
	}

	@Override
	public boolean contextPreconditionsMet() {
		return true;
	}
}
