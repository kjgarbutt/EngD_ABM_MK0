package engd_abm;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import sim.util.Int2D;

class NGOHQ {
	Int2D ngoLocation;
	private int ngoQuota; // 1
	private int ngoID;
	private double ngoTeamPresence; // 2
	private HashSet<NGOTeam> ngoagents;
	private int ngoDepartures;
	private int ngoArrivals;

	// need name, get name, set name
	protected HashMap<NGOHQ, EngDRoute> cachedPaths;

	public NGOHQ(Int2D ngolocation, int ngoid, int ngoquota, double ngoTeamPresence) {
		this.ngoLocation = ngolocation;
		this.ngoID = ngoid;
		this.ngoQuota = ngoquota;
		this.ngoTeamPresence = ngoTeamPresence;
		this.ngoagents = new HashSet<NGOTeam>();
		this.ngoDepartures = 0;
	}

	public Int2D getNGOLocation() {
		return ngoLocation;
	}

	public void setNGOLocation(Int2D ngolocation) {
		this.ngoLocation = ngolocation;
	}


	public int getAgentPopulation() {
		return ngoagents.size();
	}

	public HashSet<NGOTeam> getAgents() {
		return ngoagents;
	}

	public int getNGOQuota() {
		return ngoQuota;
	}

	public void setNGOQuota(int ngoquota) {
		this.ngoQuota = ngoquota;
	}

	public int getNGOID() {
		return ngoID;
	}

	public void setNGOID(int ngoid) {
		this.ngoID = ngoid;
	}

	public int getNGODepartures(){
		return ngoDepartures;
	}

	public int getNGOArrivals(){
		return ngoArrivals;
	}

	public double getNGOTeamPresence() {
		return ngoTeamPresence;
	}

	public void setNGOTeamPresence(double ngoTeamPresence) {
		this.ngoTeamPresence = ngoTeamPresence;
	}

	public void addNGOTeam(NGOTeam r) {
		ngoagents.add(r);
		ngoArrivals++;
	}

	public void removeNGOTeam(NGOTeam r){
		if (ngoagents.remove(r))
			ngoDepartures ++;
	}

}