package engd_abm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Bag;
import sim.util.Int2D;
import sim.util.geo.GeomPlanarGraphDirectedEdge;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.planargraph.DirectedEdgeStar;
import com.vividsolutions.jts.planargraph.Node;

import ec.util.MersenneTwisterFast;

@SuppressWarnings("restriction")
public class EngDAStar {

	/**
	 * Assumes that both the start and end location are Centroids as opposed to
	 * LOCATIONS
	 * 
	 * @param start
	 * @param goal
	 * @return
	 */
	public static Network roadNetwork = EngDModelBuilder.engdModelSim.roadNetwork;
	public static MersenneTwisterFast random = new MersenneTwisterFast();

	static public EngDRoute astarPath(EngDCentroid start, EngDCentroid goal, NGOTeam team) {
		// initial check - same as Gridlock
		long startTime = System.currentTimeMillis();
		if (start == null || goal == null) {
			System.out.println("Error: invalid Centroid provided to AStar");
		}
		// containers for the metainformation about the Centroids relative to the A* search
		
		//Following in EngD:
		//ArrayList<GeomPlanarGraphDirectedEdge> result = new ArrayList<GeomPlanarGraphDirectedEdge>();
		HashMap<EngDCentroid, AStarCentroidWrapper> foundCentroids = new HashMap<EngDCentroid, AStarCentroidWrapper>();
		//HashMap<Node, AStarNodeWrapper> foundNodes = new HashMap<Node, AStarNodeWrapper>();
		
		AStarCentroidWrapper startCentroid = new AStarCentroidWrapper(start);
		//AStarNodeWrapper startNode = new AStarNodeWrapper(start);
		AStarCentroidWrapper goalCentroid = new AStarCentroidWrapper(goal);
		//AStarNodeWrapper goalNode = new AStarNodeWrapper(goal);
		foundCentroids.put(start, startCentroid);	//foundNodes.put(start, startNode);
		foundCentroids.put(goal, goalCentroid);	//foundNodes.put(goal, goalNode);

		startCentroid.gx = 0;	//startNode.gx = 0;
		startCentroid.hx = heuristic(start, goal); 
		startCentroid.fx = heuristic(start, goal);

		// A* containers: allRoadNodes to be investigated, allRoadNodes that have been investigated
		// Following in EngD:
		 	ArrayList<AStarCentroidWrapper> closedSet = new ArrayList<AStarCentroidWrapper>(),
	            openSet = new ArrayList<AStarCentroidWrapper>();
	        openSet.add(startCentroid);
	        
		//HashSet<AStarCentroidWrapper> closedSet = new HashSet<>(10000), openSet = new HashSet<>(10000);
		//PriorityQueue<AStarCentroidWrapper> openSetQueue = new PriorityQueue<>(10000);
		//openSet.add(startCentroid);
		//openSetQueue.add(startCentroid);
		while (openSet.size() > 0) {
			// while there are reachable allRoadCities to investigate
			AStarCentroidWrapper x = findMin(openSet);
			//AStarCentroidWrapper x = openSetQueue.peek();
			System.out.println("Peek: " + x.centroid.getName());
			if (x == null) {
				AStarCentroidWrapper n = findMin(openSet);
			}
			// find the shortest path so far
			if (x.centroid == goal) {
				//we have found the shortest possible path to the goal!
				//Reconstruct the path and send it back.
				if (x.cameFrom == null)
					System.out.println(x.centroid.getName());
				return reconstructRoute(goalCentroid, startCentroid, goalCentroid, team); //return reconstructPath(goalNode); 
			}
			openSet.remove(x);
			// maintain the lists
			//openSetQueue.remove();	//new
			closedSet.add(x);
			Bag edges = roadNetwork.getEdgesOut(x.centroid);
			for (Object o : edges) {
				Edge l = (Edge) o;
				//EngDCentroid next = null;
				EngDCentroid next = (EngDCentroid) l.from();
 				if (next == x.centroid)
					next = (EngDCentroid) l.to();

				// get the A* meta information about this City
				AStarCentroidWrapper nextCentroid;	//AStarNodeWrapper nextNode;
				if (foundCentroids.containsKey(next))	//if (foundNodes.containsKey(next))	{
					nextCentroid = foundCentroids.get(next);	//nextNode = foundNodes.get(next);
				else {
					nextCentroid = new AStarCentroidWrapper(next);
					foundCentroids.put(next, nextCentroid);
				}
				System.out.println(nextCentroid.centroid.getName());
				if (closedSet.contains(nextCentroid)) // it has already been considered
					continue;

				// otherwise evaluate the cost of this City/edge combo
				EngDRoadInfo edge = (EngDRoadInfo) l.getInfo();
				// System.out.println(edge.getWeightedDistance());
				double edgeweight = edge.getWeightedDistance() * EngDParameters.DISTANCE_WEIGHT
						+ edge.getSpeed() * EngDParameters.SPEED_WEIGHT - edge.getScaledPopulation() * EngDParameters.POP_WEIGHT
						+ edge.getScaledCost() * EngDParameters.COST_WEIGHT
						+ edge.getTransportLevel() * EngDParameters.TRANSPORT_LEVEL_WEIGHT
						+ edge.getDeaths() * EngDParameters.RISK_WEIGHT * team.vulnerableCare();
				// edgeweight = getWeightedDistance * 0.1
				//+ 1 * 0.1 -
				
				System.out.println(edge.getScaledPopulation());
				System.out.println("gx: " + x.gx + " edgeweight: " + edgeweight);
				double tentativeCost = x.gx + edgeweight; // changed from integer, still need to change the weighting of the edge weight
				boolean better = false;
				if (!openSet.contains(nextCentroid)) {
					openSet.add(nextCentroid);
					//openSetQueue.add(nextCentroid);	//New
					nextCentroid.hx = heuristic(next, goal);
					better = true;
				} else if (tentativeCost < nextCentroid.gx) {
					better = true;
				}

				// store A* information about this promising candidate City
				if (better) {
					nextCentroid.cameFrom = x;
					System.out.println("Edge: " + tentativeCost);
					nextCentroid.gx = tentativeCost;
					System.out.println("hx: " + nextCentroid.hx);
					nextCentroid.fx = nextCentroid.gx + nextCentroid.hx;
					System.out.println("gx: " + nextCentroid.gx + "fx: " + nextCentroid.fx);
				}
			}

		}
		return null;
		//return result;
	}

