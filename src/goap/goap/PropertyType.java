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

/**
 * Enum which represents the types a worldStateProperty can be.
 * @author Klinge
 * TODO: Use reflection to determine the type at runtime so no type needs to
 * be explicitly specified
 */
public enum PropertyType
{
	Float, Boolean, NavNode, Vector3f,
}
