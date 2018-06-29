package engd_abm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;

import org.apache.commons.math3.distribution.NormalDistribution;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.planargraph.Node;

import net.sf.csv4j.CSVReader;
import ec.util.MersenneTwisterFast;
import sim.app.antsforage.Ant;
import sim.app.geo.campusworld.Agent;
import sim.app.geo.sickStudents.Household;
import sim.engine.Schedule;
import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomVectorField;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Network;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.geo.GeomPlanarGraph;
import sim.util.geo.GeomPlanarGraphEdge;
import sim.util.geo.MasonGeometry;

class EngDModelBuilder {
	public static EngDModel engdModelSim;
	private static NormalDistribution nd = new NormalDistribution(EngDParameters.AVG_TEAM_SIZE,
			EngDParameters.TEAM_SIZE_SD);
	private static HashMap<Integer, Double> pop_dist;
	public static GeomPlanarGraph network = new GeomPlanarGraph();
	public static GeomVectorField junctions = new GeomVectorField();
	public static HashMap<Integer, GeomPlanarGraphEdge> idsToEdges = new HashMap<Integer, GeomPlanarGraphEdge>();
	private static HashMap<Integer, NormalDistribution> stock_dist;
	
	public int numTeams = 12;

	public static void initializeWorld(EngDModel sim) {
		System.out.println("Model initializing...");
		engdModelSim = sim;
		
		stock_dist = new HashMap<Integer, NormalDistribution>();

		String[] regionAttributes = { "NAME", "AREA_CODE", "DESCRIPTIO", "FILE_NAME", "NUMBER", "NUMBER0",
				"POLYGON_ID", "UNIT_ID", "CODE", "HECTARES", "AREA", "TYPE_CODE", "DESCRIPT0", "TYPE_COD0",
				"DESCRIPT1" };
		String[] countryAttributes = { "COUNTRY" };
		String[] roadAttributes = { "JOIN_FID", "fictitious", "identifier", "roadNumber", "name1", "formOfWay",
				"length", "primary", "trunkRoad", "loop", "startNode", "endNode", "nameTOID", "numberTOID", "function",
				"objectid", "st_areasha", "st_lengths", "Shape_Leng", "GOR_Name", "GOR_Code", "MSOA_Name", "MSOA_Code",
				"LA_Name", "LA_Code", "LSOA_Name", "LSOA_Code", "ROAD_ID_1" };
		String[] cityAttributes = { "ID", "LSOA_CODE", "LSOA_NAME", "LA_NAME", "MSOA_CODE", "MSOA_NAME", "GOR_NAME",
				"CFSL", "CFSN" };
		String[] roadLinksAttributes = { "ID", "FR", "TO", "SPEED_1", "SPOP",
				"COST", "TLEVEL_1", "DEATH_1", "LENGTH_1" };
		//String[] lsoaAttributes = { "ID", "LSOA_CODE", "LSOA_NAME", "LA_NAME", "MSOA_CODE", "MSOA_NAME", "GOR_NAME",
		//		"CFSL", "CFSN" };
		String[] flood2Attributes = { "TYPE", "LAYER" };
		String[] flood3Attributes = { "TYPE", "LAYER" };

		engdModelSim.world_height = 500;
		engdModelSim.world_width = 500;

		engdModelSim.regions = new GeomVectorField(sim.world_width, 
				sim.world_height);
		Bag regionAtt = new Bag(regionAttributes);
		System.out.println("	Boundary shapefile: " + EngDParameters.REGION_SHP);

		engdModelSim.countries = new GeomVectorField(sim.world_width, 
				sim.world_height);
		Bag countryAtt = new Bag(countryAttributes);
		System.out.println("	Roads shapefile: " + EngDParameters.COUNTRY_SHP);
		
		engdModelSim.roads = new GeomVectorField(sim.world_width, 
				sim.world_height);
		Bag roadAtt = new Bag(roadAttributes);
		System.out.println("	Roads shapefile: " + EngDParameters.ROAD_SHP);

		engdModelSim.cityPoints = new GeomVectorField(sim.world_width, 
				sim.world_height);
		Bag cityAtt = new Bag(cityAttributes);
		System.out.println("	Centroids shapefile: " + EngDParameters.CITY_SHP);

		engdModelSim.cityGrid = new SparseGrid2D(sim.world_width,
				sim.world_height);
		
		engdModelSim.roadNetwork = new Network();
		
		engdModelSim.allRoadNodes = new SparseGrid2D(sim.world_width,
				sim.world_height);
		engdModelSim.roadLinks = new GeomVectorField(sim.world_width,
				sim.world_height);
		Bag roadLinksAtt = new Bag(roadLinksAttributes);
		
		engdModelSim.flood2 = new GeomVectorField(sim.world_width, 
				sim.world_height);
		Bag flood2Att = new Bag(flood2Attributes);
		System.out.println("	Floods 2 shapefile: " + EngDParameters.FLOOD2_SHP);

		engdModelSim.flood3 = new GeomVectorField(sim.world_width, 
				sim.world_height);
		Bag flood3Att = new Bag(flood3Attributes);
		System.out.println("	Floods 3 shapefile: " + EngDParameters.FLOOD3_SHP);

		String[] files = { 
				EngDParameters.REGION_SHP, EngDParameters.COUNTRY_SHP,
				EngDParameters.ROAD_SHP, EngDParameters.CITY_SHP,
				EngDParameters.ROADLINK_SHP, 
				EngDParameters.FLOOD2_SHP, EngDParameters.FLOOD3_SHP };
		Bag[] attfiles = { regionAtt, countryAtt, roadAtt, cityAtt,
				roadLinksAtt, flood2Att, flood3Att };
		GeomVectorField[] vectorFields = { engdModelSim.regions, 
				engdModelSim.countries, engdModelSim.roads,
				engdModelSim.cityPoints, engdModelSim.roadLinks,
				engdModelSim.flood2, engdModelSim.flood3 };
		
		readInShapefile(files, attfiles, vectorFields);

		Envelope MBR = engdModelSim.regions.getMBR();
		MBR.expandToInclude(engdModelSim.roadLinks.getMBR());
		MBR.expandToInclude(engdModelSim.cityPoints.getMBR());

		engdModelSim.regions.setMBR(MBR);
		engdModelSim.countries.setMBR(MBR);
		engdModelSim.roads.setMBR(MBR);
		engdModelSim.roadLinks.setMBR(MBR);
		engdModelSim.cityPoints.setMBR(MBR);
		
		engdModelSim.flood2.setMBR(MBR);
		engdModelSim.flood3.setMBR(MBR);
		
		makeCentroids(engdModelSim.cityPoints, engdModelSim.cityGrid,
				engdModelSim.centroids, engdModelSim.centroidList);
		
		//createNetwork();
		extractFromRoadLinks(engdModelSim.roadLinks, engdModelSim);

		//createNGOTeam(null);
		
		addAgents();
	}

