package engd_abm;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import sim.util.Int2D;

class LSOA {
	Int2D location;
	String name;
	private int quota; // 1
	private int ID;
	private int pop;
	private HashSet<EngDAgent> agent;
	private int departures;
	private int arrivals;

	// need name, get name, set name
	protected HashMap<LSOA, EngDRoute> cachedPaths;

	//public Lsoa(Int2D location, int ID, String name, int origin, double scaledPop, int pop, int quota) {public Lsoa(Int2D location, int ID, String name, int origin, double scaledPop, int pop, int quota) {
	public LSOA(Int2D location, int ID, String name, int pop) {
		this.name = name;
		this.location = location;
		this.ID = ID;
		this.pop = pop;
		this.agent = new HashSet<EngDAgent>();
		this.departures = 0;
	}

	public Int2D getLocation() {
		return location;
	}

	public void setLocation(Int2D location) {
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPopulation() {
		return pop;
	}

	public int getAgentPopulation() {
		return agent.size();
	}

	public HashSet<EngDAgent> getAgents() {
		return agent;
	}

	public int getQuota() {
		return quota;
	}

	public void setQuota(int quota) {
		this.quota = quota;
	}
	
	public int getID() {
		// TODO Auto-generated method stub
		return ID;
	}

	public int setID(int ID) {
		return ID;
	}

	public int getDepartures(){
		return departures;
	}

	public int getArrivals(){
		return arrivals;
	}

	/*public void addMembers(Bag people) {
		refugees.addAll(people);
	}*/

	public void addMember(EngDAgent r) {
		agent.add(r);
		arrivals++;
	}

	/*public void removeMembers(Bag people){
		refugees.remove(people);
		passerbyCount += people.size();
	}*/

	public void removeMember(EngDAgent r){
		if (agent.remove(r))
			departures ++;
	}

	public void cacheRoute(EngDRoute route, LSOA destination) {
		cachedPaths.put(destination, route);
	}

	public Map<LSOA, EngDRoute> getCachedRoutes() {
		return cachedPaths;
	}

	public EngDRoute getRoute(LSOA destination, NGOTeam team) {
		EngDRoute route;

		route = EngDAStar.astarPath(this, destination, team);
		//System.out.println(route.getNumSteps());

		return route;
	}

	public double getScale(){
		return agent.size() * 1.0 / (EngDParameters.TOTAL_POP);
	}

}