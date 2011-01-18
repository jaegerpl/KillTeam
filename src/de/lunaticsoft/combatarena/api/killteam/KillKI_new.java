package de.lunaticsoft.combatarena.api.killteam;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;
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
	private boolean isInvalidPath;
	private LinkedTile lastPathTarget; // ziel der letzten Routenberechnung

	
	private Vector3f flagPos;

	private long updateNr;

	// unknown
	private int viewRangeRadius;
	private int viewRangeOffset;
	private boolean calibrated;
	private IWaffenAutomat waffenAutomat;
	private int WOExistanceUpdate = 0;
	private final Queue<Vector3f> lastPositions;

	
	// CTF Stuff
	private boolean iHaveTheFlag = false;
	private boolean flagCollected = false;
	private boolean CTFmode = false; // muss true sein wenn CTF gespielt wird
	private boolean flagPosChanged;

	// Random fuer unstuck in update()
	private java.util.Random rand;
	private long stoppedTimeStamp;
	
	// Item oder Flag einsammeln
	private boolean pathToObjectCalculated = false;

	
	private void evalNextTask() {
		if (blackboard.curTask == Task.DEFEND)
			return;
		else if (iHaveTheFlag && flagCollected) {
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
		} else if(blackboard.toolBoxSpotted == true){
			blackboard.oldTask = blackboard.curTask;
			blackboard.curTask = Task.ITEMCOLLECTING;
			System.out.println("ITEMCOLLECTING STATE");
		} else if(blackboard.toolBoxCollected == true){
			blackboard.toolBoxCollected = false;
			blackboard.curTask = blackboard.oldTask; // alten Taks wieder herstellen
			blackboard.oldTask = null;
			System.out.println("LEAVE ITEMCOLLECTING STATE");
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
		isInvalidPath = true;
		calibrated = false;

		lastPositions = new LinkedBlockingQueue<Vector3f>(2);
		rand = new java.util.Random();

	}

	// prueft ob Tank sich momentan auf moveTarget befindet
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
				mapObjects.remove(memo);
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
	 * berechnet neuen path zu target, wenn ein ungueltiger Pfad berechnet oder
	 * das ziel nicht betretbar istwird pathReset = true gesetzt nachdem ein
	 * neuer pfad gesetzt wurde ist moveTarget der naechste Wegpunkt
	 * 
	 * @param target
	 */
	private void calcPathTo(final LinkedTile target) {
		lastPathTarget = target;
		// wenn tile nicht passable ist zudem ein Pfad berechnet werden soll,
		// nix tun
		if (!target.isPassable) {
			isInvalidPath = true;
			return;
		}
		if (isInvalidPath) {
			path = map.calculatePath(curTile, target);
			isInvalidPath = false;
		}
		/*
		 * if (myPosTile.equals(moveTarget)) //TODO muss nach update {
		 * moveTarget = path.getNextWaypoint(); }
		 */
		if (path.isEmpty()) {
			isInvalidPath = true;
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
//		System.out.println("Radius: " + viewRangeRadius);
//		System.out.println("Offset: " + viewRangeOffset);
//		System.out.println();
//		System.out.println("Front: " + counterFront);
//		System.out.println("Back: " + counterBack);
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
					blackboard.hitsTaken = 0;
				}
			}
			System.out.println("KISTE EINGESAMMELT");
			break;
		case Flag:
			flagCollected = true;
		default:
			// DO NOTHING
			break;
		}
	}

	@Override
	public void die() {
		LinkedTile currentTile = this.map.getTileAtCoordinate(this.curPos);
		if (currentTile != null && currentTile.isPassable) {
			final WorldObject wO = new WorldObject(null, color, curPos,
					EObjectTypes.Item);
			this.objectStorage.storeObject(wO.getPosition(),
					new MemorizedWorldObject(wO));
		}
		// GOAP STUFF
		globalKI.removeTank(this); // deregister tank in globalKI
	}
	
	protected void cruiseMap() {
		if(isInvalidPath || null == path || path.isEmpty()) {
			//Neues Pfad berechnen
			LinkedTile randomDestination = map.getRandomTarget();
			calcPathTo(randomDestination);
		}
	}

	private void explore() {
		if (isInvalidPath) {
//			System.out.println("berechen neues explore ziel");
			// naechstes ZIel am ende des Sichtbereiches in tile umwandeln
			final Vector3f targetPos = this.curPos.add(curDirection.normalize()
					.mult(60));
			final LinkedTile targetTile = map.getTileAtCoordinate(targetPos);
			if (!targetTile.isExplored()) {
				if (targetTile.isPassable()) {
					// Wenn tile noch nicht erkundet + betretbar ist, dieses
					// tile als neues Routenziel benutzen:
					isInvalidPath = true;
					calcPathTo(targetTile);
				}
				// wenn kein gueltiger Pfad berechnet werden konnte
				if (isInvalidPath) {
					// Rotieren und weitersuchen
					this.curDirection = rotateVector(this.curDirection, 10);
				}
			}
			// wenn tile bereits explorierbar ist, neues unexplored Tile 
			// von der map besorgen:
			else {
				final TreeMap<Integer, LinkedTile> sortedTiles = map
						.getUnexploredTilesSortedByDistance(curPos);
				// move to the nearest unexplored target, if one exists
				if(!sortedTiles.isEmpty()){
					for (final LinkedTile i : sortedTiles.values()) {
						calcPathTo(i);
						// wenn der Pfad gueltig ist, suche abschliessen
						if (!isInvalidPath)
							return;
					}
					//Keinen Pfad zu irgend einem unexplored Tile gefunden
					map.markAllUnexploredAsOutOfMap();
					setNewStateAfterMapIsExplored();
				} else {
				// move to a randomly choosen target
					//while(!isInvalidPath){
					//	calcPathTo(map.getRandomTarget());	
					//}
					setNewStateAfterMapIsExplored();
				}
			}
		}
	}
	
	protected void setNewStateAfterMapIsExplored() {
		StringTokenizer tk = new StringTokenizer(name);
		if(tk.countTokens() == 2) {
			tk.nextToken();
			int number = Integer.parseInt(tk.nextToken());
			if(0 == number % 2) {
				blackboard.curTask = Task.DEFEND;
			} else {
				blackboard.curTask = Task.CRUISE_MAP;
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
	 * holt das naechste moveTarget aus der Wegpunkt liste wenn man am wegpunkt
	 * angekommen ist und fuehrt world.move aus um den Tank zum naechsten
	 * Wegpunkt zu fuehren wenn kein gueltiges Ziel existiert haelt der Tank
	 * an(world.stop()) wenn path leer ist wird pathReset = true gesetzt
	 * 
	 * @return true wenn der tank momentan ein gueltiges moveTarget hat wohin
	 *         er sich bewegt
	 */
	private boolean moveToNextWaypoint() {
		// wenn wir an aktuellen ziel angekommen sind oder keins existiert,
		// naechstes Ziel besorgen
		if (arrivedAtMoveTarget() || (moveTarget == null)) {
			moveTarget = null;
			// und naechsten Wegpunkt holen:
			if (!path.isEmpty()) {
				moveTarget = path.getNextWaypoint();
			} else
				world.stop();
		}
		// wenn ziel nicht passierbar ist oder kein Ziel existiert,
		// pathReset=true
		if (((moveTarget != null) && !moveTarget.isPassable)
				|| (moveTarget == null)) {
			moveTarget = null;
			isInvalidPath = true;

			return false;
		}
		// zum naechsten Ziel bewegen
		curDirection = moveTarget.getTileCenterCoordinates().subtract(curPos);
		world.move(curDirection);
		if (path.isEmpty()) {
			isInvalidPath = true;
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

		}//else System.out.println("greife Hangar an");

		if (o != null) {
			if (o.getType() == EObjectTypes.Competitor) {
				waffenAutomat.action(o);
			} else {
				world.shoot(this.curPos.subtract(o.getPosition()).negate(),
						WaffenAutomat.getSpeed(45, world.getMyPosition()
								.distance(o.getPosition())), 45);
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
				LinkedTile itemTile = this.map.getTileAtCoordinate(wO
						.getPosition());
				if (itemTile != null && itemTile.isPassable) {
					this.objectStorage.storeObject(wO.getPosition(),
							new MemorizedWorldObject(wO));
				}
				if(!blackboard.toolBoxSpotted){
					blackboard.toolBoxSpotted = true; // evalNextTask will turn to ItemCollect-Task
					blackboard.toolBoxCollected = false;
					blackboard.spottedToolBox = wO;
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
//			System.out.println("Voraus ist NULL!!!");
		}
		if ((voraus.x > 10000) || (voraus.z > 10000)) {
//			System.out.println("Alerm!");
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
			if ((tile.getMapIndex().x > 60) || (tile.getMapIndex().y > 60)
					|| (tile.getMapIndex().x < 0) || (tile.getMapIndex().y < 0)) {
//				System.out.println("Debug mich");
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

	// ??? :)
	public void notify(final StatusType type, final Object obj) {
		switch (type) {
		case HangarRemoved:
			if (blackboard.curTask == Task.LOOT_AND_BURN_HANGAR) {

				// todo so ï¿½berarbeiten, das der task nur geï¿½ndert wird wenn
				// der aktuelle ziel hangar entfernt wurde:
				blackboard.curTask = Task.EXPLORE;
				isInvalidPath = true; // pfad soll neu berechnet werden

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
	 * berechnet pfad zu nï¿½chstem hangar in objectStorage
	 * objectStorage.getEnemyHangars();
	 */
	private void lootAndBurnHangar() {
		final Map<Point, MemorizedWorldObject> enemyHangars = objectStorage
				.getEnemyHangars();
		if (enemyHangars.size() >= 1) {
			final MemorizedWorldObject[] hangars = new MemorizedWorldObject[enemyHangars
					.values().size()];
			enemyHangars.values().toArray(hangars);
		
			//wenn ein hangar in der map existiert, pfad zu diesem berechnen	
			final LinkedTile target = map.getTileAtCoordinate(hangars[0].getPosition());
			if(curPos.distance(target.getTileCenterCoordinates()) <40){
//				System.out.println("halte an, befinde mich vor gegnerischem Hangar "+target);

				this.stoppedTimeStamp = updateNr; //wï¿½rgaround, bis es funktioniert das hangar korrekt als zerstï¿½rt gemeldet werden
				//wenn entfernung zum hangar weniger als X betrï¿½gt, das moveTarget auf den derzeitigen tile setzen => anhalten	
	
					path = new Path<LinkedTile>();

				moveTarget = curTile;
				blackboard.curTask = Task.STOPATHANGAR;
			} else if (!target.equals(lastPathTarget)) {
				isInvalidPath = true;
//				System.out.println("berechne Pfad zu Hangar"+ target);
				calcPathTo(target);
			}

			// wenn kein hangar existiert:
		} else {
//			System.out.println("kein hangar existiert zu dem ein pfad berechnen kann");
			isInvalidPath = true;
			blackboard.curTask = Task.EXPLORE;
		}
	}

	private boolean stuck() {
		boolean stuck = false;

		// stucken nicht, haben ziel erreicht:
		if ((moveTarget != null && moveTarget.equals(curTile) || moveTarget == null)
				|| moveTarget.getTileCenterCoordinates().distance(curPos) < 3)
			return false;
		else if (lastPositions.size() > 1) {
			final Vector3f comparisonPosition = this.lastPositions.poll();

			if ((comparisonPosition != null)
					&& (Math.abs(comparisonPosition.distance(curPos)) < 0.010f)) {
				stuck = true;
			}
		}
		this.lastPositions.add(curPos);
		return stuck;
	}

	/**
	 * berechnet neuen zirkularen Pfad um Hangar herum, wenn der Tank noch im
	 * Hangar steht wird explore genutzt um vom hangar wegzufahren
	 * 
	 */
	public void defend() {
		final float circleCourseRadius = 25;
		int tilesize = FastRoutableWorldMap.tilesize;
		
		
		if(this.curPos.subtract(spawnPos).length() > circleCourseRadius + tilesize) {
			//Zu weit weg für direkt bewegen
			if(null == path || path.isEmpty()) {
				calcPathTo(map.getTileAtCoordinate(spawnPos));
			}
		} else {
		
			// mithilfe explore vom hangar wegfahren :)
			if (blackboard.inHangar
					&& world.getMyPosition().distance(spawnPos) > 25) {
				blackboard.inHangar = false;
				isInvalidPath = true;
			}
	
			if (!blackboard.inHangar) {
				if (isInvalidPath) {
					
					// float distance = 10;
					double x = 0d; // real part
					double z = 0d; // imaginary part
	
					path = new Path<LinkedTile>();
					path.setCircleCourse(true);
	
					for (int angle = 0; angle < 360; angle += 15) {
						x = circleCourseRadius * Math.cos(angle);
						z = circleCourseRadius * Math.sin(angle);
	
						final Vector3f d = spawnPos.clone();
						d.x += x;
						d.z += z;
	
						path.addWaypoint(map.getTileAtCoordinate(d));
	
					}
	
					isInvalidPath = false;
				}
	
			}
			// wenn wir uns noch im hangar befinden, rausfahren
			else {
				explore();
			}
		}

	}

	public void goToBase() {
		if (!map.getTileAtCoordinate(spawnPos).equals(lastPathTarget)) {
			isInvalidPath = true;
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
		} else if (this.blackboard.curTask == Task.CRUISE_MAP) {
			cruiseMap();
		} else if (this.blackboard.curTask == Task.GoToBase) {
			goToBase();
		} else if (this.blackboard.curTask == Task.EXPLORE) {
			explore();
		} else if (this.blackboard.curTask == Task.CTF) {
			// todo zu aufwendig bei jeder kleiner flaggen bewegung pfad
			// neukalkulieren, besser nur jede X zyklen neu generieren
			//alle 10 zyklen
			if(this.updateNr%10 == 0){
				if (flagPosChanged) {
					isInvalidPath = true;
					calcPathTo(map.getTileAtCoordinate(flagPos));
					flagPosChanged = false;
				}
			}
		} else if (this.blackboard.curTask == Task.STOPATHANGAR) {
			if (updateNr - stoppedTimeStamp > 20)
				this.blackboard.curTask = Task.EXPLORE;
		} else if (this.blackboard.curTask == Task.LOOT_AND_BURN_HANGAR) {
			lootAndBurnHangar();
		} else if (this.blackboard.curTask == Task.ITEMCOLLECTING){
			System.out.println("IN Itemcollecting state");
			collectItem();
		}

		// wenn wir feststecken erstmal random bewegen, einen zug +
		// pathreseten:)
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

			isInvalidPath = true;
			world.move(unstuckDirection);
		} else {
			// wenn wir nicht mehr festecken nach den Wegpunkten berechnen
			moveToNextWaypoint();
		}
	}

	private void collectItem() {
		if(!pathToObjectCalculated){
			LinkedTile itemTile = map.getTileAtCoordinate(blackboard.spottedToolBox.getPosition());
			calcPathTo(itemTile);
			if(!isInvalidPath){
				pathToObjectCalculated = true;	
			} else {
				// konnte keinen Pfad zum Item berechnen, ignoriere Kiste und gehe wieder in den alten Task
				blackboard.curTask = blackboard.oldTask;
				blackboard.spottedToolBox = null;
				blackboard.toolBoxCollected = true;
				blackboard.toolBoxSpotted = false;
			}
		} else {
			if (!path.isEmpty()) {
				moveTarget = path.getNextWaypoint();
			} else {
				//wir sind am Ziel-Tile, gehe jetzt zum Item
				world.move(blackboard.spottedToolBox.getPosition().clone().subtract(world.getMyPosition()));
			}
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.lunaticsoft.combatarena.api.interfaces.IPlayer#setFlagPos(com.jme.
	 * math.Vector3f)
	 */
	@Override
	public void setFlagPos(Vector3f flagPos) {
		this.flagPos = flagPos;
		flagPosChanged = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.lunaticsoft.combatarena.api.interfaces.IPlayer#flagInBase()
	 */
	@Override
	public void flagInBase() {
		flagCollected = false;

	}
}
