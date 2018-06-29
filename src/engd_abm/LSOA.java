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
	private int origin;
	private double scaledPop;
	private int pop;
	private double violence; // 2
	private double economy; // 3
	private double familyPresence; // 2
	private HashSet<EngDAgent> refugees;
	private int departures;
	private int arrivals;

	// need name, get name, set name
	protected HashMap<LSOA, EngDRoute> cachedPaths;

	//public Lsoa(Int2D location, int ID, String name, int origin, double scaledPop, int pop, int quota) {public Lsoa(Int2D location, int ID, String name, int origin, double scaledPop, int pop, int quota) {
	public LSOA(Int2D location, int ID, String name) {
		this.name = name;
		this.location = location;
		this.ID = ID;
		this.scaledPop = scaledPop;
		this.pop = pop;
		this.quota = quota;
		this.violence = violence;
		this.economy = economy;
		this.familyPresence = familyPresence;
		this.origin = origin;
		this.refugees = new HashSet<EngDAgent>();
		this.departures = 0;
	}

	public Int2D getLocation() {
		return location;
	}

	public void setLocation(Int2D location) {
		this.location = location;
	}

	public int getOrigin() {
		return origin;
	}

	public void setOrigin(int origin) {
		this.origin = origin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getScaledPopulation() {
		return scaledPop;
	}

	public double getPopulation() {
		return pop;
	}

	public int getAgentPopulation() {
		return refugees.size();
	}

	public HashSet<EngDAgent> getAgents() {
		return refugees;
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

	public double getTeamPresence() {
		return familyPresence;
	}
	
	public void setTeamPresence(double teamPresence) {
		this.familyPresence = teamPresence;
	}

	/*public void addMembers(Bag people) {
		refugees.addAll(people);
	}*/

	public void addMember(EngDAgent r) {
		refugees.add(r);
		arrivals++;
	}

	/*public void removeMembers(Bag people){
		refugees.remove(people);
		passerbyCount += people.size();
	}*/

	public void removeMember(EngDAgent r){
		if (refugees.remove(r))
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
		return refugees.size() * 1.0 / (EngDParameters.TOTAL_POP);
	}

}