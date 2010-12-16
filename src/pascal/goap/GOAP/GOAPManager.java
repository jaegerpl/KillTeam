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

package pascal.goap.Goap;


import java.util.ArrayList;
import java.util.Vector;

import pascal.goap.AStar.AStarMachine;
import pascal.goap.AStar.IMover;
import pascal.goap.AStar.SortedList;


public class GOAPManager implements IMover{


	public Goal currentGoal = null;
	public boolean forceReplan = false;
	public WorldState currentWorldState = new WorldState();
	protected Action currentAction;
	protected ArrayList<GOAPNode> plan;
	protected AStarMachine<GOAPNode> planner = new AStarMachine<GOAPNode>(new GOAPMap(),400);
	protected SortedList goals = new SortedList();
	protected ArrayList<Action> actions = new ArrayList<Action>();
	private Goal oldGoal;
	private Vector<IGOAPListener> subscribers = new Vector<IGOAPListener>();
	
	private void OnGoalChanged(Goal oldGoal, Goal newGoal) {
		for (int i = 0, size = subscribers.size(); i < size; i++)
			((IGOAPListener) subscribers.get(i)).goalChangedEvent(this, oldGoal, newGoal);
	}
	
	private void OnActionChanged(Action oldAction, Action newAction) {
		for (int i = 0, size = subscribers.size(); i < size; i++)
			((IGOAPListener) subscribers.get(i)).actionChangedEvent(this, oldAction, newAction);
	}

	public void addGOAPListener(IGOAPListener listener) {
		subscribers.add(listener);
	}

	public void removeGOAPListener(IGOAPListener listener) {
		subscribers.remove(listener);
	}
	
	public void update()	{		
		//check if the current action is still valid
		if(getCurrentAction() == null || !getCurrentAction().isValid() || getCurrentGoal() != getGoals().get(0))
			selectAction();

		if(getCurrentAction() != null)
			getCurrentAction().performAction();
	}
	
	
	private void updateGoalRelevancy()	{
		for(int i = 0; i < goals.size(); i++)
			((Goal)goals.get(i)).updateRelevance();
		
		goals.sort();
	}
	
	private Goal selectGoal()
	{
		updateGoalRelevancy();
		
		ArrayList<GOAPNode> tempPath = null;
		oldGoal = currentGoal;
		
		for(int i = 0; i < goals.size(); i++)
		{
			currentGoal =((Goal)goals.get(i));
			
			if(currentGoal.isFullfilled())
				continue;
			
			//if the goal hasn't changed and isn't completed either
			//do not compute a new one
			if(currentGoal == oldGoal && plan.size() > 0 && !forceReplan)
				tempPath = plan;
			else
				tempPath = planner.findPath(this,((Goal)goals.get(i)).getNode() , new GOAPNode(null));
			
			
			
			//if no new goal got selected or no valid actionsequence could be formulated for the new goal 
			//do nothing
			if(!forceReplan && plan != null && plan.size() != 0 && currentGoal == oldGoal)
			{
				forceReplan = false;
				currentGoal = oldGoal;
				return currentGoal;
			}
			
			//if a new goal has been found set the new plan
			//and fire the GoalChangedEvent
			if(tempPath != null)
			{
				forceReplan = false;
				//invalidate the current action
				currentAction = null;
				currentGoal = ((Goal)goals.get(i));
				plan = tempPath;		
				OnGoalChanged(oldGoal, (Goal)goals.get(i));
				return currentGoal;
			}
		}
		
		return null;
	}
	
	int i = 0;
	
	private void selectAction() {
		
		//updateGoalRelevancy();
	//	i++;
		selectGoal();
		
		if(currentAction != null && currentAction.isFinished())
		{
			plan.remove(plan.size() - 1);
			//hier isFinished event auslösen
		}
		
		//if there still are actions left for the current goal to be performed select the next one
		//else compute a new goal
		if(plan.size() > 0 && !currentGoal.isFullfilled())
		{
			//i = 0;
			if(currentAction != plan.get(plan.size() - 1).getAction())
				OnActionChanged(currentAction, plan.get(plan.size() - 1).getAction());
			
			currentAction = plan.get(plan.size() - 1).getAction();
		}
		else
		{
			if(currentGoal.isFullfilled())
				plan.clear();

				selectAction();		
		}		
	}
	
	public Goal getCurrentGoal()
	{
		return currentGoal;
	}
	
	public Action getCurrentAction()
	{
		return currentAction;
	}
	
	public void addAction(Action a)
	{
		this.actions.add(a);
	}
	
	public void addGoal(Goal g)
	{
		this.goals.add(g);
	}
	
	public Action removeAction(Action a)
	{
		Action temp = null;
		
		if(actions.contains(a))
		{		
			temp = actions.get(actions.indexOf(a));
			actions.remove(temp);
		}
		
		return temp;
	}
	
	public Goal removeGoal(Goal g)
	{
		if(goals.contains(g))
			return (Goal)goals.get(g);
		
		return null;
	}
	
	public ArrayList<Action> getActions()
	{
		return actions;
	}
	
	public SortedList getGoals()
	{
		return goals;
	}
}
