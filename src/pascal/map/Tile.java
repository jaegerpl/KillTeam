package pascal.map;

import java.awt.Point;

import com.jme.math.Vector3f;

/**
 * 
 * A collection of world information
 * <li> Water true/false
 * <li> normalVector
 * <li> isPassable true/false
 * <br><br>
 * @author Pascal Jaeger
 *
 */
public class Tile {
	
	public final boolean isWater;
	public final boolean isPassable;
	public final Vector3f normalVector;
	public final Point mapIndex;
	public final Vector3f tileCenterCoordinates;

	public Tile(Point position, boolean isWater, boolean isPassable, Vector3f normalVector) {
		this.mapIndex = position;
		this.isWater = isWater;
		this.isPassable = isPassable;
		this.normalVector = normalVector;
		this.tileCenterCoordinates = WorldMap.getTileCenterCoordinates(mapIndex);
	}
	
	public Tile deepCopy(){
		return new Tile(mapIndex, isWater, isPassable, normalVector);
	}

}
