package de.lunaticsoft.combatarena.api.killteam;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

import map.fastmap.FastRoutableWorldMap;
import map.fastmap.LinkedTile;
import memory.map.MemorizedMap;
import memory.objectStorage.MemorizedWorldObject;
import memory.objectStorage.ObjectStorage;
import memory.pathcalulation.Path;
import battle.IWaffenAutomat;
import battle.WaffenAutomat;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.enumn.EColors;
import de.lunaticsoft.combatarena.api.enumn.EObjectTypes;
import de.lunaticsoft.combatarena.api.interfaces.IPlayer;
import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;
import de.lunaticsoft.combatarena.api.interfaces.IWorldObject;
import de.lunaticsoft.combatarena.api.killteam.globalKI.GlobalKI;
import de.lunaticsoft.combatarena.api.killteam.globalKI.StatusType;
import de.lunaticsoft.combatarena.objects.WorldObject;

public class KillKI_new implements IPlayer {
	private EColors color;
	private final String name;
	private IWorldInstance world;
	private final TankBlackboard blackboard;
	private final GlobalKI globalKI;
	private ArrayList<IWorldObject> perceivedObjects;
	private final ObjectStorage objectStorage;
	private final MemorizedMap map;
	private Vector3f spawnPos;
	// aktuelle Position des Tanks
	private Vector3f curDirection;
	private Vector3f curPos;
	private LinkedTile curTile;
	// route des tanks
	private Path<LinkedTile> path;
	private LinkedTile moveTarget;
	private boolean pathReset;
	private LinkedTile lastPathTarget; //ziel der letzten Routenberechnung


	private Vector3f flagPos;
	
	private long updateNr;

	// unknown
	private int viewRangeRadius;
	private int viewRangeOffset;
	private boolean calibrated;
	private IWaffenAutomat waffenAutomat;
	private int WOExistanceUpdate = 0;
	private final Queue<Vector3f> lastPositions;

	
	//CTF Stuff
	private boolean iHaveTheFlag = false;
	private boolean flagCollectet = false;
	private boolean CTFmode = false; //muss true sein wenn CTF gespielt wird
	private boolean flagPosChanged;
	
	//Random f�r unstuck in update()
	private java.util.Random rand;
	private long stoppedTimeStamp;
	

	private void evalNextTask() {
		if (blackboard.curTask == Task.DEFEND)
			return;
		else if (iHaveTheFlag && flagCollectet) {
			blackboard.curTask = Task.GoToBase;
		} else if (blackboard.curTask == Task.EXPLORE) {
			if (CTFmode) {
				if (pathToFlagKnown()) {
					blackboard.curTask = Task.CTF;
				}
				else
					blackboard.curTask = Task.EXPLORE;
			} else if (!objectStorage.getEnemyHangars().isEmpty()) {
				blackboard.curTask = Task.LOOT_AND_BURN_HANGAR;

			}
		}
	}
	private boolean pathToFlagKnown() {
		if (map.calculatePath(map.getTileAtCoordinate(curPos), map.getTileAtCoordinate(flagPos)).isEmpty())
			return false;
		return true;
	}

	public KillKI_new(final String name, final GlobalKI globalKI,
			final Task task) {
		this.name = name;
		this.blackboard = new TankBlackboard();
		this.globalKI = globalKI;
		this.map = globalKI.getWorldMap();
		this.objectStorage = globalKI.getObjectStorage();
		this.blackboard.curTask = task;
		this.path = new Path<LinkedTile>();
		pathReset = true;
		calibrated = false;
		
		lastPositions = new LinkedBlockingQueue<Vector3f>(2);
		rand = new java.util.Random();

	}

