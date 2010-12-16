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

package pascal.goap.GOAP;

import java.util.ArrayList;

/**
 * Abstract base class for all actions the agent can perform
 * @author Klinge
 */
public abstract class Action
{
    private float cost;
    protected ArrayList<WorldStateSymbol> preCond = new ArrayList<WorldStateSymbol>();
    protected ArrayList<WorldStateSymbol> effect = new ArrayList<WorldStateSymbol>();
    public String name;

    
    public Action(String name, float cost, ArrayList<WorldStateSymbol> preCond, ArrayList<WorldStateSymbol> effect)
    {
        this.cost = cost;
        this.preCond = preCond;
        this.effect = effect;
        this.name = name;
    }
    
    public Action(String name, float cost)
    {
    	this.cost = cost;
    	this.name = name;
    }

    /**
     * Gets the cost of this action to be performed
     * Actions with lower costs are preferred during planning
     * @return
     */
    public float getCost()
    {
        return cost;
    }

    /**
     * Checks if the current action is still valid
     * Otherwise replanning is necessary
     * @return
     */
    public abstract boolean isValid();
    
    
    /**
     * Determines if this action has accomplished its goal
     * @return
     */
    public abstract boolean isFinished();

    /**
     * This method queries the blackboard and working memory
     * to check for preconditions that are not known during compile time
     * and can not be directly altered through other actions
     * @return
     */
    public abstract boolean contextPreconditionsMet();
    
    /**
     * Returns the conditions which need to be fulfilled before
     * this action can be executed
     * @return
     */
    public ArrayList<WorldStateSymbol> getPreconditions()
    {
        return preCond;
    }

    /**
     * Returns the effect this Action has on the worldState
     * @return
     */
    public ArrayList<WorldStateSymbol> getEffect()
    {
        return effect;
    }

    /**
     * Applies the effects of this action to the world
     */
	public abstract void performAction();
	
	public String toString()
	{
		return name;
	}



}
