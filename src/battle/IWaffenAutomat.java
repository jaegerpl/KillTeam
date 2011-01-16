package battle;

import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;
import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;

public interface IWaffenAutomat {
	public IWorldObject getZiel();
	public void setZiel(IWorldObject worldObject);
	
	public void setState(IWaffenState s);
	
	public void action(IWorldObject worldObject);
	
	public void saveX1(Vector3f vector3f);
	public void saveX2(Vector3f vector3f);
	public void shoot(IWorldObject worldObject);
	
	public IWorldInstance getWorld();
	public float getForce();
	public float getAngle();
	public void setWorld(IWorldInstance world);
}
