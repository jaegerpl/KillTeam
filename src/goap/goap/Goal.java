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

package goap.goap;

import goap.scenario.GoapActionSystem;

/**
 * Abstract base class for all goals the agent can have.
 * @author Klinge
 */
public abstract class Goal implements Comparable {

    public String name;
    protected float relevancy;
    protected GoapActionSystem as;
    
    public Goal(String name, float relevancy, GoapActionSystem as)
    {
    	this.name = name;
    	this.relevancy = relevancy;
    	this.as = as;
    }

    public float getRelevancy()
    {
        return relevancy;
    }
    
    /**
     * Updates the relevance a the goal.
     * A goal with a higher relevance is more likely to be activated by the GOAPManager
     */
    public abstract void updateRelevance();

    /**
     * Checks if the current goal has been achieved
     * @return
     */
    public abstract boolean isFullfilled();
    
    /**
     * Gets the goal as a node to be able to work with A*
     * @return
     */
    public abstract GOAPNode getNode();
    
    /**
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object other) {
            Goal o = (Goal) other;

            if (this.getRelevancy() > o.getRelevancy()) {
                    return -1;
            } else if (this.getRelevancy() < o.getRelevancy()) {
                    return 1;
            } else {
                    return 0;
            }
    }
    
    public String toString()
    {
    	return name;
    }
}
