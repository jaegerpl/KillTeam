package memory.objectStorage;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import map.fastmap.FastRoutableWorldMap;

import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.enumn.EColors;

public class ObjectStorage {

	Map<Point, MemorizedWorldObject> items;
	Map<EColors, Map<Point, MemorizedWorldObject>> hangars;
	Map<EColors, Map<Point, MemorizedWorldObject>> tanks;

	public ObjectStorage() {
		items = new HashMap<Point, MemorizedWorldObject>();
		hangars = new HashMap<EColors, Map<Point,MemorizedWorldObject>>();
		tanks = new HashMap<EColors, Map<Point,MemorizedWorldObject>>();
		for(EColors color : EColors.values()) {
			tanks.put(color, new HashMap<Point, MemorizedWorldObject>());
			hangars.put(color, new HashMap<Point, MemorizedWorldObject>());
		}
	}
	
	public void storeObject(Vector3f position, MemorizedWorldObject object) {
		Point objectPosition = FastRoutableWorldMap.coordinates2MapIndex(position);
		switch(object.getType()) {
			case Competitor:
				tanks.get(object.getColor()).put(objectPosition, object);
				break;
			case Hangar:
				hangars.get(object.getColor()).put(objectPosition, object);
				break;
			case Item:
				items.put(objectPosition, object);
				break;
		}
	}
	
	public void removeObject(Vector3f position, MemorizedWorldObject object) {
		Point objectPosition = FastRoutableWorldMap.coordinates2MapIndex(position);
		switch(object.getType()) {
			case Competitor:
				tanks.get(object.getColor()).remove(objectPosition);
				break;
			case Hangar:
				hangars.get(object.getColor()).remove(objectPosition);
				break;
			case Item:
				items.remove(objectPosition);
				break;
		}
	}
	
	public Map<Point, MemorizedWorldObject> getEnemyTanks() {
		Map<Point,MemorizedWorldObject> listOfAllTanks = new HashMap<Point, MemorizedWorldObject>();
		for(Map<Point,MemorizedWorldObject> enemyList : tanks.values()) {
			listOfAllTanks.putAll(enemyList);
		}
		return listOfAllTanks;
	}
	
	public Map<Point, MemorizedWorldObject> getEnemyTanksOfPlayer(EColors enemy) {
		return tanks.get(enemy);
	}
	
	public Map<Point, MemorizedWorldObject> getEnemyHangars() {
		Map<Point,MemorizedWorldObject> listOfAllHangars = new HashMap<Point, MemorizedWorldObject>();
		for(Map<Point,MemorizedWorldObject> enemyList : hangars.values()) {
			listOfAllHangars.putAll(enemyList);
		}
		return listOfAllHangars;
	}
	
	public Map<Point, MemorizedWorldObject> getEnemyHangarsOfPlayer(EColors enemy) {
		return hangars.get(enemy);
	}
	
	public Map<Point, MemorizedWorldObject> getEnemyObjects() {
		Map<Point,MemorizedWorldObject> objects = getEnemyHangars();
		objects.putAll(getEnemyTanks());
		return objects;
	}
	
	public Map<Point, MemorizedWorldObject> getEnemyObjectsOfPlayer(EColors enemy) {
		Map<Point,MemorizedWorldObject> objects = getEnemyHangarsOfPlayer(enemy);
		objects.putAll(getEnemyTanksOfPlayer(enemy));
		return objects;
	}
	
	public Map<Point, MemorizedWorldObject> getRepairkits() {
		return items;
	}
	
}