	/**
	 * Takes the information about the given Centroid n and returns the path that
	 * found it.
	 * @param n the end point of the path
	 * @return an Route from start to goal
	 */
	static EngDRoute reconstructRoute(AStarCentroidWrapper n, AStarCentroidWrapper start, AStarCentroidWrapper end,
			NGOTeam team) {
		//In EngD: new ArrayList<GeomPlanarGraphDirectedEdge>();
		List<Int2D> locations = new ArrayList<Int2D>(100);
		List<Edge> edges = new ArrayList<Edge>(100);
	
		// double mod_speed = speed;
		double totalDistance = 0;
		AStarCentroidWrapper x = n; //Same in EngD

		// start by adding the last one
		locations.add(0, x.centroid.location);
		Edge edge = null;

		if (x.cameFrom != null) {	//while loop in EngD // != 'Not equal to'
			edge = (Edge) roadNetwork.getEdge(x.cameFrom.centroid, x.centroid);
			edges.add(0, edge);
			EngDRoadInfo edgeInfo = (EngDRoadInfo) edge.getInfo();
			//RoadInfo edge = (RoadInfo) roadNetwork.getEdge(x.cameFrom.city, x.city).getInfo();
			double mod_speed = edgeInfo.getSpeed() * EngDParameters.TEMPORAL_RESOLUTION;// now km per step
			// convert speed to cell block per step
			mod_speed = EngDParameters.convertFromKilometers(mod_speed);
			// System.out.println("" + mod_speed);
			AStarCentroidWrapper to = x;
			x = x.cameFrom;	//Same in EngD

			while (x != null) {		
				double dist = x.centroid.location.distance(locations.get(0));
				edge =  roadNetwork.getEdge(x.centroid, to.centroid);
				 edgeInfo = (EngDRoadInfo) edge.getInfo();
				mod_speed = edgeInfo.getSpeed() * EngDParameters.TEMPORAL_RESOLUTION;// now km per step
				// convert speed to cell block per step
				mod_speed = EngDParameters.convertFromKilometers(mod_speed);

				while (dist > mod_speed) {
					locations.add(0, getPointAlongLine(locations.get(0), x.centroid.location, mod_speed / dist));
					//System.out.println(x.city.getName());
					edges.add(0, edge);
					dist = x.centroid.location.distance(locations.get(0));
				}
                locations.add(0, getPointAlongLine(locations.get(0), x.centroid.location, 1)); //**CRUCIAL***
                edges.add(0,  edge);
				to = x;
				x = x.cameFrom;
				if (x != null && x.cameFrom != null)								// != 'Not equal to'
					totalDistance += x.centroid.location.distance(x.cameFrom.centroid.location);
			}
		}
		else{
		edges.add(0, edge);	
		
		}
		//locations.add(0, start.city.location);
		edges.add(0, edge);
		//In EngD: return result;
		return new EngDRoute(locations, edges, totalDistance, start.centroid, end.centroid, EngDParameters.WALKING_SPEED);
		//return new Route(locations, totalDistance, start.city, end.city, Parameters.WALKING_SPEED);
	}
	