	private static void printCentroids() {
		for (Object centroid : engdModelSim.centroids) {
			LSOA l = (LSOA) centroid;
			System.out.format("LSOA Name: " + l.getName() + " Ref Pop: " 
					+ l.getPopulation());
			System.out.println("\n");
		}
	}

	static void makeCentroids(GeomVectorField centroids_vector, SparseGrid2D grid,
			Bag addTo, Map<Integer, LSOA> centroidList) {
		Bag centroids = centroids_vector.getGeometries();
		Envelope e = centroids_vector.getMBR();
		double xmin = e.getMinX(), ymin = e.getMinY(), xmax = e.getMaxX(), ymax = e
				.getMaxY();
		int xcols = engdModelSim.world_width - 1, ycols = engdModelSim.world_height - 1;
		System.out.println("Reading in Cities");
		for (int i = 0; i < centroids.size(); i++) {
			MasonGeometry cityinfo = (MasonGeometry) centroids.objs[i];
			Point point = centroids_vector.getGeometryLocation(cityinfo);
			double x = point.getX(), y = point.getY();
			int xint = (int) Math.floor(xcols * (x - xmin) / (xmax - xmin)), yint = (int) (ycols - Math
					.floor(ycols * (y - ymin) / (ymax - ymin)));
			String name = cityinfo.getStringAttribute("lsoa11nm");
			int ID = cityinfo.getIntegerAttribute("ORIG_FID");
			Int2D location = new Int2D(xint, yint);

			//Lsoa lsoa = new Lsoa(location, ID, name);
			LSOA lsoa = new LSOA(location, ID, name);
			addTo.add(lsoa);
			centroidList.put(ID, lsoa);
			grid.setObjectLocation(lsoa, location);
		}
	}

