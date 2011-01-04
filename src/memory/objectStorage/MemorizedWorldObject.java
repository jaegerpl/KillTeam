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

	public MemorizedWorldObject(IWorldObject worldObject) {
		this.color = worldObject.getColor();
		this.position = worldObject.getPosition();
		this.type = worldObject.getType();
		this.uniqueIdentifier = "#" + color + "#" + type + "#" + worldObject.hashCode() + "#" + worldObject.toString();
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
	
	public String getUniqueIdentifier() {
		return this.uniqueIdentifier;
	}
}
