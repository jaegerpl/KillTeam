package fabian;

import com.jme.math.Vector3f;

public class ShootTarget {
	public final Vector3f direction;
	public final float force;
	public final float angle;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(angle);
		result = prime * result
				+ ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + Float.floatToIntBits(force);
		return result;
	}
	@Override
	public String toString() {
		return "ShootTarget [direction=" + direction + ", distance=" + force
				+ ", angle=" + angle + "]";
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ShootTarget other = (ShootTarget) obj;
		if (Float.floatToIntBits(angle) != Float.floatToIntBits(other.angle))
			return false;
		if (direction == null) {
			if (other.direction != null)
				return false;
		} else if (!direction.equals(other.direction))
			return false;
		if (Float.floatToIntBits(force) != Float
				.floatToIntBits(other.force))
			return false;
		return true;
	}
	public ShootTarget(Vector3f direction, float distance, float angle) {
		super();
		this.direction = direction;
		this.force = distance;
		this.angle = angle;
	}

}
