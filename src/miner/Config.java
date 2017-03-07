package miner;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Random;

@SuppressWarnings("unused")
public class Config implements Serializable {

	private static final long serialVersionUID = 1207642425572878911L;

	//CLUSTERING
	public static boolean USE_ACTIVITYPROFILE = false;
	public static double ACTIVITYPROFILE_WEIGHT = 0.05;
	public static boolean USE_TRANSITIONPROFILE = false;
	public static double TRANSITIONPROFILE_WEIGHT = 0.25;
	public static boolean USE_DATAPROFILE = true;
	public static double DATAPROFILE_WEIGHT = 1.0;//0.7
	//TODO: op 2/2 veel sneller maar minder leafs, op 10/5 trager maar meer leafs
	//==> wat is beter? (meer leafs = kleinere leafs!, dus lagere split theshold voor 2/2?)
	public static int NUM_CLUSTERS_TOP = 2;
	public static int NUM_CLUSTERS_SUB = 2;
	public static double CLUSTER_SPLIT_THRESHOLD = 0.05;//0.001=5traces 0.01=50traces 0.1=500traces (/5000)
	public static long MAX_SEARCHTIME_TOP = 30*1000;//15secs
	public static long MAX_SEARCHTIME_SUB = 15*1000;//3secs
	public static int MAX_SEARCH_ITERATIONS_TOP = 100;
	public static int MAX_SEARCH_ITERATIONS_SUB = 50;
	public static int CLUSTER_STOP = 5;
	public static int CLUSTER_RETRIES = 3;

	//MINER GENERAL
	public static double MINIMAL_CONFORMANCE = 99.99;//99.99 = 100% (to counter rounding errors)
	public static double MINIMAL_SUPPORT = 0.0001;//5.0
	public static int MAX_BRANCHING_LEVEL = 1;//0==no limit

	//SUBMINER ALTERNATIVE SETTINGS (testing purposes, otherwise should be false!)
	public static boolean PHASE2_DO_COMPLETESEARCH = false;
	public static boolean PHASE2_STORE_INTERMEDIATE_RESPOPS = true;

	//GENETIC MINER
	public static final Random RANDOM = new Random();
	public static long MAX_SEARCHTIME_GENETIC = 20 *(60*1000);//mins
	public static int CHECK_CHANGE_NR_OF_ITERATIONS = 5;//10
	public static int NEWSEED_GENERATION_RETRIES_PER_INDIVIDUAL = 10;
	//TODO: more mutations?
	public static double MUTATION_PROBABILITY = 0.075;//=% of gens that are incorrectly copied from parents (including inactive gens!)
	public static int FITNESS_CONF_WEIGHT = 8;
	public static int FITNESS_DIV_WEIGHT = 4;
	public static int FITNESS_DOM_WEIGHT = 1;
	public static int FITNESS_DECGEN_WEIGHT = 2;
	public static double FITNESS_CONF_TRACES_WEIGHT = 0.7;
	public static double FITNESS_CONF_GEN_WEIGHT = 1-FITNESS_CONF_TRACES_WEIGHT;
	public static int FITNESS_CONF_GENERALIY_WEIGHTINCREASE = 1;
	public static int POPULATIONSIZE_EDIT_TOTAL = 100;
	public static int MINIMAL_SUBPOPULATIONSIZE_EDIT = 5;
	public static double POPULATIONSIZE_RECOMBINATION_MULTIPLIER = 4;
	public static double POPULATIONSIZE_NEWEDIT_SEEDS_PERCENTAGE = 0.9;
	public static double GENERATION_DIVERSITY_FACTOR = 6.5;