	/**
	 * NOT in EngD/Gridlock
	 * Gets a point a certain percent a long the line
	 * @param start
	 * @param end
	 * @param percent the percent along the line you want to get. Must be less than 1
	 * @return
	 */
	public static Int2D getPointAlongLine(Int2D start, Int2D end, double percent) {
		return new Int2D((int) Math.round((end.getX() - start.getX()) * percent + start.getX()),
				(int) Math.round((end.getY() - start.getY()) * percent + start.getY()));
	}

	/**
	 * Measure of the estimated distance between two Cities.
	 * Extremely basic, just Euclidean distance as implemented here.
	 * @param x
     * @param y
	 * @return notional "distance" between the given allRoadCities.
	 */
	static double heuristic(EngDCentroid x, EngDCentroid y) {
		return x.location.distance(y.location) * EngDParameters.HEU_WEIGHT;
	}
	
	/**
	 * Considers the list of Cities open for consideration and returns the City
	 * with minimum fx value
	 * 
	 * @param set
	 *            list of open Cities
	 * @return
	 */
	static AStarCentroidWrapper findMin(Collection<AStarCentroidWrapper> set) {
		double min = Double.MAX_VALUE;	//double min = 100000;
		AStarCentroidWrapper minCentroid = null;	//minNode
		for (AStarCentroidWrapper n : set) {
			if (n.fx < min) {
				min = n.fx;
				minCentroid = n;
			}
		}
		return minCentroid;
	}

	/**
	 * A wrapper to contain the A* meta information about the Cities
	 *
	 */
	static class AStarCentroidWrapper implements Comparable<AStarCentroidWrapper> {
		// the underlying City associated with the metainformation
		EngDCentroid centroid;	//Node node;
		// the City from which this City was most profitably linked
		AStarCentroidWrapper cameFrom;	//edgeFrom;
		double gx, hx, fx;

		public AStarCentroidWrapper(EngDCentroid n) { //public AStarNodeWrapper(Node n)	{
			centroid = n;	//node = n;
			gx = 0;
			hx = 0;
			fx = 0;
			cameFrom = null;
			//edgeFrom = null;
		}

		@Override
		public int compareTo(AStarCentroidWrapper aStarNodeWrapper) {
			return Double.compare(this.fx, aStarNodeWrapper.fx);
		}
	}
}