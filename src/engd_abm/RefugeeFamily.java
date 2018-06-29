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

public class RefugeeFamily implements Steppable{
	private Int2D location;
	private Bag familyMembers;
	private EngDRoute route;
	private int routePosition;
	private double stockStatus;
	private City home;
	private Edge currentEdge;
	private City currentCity;
	private City goal;
	static MersenneTwisterFast random = new MersenneTwisterFast();
	private boolean isMoving;
	private HashMap<EngDRoute, Integer> cachedRoutes;
	private HashMap<City, Integer> cachedGoals;
	private boolean goalChanged;
	
	public RefugeeFamily(Int2D location, int teamSize, City lsoa) {
	//public EngDNGO(Int2D location, int size, Lsoa lsoa, double stockStatus) {
		this.location = location;
		this.home = lsoa;
		this.goal = lsoa;
		//this.stockStatus = stockStatus;
		familyMembers = new Bag();
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
		Bag cities = engdModelSim.cities;
		City goalCity = calcGoalLsoa(cities);
		
		if (this.location == goalCity.location) {									// == 'Equal to'
			goal = goalCity;
			isMoving = false;
			// if the current location is the goalCity location, then agent has arrived so isn't moving
		} else if (isMoving == false)												// == 'Equal to'
			return;
		else {
			if (this.getLocation().getX() != goal.getLocation().getX() 				// != 'Not equal to'
					|| this.getLocation().getY() != goal.getLocation().getY()) { 	// ||'Conditional-OR'
				System.out.println("Home: " + this.getHome().getName()
						+ " | Goal " + goal.getName());
				System.out.println(this + " Current: " + currentCity.getName());
				if (currentCity.getName() == goal.getName()							// == 'Equal to'
						&& this.getLocation() != goal.getLocation()) { 				// && 'Conditional-AND'
					System.out.println("-----HERE------");
					currentCity = (City) currentEdge.to();
				}
				// setGoal(currentCity, goal);
				route = calcRoute(currentCity, goal);								// Astar inside here
				// System.out.println(route);
				if (route == null) { 												// == 'Equal to'
					System.out.println("No route found:");
					return;
				}
				// System.out.println(route);
				int index = route.getLocIndex(this.location);
				int newIndex = 0;
				if (index != -1) {	// if already on the route (in between cities)	// != 'Not equal to'
					newIndex = index + 1;
					System.out.println("ALREADY ON: " + newIndex);
				} else {// new route
					newIndex = 1;
					System.out.println("NEW");
				}
				Edge edge = route.getEdge(newIndex);
				EngDRoadInfo edgeinfo = (EngDRoadInfo) edge.getInfo();
				if (this.stockStatus - edgeinfo.getCost() < 0) {
					isMoving = false;
				} else {
					Int2D nextStep = route.getLocation(newIndex);
					this.setLocation(nextStep);
					updatePositionOnMap(engdModelSim);
					// System.out.println(route.getNumSteps() + ", " +
					// route.getNumEdges());
					this.currentEdge = edge;
					//determineDeath(edgeinfo, this);
					route.printRoute();
				}

				City lsoa = (City) currentEdge.getTo();
				if (this.location.getX() == lsoa.getLocation().getX()				// == 'Equal to'
						&& this.location.getY() == lsoa.getLocation().getY()) {		// == 'Equal to'
					currentCity = home;
					EngDRoadInfo einfo = (EngDRoadInfo) this.currentEdge.getInfo();
					this.stockStatus -= (einfo.getCost() * this.familyMembers
							.size());
							// finStatus = finStatus - einfo.getCost() * this.familyMembers
							// if at the end of an edge, subtract the money
					// city.addMembers(this.familyMembers);
					for (Object or : this.familyMembers) {
						Refugee rr = (Refugee) or;
						lsoa.addMember(rr);
					}
				}

				else {
					for (Object l : cities) {
						City cremove = (City) l;
						for (Object o : this.familyMembers) {
							Refugee r = (Refugee) o;
							cremove.removeMember(r);
						}
					}
				}
			}
		}
		// }
		System.out.println(this.location.x + ", " + this.location.y);
	}		
	
	public City calcGoalLsoa(Bag lsoalist) { // returns the best city
		City bestLsoa = null;
		double max = 0.0;
		for (Object lsoa : lsoalist) {
			City l = (City) lsoa;
			double lsoaDesirability = dangerCare() 
					//* l.getViolence()			// dangerCare calculated below 
					//+ 2 * NGOHQ.getTeamPresence()
					//+ l.getEconomy() * (EngDParameters.ECON_CARE + random.nextDouble() / 4)
					+ l.getScaledPopulation() * (EngDParameters.POP_CARE + random.nextDouble() / 4);
			//if (l.getAgentPopulation() + 
			if	(familyMembers.size() >= l.getQuota()) // if reached quota, desirability is 0
				lsoaDesirability = 0;
			if (lsoaDesirability > max) {
				max = lsoaDesirability;
				bestLsoa = l;
			}

		}
		return bestLsoa;
	}
	
	private void setGoal(City from, City to) {
		this.goal = to;
	}
	
	private EngDRoute calcRoute(City from, City to) {
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
			Refugee r = (Refugee) o;
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
	
	public double dangerCare() {// 0-1, young, old, or has family weighted more
		double dangerCare = 0.5;
		for (Object o : this.familyMembers) {
			Refugee e = (Refugee) o;
			if (e.getSex() <= 1 || e.getSex() <= 0) {	//if refugee is under 12 OR over 60
				dangerCare += EngDParameters.DANGER_CARE_WEIGHT
						* random.nextDouble();
				// adds Parameters.DANGER_CARE_WEIGHT * random.nextDouble() to
				// dangerCare
			}
		}
		return dangerCare;
	}
	
	public Int2D getLocation() {
		return location;
	}

	public void setLocation(Int2D location) {
		this.location = location;
		for (Object o : this.familyMembers) {
			Refugee r = (Refugee) o;
			r.setLocation(location);
		}
	}
	
	public double getStockStatus() {
		return stockStatus;
	}

	public void setStockStatus(int stockStatus) {
		this.stockStatus = stockStatus;
	}
	
	public City getGoal() {
		return goal;
	}

	public void setGoal(City goal) {
		this.goal = goal;
	}

	public City getHome() {
		return home;
	}

	public void setCurrent(City current) {
		this.currentCity = current;
	}

	public Bag getTeam() {
		return familyMembers;
	}

	public void setTeam(Bag team) {
		this.familyMembers = team;
	}
	
}