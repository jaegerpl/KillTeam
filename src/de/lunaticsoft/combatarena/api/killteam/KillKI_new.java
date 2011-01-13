package de.lunaticsoft.combatarena.api.killteam;

import java.util.ArrayList;
import java.util.List;

import map.fastmap.FastRoutableWorldMap;
import map.fastmap.LinkedTile;
import memory.map.MemorizedMap;
import memory.objectStorage.MemorizedWorldObject;
import memory.objectStorage.ObjectStorage;
import battle.Battle;
import battle.ShootTarget;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.killteam.globalKI.GlobalKI;
import de.lunaticsoft.combatarena.api.killteam.globalKI.StatusType;

public class KillKI_new implements IPlayer {
  private EColors color;
  private final String name;
  private IWorldInstance world;
  private final TankBlackboard blackboard;
  private final GlobalKI globalKI;
  private ArrayList<IWorldObject> perceivedObjects;
  private final ObjectStorage objectStorage;
  private final MemorizedMap memoryMap;
  private Vector3f spawnDirection;
  private Vector3f spawnPos;
  private Vector3f pos;

  public KillKI_new(final String name, final GlobalKI globalKI, final Task task) {
    this.name = name;
    this.blackboard = new TankBlackboard();
    this.memoryMap = globalKI.getWorldMap();
    this.globalKI = globalKI;
    this.objectStorage = globalKI.getObjectStorage();
    this.blackboard.curTask = task;
  }

  @Override
  public void attacked(final IWorldObject competitor) {
    //TODO
  }

  @Override
  public void collected(final IWorldObject worldObject) {
    switch (worldObject.getType())
    {
      case Item:
        if (blackboard.spottedToolBox != null)
        {
          if (blackboard.spottedToolBox.getPosition() == worldObject.getPosition())
          {
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
    final WorldObject wO = new WorldObject(null, color, this.pos, EObjectTypes.Item);
    this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
    // GOAP STUFF
    globalKI.removeTank(this); // deregister tank in globalKI
  }

  private void explore() {
    final Vector3f targetPos = this.pos.add(spawnDirection.normalize().mult(30));
  }

  @Override
  public void perceive(final ArrayList<IWorldObject> worldObjects) {
    perceivedObjects = worldObjects;
    // move WorldObjects into WorkingMemory
    for (final IWorldObject wO : worldObjects)
    {
      switch (wO.getType())
      {
        case Competitor:
          if (wO.getColor() != this.color)
          {
            this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
            //System.out.println("Panzer gefunden: " + wO.hashCode());
            final ShootTarget target = Battle.getShootTarget(wO.getPosition(), this.pos);
            world.shoot(target.direction, target.force, target.angle);
            //System.out.println("Feind entdeckt");
          }
          break;
        case Hangar:
          if (wO.getColor() != this.color)
          {
            this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
            final ShootTarget target = Battle.getShootTarget(wO.getPosition(), this.pos);
            world.shoot(target.direction, target.force, target.angle);
            //System.out.println("feindlichen Hangar entdeckt");
            globalKI.tankStatusChanged(this, wO, StatusType.HangarFound);
          }
          break;
        case Item:
          this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
          //System.out.println("Item entdeckt");
          break;
        /*
         * case Flag: iHaveTheFlag = true; break;
         */
        default:
          //System.out.println("Kein WO");
      }
    }
  }

  private void scanTile(final LinkedTile tile) {
    if (!tile.isExplored())
    {
      if ((tile.mapIndex.x > 60) || (tile.mapIndex.y > 60) || (tile.mapIndex.x < 0) || (tile.mapIndex.y < 0))
      {
        System.out.println("Debug mich");
      }
      boolean isPassable = true;
      boolean isWater = false;
      final Vector3f tileCenter = tile.getTileCenterCoordinates();
      final int increment = (int) FastMath.ceil(FastRoutableWorldMap.tilesize / 4.0f);
      final List<Vector3f> scanPositions = new ArrayList<Vector3f>();
      scanPositions.add(tileCenter);
      scanPositions.add(tileCenter.add(new Vector3f(increment, 0, increment)));
      scanPositions.add(tileCenter.add(new Vector3f(-increment, 0, increment)));
      scanPositions.add(tileCenter.add(new Vector3f(increment, 0, -increment)));
      scanPositions.add(tileCenter.add(new Vector3f(-increment, 0, -increment)));
      for (final Vector3f scanPosition : scanPositions)
      {
        //if(memoryMap.positionIsInViewRange(pos, currentDirection, position) && world);
        final Vector3f terrainNormal = world.getTerrainNormal(scanPosition);
        if (null == terrainNormal)
        {
          if (memoryMap.positionIsInViewRange(pos, world.getMyDirection(), scanPosition))
          {
            //Ausserhalb der Map
            memoryMap.markTileAsOutOfMap(tile);
            return;
          }
          else
            //nicht in Sichtweite
            return;
        }
        if (world.isWater(scanPosition))
        {
          isWater = true;
          isPassable = false;
          memoryMap.exploreTile(tile, isWater, isPassable, tileCenter);
          return;
        }
        if (!world.isPassable(scanPosition))
        {
          isPassable = false;
        }
      }
      memoryMap.exploreTile(tile, isWater, isPassable, tileCenter);
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
    //System.out.println("StartPos: "+startPos);
    spawnDirection = world.getMyDirection();
    // GOAP STUFF
    globalKI.registerTank(this); // register tank in globalKI
    blackboard.direction = spawnDirection;
    blackboard.inHangar = true;
  }

  @Override
  public void update(final float interpolation) {
  }
}
