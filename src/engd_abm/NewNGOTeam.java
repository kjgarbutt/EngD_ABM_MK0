package engd_abm;

import java.awt.Color;
import ec.util.MersenneTwisterFast;

import java.util.ArrayList;
import java.util.HashMap;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.network.Edge;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.geo.MasonGeometry;

public class NewNGOTeam implements Steppable{
	private MasonGeometry location;
	private Bag teamMembers;
	private EngDRoute route;
	private int routePosition;
	private double stockStatus;
	private EngDCentroid home;
	private Edge currentEdge;
	private EngDCentroid currentCity;
	private EngDCentroid goal;
	static MersenneTwisterFast random = new MersenneTwisterFast();
	private boolean isMoving;
	private HashMap<EngDRoute, Integer> cachedRoutes;
	private HashMap<EngDCentroid, Integer> cachedGoals;
	private boolean goalChanged;
	
	public NewNGOTeam(MasonGeometry location, int teamSize, EngDCentroid engdCentroid) {
	//public EngDNGO(Int2D location, int size, Lsoa lsoa, double stockStatus) {
		this.location = location;
		this.home = engdCentroid;
		this.goal = engdCentroid;
		//this.stockStatus = stockStatus;
		teamMembers = new Bag();
		currentCity = home;
		isMoving = true;
		// routePosition = 0;
		cachedRoutes = new HashMap<EngDRoute, Integer>();
		goalChanged = false;
	}

	@Override
	public void step(SimState state) {
		System.out.println();
		EngDModel engdModelSim = (EngDModel) state;
		Bag lsoacentroids = engdModelSim.lsoacentroids;
		EngDCentroid goalCentroid = calcGoalCentroid(lsoacentroids);
		
		if (this.location == goalCentroid.location) {									// == 'Equal to'
			goal = goalCentroid;
			isMoving = false;
			// if the current location is the goalCity location, then agent has arrived so isn't moving
		}
		else if (stockStatus <= 0.0) {
			System.out.println("<<< NO STOCK >>>");
			return;}
		else if (isMoving == false)													// == 'Equal to'
			return;
		else {
			if (this.getLocation().getX() != goal.getLocation().getX() 				// != 'Not equal to'
					|| this.getLocation().getY() != goal.getLocation().getY()) { 	// ||'Conditional-OR'
				System.out.println("Home: " + this.getHome().getName()
						+ " | Goal " + goal.getName());
				System.out.println(this + " Current: " + currentCity.getName());
				if (currentCity.getName() == goal.getName()							// == 'Equal to'
						&& this.getLocation() != goal.getLocation()) { 				// && 'Conditional-AND'
					System.out.println("<<< HERE >>>");
					currentCity = (EngDCentroid) currentEdge.to();
				}
				// setGoal(currentCity, goal);
				route = calcRoute(currentCity, goal);								// Astar inside here
				// System.out.println(route);
				if (route == null) { 												// == 'Equal to'
					System.out.println("<<< NO ROUTE FOUND >>>");
					return;
				}
				// System.out.println(route);
				int index = route.getLocIndex(this.location);
				int newIndex = 0;
				if (index != -1) {	// if already on the route (in between cities)	// != 'Not equal to'
					newIndex = index + 1;
					System.out.println("<<< ALREADY ON: " + newIndex + ">>>");
				} else {// new route
					newIndex = 1;
					System.out.println("<<< NEW ROUTE >>>");
				}
				Edge edge = route.getEdge(newIndex);
				EngDRoadInfo edgeinfo = (EngDRoadInfo) edge.getInfo();
				if (this.stockStatus - edgeinfo.getCost() < 0) {
					isMoving = false;
				} else {
					MasonGeometry nextStep = route.getLocation(newIndex);
					this.setLocation(nextStep);
					updatePositionOnMap(engdModelSim);
					// System.out.println(route.getNumSteps() + ", " +
					// route.getNumEdges());
					this.currentEdge = edge;
					//determineDeath(edgeinfo, this);
					route.printRoute();
				}

				EngDCentroid lsoa = (EngDCentroid) currentEdge.getTo();
				if (this.location.getX() == lsoa.getLocation().getX()				// == 'Equal to'
						&& this.location.getY() == lsoa.getLocation().getY()) {		// == 'Equal to'
					currentCity = home;
					EngDRoadInfo einfo = (EngDRoadInfo) this.currentEdge.getInfo();
					this.stockStatus -= (einfo.getCost() * this.teamMembers
							.size());
							// finStatus = finStatus - einfo.getCost() * this.familyMembers
							// if at the end of an edge, subtract the money
					// city.addMembers(this.familyMembers);
					for (Object or : this.teamMembers) {
						EngDAgent rr = (EngDAgent) or;
						lsoa.addMember(rr);
					}
				}

				else {
					for (Object l : lsoacentroids) {
						EngDCentroid cremove = (EngDCentroid) l;
						for (Object o : this.teamMembers) {
							EngDAgent r = (EngDAgent) o;
							cremove.removeMember(r);
						}
					}
				}
			}
		}
		// }
		System.out.println(this.location.x + ", " + this.location.y);
	}		
	
