package entities;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.planargraph.Node;

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
	Node homeNode = null;
	private int homeTract;
	private int shiftStatus = 1; // default 1 (on shift), off shift = 0

	public EngDAgent() {
		GeometryFactory fact = new GeometryFactory();
		location = new MasonGeometry(fact.createPoint(new Coordinate(10, 10))) ;
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

	}

	public MasonGeometry getGeometry() {
		return location;
	}

}