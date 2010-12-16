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
package pascal.goap.GOAP;

import java.util.EventObject;

/**
 * The ActionChangedEvent gets thrown every time the GOAPManager changes the current
 * action. It provides information about the old and the new action
 * @author Klinge
 *
 */
public class ActionChangedEvent extends EventObject{

	public Action oldGoal;
	public Action newGoal;
	
    public ActionChangedEvent(Object source, Action oldAction, Action newAction){
    
	    super(source); 
	    this.oldGoal = oldAction;
	    this.newGoal = newAction;
    }
}
