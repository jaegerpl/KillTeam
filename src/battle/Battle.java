package battle;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;

public class Battle {
	/**
	 * @param target
	 * @param myPos
	 * @return ShootTarget Klasse, welche alle ben�tigten Daten f�r ein world.shoot enth�lt
	 */
	public static ShootTarget getShootTarget(final Vector3f target, final Vector3f myPos) {
		final float angle = 45;

		final Vector3f direction = myPos.clone().subtractLocal(target.clone()).negate();
		final float distance = myPos.distance(target);
		return new ShootTarget(direction, calcForce(angle, distance), angle);
	}
	
	public static float calcForce(float angleDeg, float distance) {
		float angle = angleDeg / FastMath.RAD_TO_DEG;
		float time = FastMath.sqrt((distance / 49.05f) * FastMath.tan(angle));
		float speed = distance / (FastMath.cos(angle) * time);
		return speed;
	}
	
	public static double getMaxForce(){
		return 120;
	}
	
	public static double getAccelerationOfGravity(){
		return 91.1f; //laut ki vorlage
	}
	
	
	public static double getMaxDistance(){
		return (Math.pow(getMaxForce(), 2))/getAccelerationOfGravity() * Math.sin(2*45); 
	}


}
