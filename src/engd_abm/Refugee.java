package engd_abm;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.network.Edge;
import sim.util.Int2D;
import sim.util.geo.MasonGeometry;

class Refugee {
	private int sex; // 0 male, 1 female
	private RefugeeFamily family;
	private Int2D location;
	private int shiftStatus = 1; // default 1 (on shift), off shift = 0
	
	public Refugee (int sex, RefugeeFamily team) {
		this.sex = sex;
		this.family = family;
	}

	public int getShiftStatus() {
		return shiftStatus;
	}

	public void setShiftStatus(int status) {
		this.shiftStatus = status;
	}
	
	public Int2D getLocation() {
		return location;
	}	
	public void setLocation(Int2D location) {
		this.location = location;
	}
	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public RefugeeFamily getFamily() {
		return family;
	}

	public void setFamily(RefugeeFamily family) {
		this.family = family;
	}
	
}