	static void readInShapefile(String[] files, Bag[] attfiles, GeomVectorField[] vectorFields) {
		System.out.println("Reading in shapefiles...");
		try {
			for (int i = 0; i < files.length; i++) {
				Bag attributes = attfiles[i];
				String filePath = files[i];
				File file = new File(filePath);
				URL shapeURI = file.toURI().toURL();
				ShapeFileImporter.read(shapeURI, vectorFields[i], attributes);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in ShapeFileImporter!!");
            System.out.println("SHP filename: " + files);
		}
	}

	/*private static void createNetwork() {
		System.out.println("Creating road network...");
		System.out.println();
		network.createFromGeomField(engdModelSim.roads);

		for (Object o : network.getEdges()) {
			GeomPlanarGraphEdge e = (GeomPlanarGraphEdge) o;

			idsToEdges.put(e.getIntegerAttribute("ROAD_ID_1").intValue(), e);

			e.setData(new ArrayList<EngDAgent>());
		}

		addIntersectionNodes(network.nodeIterator(), junctions);
	}*/
	
	static void extractFromRoadLinks(GeomVectorField roads,
			EngDModel engdModelSim) {
		Bag geoms = roads.getGeometries();
		Envelope e = roads.getMBR();
		double xmin = e.getMinX(), ymin = e.getMinY(), xmax = e.getMaxX(), ymax = e
				.getMaxY();
		int xcols = engdModelSim.world_width - 1, ycols = engdModelSim.world_height - 1;
		int count = 0;

		for (Object o : geoms) {
			MasonGeometry gm = (MasonGeometry) o;
			int from = gm.getIntegerAttribute("FR");
			int to = gm.getIntegerAttribute("TO");
			double speed = gm.getDoubleAttribute("SPEED_1");
			double distance = gm.getDoubleAttribute("LENGTH_1");
			double spop = gm.getDoubleAttribute("SPOP");
			double cost = gm.getDoubleAttribute("COST");
			double transportlevel = gm.getDoubleAttribute("TLEVEL_1");
			double deaths = gm.getDoubleAttribute("DEATH_1");
			System.out.println("pop weight: " + spop);
			EngDRoadInfo edgeinfo = new EngDRoadInfo(gm.geometry, from, to, speed,
					spop, distance, cost, transportlevel, deaths);
			// build road network
			engdModelSim.roadNetwork.addEdge(engdModelSim.centroidList.get(from),
					engdModelSim.centroidList.get(to), edgeinfo);
			engdModelSim.roadNetwork.addEdge(engdModelSim.centroidList.get(to),
					engdModelSim.centroidList.get(from), edgeinfo);
		}

		// addRedirects();
	}
	
	private static void addAgents() {
		System.out.println("Adding Agents... ");
		for (Object c : engdModelSim.centroids) {
			LSOA lsoa = (LSOA) c;
			if (lsoa.getID() == 1) {
				int currentPop = 0;// 1,4,5,10,3,14,24
				int maximumPop = EngDParameters.TOTAL_POP;
				while (currentPop <= maximumPop) {
					NGOTeam n = createNGOTeam(lsoa);
					 System.out.println(n.getTeam().size());
					engdModelSim.agentTeams.add(n);
					for (Object o : n.getTeam()) {
						EngDAgent agent = (EngDAgent) o;
						currentPop++;
						lsoa.addMember(agent);
						engdModelSim.agents.add(agent);
						Int2D loc = lsoa.getLocation();
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
				/*
				 * int lsoapop = (int) Math.round(pop_dist.get(lsoa.getID()) *
				 * EngDParameters.TOTAL_POP); System.out.println(lsoa.getName() + ": " +
				 * lsoapop); while (currentPop <= lsoapop) { EngDNGO n = createNGOTeam(lsoa);
				 * System.out.println(n.getTeam().size()); engdModelSim.ngoTeams.add(n); for
				 * (Object o : n.getTeam()) { EngDAgent agent = (EngDAgent) o; currentPop++;
				 * lsoa.addMember(agent); engdModelSim.agents.add(agent); Int2D loc =
				 * lsoa.getLocation(); double y_coord = (loc.y *
				 * EngDParameters.WORLD_TO_POP_SCALE) + (int) (engdModelSim.random.nextDouble()
				 * * EngDParameters.WORLD_TO_POP_SCALE); double x_coord = (loc.x *
				 * EngDParameters.WORLD_TO_POP_SCALE) + (int) (engdModelSim.random.nextDouble()
				 * * EngDParameters.WORLD_TO_POP_SCALE);
				 * engdModelSim.world.setObjectLocation(agent, new Double2D(x_coord, y_coord));
				 * int y_coordint = loc.y + (int) ((engdModelSim.random.nextDouble() - 0.5) *
				 * 3); int x_coordint = loc.x + (int) ((engdModelSim.random.nextDouble() - 0.5)
				 * * 3); engdModelSim.total_pop++; } engdModelSim.schedule.scheduleRepeating(n);
				 * }
				 */
			}
		}

	}

	private static NGOTeam createNGOTeam(LSOA lsoa) {
		int teamSize = pickTeamSize();
		double stockStatus = pick_stock_status(stock_dist, lsoa.getID()) * teamSize;

		NGOTeam ngoTeam = new NGOTeam(lsoa.getLocation(), teamSize, lsoa);
		//EngDNGO ngoTeam = new EngDNGO(lsoa.getLocation(), teamSize, lsoa, stockStatus);
		for (int i = 0; i < teamSize; i++) {
			int sex;
			if (engdModelSim.random.nextBoolean())
				sex = EngDConstants.MALE;
			else
				sex = EngDConstants.FEMALE;
			EngDAgent agent = new EngDAgent(sex, ngoTeam);
			ngoTeam.getTeam().add(agent);
		}
		return ngoTeam;

	}

	/*
	 * private static void setUpPopDist(String pop_dist_file) { try { // buffer
	 * reader for age distribution data CSVReader csvReader = new CSVReader(new
	 * FileReader(new File(pop_dist_file))); // csvReader.readLine();// skip the
	 * headers List<String> line = csvReader.readLine(); while (!line.isEmpty()) {
	 * // read in the county ids int lsoa_id =
	 * NumberFormat.getNumberInstance(java.util.Locale.US).parse(line.get(0)).
	 * intValue(); // relevant info is from 5 - 21 double percentage =
	 * Double.parseDouble(line.get(1)); pop_dist.put(lsoa_id, percentage); line =
	 * csvReader.readLine(); } System.out.println(pop_dist); } catch
	 * (FileNotFoundException e) { e.printStackTrace(); } catch (IOException e) {
	 * e.printStackTrace(); } catch (java.text.ParseException e) {
	 * e.printStackTrace(); } }
	 */

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

	private static int pickTeamSize() {
		int teamSize = EngDParameters.TEAM_SIZE;
		//int teamSize = (int) Math.round(nd.sample());
		return teamSize;
	}

	private static void addIntersectionNodes(Iterator<?> nodeIterator, GeomVectorField intersections) {
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

}