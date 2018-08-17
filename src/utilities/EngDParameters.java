package utilities;

public class EngDParameters {
	
	public static double TEMPORAL_RESOLUTION = 0.4;// steps per hour
	public static double AVG_TEAM_SIZE = 6.3;
	public static double TEAM_SIZE_SD = 2.92;
	public static int TEAM_SIZE = 6;
	public static int NUM_TEAMS = 10;
	public static int TOTAL_POP = 25;
	
	public static double DANGER_CARE_WEIGHT = 0.2;
	public static double POP_CARE = 0.75;
	public static double ECON_CARE = 0.75;
	
	public static int WORLD_TO_POP_SCALE = 0;
	
	//-------Agent Decision Weights----//
	public static double GOAL_CHANGE_PROB = 0.1;
	// -------Edge Weights-------//
	public static double MAX_EDGE_LENGTH = 1337284.917613;
	public static double MIN_EDGE_LENGTH = 81947.33824;
	public static double MIN_EDGE_COST = 0.0;
	public static double MAX_EDGE_COST = 3375;
	public static double ROAD_DEATH_PROB = 0.1;
	
	public static double COST_WEIGHT = 0.1;//0.1;
	public static double RISK_WEIGHT = 0.1;//0.1;
	public static double DISTANCE_WEIGHT = 0.1;//0.1;
	public static double SPEED_WEIGHT = 0.1;//0.1;
	public static double POP_WEIGHT = 10.0;
	public static double TRANSPORT_LEVEL_WEIGHT = 0.1;//0.1;
	public static double HEU_WEIGHT = 1/19348237.217718;

	public static double POP_BLOCK_METERS = 926.1;// Height and width of one
													// population block.
	public static double WORLD_DISCRETIZTION = 0.1;// discretization or buckets
													// for world granularity

	public static double WALKING_SPEED = 5.1;// km per hour
	
	public static String LSOA_SHP = "data/GloucestershireFinal_LSOA.shp";
	public static String BOUNDARY_SHP = "data/Gloucestershire_Boundary.shp";
	public static String CENTROIDS_SHP = "data/Gloucestershire_Centroids_Singlepart.shp";
	public static String ROAD_SHP = "data/GL_ITN_MultipartToSinglepart.shp";
	public static String FLOOD2_SHP = "data/Gloucestershire_FZ_2.shp";
	public static String FLOOD3_SHP = "data/Gloucestershire_FZ_3.shp";
	//public static String ROADLINK_SHP = "data/GL_ITN_MultipartToSinglepart.shp";
	//public static String ROADS_COST_PATH = "data/road_cost.dat";//Path to cost distance data for all allRoadNodes in the network
	//public static String CITY_SHP = "data/city.shp";
	//public static String CITY_SHP = "data/Gloucestershire_Centroids_MultipartToSinglepart.shp";
	public static String AGENTS = "data/NGOAgents.csv";
	public static String AGE_DIST = "data/age_dist2.csv";
	public static String POP_DIST = "data/pop_dist.csv";
	public static String FIN_DIST = "data/fin_dist.csv";
	public static String STOCK_DIST = "data/stock_dist.csv";
	public static String GOALS_CSV = "data/AgentGoals.csv";
	
	public static double convertToKilometers(double val) {
		return (val * (EngDParameters.POP_BLOCK_METERS / EngDParameters.WORLD_TO_POP_SCALE)) / 1000.0;

	}

	public static double convertFromKilometers(double val) {
		return (val * 1000.0) / (EngDParameters.POP_BLOCK_METERS / EngDParameters.WORLD_TO_POP_SCALE);
	}
	
}