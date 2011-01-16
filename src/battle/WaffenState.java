package battle;

import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;

public enum WaffenState implements IWaffenState {
	
	Idle{

		@Override
		public void action(IWaffenAutomat a,IWorldObject worldObject) {
			a.saveX1(worldObject.getPosition());
			a.setState(Zielen);
		}},
	Zielen{
			
		@Override
		public void action(IWaffenAutomat a,IWorldObject worldObject) {
				a.saveX2(worldObject.getPosition());
				a.setState(Schiessen);
		}},
	Schiessen{
			
		@Override
		public void action(IWaffenAutomat a,IWorldObject worldObject) {
			a.shoot(worldObject);
			a.setState(Idle);	
		}}
		;
}