	// pr�ft ob Tank sich momentan auf moveTarget befindet
	private boolean arrivedAtMoveTarget() {

		if ((moveTarget != null) || (curTile != null))
			return curTile.equals(moveTarget);
		else
			return false;
	}
	private void checkWorldObjectExistance() {
		// tiles die der tank sehen kann
		final List<LinkedTile> viewTiles = map.getTilesPossiblyInViewRange(world.getMyPosition().clone());

		// Objekte die laut Map im Sichtbereich sein sollten
		final Set<MemorizedWorldObject> mapObjects = objectStorage.getObjectsAtTiles(viewTiles);

		// entferne alle Objekte aus mapObjects die perceived wurden
		for (final IWorldObject obj : perceivedObjects) {
			MemorizedWorldObject memo = new MemorizedWorldObject(obj);
			if (mapObjects.contains(memo)) {
				mapObjects.remove(memo);
			}
		}

		// die verbleibenden Objekte in mapObjects haetten perceived werden
		// sollen, wurden aber nicht
		// Loesche sie im ObjectStorage
		for (final MemorizedWorldObject obj : mapObjects) {
			objectStorage.removeObject(obj);
		}
	}

	@Override

	public void attacked(final IWorldObject competitor) {
		final Vector3f enemy = competitor.getPosition();
		String out = "Attacked by position " + enemy;
		waffenAutomat.action(competitor);


		// GOAP STUFF
		blackboard.hitsTaken++;
	}

	/**
	 * berechnet neuen path zu target, wenn ein ung�ltiger Pfad berechnet oder
	 * das ziel nicht betretbar istwird pathReset = true gesetzt nachdem ein
	 * neuer pfad gesetzt wurde ist moveTarget der n�chste Wegpunkt
	 * 
	 * @param target
	 */
	private void calcPathTo(final LinkedTile target) {
		lastPathTarget = target;
		// wenn tile nicht passable ist zudem ein Pfad berechnet werden soll,
		// nix tun
		if (!target.isPassable) {
			pathReset = true;
			return;
		}
		if (pathReset) {
			path = map.calculatePath(curTile, target);
			pathReset = false;
		}
		/*
		 * if (myPosTile.equals(moveTarget)) //TODO muss nach update {
		 * moveTarget = path.getNextWaypoint(); }
		 */
		if (path.isEmpty()) {
			pathReset = true;
		}
		moveTarget = path.getNextWaypoint();
	}

	private void calibrate() {
		final Vector3f myPosition = world.getMyPosition();
		final Vector3f myDirection = world.getMyDirection();
		final Vector3f incrementor = myDirection.normalize();
		Vector3f currPos = myPosition;
		int counterFront = 0;
		do {
			currPos = currPos.add(incrementor);
			counterFront++;
		} while (world.getTerrainNormal(currPos) != null);
		counterFront--;
		currPos = myPosition;
		int counterBack = 0;
		do {
			currPos = currPos.subtract(incrementor);
			counterBack++;
		} while (world.getTerrainNormal(currPos) != null);
		counterBack--;
		this.viewRangeRadius = (counterFront + counterBack) / 2;
		this.viewRangeOffset = this.viewRangeRadius - counterBack;
		System.out.println("Radius: " + viewRangeRadius);
		System.out.println("Offset: " + viewRangeOffset);
		System.out.println();
		System.out.println("Front: " + counterFront);
		System.out.println("Back: " + counterBack);
		this.calibrated = true;
	}

	@Override
	public void collected(final IWorldObject worldObject) {
		switch (worldObject.getType()) {
		case Item:
			if (blackboard.spottedToolBox != null) {
				if (blackboard.spottedToolBox.getPosition() == worldObject
						.getPosition()) {
					blackboard.spottedToolBox = null;
					blackboard.toolBoxCollected = true;
					// TODO update object storage
				}
			}
			break;
		default:
			// DO NOTHING
			break;
		}
	}

	@Override
	public void die() {
		LinkedTile currentTile = this.map.getTileAtCoordinate(this.curPos);
		if(currentTile != null && currentTile.isPassable){
			final WorldObject wO = new WorldObject(null, color, curPos,
					EObjectTypes.Item);
			this.objectStorage.storeObject(wO.getPosition(),
					new MemorizedWorldObject(wO));
		}
		// GOAP STUFF
		globalKI.removeTank(this); // deregister tank in globalKI
	}

