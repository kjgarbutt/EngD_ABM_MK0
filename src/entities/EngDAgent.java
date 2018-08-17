package entities;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.network.Edge;
import sim.util.Int2D;
import sim.util.geo.GeomPlanarGraphEdge;
import sim.util.geo.MasonGeometry;

public class EngDAgent implements Steppable {
	private int sex; // 0 male, 1 female
	private NGOTeam team;
	private MasonGeometry location;
	private int shiftStatus = 1; // default 1 (on shift), off shift = 0

	public EngDAgent(int sex, NGOTeam team) {
		this.sex = sex;
		this.team = team;
		//location.isMovable = true;
	}

	public int getShiftStatus() {
		return shiftStatus;
	}

	public void setShiftStatus(int status) {
		this.shiftStatus = status;
	}

	public MasonGeometry getLocation() {
		return location;
	}

	public void setLocation(MasonGeometry location) {
		this.location = location;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public NGOTeam getTeam() {
		return team;
	}

	public void setTeam(NGOTeam team) {
		this.team = team;
	}

	@Override
	public void step(SimState arg0) {
		// TODO Auto-generated method stub

	}

}