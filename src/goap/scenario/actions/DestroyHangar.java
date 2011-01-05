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

import java.util.ArrayList;

import goap.goap.Action;
import goap.goap.PropertyType;
import goap.goap.TankWorldProperty;
import goap.goap.WorldStateSymbol;
import goap.scenario.GoapActionSystem;

/**
 * Action to shoot at a hangar to destroy it.
 * Shoot until the hangar is destroyed. Maybe its a good idea to run around the hangar 
 * in a circle, so competitors can't shoot at the tank too easy.
 * 
 * preCond
 * - atDestination = true
 * Effect
 * - der Hangar ist nicht mehr im Sichtbereich (schwierig zu formulieren mit Variablen)
 * 
 * @author Pascal Jaeger
 *
 */
public class DestroyHangar extends Action{

	protected GoapActionSystem as;

	public DestroyHangar(GoapActionSystem as, String name, float cost) {
	    super(1, null, null,null );


		this.as = as;
		// Effekt, der Hangar an der Position taucht nicht mehr in der perceive Methode auf 
//		effect.add(new WorldStateSymbol<Boolean>(TankWorldProperty.AtDestination,
//				true, PropertyType.Boolean));
		preCond.add(new WorldStateSymbol<Boolean>(TankWorldProperty.AtDestination,
				true, PropertyType.Boolean));
		
	}

	public void performAction() {
		// shooting at hangar goes here
		// maybe some running around to 
	}

	public boolean isFinished() {
		// finished wenn - siehe Effekt
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