	private void explore() {
		if (pathReset) {
			System.out.println("berechen neues explore ziel");
			// n�chstes ZIel am ende des Sichtbereiches in tile umwandeln
			final Vector3f targetPos = this.curPos.add(curDirection.normalize()
					.mult(60));
			final LinkedTile targetTile = map.getTileAtCoordinate(targetPos);
			if (!targetTile.isExplored()) {
				if (targetTile.isPassable()) {
					// Wenn tile noch nicht erkundet + betretbar ist, dieses
					// tile
					// als neues Routenziel benutzen:
					pathReset = true;
					calcPathTo(targetTile);
				}
				// wenn kein g�ltiger Pfad berechnet werden konnte
				if (pathReset) {
					// Rotieren und weitersuchen
					this.curDirection = rotateVector(this.curDirection, 10);
				}
			}
			// wenn tile bereits explorierbar ist, neues unexplored Tile von der
			// map
			// besorgen:
			else {
				final TreeMap<Integer, LinkedTile> sortedTiles = map
						.getUnexploredTilesSortedByDistance(curPos);
				for (final LinkedTile i : sortedTiles.values()) {
					calcPathTo(i);
					// wenn der Pfad g�ltig ist, suche abschliessen
					if (!pathReset)
						return;
				}
			}
		}
	}

	/**
	 * Returns closest object to tank. A minimum distance can be given, so we do
	 * not shoot us self.
	 * 
	 * @param objects
	 * @param type
	 * @param minDist
	 * @return
	 */
	private IWorldObject getNearestObject(final List<IWorldObject> objects,
			final EObjectTypes type, final float minDist) {
		IWorldObject obj = null;

		if (objects != null) {
			for (final IWorldObject o : objects) {
				// ignore other object types
				if (!o.getType().equals(type)) {
					continue;
				}

				if (o.getColor().equals(color)) {
					continue;
				}

				if (obj == null) {
					obj = o;
				} else {
					if (o.getPosition().distance(world.getMyPosition().clone()) < obj
							.getPosition().distance(
									world.getMyPosition().clone())) {
						if (o.getPosition().distance(
								world.getMyPosition().clone()) > minDist) {
							obj = o;
						}
					}
				}
			}
		}

		return obj;
	}

	/**
	 * holt das n�chste moveTarget aus der Wegpunkt liste wenn man am wegpunkt
	 * angekommen ist und f�hrt world.move aus um den Tank zum n�chsten Wegpunkt
	 * zu f�hren wenn kein g�ltiges Ziel existiert h�lt der Tank
	 * an(world.stop()) wenn path leer ist wird pathReset = true gesetzt
	 * 
	 * @return true wenn der tank momentan ein g�ltiges moveTarget hat wohin er
	 *         sich bewegt
	 */
	private boolean moveToNextWaypoint() {
		// wenn wir an aktuellen ziel angekommen sind oder keins existiert,
		// n�chstes Ziel besorgen
		if (arrivedAtMoveTarget() || (moveTarget == null)) {
			moveTarget = null;
			// und n�chsten WEgpunkt holen:
			if (!path.isEmpty()) {
				moveTarget = path.getNextWaypoint();
			}
			else
				world.stop();
		}
		// wenn ziel nicht passierbar ist oder kein Ziel existiert,
		// pathReset=true
		if (((moveTarget != null) && !moveTarget.isPassable)
				|| (moveTarget == null)) {
			moveTarget = null;
			pathReset = true;
			
			return false;
		}
		// zum n�chsten Ziel bewegen
		curDirection = moveTarget.getTileCenterCoordinates().subtract(curPos);
		world.move(curDirection);
		if (path.isEmpty()) {
			pathReset = true;
		}
		return true;
	}

