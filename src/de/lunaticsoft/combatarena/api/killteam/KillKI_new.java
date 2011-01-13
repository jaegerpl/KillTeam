package de.lunaticsoft.combatarena.api.killteam;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import map.fastmap.FastRoutableWorldMap;
import map.fastmap.LinkedTile;
import memory.map.MemorizedMap;
import memory.objectStorage.MemorizedWorldObject;
import memory.objectStorage.ObjectStorage;

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
	private final MemorizedMap memoryMap;
	
	private Vector3f spawnDirection;
	private Vector3f spawnPos;
	private Vector3f pos;
	

	@Override
	public void update(float interpolation) {
		
	}
	
private void explore(){
	Vector3f targetPos = this.pos.add(spawnDirection.normalize().mult(30));

	 
}
	

	 public KillKI_new(String name, GlobalKI globalKI, Task task) {
	        this.name = name;
	        this.blackboard = new TankBlackboard();
	        this.memoryMap = globalKI.getWorldMap();
	        this.globalKI = globalKI;
	        this.objectStorage = globalKI.getObjectStorage();
	        this.blackboard.curTask = task;
	    }

	@Override
    public void setWorldInstance(IWorldInstance world) {
        this.world = world;
        globalKI.setWorldInstance(world);
    }

	@Override
	public void attacked(IWorldObject competitor) {
		//TODO
		
	}

	@Override
	public void die() {
        WorldObject wO = new WorldObject(null, color, this.pos, EObjectTypes.Item);
        this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
        
        // GOAP STUFF
        globalKI.removeTank(this);                  // deregister tank in globalKI
		
	}

	@Override
    public void perceive(ArrayList<IWorldObject> worldObjects) {
    	perceivedObjects = worldObjects;
        
        // move WorldObjects into WorkingMemory
        for (IWorldObject wO : worldObjects) {
            
            switch(wO.getType()){
                case Competitor:
                    if(wO.getColor() != this.color){
                        this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
                        //System.out.println("Panzer gefunden: " + wO.hashCode());
                        ShootTarget target = Battle.getShootTarget(wO.getPosition(), this.pos);
                        world.shoot(target.direction, target.force, target.angle);
                        //System.out.println("Feind entdeckt");
                    }
                    break;
                case Hangar:
                    if(wO.getColor() != this.color){
                        this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
                        ShootTarget target = Battle.getShootTarget(wO.getPosition(), this.pos);
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
                case Flag:
                    iHaveTheFlag = true;
                    break;
                    */
                default: 
                    //System.out.println("Kein WO");
            }
        }
  
    }

	@Override
	public void collected(IWorldObject worldObject) {
        switch (worldObject.getType()) {
        case Item:
            if(blackboard.spottedToolBox != null){
                if(blackboard.spottedToolBox.getPosition() == worldObject.getPosition()){
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
    public void spawn() {
        spawnPos = world.getMyPosition();
        //System.out.println("StartPos: "+startPos);
        spawnDirection = world.getMyDirection();
        
        // GOAP STUFF
        globalKI.registerTank(this);                // register tank in globalKI
        blackboard.direction = spawnDirection;
        blackboard.inHangar = true;
    }

	@Override
	public void setColor(EColors color) {
        this.color = color;

		
	}
	 private void scanTile(LinkedTile tile) {
	        if(!tile.isExplored()) {
	if(tile.mapIndex.x > 60 || tile.mapIndex.y > 60 || tile.mapIndex.x < 0 || tile.mapIndex.y < 0) {
	    System.out.println("Debug mich");
	}
	            boolean isPassable = true;
	            boolean isWater = false;            
	            
	            Vector3f tileCenter = tile.getTileCenterCoordinates();
	            int increment = (int) FastMath.ceil(FastRoutableWorldMap.tilesize / 4.0f);
	            
	            List<Vector3f> scanPositions = new ArrayList<Vector3f>();
	            scanPositions.add(tileCenter);
	            scanPositions.add(tileCenter.add(new Vector3f(increment, 0, increment)));
	            scanPositions.add(tileCenter.add(new Vector3f(-increment, 0, increment)));
	            scanPositions.add(tileCenter.add(new Vector3f(increment, 0, -increment)));
	            scanPositions.add(tileCenter.add(new Vector3f(-increment, 0, -increment)));
	            
	            for(Vector3f scanPosition : scanPositions) {
	                //if(memoryMap.positionIsInViewRange(pos, currentDirection, position) && world);
	                Vector3f terrainNormal = world.getTerrainNormal(scanPosition);
	                if(null == terrainNormal) {
	                    if(memoryMap.positionIsInViewRange(pos, world.getMyDirection(), scanPosition)) {
	                        //Ausserhalb der Map
	                        memoryMap.markTileAsOutOfMap(tile);
	                        return;
	                    } else {
	                        //nicht in Sichtweite
	                        return;
	                    }
	                }
	                if(world.isWater(scanPosition)) {
	                    isWater = true;
	                    isPassable = false;
	                    memoryMap.exploreTile(tile, isWater, isPassable, tileCenter);
	                    return;
	                }
	                if(!world.isPassable(scanPosition)){
	                    isPassable = false;
	                }
	            }
	            memoryMap.exploreTile(tile, isWater, isPassable, tileCenter);
	            return;
	        }
	    }
}
