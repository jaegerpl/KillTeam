package de.lunaticsoft.combatarena.api.killteam.Movement;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

public class MovementUtils {

	static public Vector3f rotateVector(Vector3f vec, float phi){
		Vector3f result = vec.clone();
		//result.x = FastMath.cos((float) (FastMath.atan2(vec.z, vec.x)+phi));
	//	result.z = FastMath.sin((float) (FastMath.atan2(vec.z, vec.x)+phi));
		result.x =vec.x * FastMath.cos(FastMath.DEG_TO_RAD*phi) - vec.z*FastMath.sin(FastMath.DEG_TO_RAD*phi);		
		result.z =vec.z * FastMath.cos(FastMath.DEG_TO_RAD*phi) + vec.x*FastMath.sin(FastMath.DEG_TO_RAD*phi);		
		return result;
	}
}
