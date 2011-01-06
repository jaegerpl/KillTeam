package debug;

import java.io.Serializable;

public class SimpleTile implements Serializable{
	public boolean isWater;
	public boolean isPassable;
	public boolean isExplored;
	
	public int x;
	public int y;
	
	public SimpleTile(int pX, int pY, boolean water, boolean passable, boolean explored){
		x = pX;
		y = pY;
		
		isWater= water;
		isPassable = passable;
		isExplored = explored;
	}
}
