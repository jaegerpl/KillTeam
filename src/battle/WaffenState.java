package battle;

import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;

public enum WaffenState implements IWaffenState {
	
	Idle{

		@Override
		public void action(IWaffenAutomat a,IWorldObject worldObject) {
			a.saveX1(worldObject.getPosition());
			a.setState(Zielen);
//			System.out.println("I am idle");
//			if(a.getZiel()==null){
//				a.setZiel(worldObject);
//				a.saveX1(worldObject.getPosition());
//				a.setState(Zielen);
////				System.out.println("action - Idle - if");
//			}else{
////				System.out.println("action - Idle - else");
//			}
		}},
	Zielen{

			int count = 0;
		@Override
		public void action(IWaffenAutomat a,IWorldObject worldObject) {
//			System.out.println("Ich ziele");
//			if(count%0==0){
				a.saveX2(worldObject.getPosition());
				a.setState(Schiessen);
//			}
//			count++;
//			if(a.getZiel().equals(worldObject)){
//				if(count%5==0){
//					a.saveX2(worldObject.getPosition());
//					a.setState(Schiessen);
////					System.out.println("action - Zielen - if");
//				}
//				count++;
//			}else{
//				
//			}
			
		}},
	Schiessen{
			

		@Override
		public void action(IWaffenAutomat a,IWorldObject worldObject) {
//			System.out.println("Ich schiesse");
			a.shoot2(worldObject);
			a.setState(Idle);	
//			if(a.getZiel().equals(worldObject)){
//				
//					a.shoot2(worldObject);
//					a.setState(Idle);	
//					a.setZiel(null);
//	//				System.out.println("action - Schiessen - if");
//				
//			}else{
////				System.out.println("action - Schiessen - else");
//			}
		}},
		
		Nachladen{
			long lastShoot=0;
			private long fireRate = 2000000000;
			@Override
			public void action(IWaffenAutomat a, IWorldObject worldObject) {
//				System.out.println("Nachladen");
				
				long now = System.nanoTime();
				if (lastShoot > now - fireRate) {
					return;
				}
				lastShoot = now;
				
				a.setState(Idle);	

			}}
		;
}
