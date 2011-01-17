package battle;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;
import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;
import de.lunaticsoft.combatarena.objects.WorldObject;

/**
 * @author pascal
 *
 */
public class WaffenAutomat implements IWaffenAutomat {

	private IWorldObject Ziel;
	private IWaffenState stateWaffe;  
	private IWorldInstance world;
	
	private Vector3f X1,X2,X3;
	private long t1,t2,t3;
	private double ZielSpeed;
	private float Angle = 30;
	private float Force;
	
	public WaffenAutomat(IWorldInstance world){
		stateWaffe = WaffenState.Idle;
		this.world = world;
	}
	
	@Override
	public void action(IWorldObject worldObject) {
		stateWaffe.action(this, worldObject);
	}

	@Override
	public IWorldObject getZiel() {
		return Ziel;
	}

	@Override
	public void setState(IWaffenState s) {
		this.stateWaffe = s;
	}

	@Override
	public void setZiel(IWorldObject worldObject) {
		this.Ziel = worldObject;
	}

	@Override
	public void saveX1(Vector3f vector3f) {
		this.X1 = vector3f;
		this.t1 = System.nanoTime();
	}

	@Override
	public void saveX2(Vector3f vector3f) {
		this.X2 = vector3f;
		this.t2 = System.nanoTime();
	}
	
	/**
	 * Geschwindigkeit in meter/sekunde
	 * modified
	 * 
	 * @return
	 */
	public float berechneZielSpeed(){
		float distance = X2.distance(X1);
		distance = Math.abs(distance);
		
		long nanosPerSec = 1000000000;
		long time = t2 - t1;
		long faktor = nanosPerSec / time;
		float distInOneSec = distance*faktor;
		
		return (distInOneSec);
	}


	/**
	 * y = speed * time * sin(angle) - (gravity / 2) * time^2 y <br>
	 * <br>
	 * -> 0 <br>
	 * 0 = speed * time * sin(angle) - (gravity / 2) * time^2<br>
	 * <br>
	 * distance = speed * time * cos(angle) speed = distance / (time * cos(angle))<br>
	 * <br>
	 * einsetzen: <br>
	 * 0 = (distance / (time * cos(angle))) * time * sin(angle) - (gravity / 2) * time^2 <br>
	 * <br>
	 * umstellen und 'time' kuerzen: <br>
	 * 0 = (distance * (sin(angle) / cos(angle))) - (gravity / 2) * time^2 sin(angle) / cos(angle) <br>
	 * <br>
	 * -> <br>
	 * tan(angle) 0 = (distance * tan(angle)) - (gravity / 2) * time^2 | (gravity / 2) (gravity / 2) <br>
	 * <br>
	 * -> <br>
	 * 49.05f 0 = (distance * tan(angle)) - (gravity / 2) * time^2<br>
	 * <br>
	 * time^2 = (distance / 45.05f) * tan(angle) time = sqrt((distance / 45.05f) * tan(angle))<br>
	 * <br>
	 * distance = speed * time * cos(angle) umstellen: speed = distance / (cos(angle) * time)<br>
	 *
	 * @param angleDeg
	 * @param distance
	 * @return
	 */
	public static float getSpeed(float angleDeg, float distance) {
		// Bogenmass
		float angle = angleDeg / FastMath.RAD_TO_DEG;
		// gravity = 98.1f -> gravity/2 = 49.05f
		
		float time = FastMath.sqrt((distance / 49.05f) * FastMath.tan(angle));
		float speed = distance / (FastMath.cos(angle) * time);

		return speed;
	}

	@Override
	public IWorldInstance getWorld() {
		return this.world;
	}

	@Override
	public float getForce() {
		return Force;
	}

	@Override
	public float getAngle() {
		return Angle;
	}

	@Override
	public void setWorld(IWorldInstance world) {
		this.world = world;
	}

