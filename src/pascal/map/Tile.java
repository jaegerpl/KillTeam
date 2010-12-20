package pascal.map;

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

	public Tile(boolean isWater, boolean isPassable, Vector3f normalVector) {
		this.isWater = isWater;
		this.isPassable = isPassable;
		this.normalVector = normalVector;
	}

}
