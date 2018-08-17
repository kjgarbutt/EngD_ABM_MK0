package sim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.io.BufferedReader;
import java.io.DataInputStream;

import org.apache.commons.math3.distribution.NormalDistribution;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.planargraph.Node;

import sim.engine.SimState;
import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;
import net.sf.csv4j.CSVReader;
import objects.EngDCentroid;
import refugee.refugeeData.RefugeeData;
import ec.util.MersenneTwisterFast;
import entities.EngDAgent;
import entities.NGOTeam;
import sim.app.antsforage.Ant;
import sim.app.flockers.Flocker;
import sim.app.pacman.Blinky;
import sim.engine.Schedule;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomVectorField;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.geo.GeomPlanarGraph;
import sim.util.geo.GeomPlanarGraphEdge;
import sim.util.geo.MasonGeometry;
import utilities.EngDConstants;
import utilities.EngDParameters;

public class EngDModelBuilder {
	public static EngDModel engdModelSim;
	private static NormalDistribution nd = new NormalDistribution(EngDParameters.AVG_TEAM_SIZE,
			EngDParameters.TEAM_SIZE_SD);
	private static HashMap<Integer, Double> pop_dist;
	private static HashMap<Integer, NormalDistribution> stock_dist;
	public GeomVectorField roads = new GeomVectorField();
	public static GeomPlanarGraph network = new GeomPlanarGraph();
	public static GeomVectorField junctions = new GeomVectorField();
	public static HashMap<Integer, GeomPlanarGraphEdge> idsToEdges = new HashMap<Integer, GeomPlanarGraphEdge>();
	private static HashMap<GeomPlanarGraphEdge, ArrayList<EngDAgent>> edgeTraffic = new HashMap<GeomPlanarGraphEdge, ArrayList<EngDAgent>>();
	public static GeomVectorField agents = new GeomVectorField();

	static ArrayList<EngDAgent> agentList = new ArrayList<EngDAgent>();
	ArrayList<String> csvData = new ArrayList<String>();

	public int numTeams = 12;
	public static int numAgents = 10;
	public MersenneTwisterFast random;

