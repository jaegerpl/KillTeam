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
 * Action to destroy a tank. Shooting and maybe following the spotted tank.
 * 
 * preCond
 * - tankSpotted = true - ist true, wenn ein Tank im Sichtbereich ist.
 * Effect
 * - toolBoxSpotted = true - ist true, wenn eine ToolBox im Sichtbereich ist.
 * 
 * @author Pascal Jaeger
 *
 */
public class DestroyTank extends Action{

	protected GoapActionSystem as;

	public DestroyTank(GoapActionSystem as, String name, float cost) {
	    super(1, null, null,null );


		this.as = as;
		effect.add(new WorldStateSymbol<Boolean>(TankWorldProperty.ToolBoxSpotted,
				true, PropertyType.Boolean));
		preCond.add(new WorldStateSymbol<Boolean>(TankWorldProperty.TankSpotted,
				true, PropertyType.Boolean));
		
	}

	public void performAction() {
		// Shooting and maybe following the spotted tank is done here.
	}

	public boolean isFinished() {
		// finished wenn tank durch eine ToolBox ersetzt wird.
		// lastPositionOfTank.somewhereNear(spottedToolBox.position)
		return true;
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
