/*
 * Copyright (C) 2010 Arne Klingenberg
 * E-Mail: klingenberg.a@googlemail.com
 * 
 * This software is free; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 */
package goap.goap;

public interface IGOAPListener {
	
	/**
	 * The GoalChangedEvent gets thrown every time the GOAPManager changes the current
	 * goal. It provides information about the old and the new goal
	 */
	void goalChangedEvent(Object sender, Goal oldGoal, Goal newGoal);
	
	/**
	 * The ActionChangedEvent gets thrown every time the GOAPManager changes the current
	 * action. It provides information about the old and the new action
	 */
	void actionChangedEvent(Object sender, Action oldAction, Action newAction);
}
