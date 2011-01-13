package battle;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;
import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;

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
//		System.out.println("Ziel x1= "+Ziel.getPosition());
	}

	@Override
	public void saveX2(Vector3f vector3f) {
		this.X2 = vector3f;
		this.t2 = System.nanoTime();
//		System.out.println("Ziel x2= "+Ziel.getPosition());
//		System.out.println("X2 = "+X2);
	}
	
	public double berechneZielSpeed(){
		float distance = X2.distance(X1);
		distance = Math.abs(distance);
		
		long time = t2 - t1;
		double Deltat = time / 1000000;
		Deltat = Deltat / 1000;
		
		
		return (distance / Deltat);
	}

	@Override
	public Vector3f getX3() {
		float schussWinkel = Angle;
		double VorschussX,VorschussZ;
		
//		berechne Ziel Geschwindigkeit
		berechneZielSpeed();
		
//		berechne Kugel Geschwindigkeit
		Vector3f pos = world.getMyPosition().clone();
		float distance = pos.distance(X2);
		float KugelSpeed = getSpeed(schussWinkel,distance);
		this.Force = KugelSpeed;
		
//		System.out.println("\t\t KugelSpeed = "+KugelSpeed);
//		System.out.println("\t\t ZielSpeed = "+ZielSpeed);
		
		Vector3f flugbahn = pos.clone();
		pos.subtract(X2, flugbahn);
//		Richtung, in die das Ziel sich bewegt.
		Vector3f ZielRichtung = X1.clone();
		X2.subtract(X1, ZielRichtung);
		
//		Winkel der Bewegung von Ziel
		float angle = ZielRichtung.angleBetween(flugbahn);
		
//		Zeit, die Kugel braucht um Ziel zu erreichen
		double KugelTime = distance / (KugelSpeed * Math.cos(Math.toRadians(schussWinkel)));
		
//		System.out.println("\t\t flugbahn = "+flugbahn);
//		System.out.println("\t\t ZielRichtung = "+ZielRichtung);

//		System.out.println("\t\t distance = "+distance);
//		System.out.println("\t\t angle = "+angle);
//		System.out.println("\t\t KugelTime = "+KugelTime);
		
		VorschussX = ZielSpeed * KugelTime * Math.sin(angle);
		VorschussZ = ZielSpeed * KugelTime * Math.cos(angle);
		
//		System.out.println("\t\t VorschussX = "+VorschussX);
//		System.out.println("\t\t VorschussZ = "+VorschussZ);
		
//		X3 = X2.clone();
//		X3.setX((float) (X3.x + VorschussX));
//		X3.setZ((float) (X3.z + VorschussZ));
		X3 = new Vector3f();
		X3.setX((float) VorschussX);
		X3.setZ((float) VorschussZ);
		
//		System.out.println("X3 vor = "+X3);
//		if(X2.x > X1.x){
//			X3.setX((float) (X3.x + VorschussX));
//		}else{
//			X3.setX((float) (X3.x - VorschussX));
//		}
//		
//		if(X2.z > X1.z){
//			X3.setZ((float) (X3.z + VorschussZ));
//		}else{
//			X3.setZ((float) (X3.z - VorschussZ));
//		}
//		System.out.println("X3 = "+X3);
		return X3;
	}

	public static float getSpeed(float angleDeg, float distance) {
		// Bogenmaß
		float angle = angleDeg / FastMath.RAD_TO_DEG;
		// gravity = 98.1f -> gravity/2 = 49.05f
		/**
		 * y = speed * time * sin(angle) - (gravity / 2) * time^2 y -> 0 0 =
		 * speed * time * sin(angle) - (gravity / 2) * time^2
		 * 
		 * distance = speed * time * cos(angle) speed = distance / (time *
		 * cos(angle))
		 * 
		 * einsetzen: 0 = (distance / (time * cos(angle))) * time * sin(angle) -
		 * (gravity / 2) * time^2 umstellen und 'time' kürzen: 0 = (distance *
		 * (sin(angle) / cos(angle))) - (gravity / 2) * time^2 sin(angle) /
		 * cos(angle) -> tan(angle) 0 = (distance * tan(angle)) - (gravity / 2)
		 * * time^2 | (gravity / 2) (gravity / 2) -> 49.05f 0 = (distance *
		 * tan(angle)) - (gravity / 2) * time^2
		 * 
		 * time^2 = (distance / 45.05f) * tan(angle) time = sqrt((distance /
		 * 45.05f) * tan(angle))
		 * 
		 * distance = speed * time * cos(angle) umstellen: speed = distance /
		 * (cos(angle) * time)
		 * 
		 */
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

	public void shoot2(IWorldObject worldObject) {
		boolean print = false;
		float schussWinkel = Angle;
//		berechne Ziel Geschwindigkeit
		float ZielSpeed = (float) berechneZielSpeed();

//		berechne Kugel Geschwindigkeit
		Vector3f pos = world.getMyPosition().clone();
		float distance = pos.distance(worldObject.getPosition());
		float KugelSpeed = getSpeed(schussWinkel,distance);
		this.Force = KugelSpeed;
		
//		Zeit, die Kugel braucht um Ziel zu erreichen
		float time = (float) Math.sqrt((distance / 49.05f) * Math.tan(Math.toRadians(schussWinkel)));
		
		
//		Punkt an dem sich das Ziel und Geschoss treffen
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
//		Mit doppelter Zeit schiesst er genauer 
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
		pos = world.getMyPosition().clone();
		distance = pos.distance(treffPunkt);
		KugelSpeed = getSpeed(schussWinkel,distance);
		this.Force = KugelSpeed;
		if(print){
			System.out.println("distance = "+distance);
		}
		
		world.shoot(direction, Force, Angle);
	}
	@Override
	public void shoot(IWorldObject worldObject) {
		boolean print = false;
		float schussWinkel = Angle;
		float VorschussX,VorschussZ;
		

//		berechne Ziel Geschwindigkeit
		float ZielSpeed = (float) berechneZielSpeed();
		if(print){
			System.out.println("ZielSpeed = "+ZielSpeed);
		}
//		berechne Kugel Geschwindigkeit
		Vector3f pos = world.getMyPosition().clone();
		float distance = pos.distance(worldObject.getPosition());
		float KugelSpeed = getSpeed(schussWinkel,distance);
		this.Force = KugelSpeed;
		if(print){
			System.out.println("KugelSpeed = "+KugelSpeed);
		}
		
		float angle2 = this.getAngleGrad(pos, X1, X2);
		
//		System.out.println("angle2 �= "+angle2);

//		Zeit, die Kugel braucht um Ziel zu erreichen
		float time = (float) Math.sqrt((distance / 49.05f) * Math.tan(Math.toRadians(schussWinkel)));
		float KugelTime = (float) (distance / KugelSpeed * Math.cos(Math.toRadians(schussWinkel)));
		if(print){
			System.out.println("KugelTime = "+KugelTime);
			System.out.println("time = "+time);
		}
		


		angle2 = angle2 * FastMath.DEG_TO_RAD;
		VorschussX = ZielSpeed * time * FastMath.sin(angle2);
		VorschussZ = ZielSpeed * time * FastMath.cos(angle2);
		
		
		VorschussX = Math.abs(VorschussX);
		VorschussZ = Math.abs(VorschussZ);
		
		if(print){
			System.out.println("VorschussX = "+VorschussX);
			System.out.println("VorschussZ = "+VorschussZ);
		}
//		Punkt an dem sich das Ziel und Geschoss treffen
		Vector3f Punkt = worldObject.getPosition().clone();
		Vector3f treffPunkt = new Vector3f();
//		System.out.println("Punkt  -> "+Punkt);
//		System.out.println("VorschussX = "+VorschussX);
//		System.out.println("VorschussZ = "+VorschussZ);
//		System.out.println("treffPunkt vor setX -> "+treffPunkt);
		
//		float x = Punkt.getX() - VorschussX;
//		float z = Punkt.getZ() - VorschussZ;
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
		
//		System.out.println("x="+x);
//		System.out.println("z="+z);
//		System.out.println("treffPunkt nach setX -> "+treffPunkt);
		Vector3f direction = world.getMyPosition().clone().subtract(treffPunkt).negate();
		if(print){
			System.out.println("direction = "+direction);
		}

//		berechne Kugel Geschwindigkeit
		pos = world.getMyPosition().clone();
		distance = pos.distance(treffPunkt);
		float KugelSpeed2 = getSpeed(schussWinkel,distance);
		
		float speed = 0;
//		System.out.println("distance = "+distance);
		float alfa = schussWinkel * FastMath.DEG_TO_RAD;
		speed = (distance * 98.1f)/(2*FastMath.cos(alfa)*FastMath.sin(alfa));
//		System.out.println("Speed vor sqrt= "+speed);
		speed = FastMath.sqrt(speed);
//		System.out.println("Speed = "+speed);
//		System.out.println("KugelSpeed2 = "+KugelSpeed2);
//		this.Force = KugelSpeed2;

//		System.out.println("treffPunkt vor optimierung -> "+treffPunkt);
		
		treffPunkt = this.optimaleRichtung(KugelSpeed, KugelSpeed2, angle2, distance, ZielSpeed, worldObject,treffPunkt,10);
		
		
		direction = world.getMyPosition().clone().subtract(treffPunkt).negate();

//		System.out.println("treffPunkt nach optimierung -> "+treffPunkt);
//		berechne neue Kugel Geschwindigkeit
		pos = world.getMyPosition().clone();
		distance = pos.distance(treffPunkt);
		KugelSpeed2 = getSpeed(schussWinkel,distance);
		this.Force = KugelSpeed2;
		
		
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
