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

import goap.pathfinding.NavNode;

import java.util.List;
import java.util.TreeMap;


import com.jme.math.Vector3f;

/**
 * The WorldState is basically just a collection of WorldStateSymbols
 * which make up the current state of the world
 * @author Arne Klingenberg
 */
public class WorldState {

    TreeMap<TankWorldProperty, WorldStateSymbol> states;
    
    public WorldState()
    {
    	states = new TreeMap<TankWorldProperty, WorldStateSymbol>();
    }
    
    public WorldState(TreeMap<TankWorldProperty,WorldStateSymbol> currentState)
    {
    	states = currentState;
    }

    /**
     * Adds a new WorldStateSymbol to the current worldstate
     * @param state
     */
    public void add(WorldStateSymbol state)
    {
    	if(states.containsKey(state.prop))
    		states.get(state.prop).value = state.value;
    	else
    		states.put(state.prop, state);
    }
    
    /**
     * Adds a set of WorldStateSymbols to the current worldstate
     * @param states
     */
    public void addAll(List<WorldStateSymbol> states)
    {
    	for(WorldStateSymbol s: states)
    		add(s);
    }

    /**
     * Removes a worldStateSymbol from the current worldstate
     * @param state
     */
    public void remove(WorldStateSymbol state)
    {
        states.remove(state.prop);
    }
    
    /**
     * Removes a set of worldStateSymbols from the current worldstate
     * @param state
     */
    public void remove(TankWorldProperty prop)
    {
    	states.remove(prop);
    }

    /**
     * Checks the current worldState against a desired worldState and returns
     * the number of currently unsatisfied worldStateSymbols
     * @param expected
     * @return
     */
    public int getNumUnsatisfiedStates(List<WorldStateSymbol> expected)
    {
        int count = 0;
        
        for(WorldStateSymbol s: expected)
        	if(!isSatisfied(s))
        		count++;
        
        return count;
    }
    
    /**
     * Checks if a specific WorldStateSymbol is currently satisfied
     * @param state
     * @return
     */
    public boolean isSatisfied(WorldStateSymbol state)
    {
    	if(states.containsKey(state.prop))
    	{
    		switch (state.getType()) {
			case Boolean:  if(((Boolean)states.get(state.prop).value) == (Boolean)state.value)
	    		return true;	
				break;
			case NavNode: if(((NavNode)states.get(state.prop).value) == (NavNode)state.value)
	    		return true;
				break;
			case Float:
				if(((Float)states.get(state.prop).value).floatValue() == ((Float)state.value).floatValue())
	    		return true;
				break;
			case Vector3f:if(((Vector3f)states.get(state.prop).value) == (Vector3f)state.value)
	    		return true;
				break;
			default: try {
					throw new Exception("PropertyType not supported");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
    	}
    	
    		return false;
    }
    
    /**
     * Gets the value of a specific WorldStateSymbol
     * @param state
     * @return
     */
    public Object getValue(WorldStateSymbol state)
    {
    	return states.get(state.prop).value;
    }
    
    /**
     * Gets the value of a specific WorldProperty
     * @param prop
     * @return
     */
    public Object getValue(TankWorldProperty prop)
    {
    	return states.get(prop).value;
    }
    
    /**
     * Changes the current WorldStateSymbol to the given value
     * @param prop
     * @param value
     */
    public void setValue(TankWorldProperty prop, Object value)
    {
    	WorldStateSymbol s = states.get(prop);
    	
    	s.value = value;
    }
    
    public WorldState clone()
    {
    	TreeMap<TankWorldProperty, WorldStateSymbol> copy = new TreeMap<TankWorldProperty, WorldStateSymbol>();
    	
    	for(WorldStateSymbol s : states.values())
    		copy.put(s.prop, s.clone());
    	
    	return new WorldState(copy);
    }

}
