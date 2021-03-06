package memory.pathcalulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import map.fastmap.LinkedTile;
import memory.map.MemorizedMap;


public class Path<T extends LinkedTile> implements Iterable<T>{
	
	protected List<T> waypoints;
	protected AStarPathCalculator pathCalculator;
	protected MemorizedMap map;
	protected T lastVisitedWaypoint = null;
	
	protected boolean isCircleCourse = false;

	public void setCircleCourse(boolean isCircleCourse) {
		this.isCircleCourse = isCircleCourse;
	}
	


	public Path() {
		waypoints = new ArrayList<T>();
		this.map = new MemorizedMap();
		this.pathCalculator = new AStarPathCalculator(map);
	}
	
	public Path(AStarPathCalculator pathCalculator, MemorizedMap map) {
		waypoints = new ArrayList<T>();
		this.pathCalculator = pathCalculator;
		this.map = map;
	}
	
	public void addWaypoint(T waypoint) {
		waypoints.add(waypoint);
	}
	
	public void addWaypointToFront(T waypoint) {
		waypoints.add(0, waypoint);
	}
	
	public void appendPath(Path<T> path) {
		if(this.waypoints.size() > 0 && path.waypoints.size() > 0){
			if(this.waypoints.get(this.waypointCount()-1).equals(path.waypoints.get(0))) {
				this.waypoints.remove(this.waypointCount()-1);
			}
		}
		this.waypoints.addAll(path.waypoints);
	}
	
	public int waypointCount() {
		return waypoints.size();
	}
	
	public T getNextWaypoint() {
		if(!this.isEmpty()){
			T waypoint = waypoints.get(0);
			
			if(0 < waypoint.getPrecisionLevel()) {
				increasePrecisionOfPathHead();
				waypoint = waypoints.get(0);
			}
			
			waypoints.remove(0);
			lastVisitedWaypoint = waypoint;
			
			if(isCircleCourse) {
				this.addWaypoint(waypoint);
			}
			return waypoint;
		}
		return null;
	}
	
	public Iterator<T> getIterator() {
		return waypoints.iterator();
	}
	
	public boolean isEmpty() {
		return waypoints.isEmpty();
	}
	
	public String toString() {
		String output = "";
		Iterator<T> it = waypoints.iterator();
		while(it.hasNext()) {
			output += it.next() + " -> ";
		}
		return output;
	}

	public Iterator<T> iterator() {
		return waypoints.iterator();
	}
	
	protected void increasePrecisionOfPathHead() {
		T start;
		T intermediateTarget;
		
		if(null == lastVisitedWaypoint) {
			if(3 <= waypointCount()) {
				start = waypoints.get(0);
				intermediateTarget = waypoints.get(2);
				
				waypoints.remove(0);
				waypoints.remove(1);
				waypoints.remove(2);
			} else if(2 == waypointCount()) {
				start = waypoints.get(0);
				intermediateTarget = waypoints.get(1);
				
				waypoints.remove(0);
				waypoints.remove(1);
			} else {
				T onlyLeftWaypoint = waypoints.get(0);
				waypoints.remove(0);
				waypoints.add((T) map.convertTileToBase(onlyLeftWaypoint));
				return;
			}
		} else {
			if(2 <= waypointCount()) {
				start = lastVisitedWaypoint;
				intermediateTarget = waypoints.get(1);
				
				waypoints.remove(0);
				waypoints.remove(1);
			} else if(1 == waypointCount()) {
				start = lastVisitedWaypoint;
				intermediateTarget = waypoints.get(0);
				
				waypoints.remove(0);
			} else {
				return;
			}
		}
		
		start = (T) map.convertTileToBase(start);
		intermediateTarget = (T) map.convertTileToBase(intermediateTarget);
		
		Path<T> precisePath = (Path<T>) pathCalculator.calculatePath(start, intermediateTarget);
		precisePath.waypoints.addAll(this.waypoints);
		this.waypoints = precisePath.waypoints;
	}
}
