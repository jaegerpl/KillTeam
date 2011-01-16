package battle;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;
import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;

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
	public double berechneZielSpeed(){
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
	
	
	public Vector3f optimaleRichtung(
			float kugelspeed1,
			float kugelspeed2,
			float angle,
			float distance,
			float zielspeed,
			IWorldObject worldObject,
			Vector3f treffpunkt, int count){
		
		Vector3f treffPunkt = treffpunkt;
		float KugelSpeedNew=0;
		float schussWinkel=Angle;
		float kugelspeedMittel;
		float VorschussX=0,VorschussZ = 0;
		float KugelTimeMittel;
		
//		kugelspeedMittel = (kugelspeed1 + kugelspeed2)/2;
		kugelspeedMittel = kugelspeed1;
		KugelSpeedNew = kugelspeed2;
		
		while(count!=0){
			treffPunkt = new Vector3f();
			kugelspeedMittel = (kugelspeedMittel + KugelSpeedNew) / 2;
			
//			KugelTimeMittel = (float) (distance / kugelspeedMittel * Math.cos(Math.toRadians(schussWinkel)));
			KugelTimeMittel = (float) Math.sqrt((distance / 49.05f) * Math.tan(Math.toRadians(schussWinkel)));
			VorschussZ = zielspeed * KugelTimeMittel * FastMath.sin(angle);
			VorschussX = zielspeed * KugelTimeMittel * FastMath.cos(angle);
			
			
			VorschussX = Math.abs(VorschussX);
			VorschussZ = Math.abs(VorschussZ);
			
//			Punkt an dem sich das Ziel und Geschoss treffen
			Vector3f Punkt = worldObject.getPosition().clone();
		    
//		    float x = Punkt.getX() - VorschussX;
//			float z = Punkt.getZ() - VorschussZ;
			float x = 0;
			float z = 0;
			if(X2.x == X1.x){
				x = Punkt.getX();
			}else if(X2.x > X1.x){
				x = Punkt.getX() + VorschussX;
			}else{
				x = Punkt.getX() - VorschussX;
			}
			
			if(X2.z == X1.z){
				z = Punkt.getZ();
			}else if(X2.z > X1.z){
				z = Punkt.getZ() + VorschussZ;
			}else{
				z = Punkt.getZ() - VorschussZ;
			}
			treffPunkt.setX(x);
			treffPunkt.setY(Punkt.getY());
			treffPunkt.setZ(z);
			
			distance = world.getMyPosition().clone().distance(treffPunkt);
			
			KugelSpeedNew = getSpeed(schussWinkel,distance);
//			this.Force = (Force + KugelSpeedNew) / 2;
			
			count--;
		}
//		System.out.println("VorschussZ = "+VorschussZ);
//		System.out.println("VorschussX = "+VorschussX);
//		System.out.println("treffPunkt = "+treffPunkt);
		return treffPunkt;
		
	}
	
	public float getAngleGrad(Vector3f me,Vector3f x1,Vector3f x2){
		float a,b,c,cosAlfa,Alfa=0,tgAlfa;
		
//		System.out.println("me = "+me);
//		System.out.println("x1 = "+x1);
//		System.out.println("x2 = "+x2);
		
		c = (me.x - x2.x)*(me.x - x2.x) + (me.z - x2.z)*(me.z - x2.z);
		c = FastMath.sqrt(c);
		
		a = (me.x - x1.x)*(me.x - x1.x) + (me.z - x1.z)*(me.z - x1.z);
		a = FastMath.sqrt(a);
		
		b = (x1.x - x2.x)*(x1.x - x2.x) + (x1.z - x2.z)*(x1.z - x2.z);
		b = FastMath.sqrt(b);
		
		cosAlfa = (b*b + c*c - a*a)/(2*b*c);
		
		tgAlfa = 1/(cosAlfa*cosAlfa) - 1;
		tgAlfa = FastMath.sqrt(tgAlfa);
		
		Alfa = FastMath.atan(tgAlfa);
		
		Alfa = Alfa*FastMath.RAD_TO_DEG;
		Alfa = 180 - Alfa;

		
		float alfa2,sinalfa;
		sinalfa = 1 - cosAlfa*cosAlfa;
		sinalfa = FastMath.sqrt(sinalfa);
		
		alfa2 = FastMath.atan2(sinalfa, cosAlfa);
		alfa2 = alfa2*FastMath.RAD_TO_DEG;
		alfa2 = 180 - alfa2;
//		System.out.println("alfa2 = "+alfa2);
		return alfa2;
	}
}
