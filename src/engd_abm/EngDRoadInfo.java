package engd_abm;

import sim.util.Int2D;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

class EngDRoadInfo {
	private Geometry path;// real-world path geometry
	private int from; // from City ID
	private int to; // to City ID
	private double distance;
	private double speed;
	private int population;
	private double spop;
	private double cost;
	private double transportLevel;
	private double deaths;

	public EngDRoadInfo(Geometry path, int from, int to, double speed,
			double spop, double distance, double cost, double transportLevel,
			double deaths) {
		this.population = 0; // starts with no one travelling
		this.path = path;
		this.from = from;
		this.to = to;
		this.spop = spop;
		this.speed = speed;
		this.distance = distance;
		this.cost = cost;
		this.transportLevel = transportLevel;
		this.deaths = deaths;

	}

	/**
	 * @return next location to move, null if no more moves
	 */
	/*
	 * public Int2D getLocation(int index) { Int2D location = path.get(index);
	 * return location; }
	 */
	public void addPeople(int n) {
		this.population += n;
	}

	public double getDistance() {
		return distance;
	}

	public double getWeightedDistance() {

		return (this.distance - EngDParameters.MIN_EDGE_LENGTH)
				/ (EngDParameters.MAX_EDGE_LENGTH - EngDParameters.MIN_EDGE_LENGTH);
	}

	public double getSpeed() {
		return speed;
	}

	public double getScaledCost() {
		return (this.cost - EngDParameters.MIN_EDGE_COST)
				/ (EngDParameters.MAX_EDGE_COST - EngDParameters.MIN_EDGE_COST);
	}

	public double getScaledPopulation() {
		return spop;
	}

	public double getCost() {
		return cost;
	}

	public double getTransportLevel() {
		return transportLevel;
	}

	public double getDeaths() {
		return deaths;
	}

	public Geometry getPaths() {
		return path;
	}
	/*
	 * public void addToPaths( Int2D node) { path.add(node); }
	 */
}