	//CONSTRAINTS (not final to facilitate test automatic runs with different configurations)
	//Classes
	public static boolean MINE_EXISTENCE = true;
	public static boolean MINE_RELATION = true;
	public static boolean MINE_RESOURCE = true;
	//Subclasses
	public static boolean MINE_POSITIVE = true;
	public static boolean MINE_NEGATIVE = true;
	//Templates
	public static boolean MINE_FIRST;
	public static boolean MINE_LAST;
	public static boolean MINE_ATLEAST;
	public static boolean MINE_ATMOST;
	//	public static boolean MINE_ATLEASTCHOICE;
	//	public static boolean MINE_ATMOSTCHOICE;
	public static boolean MINE_RESPONDEDPRESENCE;
	public static boolean MINE_NOTRESPONDEDPRESENCE;
	public static boolean MINE_RESPONSE;
	public static boolean MINE_NOTRESPONSE;
	public static boolean MINE_CHAINRESPONSE;
	public static boolean MINE_NOTCHAINRESPONSE;
	//	public static boolean MINE_ALTERNATERESPONSE;
	public static boolean MINE_PRECEDENCE;
	public static boolean MINE_NOTPRECEDENCE;
	public static boolean MINE_CHAINPRECEDENCE;
	public static boolean MINE_NOTCHAINPRECEDENCE;
	//	public static boolean MINE_RESOURCEUSAGE;
	//	public static boolean MINE_RESOURCEEXCLUSION;
	static {//To make sure that the others are assigned first
		MINE_FIRST = true && MINE_EXISTENCE;
		MINE_LAST = true && MINE_EXISTENCE;
		MINE_ATLEAST = true && MINE_EXISTENCE;
		MINE_ATMOST = (true || MINE_ATLEAST) && MINE_EXISTENCE;
		//		MINE_ATLEASTCHOICE = true && MINE_EXISTENCE;
		//		MINE_ATMOSTCHOICE = (true || MINE_ATLEASTCHOICE) && MINE_EXISTENCE;
		MINE_RESPONDEDPRESENCE = true && MINE_RELATION && MINE_POSITIVE;
		MINE_NOTRESPONDEDPRESENCE = true && MINE_RELATION && MINE_NEGATIVE;
		MINE_RESPONSE = true && MINE_RELATION && MINE_POSITIVE;
		MINE_NOTRESPONSE = true && MINE_RELATION && MINE_NEGATIVE;
		MINE_CHAINRESPONSE = true && MINE_RELATION && MINE_POSITIVE;
		MINE_NOTCHAINRESPONSE = true && MINE_RELATION && MINE_NEGATIVE;
		//		MINE_ALTERNATERESPONSE = false && MINE_RELATION;
		MINE_PRECEDENCE = true && MINE_RELATION && MINE_POSITIVE;
		MINE_NOTPRECEDENCE = true && MINE_RELATION && MINE_NEGATIVE;
		MINE_CHAINPRECEDENCE = true && MINE_RELATION && MINE_POSITIVE;
		MINE_NOTCHAINPRECEDENCE = true && MINE_RELATION && MINE_NEGATIVE;
		//		MINE_RESOURCEUSAGE = true && MINE_RESOURCE;
		//		MINE_RESOURCEEXCLUSION = true && MINE_RESOURCE;

		MINE_EXISTENCE = MINE_FIRST || MINE_LAST || MINE_ATLEAST || MINE_ATMOST
				//				|| MINE_ATLEASTCHOICE || MINE_ATMOSTCHOICE
				;
		MINE_RELATION = MINE_RESPONDEDPRESENCE || MINE_NOTRESPONDEDPRESENCE
				|| MINE_RESPONSE || MINE_NOTRESPONSE || MINE_CHAINRESPONSE || MINE_NOTCHAINRESPONSE
				//				|| MINE_ALTERNATERESPONSE
				|| MINE_PRECEDENCE || MINE_NOTPRECEDENCE || MINE_CHAINPRECEDENCE || MINE_NOTCHAINPRECEDENCE
				//				|| MINE_ALTERNATEPRECEDENCE
				;
		//		MINE_RESOURCE = MINE_RESOURCEUSAGE || MINE_RESOURCEEXCLUSION;
		MINE_POSITIVE = MINE_RESPONDEDPRESENCE
				|| MINE_RESPONSE || MINE_CHAINRESPONSE
				//				|| MINE_ALTERNATERESPONSE
				|| MINE_PRECEDENCE || MINE_CHAINPRECEDENCE
				//				|| MINE_ALTERNATEPRECEDENCE
				;
		MINE_NEGATIVE = MINE_NOTRESPONDEDPRESENCE
				|| MINE_NOTRESPONSE || MINE_NOTCHAINRESPONSE
				|| MINE_NOTPRECEDENCE || MINE_NOTCHAINPRECEDENCE;
	}

	//CONSOLE OUTPUT
	public static String LEVEL_SPACES = "   ";
	public static boolean SHOW_P1_INTERMEDIATE_RULES = false;
	public static boolean SHOW_P1_RESULT_RULES = true;
	public static boolean SHOW_RESULT_RULES = false;
	public static boolean SHOW_RESULT_RULES_TEXTUALREPRESENTATION = false;

	//SAVE FILES
	public static String FILENAME_INTERMEDIATE_P1 = "Intermediate_Rules_P1";
	public static String FILENAME_INTERMEDIATE_P2 = "Intermediate_Rules_P2_Iteration";
	public static String FILENAME_RESULT_P1 = "Result_Rules_P1";
	public static String FILENAME_RESULT = "Result_Rules";
	public static String FILE_CSV = "csv";
	public static String CSV_DELIMITER = ";";

	//LOGGER
	public static boolean DO_LOGGING = true;
	public static String FILENAME_LOGGER = "Log";
	public static String FILENAME_LOGGER_P1 = "Phase 1";
	public static String FILENAME_LOGGER_P2 = "Phase 2";
	public static String FILENAME_LOGGER_P2_BOUNDED = "Phase 2 - Bounded";
	public static String FILENAME_LOGGER_P2_RELATION = "Phase 2 - Relation";
	public static String FILENAME_LOGGER_P2_OTHER = "Phase 2 - Other";
	public static String FILENAME_RESULTS_DATAOBJECT = "Results";
	public static String FILENAME_DATAOBJECT_EXT = "data";

	//PRELOADING
	public static final String PRELOADING_PREFIX = "Preloaded_";
	public static final String PRELOADING_EXT = ".pldata";
	public static final String PRELOADING_COMPLETE_RULESSEED = PRELOADING_PREFIX + "Complete_RulesSeed";

	//REPRESENTATION
	public static boolean ALWAYS_USE_SHOW_FULL_CONSTRAINT = false;

	//TEST LOG
	public static LocalDateTime BASE_DATETIME = LocalDateTime.now();

	//TASK
	//	public static void main(String[] args) throws Exception {
	//		System.out.println("Exporting Config...");
	//		FileManager.writeAll(new File("config.txt"), exportConfig());
	//		System.out.println("Exported Config");
	//	}
	//	
	//	public static void importConfig(String configTXT) {
	//		
	//	}
	//	
	//	public static String exportConfig() {
	//		String res = "MINIMAL_CONFORMANCE=" + MINIMAL_CONFORMANCE;
	//		res += "\n" + "MINIMAL_CONFORMANCE_INTERMEDIATE=" + MINIMAL_CONFORMANCE_INTERMEDIATE;
	//		res += "\n" + "MINIMAL_SUPPORT=" + MINIMAL_SUPPORT;
	//		res += "\n" + "MINIMAL_SUPPORT=" + MINIMAL_SUPPORT;
	//		res += "\n" + "MINIMAL_SUPPORT=" + MINIMAL_SUPPORT;
	//		
	//		return res.trim();
	//	}
}