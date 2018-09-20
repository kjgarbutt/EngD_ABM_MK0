package sim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.planargraph.Node;

import ec.util.MersenneTwisterFast;
import entities.EngDAgent;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Network;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.GeomPlanarGraph;
import sim.util.geo.GeomPlanarGraphEdge;
import sim.util.geo.MasonGeometry;
import utilities.EngDParameters;

public class EngDModel extends SimState {

	public Continuous2D world;
	private static final long serialVersionUID = -4554882816749973618L;

	public GeomVectorField boundary = new GeomVectorField();
	public GeomVectorField lsoa = new GeomVectorField();
	public GeomVectorField centroids = new GeomVectorField();
	public GeomVectorField roads = new GeomVectorField();
	public GeomVectorField osvi = new GeomVectorField();
	public GeomVectorField flood3 = new GeomVectorField();
	public GeomVectorField flood2 = new GeomVectorField();
	// public GeomVectorField HouseholdsFZ = new GeomVectorField();
	// public GeomVectorField Households = new GeomVectorField();
	public GeomVectorField agents = new GeomVectorField();
	public GeomPlanarGraph network = new GeomPlanarGraph();
	// Stores road network connections
	public GeomVectorField junctions = new GeomVectorField();
	// Stores nodes for road intersections
	HashMap<Integer, GeomPlanarGraphEdge> idsToEdges = new HashMap<Integer, GeomPlanarGraphEdge>();
	public HashMap<GeomPlanarGraphEdge, ArrayList<entities.EngDAgent>> edgeTraffic = new HashMap<GeomPlanarGraphEdge, ArrayList<entities.EngDAgent>>();
	public GeomVectorField mainagents = new GeomVectorField();

	// Model ArrayLists for agents and OSVI Polygons
	ArrayList<entities.EngDAgent> agentList = new ArrayList<entities.EngDAgent>();
	ArrayList<Polygon> osviPolys = new ArrayList<Polygon>();
	ArrayList<String> csvData = new ArrayList<String>();

	// Here we force the agents to go to or from work at any time
	public boolean goToWork = true;

	public boolean getGoToWork() {
		return goToWork;
	}

	/**
	 * //////////////////////// Model Constructor ////////////////////////////////
	 * Model Constructor
	 */
	public EngDModel(long seed) {
		super(seed);
		random = new MersenneTwisterFast(12345);
	}

	/**
	 * //////////////////////// OSVI Polygon Setup ///////////////////////////////
	 * Polygon Setup
	 */
	void setupOSVIPoly() {
		// copy over the geometries into a list of Polygons
		Bag ps = osvi.getGeometries();
		osviPolys.addAll(ps);
	}