	@Override
	public void perceive(final ArrayList<IWorldObject> worldObjects) {
		perceivedObjects = worldObjects;

		IWorldObject o = null;
		// first shoot other hangars
		o = getNearestObject(worldObjects, EObjectTypes.Hangar, 10);
		// then aim for tanks
		if (o == null) {
			o = getNearestObject(worldObjects, EObjectTypes.Competitor, 10);
		}else System.out.println("greife Hangar an");

		if (o != null) {
			if (o.getType() == EObjectTypes.Competitor) {
				waffenAutomat.action(o);
			} else {
				world.shoot(
						this.curPos.subtract(o.getPosition()).negate(),
						WaffenAutomat.getSpeed(45,
								world.getMyPosition().distance(o.getPosition())),
						45);
			}
		}

		// move WorldObjects into WorkingMemory
		for (final IWorldObject wO : worldObjects) {

			switch (wO.getType()) {
			case Competitor:
				if (wO.getColor() != this.color) {
					this.objectStorage.storeObject(wO.getPosition(),
							new MemorizedWorldObject(wO));
		
				}
				break;
			case Hangar:
				if (wO.getColor() != this.color) {
					this.objectStorage.storeObject(wO.getPosition(),
							new MemorizedWorldObject(wO));

					globalKI.tankStatusChanged(this, wO, StatusType.HangarFound);
				}
				break;
			case Item:
				LinkedTile itemTile = this.map.getTileAtCoordinate(wO.getPosition());
				if(itemTile != null && itemTile.isPassable){
					this.objectStorage.storeObject(wO.getPosition(),
							new MemorizedWorldObject(wO));
				}
				// System.out.println("Item entdeckt");
				break;
			/*
			 * case Flag: iHaveTheFlag = true; break;
			 */
			default:
			}
		}
	
	}

	private Vector3f rotateVector(final Vector3f vec, final float phi) {
		final Vector3f result = vec.clone();
		result.x = vec.x * FastMath.cos(FastMath.DEG_TO_RAD * phi) - vec.z
				* FastMath.sin(FastMath.DEG_TO_RAD * phi);
		result.z = vec.z * FastMath.cos(FastMath.DEG_TO_RAD * phi) + vec.x
				* FastMath.sin(FastMath.DEG_TO_RAD * phi);
		return result;
	}

	private void scanTerrain() {
		final List<LinkedTile> tiles = this.globalKI.getWorldMap()
				.getTilesPossiblyInViewRange(this.curPos);
		for (final LinkedTile tile : tiles) {
			scanTile(tile);
		}
		// Direkt voraus gucken
		final Vector3f voraus = curPos.add(world.getMyDirection().clone()
				.normalize().mult(2));
		if (null == voraus) {
			System.out.println("Voraus ist NULL!!!");
		}
		if ((voraus.x > 10000) || (voraus.z > 10000)) {
			System.out.println("Alerm!");
		}
		if ((null != world.getTerrainNormal(voraus))
				&& !world.isPassable(voraus)) {
			final LinkedTile tileVoraus = map.getTileAtCoordinate(voraus);
			map.exploreTile(tileVoraus, tileVoraus.isWater(), false,
					tileVoraus.getNormalVector());
		}

		if (WOExistanceUpdate == 10) {
			checkWorldObjectExistance();
			WOExistanceUpdate = 0;
		} else {
			WOExistanceUpdate++;
		}

	}

	private void scanTile(final LinkedTile tile) {
		if (!tile.isExplored()) {
			if ((tile.mapIndex.x > 60) || (tile.mapIndex.y > 60)
					|| (tile.mapIndex.x < 0) || (tile.mapIndex.y < 0)) {
				System.out.println("Debug mich");
			}
			boolean isPassable = true;
			boolean isWater = false;
			final Vector3f tileCenter = tile.getTileCenterCoordinates();
			final int increment = (int) FastMath
					.ceil(FastRoutableWorldMap.tilesize / 4.0f);
			final List<Vector3f> scanPositions = new ArrayList<Vector3f>();
			scanPositions.add(tileCenter);
			scanPositions.add(tileCenter.add(new Vector3f(increment, 0,
					increment)));
			scanPositions.add(tileCenter.add(new Vector3f(-increment, 0,
					increment)));
			scanPositions.add(tileCenter.add(new Vector3f(increment, 0,
					-increment)));
			scanPositions.add(tileCenter.add(new Vector3f(-increment, 0,
					-increment)));
			for (final Vector3f scanPosition : scanPositions) {
				// if(memoryMap.positionIsInViewRange(pos, currentDirection,
				// position) && world);
				final Vector3f terrainNormal = world
						.getTerrainNormal(scanPosition);
				if (null == terrainNormal) {
					if (map.positionIsInViewRange(curPos,
							world.getMyDirection(), scanPosition)) {
						// Ausserhalb der Map
						map.markTileAsOutOfMap(tile);
						return;
					} else
						// nicht in Sichtweite
						return;
				}
				if (world.isWater(scanPosition)) {
					isWater = true;
					isPassable = false;
					map.exploreTile(tile, isWater, isPassable, tileCenter);
					return;
				}
				if (!world.isPassable(scanPosition)) {
					isPassable = false;
				}
			}
			map.exploreTile(tile, isWater, isPassable, tileCenter);
			return;
		}
	}

