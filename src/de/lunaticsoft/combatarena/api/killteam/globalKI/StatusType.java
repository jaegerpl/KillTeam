package de.lunaticsoft.combatarena.api.killteam.globalKI;

/**
 * 
 * Every type needs to be reflected in the {@link PlayerData}
 * 
 * @author pascal
 *
 */
public enum StatusType {
	Goal, 			// current Goal
	Action, 		// current Action
	Position, 		// current position
	Attacked, 		// if tank has been attacked
	GoalPosition	// the position the tank moves to

}
