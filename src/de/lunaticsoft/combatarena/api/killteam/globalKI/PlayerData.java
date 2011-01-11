package de.lunaticsoft.combatarena.api.killteam.globalKI;

import goap.goap.Action;
import goap.goap.Goal;

import com.jme.math.Vector3f;


/**
 * Keeps track of the status of a tank.
 * 
 * @author pascal
 *
 */
public class PlayerData {
	public Vector3f Position;
	public Vector3f lastPosition;
	public Vector3f GoalPosition;
	public int attacked;
	public Goal goal;
	public Goal oldGoal;
	public Action action;
	public Action oldAction;

}
