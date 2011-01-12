package de.lunaticsoft.combatarena.api.testteam;

import java.util.ArrayList;
import java.util.List;

import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.enumn.EColors;
import de.lunaticsoft.combatarena.api.interfaces.IPlayer;
import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;
import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;
import fabian.Battle;
import fabian.ShootTarget;

public class Defender implements IPlayer {
	List<Vector3f> route = new ArrayList<Vector3f>();
	private IWorldInstance world;
	private Vector3f lastPos;
	private Vector3f startPos;
	private int DestIndex = 0;

	@Override
	public void update(float interpolation) {

		if (route.isEmpty()) { // => sind noch dabei aus dem Hangar
								// herauszufahren
			world.move(world.getMyDirection());
			if (lastPos != null) {
				// prüfen ob wir uns letzte runde bewegt haben, falls nicht
				// hängen wir irgendwo fest => richtung wechseln
				if (world.getMyPosition().distance(lastPos) < 0.01f) {
					world.move(world.getMyDirection().negate());
				}
			}
			if (world.getMyPosition().distance(startPos) > 25) {
				world.stop();

				// route berechnen

				float distance = world.getMyPosition().distance(startPos);
				double x = 0d; // real part
				double z = 0d; // imaginary part
				for (int angle = 0; angle < 360; angle += 10) {
					x = distance * Math.cos(angle);
					z = distance * Math.sin(angle);

					Vector3f d = startPos.clone();
					d.x += x;
					d.z += z;

					route.add(d);
				}
			}
		}
		// fahre berechnete Route ab
		else {
			// use the pracalculated route to drive around our hangar
			Vector3f d = route.get(DestIndex).clone();

			// check if we reached our waypoint
			if (world.getMyPosition().distance(d) < 0.5f) {
				System.out.println("waypoint erreicht:" + route.get(DestIndex));
				DestIndex = (DestIndex + 1) % route.size();
			}

			world.move(d.subtract(world.getMyPosition()));

		}
		lastPos = world.getMyPosition();

	}

	@Override
	public void setWorldInstance(IWorldInstance world) {
		this.world = world;

	}

	@Override
	public void attacked(IWorldObject competitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void die() {
		// TODO Auto-generated method stub

	}

	@Override
	public void perceive(ArrayList<IWorldObject> worldObjects) {
		for (IWorldObject worldObject : worldObjects) {
			switch (worldObject.getType()) {
			case Competitor:
				ShootTarget target = Battle.getShootTarget(
						worldObject.getPosition(), world.getMyPosition());
				world.shoot(target.direction, target.force, target.angle);

			}
		}

	}

	@Override
	public void collected(IWorldObject worldObject) {
		// TODO Auto-generated method stub

	}

	@Override
	public void spawn() {
		startPos = world.getMyPosition();

	}

	@Override
	public void setColor(EColors color) {
		// TODO Auto-generated method stub

	}

}
