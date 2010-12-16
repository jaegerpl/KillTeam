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

package pascal.goap.Scenario.Actions;

import pascal.goap.Goap.Action;
import pascal.goap.Goap.PropertyType;
import pascal.goap.Goap.TankWorldProperty;
import pascal.goap.Goap.WorldStateSymbol;
import pascal.goap.Scenario.GoapActionSystem;

/**
 * Action to move the agent along a path. Is also responsible to control the agents
 * movement speed. Triggers emotions.
 * preCondition: WorldProperty.HasDestination, true
 * effect: WorldProperty.AtDestination, true
 * @author Klinge
 *
 */
public class GotoLocation extends Action{

	private static final float FAST = 15f;
	private static final float NORMAL = 10f;
	private static final float SLOW = 5f;
	private static final float STOP = 0f;
	protected GoapActionSystem as;

	public GotoLocation(GoapActionSystem as, String name, float cost) {
		super(name, cost);

		this.as = as;
		effect.add(new WorldStateSymbol<Boolean>(TankWorldProperty.AtDestination,
				true, PropertyType.Boolean));
		preCond.add(new WorldStateSymbol<Boolean>(TankWorldProperty.HasDestination,
				true, PropertyType.Boolean));
	}

	public void performAction() {
		
//		if(as.getBlackboard().happyness > 0)
//			as.getBlackboard().speed = NORMAL;
//		else
//			as.getBlackboard().speed = SLOW;
//		
//		if(as.getBlackboard().anger < -0.5)
//			as.getBlackboard().speed = FAST;
//		
//		// TODO: muss hier noch gegen überlauf geschütz werden und auch genauer
//		// upgedatet werden
//		as.currentWorldState
//				.setValue(TankWorldProperty.Boredom, ((Float) as.currentWorldState
//						.getValue(TankWorldProperty.Boredom)) + 0.0003f);
//		
//		as.currentWorldState
//				.setValue(TankWorldProperty.Hunger, ((Float) as.currentWorldState
//						.getValue(TankWorldProperty.Hunger)) + 0.0002f);
//		
//		as.currentWorldState.setValue(TankWorldProperty.Exhaustion,
//				((Float) as.currentWorldState
//						.getValue(TankWorldProperty.Exhaustion)) + 0.0002f);
	}

	public boolean isFinished() {
		if (as.getBlackboard().currentNode == as.getBlackboard().destinationNode) {
			as.getBlackboard().destinationNode = null;
			return true;
		}

		return false;
	}

	@Override
	public boolean isValid() {

		if (as.getBlackboard().currentNode == as.getBlackboard().destinationNode
				|| as.getBlackboard().path == null
				|| as.getBlackboard().path.size() == 0)
			return false;

		// if the previously calculated destination is now blocked
		// the agent needs to replan
		if (as.getBlackboard().destinationNode == null
				|| (as.getBlackboard().path.size() == 1 && as.getBlackboard().destinationNode.blocked)) {
			as.forceReplan = true;
			return false;
		}

		return true;
	}

	@Override
	public boolean contextPreconditionsMet() {

		// GoToLocation is allways a valid option
		return true;
	}
}
