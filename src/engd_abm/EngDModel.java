package engd_abm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Network;
import sim.util.Bag;

class EngDModel extends SimState {
	
	public Continuous2D world;
	
	public SparseGrid2D cityGrid;
	public Network roadNetwork = new Network();
	public GeomVectorField centroids;
	public GeomVectorField boundary;
	public GeomVectorField lsoa;
	public static GeomVectorField roads;
	public GeomVectorField flood2;
	public GeomVectorField flood3;
	public GeomVectorField centroidPoints;
	//public GeomVectorField adminBoundaries;
	//public GeomVectorField osvi;
	//public SparseGrid2D allRoadNodes;
	public DoubleGrid2D road_cost; //Accumulated cost to get to nearest node on the road network
	
	public int pop_width;
	public int pop_height;
	public int world_width;
	public int world_height;
	public int total_scaled_pop = 0;
	public long total_pop = 0;
	
	public Bag agents;
	public Bag agentTeams;
	public Bag newAgents;
	public Bag lsoacentroids = new Bag();
	public Map<Integer, EngDCentroid> cityList = new HashMap<>();
	ArrayList<Polygon> polys = new ArrayList<Polygon>();
	
	public EngDModel(long seed) {
		super(seed);
	}
	
	public void start() {
		super.start();
		agents = new Bag();
		agentTeams = new Bag();
		newAgents = new Bag();
		//Bag ps = lsoa.getGeometries();
        //polys.addAll(ps);
		EngDModelBuilder.initializeWorld(this);
	}
	
	public void finish() {
		System.out.println("Simulation ended by user.");
		super.finish();
	}
	
	public static void main(String[] args) {
		{
			long seed = System.currentTimeMillis();
			EngDModel simState = new EngDModel(seed);
			long io_start = System.currentTimeMillis();
			simState.start();
			long io_time = (System.currentTimeMillis() - io_start) / 1000;
			System.out.println("io_time = " + io_time);
			Schedule schedule = simState.schedule;
			while (true) {
				if (!schedule.step(simState)) {
					break;
				}
			}
	        //doLoop(EngDModel.class, args);
	        System.exit(0);
	        }  
	}
	
}