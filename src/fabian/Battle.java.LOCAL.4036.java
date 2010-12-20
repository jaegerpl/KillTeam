package fabian;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;

public class Battle {
	public static ShootTarget getShootTarget(IWorldObject target, Vector3f myPos) {
		Vector3f direction = myPos.subtract(target.getPosition()).negateLocal();
		float distance = myPos.distance(target.getPosition());
		float force = 120f; // max
		// float angle = getAngle(force, ) //???
		/*
		 * if (distance < 50) { world.move(direction); } else if (distance > 54)
		 * { world.move(direction.negateLocal()); } else { world.stop(); stop =
		 * true; world.shoot(direction.negateLocal(), distance, 1f); }
		 */
		float angle = 0f;
		return new ShootTarget(direction, distance, angle);

	}

	float getTime(float distance, float angleDeg) {
		float angle = angularToRadian(angleDeg);
		return FastMath.sqrt((distance / 49.05f) * FastMath.tan(angle));
	}

	float angularToRadian(float angleDeg) {
		return angleDeg / FastMath.RAD_TO_DEG;
		
	}

	float getAngle(float force, float time, float distance) {
		return FastMath.acos((force * time) / distance);
	}

	float getForce(float distance, float angleDeg, float time) {
		float angle = angularToRadian(angleDeg);

		return distance / (FastMath.cos(angle) * time);
	}

}