	public static void initializeWorld(EngDModel sim) {
		System.out.println("Model initializing...");
		engdModelSim = sim;

		stock_dist = new HashMap<Integer, NormalDistribution>();

		String[] boundaryAttributes = { "NAME", "AREA_CODE", "DESCRIPTIO", "FILE_NAME", "NUMBER", "NUMBER0",
				"POLYGON_ID", "UNIT_ID", "CODE", "HECTARES", "AREA", "TYPE_CODE", "DESCRIPT0", "TYPE_COD0",
				"DESCRIPT1" };
		String[] centroidAttributes = { "objectid", "lsoa11cd", "lsoa11nm", "NAME", "AREA_CODE", "DESCRIPTIO",
				"FILE_NAME", "NUMBER", "NUMBER0", "POLYGON_ID", "UNIT_ID", "CODE", "HECTARES", "AREA", "TYPE_CODE",
				"DESCRIPT0", "TYPE_COD0", "DESCRIPT1", "OID_", "GOR_NAME", "GOR_CODE", "LTA_NAME", "MSOA_NAME",
				"MSOA_CODE", "LA_NAME", "LA_CODE", "LSOA_NAME", "LSOA_CODE", "Name_1", "ALL_RES_C", "MALES_C",
				"MALES_P", "FEM_C", "FEM", "ALL_PPL_C", "lsoaID", "POP", "ORIG_FID", "lsoaname", "lsoacdstri" };
		String[] lsoaAttributes = { "ID", "OBJECTID", "LSOA_CODE", "LSOA_NAME", "LA_CODE", "LA_NAME", "MSOA_CODE",
				"MSOA_NAME", "GOR_NAME", "GOR_CODE", "GLOUCEST_1", "CFSL", "PRIOL", "L_GL_OSVI_", "RankColL",
				"RankNumL", "L_OSVI_Adj", "RankColAdj", "RankNumAdj", "PRION", "CFSN", "N_GL_OSV_1", "RankColN",
				"RankNumN", "N_OSVI_Adj", "RankColA_1", "RankNumA_1", "objectid1", };
		String[] roadAttributes = { "JOIN_FID", "fictitious", "identifier", "roadNumber", "name1", "formOfWay",
				"length", "primary", "trunkRoad", "loop", "startNode", "endNode", "nameTOID", "numberTOID", "function",
				"objectid", "st_areasha", "st_lengths", "Shape_Leng", "GOR_Name", "GOR_Code", "MSOA_Name", "MSOA_Code",
				"LA_Name", "LA_Code", "LSOA_Name", "LSOA_Code", "ROAD_ID_1", "ORIG_FID" };
		String[] flood2Attributes = { "TYPE", "LAYER" };
		String[] flood3Attributes = { "TYPE", "LAYER" };

		engdModelSim.world_height = 500;
		engdModelSim.world_width = 500;

		engdModelSim.boundary = new GeomVectorField(sim.world_width, sim.world_height);
		Bag boundaryAtt = new Bag(boundaryAttributes);
		System.out.println("	Boundary shapefile: " + EngDParameters.BOUNDARY_SHP);

		engdModelSim.centroidPoints = new GeomVectorField(sim.world_width, sim.world_height);
		Bag centroidAtt = new Bag(centroidAttributes);
		System.out.println("	Centroids shapefile: " + EngDParameters.CENTROIDS_SHP);

		engdModelSim.cityGrid = new SparseGrid2D(sim.world_width, sim.world_height);

		EngDModel.roads = new GeomVectorField(sim.world_width, sim.world_height);
		Bag roadAtt = new Bag(roadAttributes);
		System.out.println("	Roads shapefile: " + EngDParameters.ROAD_SHP);

		engdModelSim.lsoa = new GeomVectorField(sim.world_width, sim.world_height);
		Bag lsoaAtt = new Bag(lsoaAttributes);
		System.out.println("	LSOA shapefile: " + EngDParameters.LSOA_SHP);

		engdModelSim.flood2 = new GeomVectorField(sim.world_width, sim.world_height);
		Bag flood2Att = new Bag(flood2Attributes);
		System.out.println("	Floods 2 shapefile: " + EngDParameters.FLOOD2_SHP);

		engdModelSim.flood3 = new GeomVectorField(sim.world_width, sim.world_height);
		Bag flood3Att = new Bag(flood3Attributes);
		System.out.println("	Floods 3 shapefile: " + EngDParameters.FLOOD3_SHP);

		engdModelSim.roadNetwork = new Network();

		String[] shapeFiles = { EngDParameters.BOUNDARY_SHP, EngDParameters.CENTROIDS_SHP, EngDParameters.ROAD_SHP,
				EngDParameters.LSOA_SHP, EngDParameters.FLOOD2_SHP, EngDParameters.FLOOD3_SHP };
		Bag[] attFiles = { boundaryAtt, centroidAtt, roadAtt, lsoaAtt, flood2Att, flood3Att };
		GeomVectorField[] vectorFields = { engdModelSim.boundary, engdModelSim.centroidPoints, EngDModel.roads,
				engdModelSim.lsoa, engdModelSim.flood2, engdModelSim.flood3 };
		SparseGrid2D[] gridFields = { engdModelSim.cityGrid };

		readInShapefile(shapeFiles, attFiles, vectorFields, gridFields);

		Envelope MBR = engdModelSim.boundary.getMBR();
		MBR.expandToInclude(EngDModel.roads.getMBR());
		// MBR.expandToInclude(engdModelSim.boundary.getMBR());
		// MBR.expandToInclude(engdModelSim.flood2.getMBR());
		// MBR.expandToInclude(engdModelSim.flood3.getMBR());

		engdModelSim.boundary.setMBR(MBR);
		EngDModel.roads.setMBR(MBR);
		engdModelSim.flood2.setMBR(MBR);
		engdModelSim.flood3.setMBR(MBR);
		engdModelSim.centroidPoints.setMBR(MBR);

		// System.out.print("Reading in road_cost ");
		// readInRoadCost();
		// printCentroids();
		makeCentroids(engdModelSim.centroidPoints, engdModelSim.cityGrid, engdModelSim.lsoacentroids,
				engdModelSim.cityList);

		createNetwork();
		/*
		 * try { setUpAgentGoals(EngDParameters.GOALS_CSV);
		 * //agentGoals("data/AgentGoals.csv"); } catch (IOException e) {
		 * e.printStackTrace(); }
		 */
		// createNewNGOTeam();

		createNewNGOAgents();

		// createNGOAgents();
	}

	private static void printCentroids() {
		// System.out.println("Printing Centroids...");
		for (Object centroid : engdModelSim.lsoacentroids) {
			EngDCentroid l = (EngDCentroid) centroid;
			// System.out.println(" LSOA: " + l.getName() + " -- Population: " +
			// l.getPopulation());
			// System.out.println("\n");

		}
	}

