package debug;

import java.io.Serializable;

public class SimpleObject implements Serializable{
	
	public int durability;
	public int type;
	public int x;
	public int y;
	
	public SimpleObject(int type, int durability, int x, int y){
		this.durability = durability;
		this.type = type;
		this.x = x;
		this.y = y;
	}

}