	@Override
	public void setColor(final EColors color) {
		this.color = color;
	}

	@Override
	public void setWorldInstance(final IWorldInstance world) {
		this.world = world;
		globalKI.setWorldInstance(world);
		this.waffenAutomat = new WaffenAutomat(world);
	}

	@Override
	public void spawn() {
		spawnPos = world.getMyPosition();
		curPos = spawnPos;
		curDirection = world.getMyDirection();

		// GOAP STUFF
		globalKI.registerTank(this); // register tank in globalKI
		blackboard.direction = curDirection;
		blackboard.inHangar = true;

	}
	//??? :)
	public void notify(final StatusType type, final Object obj) {
		switch (type) {
		case HangarRemoved:
			if (blackboard.curTask == Task.LOOT_AND_BURN_HANGAR) {
				
				//todo so �berarbeiten, das der task nur ge�ndert wird wenn der aktuelle ziel hangar entfernt wurde:
				blackboard.curTask = Task.EXPLORE;
				pathReset =true; //pfad soll neu berechnet werden
				
				final MemorizedWorldObject hangar = (MemorizedWorldObject) obj;
				System.out.println("Tank " + name + " was notified about "
						+ type + " at Position " + hangar.getPosition());
				if (blackboard.spottedHangar == hangar) {
					final EColors color = hangar.getColor();
					final Object[] hangars = objectStorage
							.getEnemyHangarsOfPlayer(color).values().toArray();
					blackboard.spottedHangar = (MemorizedWorldObject) hangars[0];
				}
			}
			break;
		default:
		}
	}

	/**
	 * berechnet pfad zu n�chstem hangar in objectStorage objectStorage.getEnemyHangars();
	 */
	private void lootAndBurnHangar() {
		final Map<Point, MemorizedWorldObject> enemyHangars = objectStorage
				.getEnemyHangars();
		if (enemyHangars.size() >= 1) {
			final MemorizedWorldObject[] hangars = new MemorizedWorldObject[enemyHangars.values().size()];
			enemyHangars.values().toArray(hangars);
		
			//wenn ein hangar in der map existiert, pfad zu diesem berechnen	
			final LinkedTile target = map.getTileAtCoordinate(hangars[0].getPosition());
			if(curPos.distance(target.getTileCenterCoordinates()) <40){
				System.out.println("halte an, befinde mich vor gegnerischem Hangar "+target);

				this.stoppedTimeStamp = updateNr; //w�rgaround, bis es funktioniert das hangar korrekt als zerst�rt gemeldet werden
				//wenn entfernung zum hangar weniger als X betr�gt, das moveTarget auf den derzeitigen tile setzen => anhalten	
	
					path = new Path<LinkedTile>();

				moveTarget = curTile; 
				blackboard.curTask = Task.STOPATHANGAR;
		} else if (!target.equals(lastPathTarget)) {
				pathReset = true;
				System.out.println("berechne Pfad zu Hangar"+ target);
				calcPathTo(target);
			}

			//wenn kein hangar existiert:
		} else {

			System.out.println("kein hangar existiert zu dem ein pfad berechnen kann");
			pathReset = true;
			blackboard.curTask = Task.EXPLORE;
		}
	}
	private boolean stuck() {
		boolean stuck = false;
		
		//stucken nicht, haben ziel erreicht:
		if((moveTarget != null && moveTarget.equals(curTile) || moveTarget == null) || moveTarget.getTileCenterCoordinates().distance(curPos) < 3)
			return false;
		else if (lastPositions.size() > 1) {
			final Vector3f comparisonPosition = this.lastPositions.poll();

			if ((comparisonPosition != null)
					&& (Math.abs(comparisonPosition.distance(curPos)) < 0.015f)) {
				stuck = true;
			}
		}
		this.lastPositions.add(curPos);
		return stuck;
	}
	/**
	 * berechnet neuen zirkularen Pfad um Hangar herum, wenn der Tank noch im Hangar steht wird explore genutzt um vom hangar wegzufahren
	 * 
	 */

