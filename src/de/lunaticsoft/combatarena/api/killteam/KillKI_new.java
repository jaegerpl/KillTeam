package de.lunaticsoft.combatarena.api.killteam;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import map.fastmap.FastRoutableWorldMap;
import map.fastmap.LinkedTile;
import memory.map.MemorizedMap;
import memory.objectStorage.MemorizedWorldObject;
import memory.objectStorage.ObjectStorage;
import memory.pathcalulation.Path;
import battle.Battle;
import battle.ShootTarget;

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
	private Object flagPos;
	private Object flagPosPath;
	private int viewRangeRadius;
	private int viewRangeOffset;
	private boolean calibrated;

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
	}

	@Override
	public void attacked(final IWorldObject competitor) {
		// TODO
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
		// final WorldObject wO = new WorldObject(null, color, this.curPos,
		// EObjectTypes.Item);
		// this.objectStorage.storeObject(wO.getPosition(), new
		// MemorizedWorldObject(wO));
		// GOAP STUFF
		globalKI.removeTank(this); // deregister tank in globalKI
	}

	private Vector3f rotateVector(final Vector3f vec, final float phi) {
		final Vector3f result = vec.clone();
		result.x = vec.x * FastMath.cos(FastMath.DEG_TO_RAD * phi) - vec.z
				* FastMath.sin(FastMath.DEG_TO_RAD * phi);
		result.z = vec.z * FastMath.cos(FastMath.DEG_TO_RAD * phi) + vec.x
				* FastMath.sin(FastMath.DEG_TO_RAD * phi);
		return result;
	}

	/**
	 * berechnet neuen path zu target, wenn ein ungültiger Pfad berechnet oder
	 * das ziel nicht betretbar istwird pathReset = true gesetzt nachdem ein
	 * neuer pfad gesetzt wurde ist moveTarget der nächste Wegpunkt
	 * 
	 * @param target
	 */
	private void calcPathTo(final LinkedTile target) {
		System.out.println("calcPathTo: " + target);
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
		System.out.println("gültiges Ziel berechnet!");
	}

	private void explore() {

		if (pathReset) {
			System.out.println("berechen neues explore ziel");
			// nächstes ZIel am ende des Sichtbereiches in tile umwandeln
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

				// wenn kein gültiger Pfad berechnet werden konnte
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
					// wenn der Pfad gültig ist, suche abschliessen
					if (!pathReset)
						return;
				}
			}
		}

	}

	@Override
	public void perceive(final ArrayList<IWorldObject> worldObjects) {
		perceivedObjects = worldObjects;
		// move WorldObjects into WorkingMemory
		for (final IWorldObject wO : worldObjects) {
			switch (wO.getType()) {
			case Competitor:
				if (wO.getColor() != this.color) {
					this.objectStorage.storeObject(wO.getPosition(),
							new MemorizedWorldObject(wO));
					// System.out.println("Panzer gefunden: " + wO.hashCode());
					final ShootTarget target = Battle.getShootTarget(
							wO.getPosition(), this.curPos);
					world.shoot(target.direction, target.force, target.angle);
					// System.out.println("Feind entdeckt");
				}
				break;
			case Hangar:
				if (wO.getColor() != this.color) {
					this.objectStorage.storeObject(wO.getPosition(),
							new MemorizedWorldObject(wO));
					final ShootTarget target = Battle.getShootTarget(
							wO.getPosition(), this.curPos);
					world.shoot(target.direction, target.force, target.angle);
					// System.out.println("feindlichen Hangar entdeckt");
					globalKI.tankStatusChanged(this, wO, StatusType.HangarFound);
				}
				break;
			case Item:
				this.objectStorage.storeObject(wO.getPosition(),
						new MemorizedWorldObject(wO));
				// System.out.println("Item entdeckt");
				break;
			/*
			 * case Flag: iHaveTheFlag = true; break;
			 */
			default:
				// System.out.println("Kein WO");
			}
		}
	}

	private void scanTerrain() {
		final List<LinkedTile> tiles = this.globalKI.getWorldMap()
				.getTilesPossiblyInViewRange(curPos);
		for (final LinkedTile tile : tiles) {
			scanTile(tile);
		}
		// Direkt voraus gucken
		final Vector3f voraus = curPos.add(world.getMyDirection().normalize()
				.mult(2));
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
		/*
		 * if (WOExistanceUpdate == 10) { checkWorldObjectExistance();
		 * WOExistanceUpdate = 0; } else { WOExistanceUpdate++; }
		 */
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
	}

	@Override
	public void spawn() {
		spawnPos = world.getMyPosition();
		// System.out.println("StartPos: "+startPos);
		curDirection = world.getMyDirection();
		// GOAP STUFF
		globalKI.registerTank(this); // register tank in globalKI
		blackboard.direction = curDirection;
		blackboard.inHangar = true;
	}

	@Override
	public void update(final float interpolation) {
		if (!calibrated) {
			calibrate();
		}

		// aktualisiere Klassenvariablen
		scanTerrain();
		curPos = world.getMyPosition();
		curTile = map.getTileAtCoordinate(curPos);

		explore();
		moveToNextWaypoint();

	}

	// prüft ob Tank sich momentan auf moveTarget befindet
	private boolean arrivedAtMoveTarget() {
		System.out.println("moveTarget: " + moveTarget);
		System.out.println("curTile: " + moveTarget);
		System.out.println("curTile == moveTarget: "
				+ curTile.equals(moveTarget));
		if (moveTarget != null || curTile != null)
			return curTile.equals(moveTarget);
		else
			return false;

	}

	@Override
	public String toString() {
		return "KillKI_new [color=" + color + ", name=" + name + ", world="
				+ world + ", blackboard=" + blackboard + ", globalKI="
				+ globalKI + ", perceivedObjects=" + perceivedObjects
				+ ", objectStorage=" + objectStorage + ", map=" + map
				+ ", spawnPos=" + spawnPos + ", curDirection=" + curDirection
				+ ", curPos=" + curPos + ", curTile=" + curTile + ", path="
				+ path + ", moveTarget=" + moveTarget + ", pathReset="
				+ pathReset + ", flagPos=" + flagPos + ", flagPosPath="
				+ flagPosPath + "]";
	}

	/**
	 * holt das nächste moveTarget aus der Wegpunkt liste wenn man am wegpunkt
	 * angekommen ist und führt world.move aus um den Tank zum nächsten Wegpunkt
	 * zu führen wenn kein gültiges Ziel existiert hält der Tank
	 * an(world.stop()) wenn path leer ist wird pathReset = true gesetzt
	 * 
	 * @return true wenn der tank momentan ein gültiges moveTarget hat wohin er
	 *         sich bewegt
	 */
	private boolean moveToNextWaypoint() {
		// wenn wir an aktuellen ziel angekommen sind oder keins existiert,
		// nächstes Ziel besorgen
		if (arrivedAtMoveTarget() || moveTarget == null) {
			System.out.println("an ziel angekommen oder moveTarget == null");
			moveTarget = null;

			// und nächsten WEgpunkt holen:
			if (!path.isEmpty())
				moveTarget = path.getNextWaypoint();
		}
		// wenn ziel nicht passierbar ist oder kein Ziel existiert,
		// pathReset=true
		if ((moveTarget != null && !moveTarget.isPassable)
				|| moveTarget == null) {
			pathReset = true;
			world.stop();
			return false;
		}

		// zum nächsten Ziel bewegen
		curDirection = moveTarget.getTileCenterCoordinates().subtract(curPos);
		System.out.println("WORLD.MOVE: " + curDirection);
		world.move(curDirection);
		if (path.isEmpty())
			pathReset = true;
		return true;

	}
}
