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

import de.lunaticsoft.combatarena.api.enumn.EColors;
import goap.goap.Action;
import goap.goap.PropertyType;
import goap.goap.TankWorldProperty;
import goap.goap.WorldStateSymbol;
import goap.scenario.GoapActionSystem;

/**
 * Action to move the agent along a path. Is also responsible to control the agents
 * movement speed. Triggers emotions.
 * preCond
 * - tankSpotted = true - ist true, wenn ein Tank im Sichtbereich ist.
 * - spottedTankColor = color
 * Effect
 * - toolBoxSpotted = true - ist true, wenn eine ToolBox im Sichtbereich ist.
 * - spottedTankColor != color (kann auch leer sein, wenn kein Tank gespotted ist)
 * 
 * @author Pascal Jaeger
 *
 */
public class DestroyTankColor extends Action{

	protected GoapActionSystem as;
	private final EColors color;

	public DestroyTankColor(GoapActionSystem as, String name, float cost, EColors color) {
	    super(1, null, null,null );
	    this.color = color;


		this.as = as;
		effect.add(new WorldStateSymbol<Boolean>(TankWorldProperty.AtDestination,
				true, PropertyType.Boolean));
		preCond.add(new WorldStateSymbol<Boolean>(TankWorldProperty.TankSpotted, true, PropertyType.Boolean));
		preCond.add(new WorldStateSymbol<EColors>(TankWorldProperty.SpottedTanksColor, color, PropertyType.EColors));
		
	}

	public void performAction() {
		// destroying tank of a specific color goes in here
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