	/**
	 * //////////////////////// Model Initialisation /////////////////////////////
	 * Model Initialisation
	 */
	public void start() {
		super.start();
		System.out.println("Reading shapefiles...");
		
		try {
			URL roadsFile = EngDModel.class.getResource("/data/GL_ITN.shp");
			ShapeFileImporter.read(roadsFile, roads);
			System.out.println("	Roads shapefile: " + roadsFile);

			Envelope MBR = roads.getMBR();

			URL lsoaFile = EngDModel.class.getResource("/data/GloucestershireFinal_LSOA.shp");
			ShapeFileImporter.read(lsoaFile, lsoa, Polygon.class);
			System.out.println("	LSOA shapefile: " + lsoaFile);

			MBR.expandToInclude(lsoa.getMBR());

			URL flood3File = EngDModel.class.getResource("/data/Gloucestershire_FZ_3.shp");
			ShapeFileImporter.read(flood3File, flood3);
			System.out.println("	FZ3 shapefile: " + flood3File);

			MBR.expandToInclude(flood3.getMBR());

			URL flood2File = EngDModel.class.getResource("/data/Gloucestershire_FZ_2.shp");
			ShapeFileImporter.read(flood2File, flood2);
			System.out.println("	FZ2 shapefile: " + flood2File);

			MBR.expandToInclude(flood2.getMBR());
/*
			// Envelope MBR = boundary.getMBR();
			MBR.expandToInclude(boundary.getMBR());
			MBR.expandToInclude(roads.getMBR());
			MBR.expandToInclude(flood2.getMBR());
			MBR.expandToInclude(flood3.getMBR());
			MBR.expandToInclude(centroids.getMBR());
			MBR.expandToInclude(agents.getMBR());

*/
			createNetwork();

			setupOSVIPoly();
			agents.clear();

			//agentGoals("/data/AgentGoals.csv");
			//populateAgent("/data/NorfolkITNAGENT.csv");

			System.out.println();
			System.out.println("Starting simulation...");

			boundary.setMBR(MBR);
			roads.setMBR(MBR);
			flood2.setMBR(MBR);
			flood3.setMBR(MBR);
			centroids.setMBR(MBR);
			agents.setMBR(MBR);

			schedule.scheduleRepeating(agents.scheduleSpatialIndexUpdater(), Integer.MAX_VALUE, 1.0);

		} catch (FileNotFoundException e) {
			System.out.println("Error: missing required data file");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	 * static void readInShapefile(String[] files, Bag[] attfiles, GeomVectorField[]
	 * vectorFields) { System.out.println("Reading in shapefiles..."); try { for
	 * (int i = 0; i < files.length; i++) { Bag attributes = attfiles[i]; String
	 * filePath = files[i]; File file = new File(filePath);
	 * System.out.println("	" + filePath); URL shapeURI = file.toURI().toURL();
	 * ShapeFileImporter.read(shapeURI, vectorFields[i], attributes); } } catch
	 * (Exception e) { e.printStackTrace();
	 * System.out.println("Error in ShapeFileImporter!!");
	 * System.out.println("SHP filename: " + files); }
	 * 
	 * }
	 */

	/**
	 * //////////////////////// Model Finish & Cleanup ///////////////////////////
	 * Finish the simulation and clean up
	 */
	public void finish() {
		System.out.println();
		System.out.println("Simulation ended by user.");
		super.finish();
	}
	
	/**
     * //////////////////////// Create Road Network //////////////////////////////
     * Create the road network the agents will traverse
     */
	private void createNetwork() {
		System.out.println("Creating road network...");
		System.out.println("	" + network);
		System.out.println();
		network.createFromGeomField(roads);

		for (Object o : network.getEdges()) {
			GeomPlanarGraphEdge e = (GeomPlanarGraphEdge) o;

			idsToEdges.put(e.getIntegerAttribute("ROAD_ID_1").intValue(), e);

			e.setData(new ArrayList<EngDAgent>());
		}

		addIntersectionNodes(network.nodeIterator(), junctions);
	}

	/**
	 * ///////////////////////// Setup agentGoals /////////////////////////////////
	 * Read in the agent goals CSV
	 * 
	 * @param agentfilename
	 * @return
	 *
	 */
	public ArrayList<String> agentGoals(String agentfilename) throws IOException {
		String csvGoal = null;
		BufferedReader agentGoalsBuffer = null;

		String agentFilePath = EngDModel.class.getResource(agentfilename).getPath();
		FileInputStream agentfstream = new FileInputStream(agentFilePath);
		System.out.println("Reading Agent's Goals CSV file: " + agentFilePath);

		try {
			agentGoalsBuffer = new BufferedReader(new InputStreamReader(agentfstream));
			agentGoalsBuffer.readLine();
			while ((csvGoal = agentGoalsBuffer.readLine()) != null) {
				String[] splitted = csvGoal.split(",");

				ArrayList<String> agentGoalsResult = new ArrayList<String>(splitted.length);
				for (String data : splitted)
					agentGoalsResult.add(data);
				csvData.addAll(agentGoalsResult);
			}
			System.out.println("Full csvData Array: " + csvData);

		} finally {
			if (agentGoalsBuffer != null)
				agentGoalsBuffer.close();
		}
		return csvData;
	}

	/**
	 * //////////////////////// Setup EngDAgent //////////////////////////////////
	 * Read in the population files and create appropriate populations
	 * 
	 * @param filename
	 */
	public void populateAgent(String filename) {
		try {
			String filePath = EngDModel.class.getResource(filename).getPath();
			FileInputStream fstream = new FileInputStream(filePath);
			System.out.println();
			System.out.println("Populating model with NGO Agents: " + filePath);

			BufferedReader d = new BufferedReader(new InputStreamReader(fstream));
			String s;

			// get rid of the header
			d.readLine();
			// read in all data
			while ((s = d.readLine()) != null) {
				String[] bits = s.split(",");

				int pop = Integer.parseInt(bits[2]);

				String homeTract = bits[3];
				String ROAD_ID = bits[3];

				Random randomiser = new Random();
				String random = csvData.get(new Random().nextInt(csvData.size()));
				String goalTract = random;
				System.out.println();
				System.out.println("Agent goalTract: " + goalTract);

				GeomPlanarGraphEdge startingEdge = idsToEdges.get((int) Double.parseDouble(ROAD_ID));
				GeomPlanarGraphEdge goalEdge = idsToEdges.get((int) Double.parseDouble(goalTract));

				for (int i = 0; i < pop; i++) {
					EngDAgent a = new EngDAgent(this, homeTract, startingEdge, goalEdge);

					boolean successfulStart = a.start(this);

					if (!successfulStart) {
						System.out.println("Main agents added successfully!!");
						continue;
					}

					MasonGeometry newGeometry = a.getGeometry();
					newGeometry.isMovable = true;
					agents.addGeometry(newGeometry);
					agentList.add(a);
					schedule.scheduleRepeating(a);
				}
			}

			d.close();

		} catch (Exception e) {
			System.out.println("ERROR: issue with population file: ");
			e.printStackTrace();
		}
	}

	/**
	 * //////////////////////// Network Intersections ////////////////////////////
	 * adds nodes corresponding to road intersections to GeomVectorField
	 *
	 * @param nodeIterator
	 *            Points to first node
	 * @param intersections
	 *            GeomVectorField containing intersection geometry
	 *
	 *            Nodes will belong to a planar graph populated from LineString
	 *            network.
	 */
	private void addIntersectionNodes(Iterator<?> nodeIterator, GeomVectorField intersections) {
		GeometryFactory fact = new GeometryFactory();
		Coordinate coord = null;
		Point point = null;
		@SuppressWarnings("unused")
		int counter = 0;

		while (nodeIterator.hasNext()) {
			Node node = (Node) nodeIterator.next();
			coord = node.getCoordinate();
			point = fact.createPoint(coord);

			junctions.addGeometry(new MasonGeometry(point));
			counter++;
		}
	}
	
	/**
     * //////////////////////// Main Function ////////////////////////////////////
     * Main function allows simulation to be run in stand-alone, non-GUI mode
     */
	public static void main(String[] args) {
		// {
		// long seed = System.currentTimeMillis();
		// EngDModel simState = new EngDModel(seed);
		// long io_start = System.currentTimeMillis();
		// simState.start();
		// long io_time = (System.currentTimeMillis() - io_start) / 1000;
		// System.out.println("io_time = " + io_time);
		// Schedule schedule = simState.schedule;
		// while (true) {
		// if (!schedule.step(simState)) {
		// break;
		// }
		// }
		doLoop(EngDModel.class, args);
		System.exit(0);
		// }
	}

}