	public EngDCentroid calcGoalCentroid(Bag centroidlist) { // returns the best centroid
		EngDCentroid bestLsoa = null;
		double max = 0.0;
		for (Object lsoa : centroidlist) {
			EngDCentroid l = (EngDCentroid) lsoa;
			double lsoaDesirability = vulnerableCare() 
					//* l.getViolence()			// dangerCare calculated below 
					//+ 2 * NGOHQ.getTeamPresence()
					//+ l.getEconomy() * (EngDParameters.ECON_CARE + random.nextDouble() / 4)
					+ l.getAgentPopulation() * (EngDParameters.POP_CARE + random.nextDouble() / 4);
			//if (l.getAgentPopulation() + 
			if	(teamMembers.size() >= l.getQuota()) // if reached quota, desirability is 0
				lsoaDesirability = 0;
			if (lsoaDesirability > max) {
				max = lsoaDesirability;
				bestLsoa = l;
			}

		}
		return bestLsoa;
	}
	
	private void setGoal(EngDCentroid from, EngDCentroid to) {
		this.goal = to;
	}
	
	private EngDRoute calcRoute(EngDCentroid from, EngDCentroid to) {
		EngDRoute newRoute = from.getRoute(to, this);

		if (goalChanged)
			return newRoute;
		else
			return this.route;
	}

	public void updatePositionOnMap(EngDModel engdModelSim) {
		// migrationSim.world.setObjectLocation(this.getFamily(), new
		// Double2D(location.getX() , location.getY() ));
		for (Object o : this.getTeam()) {
			EngDAgent r = (EngDAgent) o;
			double randX = 0;// migrationSim.random.nextDouble() * 0.3;
			double randY = 0;// migrationSim.random.nextDouble() * 0.3;
			// System.out.println("Location: " + location.getX() + " " +
			// location.getY());
			engdModelSim.world.setObjectLocation(r,
					new Double2D(location.getX() + randX / 10, location.getY()
							+ randY / 10));
			// migrationSim.worldPopResolution.setObjectLocation(this,
			// (int)location.getX()/10, (int)location.getY()/10);
		}
	}
	
	public double vulnerableCare() {	// 0-1, young, old, or has family weighted more
		double dangerCare = 0.5;
		for (Object o : this.teamMembers) {
			EngDAgent e = (EngDAgent) o;
			if (e.getSex() <= 1 || e.getSex() <= 0) {
				dangerCare += EngDParameters.DANGER_CARE_WEIGHT
						* random.nextDouble();
				// adds Parameters.DANGER_CARE_WEIGHT * random.nextDouble() to
				// dangerCare
			}
		}
		return dangerCare;
	}
	
	public MasonGeometry getLocation() {
		return location;
	}

	public void setLocation(MasonGeometry location) {
		this.location = location;
		for (Object o : this.teamMembers) {
			EngDAgent r = (EngDAgent) o;
			r.setLocation(location);
		}
	}
	
	public double getStockStatus() {
		return stockStatus;
	}

	public void setStockStatus(int stockStatus) {
		this.stockStatus = stockStatus;
	}
	
	public EngDCentroid getGoal() {
		return goal;
	}

	public void setGoal(EngDCentroid goal) {
		this.goal = goal;
	}

	public EngDCentroid getHome() {
		return home;
	}

	public void setCurrent(EngDCentroid current) {
		this.currentCity = current;
	}

	public Bag getTeam() {
		return teamMembers;
	}

	public void setTeam(Bag team) {
		this.teamMembers = team;
	}
	
}