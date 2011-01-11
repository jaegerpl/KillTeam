package memory.map;

import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import map.fastmap.FastRoutableWorldMap;
import map.fastmap.LinkedTile;
import map.maplayer.MapLayer;
import memory.pathcalulation.AStarPathCalculator;
import memory.pathcalulation.Path;

import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.interfaces.IPlayer;


public class MemorizedMap{
	AStarPathCalculator pathCalculator;
	FastRoutableWorldMap worldMap;
	
	int lowPrecisionMapLimit = 15;
	MapLayer layer1;
	
	public MemorizedMap() {
		pathCalculator = new AStarPathCalculator(this);
		worldMap = new FastRoutableWorldMap();
		layer1 = new MapLayer(worldMap, 1);
	}
	
	public FastRoutableWorldMap getUnderlyingMap(){
		return worldMap;
	}
	
	public synchronized ConcurrentLinkedQueue<LinkedTile> registerTank(IPlayer tank) {
		return worldMap.registerTank(tank);
	}
	
	public synchronized LinkedTile convertTileToBase(LinkedTile tile) {
		if(1 == tile.getPrecisionLevel()) {
			return layer1.getCorrespondingTileInBaseMap(tile);
		} else {
			return tile;
		}
	}
	
	public static Point coordinates2MapIndex(Vector3f position){
		return FastRoutableWorldMap.coordinates2MapIndex(position);
	}
	
	public synchronized Path<LinkedTile> calculatePath(LinkedTile from, LinkedTile to) {
		/*if(lowPrecisionMapLimit < pathCalculator.calculateApproximatedDistance(from, to)) {
			from = layer1.getCorrespondingTileInThisLayer(from);
			to = layer1.getCorrespondingTileInThisLayer(to);
			
			return pathCalculator.calculatePath(from, to);
		} else {*/
			return pathCalculator.calculatePath(from, to);
		//}
	}
	
	public void exploreTile(LinkedTile tile, boolean isWater, boolean isPassable, Vector3f normalVector) {
		worldMap.exploreTile(tile, isWater, isPassable, normalVector);
	}
	
	public void markTileAsOutOfMap(LinkedTile tile) {
		
		worldMap.markTileAsOutOfMap(tile);
	}
	
	
	public void addTile(LinkedTile tile){
		worldMap.addTile(tile);
	}
	
	public TreeMap<Integer, LinkedTile> getUnexploredTilesSortedByDistance(Vector3f position) {
		TreeMap<Integer, LinkedTile> sortedTiles = new TreeMap<Integer, LinkedTile>();
		
		Point myPosition = worldMap.getTileAtCoordinate(position).getMapIndex();
		Map<Point, LinkedTile> unexploredTiles = worldMap.getUnexploredTiles();
		
		for(LinkedTile tile : unexploredTiles.values()) {
			sortedTiles.put(pathCalculator.calculateApproximatedDistance(myPosition, tile.getMapIndex()), tile);
		}
		
		return sortedTiles;
	}
	
	public LinkedTile getNearestUnexploredTile(Vector3f position) {
		Point myPosition = worldMap.getTileAtCoordinate(position).getMapIndex();
		Map<Point, LinkedTile> unexploredTiles = worldMap.getUnexploredTiles();
		if(0 == unexploredTiles.size()) {
			return null;
		}
		if(!unexploredTiles.containsKey(myPosition)) {
			Set<Point> coordinates = unexploredTiles.keySet();
			Point nearest = null;
			int nearestDistance = 0;
			for(Point coord : coordinates) {
				int thisDistance = pathCalculator.calculateApproximatedDistance(myPosition, coord);
				if(nearest == null || thisDistance < nearestDistance) {
					nearest = coord;
					nearestDistance = thisDistance;
				}
			}
			return unexploredTiles.get(nearest);
		} else {
			return unexploredTiles.get(myPosition);
		}
	}
	
	public LinkedTile getTileAtCoordinate(Vector3f position){
		return worldMap.getTileAtCoordinate(position);
	}
	
	public List<LinkedTile> getTilesPossiblyInViewRange(Vector3f myPosition) {
		return worldMap.getTilesPossiblyInViewRange(myPosition);
	}
	
	public List<LinkedTile> getEmptyTilesPossiblyInViewRange(Vector3f myPosition) {
		return worldMap.getEmptyTilesPossiblyInViewRange(myPosition);
	}
	
	public LinkedTile getTileAtMapIndex(Point point){
		return getTileAtCoordinate(FastRoutableWorldMap.getTileCenterCoordinates(point));
	}
	
	public int getApproxDistance(Point x, Point y){
		AStarPathCalculator astar = new AStarPathCalculator(this);
		return astar.calculateApproximatedDistance(x, y);
	}
	
	public boolean tileIsInViewRange(Vector3f myPosition, Vector3f viewDirection, LinkedTile tile) {
		return worldMap.tileIsInViewRange(myPosition, viewDirection, tile);
	}
	
	public boolean positionIsInViewRange(Vector3f myPosition, Vector3f viewDirection, Vector3f position) {
		return worldMap.positionIsInViewRange(myPosition, viewDirection, position);
	}

}
