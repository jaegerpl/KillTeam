package map.memory.map;

import java.awt.Point;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import map.fastmap.FastRoutableWorldMap;
import map.fastmap.LinkedTile;
import map.maplayer.MapLayer;
import map.memory.pathcalulation.AStarPathCalculator;
import map.memory.pathcalulation.Path;

import com.jme.math.Vector3f;

import de.lunaticsoft.combatarena.api.interfaces.IPlayer;


public class MemorizedMap {
	AStarPathCalculator pathCalculator;
	FastRoutableWorldMap worldMap;
	
	int lowPrecisionMapLimit = 15;
	MapLayer layer1;
	
	public MemorizedMap() {
		pathCalculator = new AStarPathCalculator(this);
		worldMap = new FastRoutableWorldMap();
		layer1 = new MapLayer(worldMap, 1);
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
		if(lowPrecisionMapLimit < pathCalculator.calculateApproximatedDistance(from, to)) {
			from = layer1.getCorrespondingTileInThisLayer(from);
			to = layer1.getCorrespondingTileInThisLayer(to);
			
			return pathCalculator.calculatePath(from, to);
		} else {
			return pathCalculator.calculatePath(from, to);
		}
	}
	
//	public List<Tile> getEmptyTilesInViewRange(Vector3f pos){
//		return worldMap.getEmptyTilesInViewRange(pos);
//	}
//	
//	public List<Tile> getTilesInViewRange(Vector3f pos){
//		return worldMap.getTilesInViewRange(pos);
//	}
	
	
	public void addTile(LinkedTile tile){
		worldMap.addTile(tile);
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
}
