package pascal.map;

import java.awt.Point;

import com.jme.math.Vector3f;

public class EmptyTile extends Tile {

	public EmptyTile(Point pos) {
		super(pos, false, false, new Vector3f(0,0,0));
	}
	
	@Override
	public Tile deepCopy(){
		return new EmptyTile(mapIndex);
	}

}
