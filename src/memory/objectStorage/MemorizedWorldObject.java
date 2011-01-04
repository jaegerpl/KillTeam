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
	
	public int getDurability(){
		return this.durability;
	}
	
	public void startDegeneration(ObjectStorage storage){
		//movable objects can degenerate
		if(this.type == EObjectTypes.Competitor){
			Thread t = new Thread(new DegenerationThread(storage, this));
			t.start();
		}
	}
	
	private class DegenerationThread implements Runnable{
		ObjectStorage storage;
		MemorizedWorldObject object;
		
		public DegenerationThread(ObjectStorage storage, MemorizedWorldObject object){
			this.object = object;
			this.storage = storage;
		}
		
		@Override
		public void run() {
			
			//as long as object is durable decrease durability
			while (this.object.durability > 0){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//decrease
				this.object.durability -= 1000;
			}
			//remove the object as it's not durable anymore
			this.storage.removeObject(this.object);
		}
		
	}
}
