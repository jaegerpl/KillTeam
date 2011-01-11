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
 * Action to collect a flaf during "Capture the Flag".
 * preCond
 * - flagSpotted = true - die Flag ist im Sichtbereich
 * - hasFlag= false - der Panzer ist nicht im besitz der Flagge
 * Effect
 * - hasFlag = true
 * - flagSpotted = false - müssen mal abwarten, wie das konnkret aussieht, wenn ein Tank eine Flag einsammelt
 * 
 * @author Pascal Jaeger
 *
 */
public class CollectFlag extends Action{

	protected GoapActionSystem as;

	public CollectFlag(GoapActionSystem as, String name, float cost) {
	    super(1, null, null,null );


		this.as = as;
		effect.add(new WorldStateSymbol<Boolean>(TankWorldProperty.HasFlag, true, PropertyType.Boolean));
		effect.add(new WorldStateSymbol<Boolean>(TankWorldProperty.FlagSpotted, false, PropertyType.Boolean));
		preCond.add(new WorldStateSymbol<Boolean>(TankWorldProperty.FlagSpotted, true, PropertyType.Boolean));
		preCond.add(new WorldStateSymbol<Boolean>(TankWorldProperty.HasFlag, false, PropertyType.Boolean));
		
	}

	public void performAction() {
		// moving to the flags posi
	}

	public boolean isFinished() {
		// finished, wenn ich die Flag habe
		return true;
	}

	@Override
	public boolean isValid() {
		// valid solange die Flag im Sichtbereich und nicht von einem anderen Tank (mein Team oder Gegner) weggeschnappt wurde.
		return true;
	}

	@Override
	public boolean contextPreconditionsMet() {
		return true;
	}
}