	static void makeCentroids(GeomVectorField centroids_vector, SparseGrid2D grid, Bag addTo,
			Map<Integer, EngDCentroid> centroidList) {
		System.out.println("Creating Centroids...");
		Bag centroids = centroids_vector.getGeometries();
		Envelope e = centroids_vector.getMBR();
		double xmin = e.getMinX(), ymin = e.getMinY(), xmax = e.getMaxX(), ymax = e.getMaxY();
		int xcols = engdModelSim.world_width - 1, ycols = engdModelSim.world_height - 1;
		for (int i = 0; i < centroids.size(); i++) {
			MasonGeometry centroidsinfo = (MasonGeometry) centroids.objs[i];
			Point point = centroids_vector.getGeometryLocation(centroidsinfo);
			double x = point.getX(), y = point.getY();
			int xint = (int) Math.floor(xcols * (x - xmin) / (xmax - xmin)),
					yint = (int) (ycols - Math.floor(ycols * (y - ymin) / (ymax - ymin)));
			String name = centroidsinfo.getStringAttribute("lsoacdstri");
			int ID = centroidsinfo.getIntegerAttribute("lsoaID");
			int pop = centroidsinfo.getIntegerAttribute("POP");
			Int2D location = new Int2D(xint, yint);

			EngDCentroid engdcentroid = new EngDCentroid(location, ID, name, pop);
			addTo.add(engdcentroid);
			centroidList.put(ID, engdcentroid);
			grid.setObjectLocation(engdcentroid, location);
		}
		printCentroids();
	}

