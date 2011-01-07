package memory.objectStorage;

import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.enumn.EColors;
import de.lunaticsoft.combatarena.api.enumn.EObjectTypes;
import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;

public class MemorizedWorldObject {
	protected EColors color;
	protected EObjectTypes type;
	protected Vector3f position;
	protected String uniqueIdentifier;
	
	private int durability;

	public MemorizedWorldObject(IWorldObject worldObject) {
		this.color = worldObject.getColor();
		this.position = worldObject.getPosition();
		this.type = worldObject.getType();
		
		if(this.type == EObjectTypes.Competitor)
			this.durability = 5000;
		else
			this.durability = 0;
	}

	public EColors getColor() {
		return this.color;
	}
	
	public Vector3f getPosition() {
		return this.position;
	}
	
	public EObjectTypes getType() {
		return this.type;
	}
	
	synchronized public int getDurability(){
		return this.durability;
	}
	
	synchronized public void decreaseDurability(int value){
		this.durability -= value;
	}
}