	public void defend() {
		//mithilfe explore vom hangar wegfahren :)
		if (blackboard.inHangar && world.getMyPosition().distance(spawnPos) > 25) {
			blackboard.inHangar = false;
			pathReset=true;
		}

		
		if (!blackboard.inHangar) {
			if (pathReset) {
				final float distance = 25;
				// float distance = 10;
				double x = 0d; // real part
				double z = 0d; // imaginary part

				path = new Path<LinkedTile>();
				path.setCircleCourse(true);

				for (int angle = 0; angle < 360; angle += 15) {
					x = distance * Math.cos(angle);
					z = distance * Math.sin(angle);

					final Vector3f d = spawnPos.clone();
					d.x += x;
					d.z += z;

					path.addWaypoint(map.getTileAtCoordinate(d));

				}

				pathReset = false;
			}
			
		}
		//wenn wir uns noch im hangar befinden, rausfahren
		else{
			explore();
		}
			
	}

	public void goToBase(){
		if (!map.getTileAtCoordinate(spawnPos).equals(lastPathTarget)) {
            pathReset = true;
            calcPathTo(map.getTileAtCoordinate(spawnPos));
        }
    }
	@Override
	public void update(final float interpolation) {
		updateNr++;
		if (!calibrated) {
			calibrate();
		}
		// aktualisiere Klassenvariablen
		scanTerrain();
		evalNextTask();
		curPos = world.getMyPosition();
		curTile = map.getTileAtCoordinate(curPos);
		
		if (this.blackboard.curTask == Task.DEFEND) {
			defend();
		} else if (this.blackboard.curTask == Task.GoToBase){
            goToBase();
		} else if (this.blackboard.curTask == Task.EXPLORE) {
			explore();
		} else if (this.blackboard.curTask == Task.CTF) {
            //todo zu aufwendig bei jeder kleiner flaggen bewegung pfad neukalkulieren, besser nur jede X zyklen neu generieren
			if (flagPosChanged) {
				pathReset = true;
				calcPathTo(map.getTileAtCoordinate(flagPos));
			}
		} else if(this.blackboard.curTask == Task.STOPATHANGAR){
			if(updateNr - stoppedTimeStamp > 20)
				this.blackboard.curTask = Task.EXPLORE;
		} 
			
		 else if (this.blackboard.curTask == Task.LOOT_AND_BURN_HANGAR) {
			lootAndBurnHangar();
		}
		
		
		//wenn wir feststecken erstmal random bewegen, einen zug + pathreseten:)
		if (stuck()) {
			System.out.println(name + " steckt fest. Sein Ziel ist "
					+ moveTarget + "seine Aufgabe: " + blackboard.curTask
					+ "und er befindet sich an Position "
					+ map.getTileAtCoordinate(curPos)
					+ " seine aktuellen Wegpunkte: " + path);

			final int offset = rand.nextInt(45);
			final int alpha = 90 + offset;
			final Vector3f unstuckDirection = rotateVector(world
					.getMyDirection().clone(), alpha);

			pathReset = true;
			world.move(unstuckDirection);
		}
		//wenn wir nicht mehr festecken nach den Wegpunkten berechnen
		else
		
			moveToNextWaypoint();
	}
}
