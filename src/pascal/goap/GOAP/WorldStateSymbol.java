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

/**
 * A WorldStateSymbol is a key/value pair which represents
 * a condition the world is currently in
 * @author Klinge
 */
public class WorldStateSymbol<T> implements Cloneable {

    public TankWorldProperty prop;
    public T value;
    private PropertyType type;

    //TODO: sollte auch andere typen als nur boolean unterstützen
    public WorldStateSymbol(TankWorldProperty prop, T value, PropertyType type)
    {
        this.prop = prop;
        this.value = value;
        this.type = type;
    }
    
    public PropertyType getType()
    {
    	return type;
    }
    
    public boolean equals(Object o)
    {
    	return (this.prop == ((WorldStateSymbol)o).prop);
    }
    
    public WorldStateSymbol<T> clone()
    {
    	return new WorldStateSymbol(prop, value, type);
    }
}
