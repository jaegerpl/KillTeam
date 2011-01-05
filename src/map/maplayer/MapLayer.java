package map.maplayer;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import map.fastmap.FastRoutableWorldMap;
import map.fastmap.LinkedTile;


public class MapLayer {
	
	FastRoutableWorldMap worldMap;
	int abstractionValue;
	int abstractionLevel;
	
	protected Map<Point,LinkedTile> mapAbstraction = new HashMap<Point, LinkedTile>();

	public MapLayer() {
		super();
		this.worldMap = null;
		this.abstractionValue = 1;
		this.abstractionLevel = 0;
	}

	public MapLayer(FastRoutableWorldMap worldMap, int abstractionLevel) {
		this.worldMap = worldMap;
		this.abstractionLevel = abstractionLevel;
		//Nur ungerade Seitenl�ngen
		this.abstractionValue = 1 + (abstractionLevel * 2);
	}

	public int getAbstractionValue() {
		return abstractionValue;
	}
		
	public int getAbstractionLevel() {
		return abstractionLevel;
	}

	public LinkedTile getCorrespondingTileInThisLayer(LinkedTile tileInBaseCoordinateSystem) {
		Point coordsInBaseSystem = tileInBaseCoordinateSystem.getMapIndex();
		Point coordInThisLayer = new Point((int)Math.floor(coordsInBaseSystem.x / abstractionValue), (int)Math.floor(coordsInBaseSystem.y / abstractionValue));
		
		return getTileAtPosition(coordInThisLayer);
	}
	
	public LinkedTile getCorrespondingTileInBaseMap(LinkedTile tileInThisLayer) {
		Point coordInThisLayer = tileInThisLayer.getMapIndex();
		Point coordsInBaseSystem = new Point((coordInThisLayer.x * abstractionValue) + (int) Math.floor(abstractionValue / 2.0), (coordInThisLayer.y * abstractionValue) + (int) Math.floor(abstractionValue / 2.0));
		
		LinkedTile tmp = worldMap.getTileAtMapIndex(coordsInBaseSystem);
		return (LinkedTile) tmp;
	}
	
	public LinkedTile getTileAtPosition(Point position) {
		if(mapAbstraction.containsKey(position)) {
			return mapAbstraction.get(position);
		}
		
		//Abstrahiere Feld dieser Stufe von darunterliegender Stufe
		//verwende den abstractionLevel hierzu
		Point positionInSubjacentLevelRightmost = new Point((position.x * abstractionValue) + 1, (position.y * abstractionValue) + 1);
		Point middlePositionInSubjacentLevel = new Point((position.x * abstractionValue) + (int) Math.floor(abstractionValue / 2.0), (position.y * abstractionValue) + (int) Math.floor(abstractionValue / 2.0));
		
		boolean isWater = false;
		boolean isPassable = true;
		boolean isExplored = true;
		boolean atLeastOneSet = false;
		
		for(int i = 0; i < abstractionValue && !isWater; i++) {
			for(int j = 0; j < abstractionValue && !isWater; j++) {
				 Point curPosInSubLevel = new Point(positionInSubjacentLevelRightmost.x + i, positionInSubjacentLevelRightmost.y + j);
				 LinkedTile curTile = worldMap.getTileAtMapIndex(curPosInSubLevel);
				 if(null != curTile) {
					 if(!isWater) {
						 isWater = curTile.isWater();
					 }
					 if(isPassable) {
						 isPassable = curTile.isPassable();
					 }
					 if(!isExplored){
						 isExplored = curTile.isExplored();
					 }
					 atLeastOneSet = true;
				 }
			}
		}
		LinkedTile newTile = null;
		if(atLeastOneSet) {
			newTile = new LinkedTile(position, isWater, isPassable, worldMap.getTileAtMapIndex(middlePositionInSubjacentLevel).getNormalVector(), true, abstractionLevel);
		}
		mapAbstraction.put(position, newTile);
		return newTile;
	}
}