	static void readInShapefile(String[] files, Bag[] attfiles, GeomVectorField[] vectorFields,
			SparseGrid2D[] gridFields) {
		System.out.println("Reading in shapefiles...");
		try {
			for (int i = 0; i < files.length; i++) {
				Bag attributes = attfiles[i];
				String filePath = files[i];
				File file = new File(filePath);
				// System.out.println(" " + filePath);
				URL shapeURI = file.toURI().toURL();
				ShapeFileImporter.read(shapeURI, vectorFields[i], attributes);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in ShapeFileImporter!!");
			System.out.println("SHP filename: " + files);
		}
	}

	/*
	 * static void readInRoadCost() { try { engdModelSim.road_cost = new
	 * DoubleGrid2D(engdModelSim.world_width, engdModelSim.world_height);
	 * 
	 * // FileInputStream fileInputStream = new FileInputStream(new //
	 * File(Parameters.ROADS_COST_PATH)); DataInputStream dataInputStream = new
	 * DataInputStream(
	 * EbolaData.class.getResourceAsStream(Parameters.ROADS_COST_PATH));
	 * 
	 * for (int i = 0; i < engdModelSim.world_width; i++) for (int j = 0; j <
	 * engdModelSim.world_height; j++) engdModelSim.road_cost.set(i, j,
	 * dataInputStream.readDouble()); } catch (FileNotFoundException e) {
	 * e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
	 * 
	 * }
	 */

	private static void createNetwork() {
		System.out.println();
		System.out.println("Creating road network...");
		network.createFromGeomField(EngDModel.roads);

		for (Object o : network.getEdges()) {
			GeomPlanarGraphEdge e = (GeomPlanarGraphEdge) o;

			idsToEdges.put(e.getIntegerAttribute("ROAD_ID_1").intValue(), e);
			e.setData(new ArrayList<EngDAgent>());
		}

		addIntersectionNodes(network.nodeIterator(), junctions);
	}

	private static void setUpStockDist(String stock_dist_file) {
		try {
			CSVReader csvReader = new CSVReader(new FileReader(new File(stock_dist_file)));
			// csvReader.readLine();// skip the headers
			List<String> line = csvReader.readLine();
			while (!line.isEmpty()) {
				// read in the county ids
				int lsoa_id = NumberFormat.getNumberInstance(java.util.Locale.US).parse(line.get(0)).intValue();
				// relevant info is from 5 - 21
				double avgfin = Double.parseDouble(line.get(2));
				double sd = Double.parseDouble(line.get(3));
				stock_dist.put(lsoa_id, new NormalDistribution(avgfin, sd));
				line = csvReader.readLine();
			}
			System.out.println("fin");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
	}

	private static double pick_stock_status(HashMap<Integer, NormalDistribution> fin_dist, int i) {
		NormalDistribution nd = fin_dist.get(i);
		return nd.sample();

	}
	/*
	 * public static ArrayList<String> setUpAgentGoals(String agentfilename) throws
	 * IOException{ String csvGoal = null; BufferedReader agentGoalsBuffer = null;
	 * 
	 * String agentFilePath =
	 * EngDModelBuilder.class.getResource(agentfilename).getPath(); FileInputStream
	 * agentfstream = new FileInputStream(agentFilePath);
	 * System.out.println("Reading Agent's Goals CSV file: " +agentFilePath);
	 * 
	 * try { agentGoalsBuffer = new BufferedReader (new
	 * InputStreamReader(agentfstream)); agentGoalsBuffer.readLine(); while
	 * ((csvGoal = agentGoalsBuffer.readLine()) != null) { String[] splitted =
	 * csvGoal.split(",");
	 * 
	 * ArrayList<String> agentGoalsResult = new ArrayList<String>(splitted.length);
	 * for (String data : splitted) agentGoalsResult.add(data);
	 * csvData.addAll(agentGoalsResult); } System.out.println("Full csvData Array: "
	 * +csvData);
	 * 
	 * } finally { if (agentGoalsBuffer != null) agentGoalsBuffer.close(); } return
	 * csvData; }
	 */

	// Move to NewNGOTeam.java?
	private static int pickTeamSize() {
		// pick random number between 1 and TEAM_SIZE
		Random r = new Random();
		int minTeam = 1;
		int maxTeam = EngDParameters.TEAM_SIZE;
		int teamSize = r.nextInt(maxTeam - minTeam) + minTeam;
		return teamSize;
	}

	static void createNewNGOAgents() {
		System.out.println("Creating NEW NGO Agents...");
		for (int i = 0; i < numAgents; i++) {
			EngDAgent newagent = new EngDAgent();
			engdModelSim.agents.add(newagent);
			MasonGeometry newGeometry = newagent.getGeometry();
			newGeometry.isMovable = true;
			//agents.addGeometry(newGeometry);	//This breaks things. Why?
			System.out.println("	EngDAgent: " + newagent);
		}
	}

	private static void createNewNGOTeam() {
		System.out.println("Creating NEW NGO Teams...");
		// Iterate through NUM_TEAMS
		// And create a NewNGOTeam each time
		// Give each team a number
		// pickTeamSize() to assign a teamSize to the newly created Team

		for (int i = 0; i < EngDParameters.NUM_TEAMS; i++) {
			NGOTeam newngoteam = new NGOTeam(null, i, null);
			pickTeamSize();
			// schedule.scheduleRepeating(newngoteam);
		}

	}

	private static void createNGOAgents() {
		System.out.println("Creating NGO Agents...");
		for (Object c : engdModelSim.lsoacentroids) {
			EngDCentroid centroid = (EngDCentroid) c;
			if (centroid.getID() == 1032936) { // == 'Equal to'
				int currentPop = 0;
				int maximumPop = EngDParameters.TOTAL_POP;
				System.out.println("Total Number of NGO Agents: " + EngDParameters.TOTAL_POP);
				while (currentPop <= maximumPop) {
					NGOTeam n = createNGOTeam(centroid);
					System.out.println("NGO Team Size: " + n.getTeam().size());
					System.out.println(n.getTeam().size());
					engdModelSim.agentTeams.add(n);
					for (Object o : n.getTeam()) {
						EngDAgent agent = (EngDAgent) o;
						currentPop++;
						centroid.addMember(agent);
						engdModelSim.agents.add(agent);
						Int2D loc = centroid.getLocation();
						double y_coord = (loc.y * EngDParameters.WORLD_TO_POP_SCALE)
								+ (int) (engdModelSim.random.nextDouble() * EngDParameters.WORLD_TO_POP_SCALE);
						double x_coord = (loc.x * EngDParameters.WORLD_TO_POP_SCALE)
								+ (int) (engdModelSim.random.nextDouble() * EngDParameters.WORLD_TO_POP_SCALE);
						engdModelSim.world.setObjectLocation(agent, new Double2D(x_coord, y_coord));
						int y_coordint = loc.y + (int) ((engdModelSim.random.nextDouble() - 0.5) * 3);
						int x_coordint = loc.x + (int) ((engdModelSim.random.nextDouble() - 0.5) * 3);
						engdModelSim.total_pop++;
					}
					engdModelSim.schedule.scheduleRepeating(n);
				}
			}
		}

	}

	private static NGOTeam createNGOTeam(EngDCentroid lsoa) {
		System.out.println("Creating NGO Teams...");
		int teamSize = pickTeamSize();
		// double stockStatus = pick_stock_status(stock_dist, lsoa.getID()) * teamSize;

		NGOTeam ngoTeam = new NGOTeam(lsoa.getLocation(), teamSize, lsoa);
		// EngDNGO ngoTeam = new EngDNGO(lsoa.getLocation(), teamSize, lsoa,
		// stockStatus);
		for (int i = 0; i < teamSize; i++) {
			int sex;
			if (engdModelSim.random.nextBoolean())
				sex = EngDConstants.MALE;
			else
				sex = EngDConstants.FEMALE;
			EngDAgent agent = new EngDAgent();
			ngoTeam.getTeam().add(agent);
		}
		return ngoTeam;

	}

	private static void addIntersectionNodes(Iterator<?> nodeIterator, GeomVectorField intersections) {
		GeometryFactory fact = new GeometryFactory();
		Coordinate coord = null;
		Point point = null;
		int counter = 0;

		while (nodeIterator.hasNext()) {
			Node node = (Node) nodeIterator.next();
			coord = node.getCoordinate();
			point = fact.createPoint(coord);

			junctions.addGeometry(new MasonGeometry(point));
			counter++;
		}
	}

}