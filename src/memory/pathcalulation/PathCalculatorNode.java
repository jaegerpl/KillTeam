package memory.pathcalulation;

import map.fastmap.LinkedTile;


public class PathCalculatorNode implements Comparable<PathCalculatorNode> {
	
	protected LinkedTile coordinate;
	protected float h_approximatedDistanceToDestination;
	protected float g_distanceToStart;
	protected PathCalculatorNode predesessor = null;
	

	public PathCalculatorNode(LinkedTile coordinate, int h_approximatedDistance, int g_distanceToStart) {
		super();
		this.coordinate = coordinate;
		this.h_approximatedDistanceToDestination = h_approximatedDistance;
		this.g_distanceToStart = g_distanceToStart;
	}

	public float getH() {
		return h_approximatedDistanceToDestination;
	}

	public void setH(float approximatedDistance) {
		this.h_approximatedDistanceToDestination = approximatedDistance;
	}
	
	public float getG() {
		return g_distanceToStart;
	}

	public void setG(float g_distanceToStart) {
		this.g_distanceToStart = g_distanceToStart;
	}
	
	public float getF() {
		return g_distanceToStart + h_approximatedDistanceToDestination;
	}

	public PathCalculatorNode getPredesessor() {
		return predesessor;
	}

	public void setPredesessor(PathCalculatorNode predessesor) {
		this.predesessor = predessesor;
	}

	public int compareTo(PathCalculatorNode other) {		
		if(getF() == other.getF()) {
			return 0;
		}
		if(getF() > other.getF()) {
			return 1;
		}
		return -1;
	}

	public LinkedTile getCoordinate() {
		return coordinate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((coordinate == null) ? 0 : coordinate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {			
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PathCalculatorNode other = (PathCalculatorNode) obj;
		if (coordinate == null) {
			if (other.coordinate != null)
				return false;
		} else if (!coordinate.equals(other.coordinate))
			return false;
		return true;
	}

}