	public void shoot(IWorldObject worldObject) {
		
		boolean print = false;
		float schussWinkel = Angle;

		// berechne Ziel Geschwindigkeit
		float ZielSpeed = (float) berechneZielSpeed(); // DONE
		Vector3f ZielDirection = berechneZielDirection();

		// berechne Kugel Geschwindigkeit
		Vector3f myPosition = world.getMyPosition().clone();
		float distance = myPosition.distance(worldObject.getPosition());
		float KugelSpeed = getSpeed(schussWinkel,distance);
		this.Force = KugelSpeed;
		
		// Zeit, die Kugel braucht um Ziel zu erreichen
		float time = (float) Math.sqrt((distance / 49.05f) * Math.tan(Math.toRadians(schussWinkel)));
		
		
		// Punkt an dem sich das Ziel und Geschoss treffen
		Vector3f Punkt = worldObject.getPosition().clone();
		Vector3f treffPunkt = new Vector3f();
		
		float S = X2.distance(X1);
		S = Math.abs(S);
		
		float deltaX,deltaZ,sin,cos;
		deltaX = X2.x-X1.x;
		deltaZ = X2.z-X1.z;
		sin = deltaX / S;
		cos = deltaZ / S;
		if(print){
			System.out.println("deltaX = "+deltaX);
			System.out.println("deltaZ = "+deltaZ);
			System.out.println("sin = "+sin);
			System.out.println("cos = "+cos);
		}
		// Mit doppelter Zeit schiesst er genauer 
		float timekorrektur = 2.3f;
		time = time * timekorrektur;
//		time = time * time;
		
		float korrekturX = (ZielSpeed * time * sin);
		float korrekturZ = (ZielSpeed * time * cos);
		
		if(print){
			System.out.println("korrekturX = "+korrekturX);
			System.out.println("korrekturZ = "+korrekturZ);
		}
		
		korrekturX = Math.abs(korrekturX);
		korrekturZ = Math.abs(korrekturZ);
		
		if(X2.x > X1.x){
			treffPunkt.setX(Punkt.x + korrekturX); 
		}else{
			treffPunkt.setX(Punkt.x - korrekturX); 
		}
		
		if(X2.z > X1.z){
			treffPunkt.setZ(Punkt.z + korrekturZ);
		}else{
			treffPunkt.setZ(Punkt.z - korrekturZ);
		}
		if(print){
			System.out.println("X1  -> "+X1);
			System.out.println("X2  -> "+X2);
			System.out.println("Punkt  -> "+Punkt);
			System.out.println("treffPunkt -> "+treffPunkt);
		}
		Vector3f direction = world.getMyPosition().clone().subtract(treffPunkt).negate();

//		System.out.println("treffPunkt -> "+treffPunkt);
//		berechne neue Kugel Geschwindigkeit
		distance = myPosition.distance(treffPunkt);
		KugelSpeed = getSpeed(schussWinkel,distance);
		this.Force = KugelSpeed;
		if(print){
			System.out.println("distance = "+distance);
		}
		
		world.shoot(direction, Force, Angle);
	}
	
	
	/**
	 * Berechnet die Richtung, in die sich das Ziel bewegt
	 * 
	 * @return
	 */
	private Vector3f berechneZielDirection() {
		return X2.clone().subtract(X1).normalize().clone();
	}


	public void shoot2(IWorldObject target){
		float time = 1f; // halbe sekunde nach dem messen soll das ziel getroffen werden
		float angle = 30;
		float targetSpeed = berechneZielSpeed();
		Vector3f targetDirection = berechneZielDirection();
		Vector3f hitPosition = X2.clone();
		hitPosition.addLocal(targetDirection.mult(targetSpeed*time));
		
		Vector3f myPosition = world.getMyPosition().clone();
		float distance = myPosition.distance(hitPosition);
		float force = getSpeed(angle, distance);
		
		Vector3f shootDirection = hitPosition.subtract(myPosition);
		world.shoot(shootDirection, force, angle);
		
	}
}
