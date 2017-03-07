package miner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.JFileChooser;

import miner.kb.KnowledgeBase;
import miner.log.Log;
import miner.rule.BatchRule;
import miner.rule.BoundedConstraintComparator;
import miner.rule.Rule;
import miner.rule.RuleLengthComparator;
import miner.rule.RulePrintComparator;
import miner.rule.SingleRule;
import miner.subminer.AprioriGeneralRuleMiner;
import miner.subminer.CompleteSearchDecisionRuleMiner;
import miner.subminer.genetic.SeededGeneticDecisionRuleMiner;
import model.Activity;
import model.Constraint;
import model.constraint.BoundedConstraint;
import model.constraint.ExistenceConstraint;
import model.constraint.RelationConstraint;
import model.constraint.existence.AtLeast;
import model.constraint.existence.AtMost;
import model.constraint.existence.OccurrenceConstraint;
import model.constraint.existence.extra.First;
import model.constraint.existence.extra.Last;
import model.constraint.relation.AlternatePrecedence;
import model.constraint.relation.AlternateResponse;
import model.constraint.relation.ChainPrecedence;
import model.constraint.relation.ChainResponse;
import model.constraint.relation.Precedence;
import model.constraint.relation.RespondedPresence;
import model.constraint.relation.Response;
import model.data.BooleanDataAttribute;
import model.data.DataAttribute;
import model.data.Decision;
import model.expression.ActivityExpression;
import model.expression.AtomicActivityExpression;
import model.expression.AtomicExistenceExpression;
import model.expression.NonAtomicActivityExpression;
import model.resource.DirectResource;
import model.resource.Resource;
import util.FileManager;
import util.Logger;
import util.xes.XESReader;

//First phase: complete search (apriori based) for all general rules
//Second phase: evolutionary generation of decision-dependent rules
public class DeciClareMinerV11 extends KnowledgeBaseMiner {

	public static void main(String[] args) {
		try {
			if(args != null && args.length > 0) {
				if(args.length == 1)
					oneTest(args[0], null);
				else if(args.length == 2)
					oneTest(args[0], Integer.parseInt(args[1]));
				else if(args.length == 3)
					batchTest(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			} else
				manualStart();
		} catch(Exception e) {
			e.printStackTrace();
		} finally
		{ 
			Logger.getInstance().close();
		}
		System.exit(0);
	}

	private static void manualStart() throws Exception {
		System.out.println("Select log...");
		File[] files = FileManager.selectOpenFile(null, new File("models"), JFileChooser.FILES_AND_DIRECTORIES,
				FileManager.getFileExtensionFilter("XES-files", "xes"));
		if(files == null)
			System.exit(0);
		Log log = loadLog(files[0]);
		//Pre-processing log
		KnowledgeBase kb = new KnowledgeBase(log);
		//		Activity[] activitiesInScope = kb.getActivities().toArray(new Activity[kb.getActivities().size()]);
		//		DataAttribute[] dataAttributesInScope = kb.getDataElementsWithAllUsedValues().toArray(
		//				new DataAttribute[kb.getDataElementsWithAllUsedValues().size()]);
		//		Cluster[] clusters = calculateClusters(log, activitiesInScope, dataAttributesInScope);
		//Start mining
		DeciClareMinerV11 dm = new DeciClareMinerV11(log, kb, null);
		dm.execute();
	}

	private static void oneTest(String logFile, Integer seedToCheck) throws Exception {
		File f = new File(logFile);
		Log log = loadLog(f);
		//Pre-processing log
		KnowledgeBase kb = new KnowledgeBase(log);
		//		Activity[] activitiesInScope = kb.getActivities().toArray(new Activity[kb.getActivities().size()]);
		//		DataAttribute[] dataAttributesInScope = kb.getDataElementsWithAllUsedValues().toArray(
		//				new DataAttribute[kb.getDataElementsWithAllUsedValues().size()]);
		//		Cluster[] clusters = calculateClusters(log, activitiesInScope, dataAttributesInScope);
		//Start mining
		DeciClareMinerV11 dm = new DeciClareMinerV11(log, kb, null, seedToCheck);
		dm.execute();
	}

	private static void batchTest(String logFile, int nrOfRunsPerBatch, int startNr) throws Exception {
		if(nrOfRunsPerBatch < 1)
			throw new IllegalArgumentException("Wrong input: " + nrOfRunsPerBatch);
		File f = new File(logFile);
		Log log = loadLog(f);
		//Pre-processing log
		KnowledgeBase kb = new KnowledgeBase(log);
		//to facilitate automatic test runs
		//Run algorithm multiple times
		int prefixLength = (""+((startNr + nrOfRunsPerBatch - 1) * 1)).length();
		run("ALL_" + Config.MAX_BRANCHING_LEVEL + "_" + Math.round(((double) Config.MAX_SEARCHTIME_GENETIC)/60000) + "_",
				nrOfRunsPerBatch, startNr, prefixLength, log, kb);
	}

	private static void run(String preprefix, int nrOfRunsPerBatch, int startNr, int prefixLength,
			Log log, KnowledgeBase kb) {
		for(int i = 0; i < nrOfRunsPerBatch; i++) {
			String prefix = ""+(i+startNr);
			while(prefix.length() < prefixLength || prefix.length() < 5)
				prefix = "0" + prefix;
			try {
				System.out.println("---------START " + preprefix+prefix + "---------");
				new DeciClareMinerV11(log, kb, preprefix+prefix).execute();
				System.out.println("---------END " + preprefix+prefix + "---------");
			} catch(Exception e) {
				e.printStackTrace();
			} finally
			{ 
				Logger.getInstance().close();
			}
		}
	}

	private static Log loadLog(File f) throws Exception {
		//Trace need to be sorted on start time (small to big) and secondarily on end time (small to big)!!!
		System.out.print("Loading log...");
		Log log;
		//check if preloaded file is available
		File preloadedLog = new File(Config.PRELOADING_PREFIX + f.getName() + Config.PRELOADING_EXT);
		if(preloadedLog.exists()) {//preloaded file available
			ObjectInputStream input = null;
			try {
				input = new ObjectInputStream(new FileInputStream(preloadedLog));
				log = (Log) input.readObject();
				input.close();
			} catch (Exception ex) {
				throw new RuntimeException("An error occurred when loading the preloaded log!", ex);
			}
		} else {//preloaded file not available
			if(f == null || !f.exists()) {
				System.out.println("The given log can't be found! (" + f + ")");
				System.exit(0);
			}
			log = XESReader.getLog(f);
			log.trimToSize();
			//make preloaded file
			ObjectOutputStream out = null;
			try {
				out = new ObjectOutputStream(new FileOutputStream(preloadedLog));
				out.writeObject(log);
				out.close();
			} catch (Exception ex) {
				throw new RuntimeException("An error occurred when creating the preloaded log!", ex);
			}
		}
		System.out.println("Done");
		return log;
	}

	private final String filenamePrefix;
	private final Integer seedToCheck;

	public DeciClareMinerV11(Log log, KnowledgeBase kb, String filenamePrefix) {
		this(log, kb, filenamePrefix, null);
	}

	public DeciClareMinerV11(Log log, KnowledgeBase kb, String filenamePrefix, Integer seedToCheck) {
		super(log, kb);
		if(filenamePrefix == null || filenamePrefix.equals(""))
			this.filenamePrefix = "";
		else
			this.filenamePrefix = filenamePrefix + " ";
		this.seedToCheck = seedToCheck;
		System.out.println(this.getClass().getSimpleName() + " is initiating mining");
		//Initialize mining
		if(Config.DO_LOGGING) {
			Logger.getInstance().setLogName(this.filenamePrefix + this.getClass().getSimpleName());
			Logger.getInstance().logConfig();
		}

		//print and log general info
		System.out.println("Mining initiated on log with " + kb.getNrOfTraces() + " traces");
		if(Config.DO_LOGGING)
			Logger.getInstance().logStat_general("Log size", kb.getNrOfTraces()+"");
		System.out.println(kb.getActivities().size() + " activities have been recognized: " + kb.getActivities());
		if(Config.DO_LOGGING)
			Logger.getInstance().logStat_general("Log activities", kb.getActivities().size()+"", kb.getActivities()+"");
		String tmp = null;
		{
			for(Resource r : kb.getResources()) {
				String name = r.getName() + ":";
				if(r instanceof DirectResource)
					name += "DirectResource";
				else
					name += "ResourceRole";
				if(tmp == null)
					tmp = name;
				else
					tmp += ", " + name;
			}
			if(tmp == null)
				tmp = "[]";
			else
				tmp = "[" + tmp + "]";
		}
		System.out.println(kb.getResources().size() + " resources have been recognized: "
				+ tmp);
		if(Config.DO_LOGGING)
			Logger.getInstance().logStat_general("Log resources", kb.getResources().size()+"", tmp);
		tmp = null;
		{
			for(DataAttribute de : kb.getDataElements()) {
				String name = de.getName() + ":";
				if(de instanceof BooleanDataAttribute)
					name += "Boolean";
				else
					name += "Category";
				if(tmp == null)
					tmp = name;
				else
					tmp += ", " + name;
			}
			if(tmp == null)
				tmp = "[]";
			else
				tmp = "[" + tmp + "]";
		}
		System.out.println(kb.getDataElements().size() + " data elements have been recognized: "
				+ tmp);
		if(Config.DO_LOGGING)
			Logger.getInstance().logStat_general("Log data elements", kb.getDataElements().size()+"", tmp);
	}

	public String getFilenamePrefix() {
		return filenamePrefix;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Rule> execute() throws Exception {
		//Step 1: Create knowledge base (executed in constructor)
		//Step 2: Start mining phase 1
		//		==> check if preloaded file is available (step 1 = deterministic!)
		ArrayList<Rule> rules_phase1;
		File preloadedResultsPhase1 = new File(Config.PRELOADING_PREFIX + getLog().getName()
				+ "_ResP1_" + Config.MINIMAL_CONFORMANCE + "_" + Config.MINIMAL_SUPPORT + "_" + Config.MAX_BRANCHING_LEVEL
				+ Config.PRELOADING_EXT);
		if(preloadedResultsPhase1.exists()) {//preloaded file available
			ObjectInputStream input = null;
			try {
				input = new ObjectInputStream(new FileInputStream(preloadedResultsPhase1));
				rules_phase1 = (ArrayList<Rule>) input.readObject();
				input.close();
			} catch (Exception ex) {
				throw new RuntimeException("An error occurred when loading the results from phase 1!", ex);
			}
		} else {//preloaded file not available
			AprioriGeneralRuleMiner am = new AprioriGeneralRuleMiner(getLog(), getKB());
			long t_start = System.currentTimeMillis();
			rules_phase1 = am.execute();
			if(Config.DO_LOGGING)
				Logger.getInstance().close_p1();
			long t_end = System.currentTimeMillis();
			System.out.println("Phase 1 completed in " + (t_end-t_start) + "ms");
			if(Config.DO_LOGGING)
				Logger.getInstance().logStat_general("Phase 1", (t_end-t_start)+"");
			//make preloaded file
			ObjectOutputStream out = null;
			try {
				out = new ObjectOutputStream(new FileOutputStream(preloadedResultsPhase1));
				out.writeObject(rules_phase1);
				out.close();
			} catch (Exception ex) {
				throw new RuntimeException("An error occurred when creating the results for phase 1!", ex);
			}
		}

		//Collect general rules from p1
		if(Config.SHOW_P1_RESULT_RULES)
			System.out.println("Resulting P1 rules:");
		SingleRule[] generalRules_P1 = collectGeneralRules(rules_phase1);

		//Collect intermediate rules from p1
		if(Config.SHOW_P1_RESULT_RULES)
			System.out.println("");
		if(Config.SHOW_P1_INTERMEDIATE_RULES) {
			System.out.println("Intermediate P1 rules:");
		}
		Rule[] seedsP2 = collectSeedsP2(rules_phase1);

		System.out.println("Mined " + generalRules_P1.length + " general rules");
		System.out.println("Mined " + seedsP2.length + " seed rules for phase 2");
		if(Config.DO_LOGGING) {
			Logger.getInstance().logStat_general("Phase 1 general rules", generalRules_P1.length+"");
			Logger.getInstance().logStat_general("Phase 2 seed rules", seedsP2.length+"");
		}
		System.out.println();

		//Start mining phase 2
		Miner gm;
		if(!Config.PHASE2_DO_COMPLETESEARCH)
			gm = new SeededGeneticDecisionRuleMiner(
					getLog(), getKB(), seedsP2);
		else
			gm = new CompleteSearchDecisionRuleMiner(
					getLog(), getKB(), seedsP2, seedToCheck);
		long t_start = System.currentTimeMillis();
		List<Rule> rules_phase2 = gm.execute();
		long t_end = System.currentTimeMillis();
		System.out.println("Phase 2 completed in " + (t_end-t_start) + "ms");
		if(Config.DO_LOGGING)
			Logger.getInstance().logStat_general("Phase 2", (t_end-t_start)+"");

		//merge and filter results from phase 2
		System.out.println("Merging and filtering rules mined in phase 2...");
		List<SingleRule> rules_phase2_mergeAndFilter = mergeAndFilter(rules_phase2, getLog());

		ArrayList<SingleRule> rules = new ArrayList<>(Arrays.asList(generalRules_P1));
		rules.addAll(rules_phase2_mergeAndFilter);
		postProcessing(rules);
		if(Config.DO_LOGGING)
			Logger.getInstance().logStat_general("Phase 1 and phase 2 combined", rules.size()+"");
		//		System.out.println("Filtering mined rules...");
		//		rules = filterRules(rules);
		System.out.println(this.getClass().getSimpleName() + " mined " + rules.size() + " rules");
		if(Config.DO_LOGGING)
			Logger.getInstance().logStat_general("Phase 1 and phase 2 combined and filtered", rules.size()+"");
		System.out.println("Sorting mined rules...");
		Collections.sort(rules, new RulePrintComparator());
		System.out.println("Exporting mined rules...");
		if(Config.SHOW_RESULT_RULES)
			System.out.println("Resulting rules:");
		String text = Rule.getCSV_HEADERS();
		for(SingleRule r : rules) {
			if(Config.SHOW_RESULT_RULES)
				System.out.println(r);
			text += "\n" + ((SingleRule) r).getCSV();
		}
		if(Config.SHOW_RESULT_RULES_TEXTUALREPRESENTATION) {
			System.out.println("Resulting rules in text:");
			for(SingleRule r : rules)
				System.out.println(r.toString(true));
		}
		//write out result as java data object
		Logger.getInstance().writeToFile(rules, Config.FILENAME_RESULTS_DATAOBJECT,
				Config.FILENAME_DATAOBJECT_EXT);
		//write out results as csv
		Logger.getInstance().writeToFile(text.trim(), Config.FILENAME_RESULT, Config.FILE_CSV);
		Logger.getInstance().close_p2();
		System.out.println(this.getClass().getSimpleName() + " is finished mining!");
		return new ArrayList<Rule>(rules);
	}

	//TODO: in BatchRules ipv SingleRules!
	private SingleRule[] collectGeneralRules(ArrayList<Rule> rules_phase1) throws Exception {
		String text = Rule.getCSV_HEADERS();
		HashSet<SingleRule> tmp = new HashSet<>();
		for(Rule r : rules_phase1) {
			if(r instanceof BatchRule) {
				BatchRule br = (BatchRule) r;
				ArrayList<SingleRule> x = new ArrayList<>();
				for(SingleRule sr : br.rules())
					if(sr.getConformancePercentage_Traces() >= Config.MINIMAL_CONFORMANCE
					&& sr.getSupportPercentage() >= Config.MINIMAL_SUPPORT)
						x.add(sr);
				removeSubsumedRules(x);
				tmp.addAll(x);
				for(SingleRule srx : x)
					text += "\n" + srx.getCSV();
			} else
				throw new IllegalArgumentException("TODO?");
		}

		ArrayList<SingleRule> generalRules_P1 = filter(new ArrayList<>(tmp));
		if(Config.SHOW_P1_RESULT_RULES)
			for(SingleRule s : generalRules_P1)
				System.out.println(s);
		Logger.getInstance().writeToFile(text.trim(), Config.FILENAME_RESULT_P1, Config.FILE_CSV);
		return generalRules_P1.toArray(new SingleRule[generalRules_P1.size()]);
	}

	private static void removeSubsumedRules(List<SingleRule> list) {
		for(int i1 = 0; i1 < list.size(); i1++)
			for(int i2 = i1+1; i2 < list.size(); i2++)
				if(list.get(i1).getConstraint() instanceof RelationConstraint) {
					RelationConstraint rc1 = (RelationConstraint) list.get(i1).getConstraint();
					RelationConstraint rc2 = (RelationConstraint) list.get(i2).getConstraint();
					if(rc1.getConditionExpression().equals(rc2.getConditionExpression())
							&& rc1.getConsequenceExpression().equals(rc2.getConsequenceExpression())) {
						boolean isForward1 = (rc1 instanceof Response || rc1 instanceof ChainResponse || rc1 instanceof AlternateResponse);
						boolean isForward2 = (rc2 instanceof Response || rc2 instanceof ChainResponse || rc2 instanceof AlternateResponse);
						boolean isBackward1 = (rc1 instanceof Precedence || rc1 instanceof ChainPrecedence || rc1 instanceof AlternatePrecedence);
						boolean isBackward2 = (rc2 instanceof Precedence || rc2 instanceof ChainPrecedence || rc2 instanceof AlternatePrecedence);
						boolean isCentral1 = (rc1 instanceof RespondedPresence);
						boolean isCentral2 = (rc2 instanceof RespondedPresence);
						if(((isForward1 || isCentral1) && (isForward2 || isCentral2))
								|| ((isBackward1 || isCentral1) && (isBackward2 || isCentral2))) {
							int o1 = RulePrintComparator.ORDER.indexOf(rc1.getClass());
							int o2 = RulePrintComparator.ORDER.indexOf(rc2.getClass());
							if(o1 > o2) {
								list.remove(i2);
								i2--;
							} else if(o1 < o2) {
								list.remove(i1);
								i1--;
								break;
							} else
								throw new IllegalArgumentException("TODO?");
						}
					}
				}
	}

	private ArrayList<SingleRule> filter(ArrayList<SingleRule> rules) {
		for(int i1 = 0; i1 < rules.size(); i1++)
			for(int i2 = i1+1; i2 < rules.size(); i2++) {//TODO: ook als zelfde decision (of algemenere zelfde)?
				if((rules.get(i1).getConstraint() instanceof AtLeast
						&& rules.get(i1).getNrOfViolations() == 0
						&& rules.get(i2).getConstraint() instanceof RespondedPresence)
						|| (rules.get(i2).getConstraint() instanceof AtLeast
								&& rules.get(i2).getNrOfViolations() == 0
								&& rules.get(i1).getConstraint() instanceof RespondedPresence)) {
					AtLeast al;
					RespondedPresence rp;
					if(rules.get(i1).getConstraint() instanceof AtLeast) {
						al = (AtLeast) rules.get(i1).getConstraint();
						rp = (RespondedPresence) rules.get(i2).getConstraint();
					} else {
						al = (AtLeast) rules.get(i2).getConstraint();
						rp = (RespondedPresence) rules.get(i1).getConstraint();
					}
					if((rp.getConditionExpression() instanceof AtomicExistenceExpression
							&& ((AtomicExistenceExpression) rp.getConditionExpression()).
							getExistenceConstraint().equals(al))
							|| (rp.getConsequenceExpression() instanceof AtomicExistenceExpression
									&& ((AtomicExistenceExpression) rp.getConsequenceExpression()).
									getExistenceConstraint().equals(al))) {
						if(rules.get(i1).getConstraint() instanceof RespondedPresence) {//remove i1
							rules.remove(i1);
							i1--;
							break;
						} else {//remove i2
							rules.remove(i2);
							i2--;
						}
					}
				}
			}

		//1) alle AtLeasts in 'todo'-lijst
		//2) resultaatlijst: bij start bevat deze alle AtLeasts van size 1 (die uit andere lijst halen)
		//3) 'todo'-lijst sorteren volgens aantal elementen (korter = vooraan)
		//4) neem eerste element van 'todo'-lijst
		//				a) indien aan voorwaarden voldoen om in resultaatlijst te komen, verplaatsen van ene naar andere lijst
		//					==> vgl enkel op basis van elementen in resultaatlijst
		//				b) anders, verwijderen uit 'todo'-lijst
		//
		//voorwaarden AtLeast: als # > som 'ouders', dan houden
		//voorwaarden AtMost: als # < som 'ouders', dan houden
		ArrayList<SingleRule> todos_atL = new ArrayList<>();
		ArrayList<SingleRule> todos_atM = new ArrayList<>();
		ArrayList<SingleRule> res_atL = new ArrayList<>();
		ArrayList<SingleRule> res_atM = new ArrayList<>();
		ArrayList<SingleRule> others = new ArrayList<>();
		for(SingleRule sr : rules) {
			if(sr.getConstraint() instanceof AtLeast) {
				if(((AtLeast) sr.getConstraint()).getActivityExpression().getNrOfElements() > 1)
					todos_atL.add(sr);
				else
					res_atL.add(sr);
			} else if(sr.getConstraint() instanceof AtMost) {
				if(((AtMost) sr.getConstraint()).getActivityExpression().getNrOfElements() > 1)
					todos_atM.add(sr);
				else
					res_atM.add(sr);
			} else
				others.add(sr);
		}
		//sort according to bound
		Collections.sort(todos_atL, new BoundedConstraintComparator());
		while(!todos_atL.isEmpty()) {
			SingleRule sr = todos_atL.get(0);
			HashSet<SingleRule> parents = getParents(sr, res_atL);
			if(parents == null)
				res_atL.add(sr);
			else {
				int sum = 0;
				for(SingleRule p : parents)
					sum += ((BoundedConstraint) p.getConstraint()).getBound();
				if(((BoundedConstraint) sr.getConstraint()).getBound() > sum)
					res_atL.add(sr);
			}
			todos_atL.remove(0);
		}
		others.addAll(res_atL);
		//sort according to bound
		Collections.sort(todos_atM, new BoundedConstraintComparator());
		while(!todos_atM.isEmpty()) {
			SingleRule sr = todos_atM.get(0);
			HashSet<SingleRule> parents = getParents(sr, res_atM);
			if(parents == null)
				res_atM.add(sr);
			else {
				int sum = 0;
				for(SingleRule p : parents)
					sum += ((BoundedConstraint) p.getConstraint()).getBound();
				if(((BoundedConstraint) sr.getConstraint()).getBound() < sum)
					res_atM.add(sr);
			}
			todos_atM.remove(0);
		}
		others.addAll(res_atM);
		Collections.sort(others, new RulePrintComparator());
		return others;
	}

	private HashSet<SingleRule> getParents(SingleRule r, ArrayList<SingleRule> list) {
		HashSet<ActivityExpression> childExpressions = ((NonAtomicActivityExpression) ((ExistenceConstraint)
				r.getConstraint()).getActivityExpression()).getExpressions();
		boolean isAtM = ((ExistenceConstraint) r.getConstraint()) instanceof AtMost;
		HashSet<Activity> toFind = new HashSet<>();
		for(ActivityExpression ae : childExpressions)
			toFind.add(((AtomicActivityExpression) ae).getActivity());
		int maxSize = toFind.size();
		HashSet<SingleRule> res = new HashSet<>();
		for(int i = list.size()-1; i >= 0; i--) {
			SingleRule sr = list.get(i);
			if(((ExistenceConstraint) sr.getConstraint()).getActivityExpression().getNrOfElements() < maxSize) {
				if(((ExistenceConstraint) sr.getConstraint()).getActivityExpression() instanceof AtomicActivityExpression) {
					AtomicActivityExpression x = (AtomicActivityExpression) ((ExistenceConstraint) sr.getConstraint()).getActivityExpression();
					if(toFind.contains(x.getActivity())) {
						res.add(sr);
						toFind.remove(x.getActivity());
					}
				} else {
					NonAtomicActivityExpression x = (NonAtomicActivityExpression) ((ExistenceConstraint) sr.getConstraint()).getActivityExpression();
					HashSet<Activity> tmp = new HashSet<>();
					for(ActivityExpression ae : x.getExpressions())
						tmp.add(((AtomicActivityExpression) ae).getActivity());
					//				if(!isAtM && tmp.size() > maxSize)
					//					break;
					if(toFind.containsAll(tmp)) {
						res.add(sr);
						toFind.removeAll(tmp);
					}
				}
				if(toFind.isEmpty())
					return res;
			}
		}
		if(res.isEmpty() || isAtM)
			return null;
		return res;
	}

	private Rule[] collectSeedsP2(ArrayList<Rule> rules_phase1) throws Exception {
		String text = Rule.getCSV_HEADERS();
		HashSet<Rule> tmp = new HashSet<>();
		for(Rule r : rules_phase1) {
			if(r instanceof BatchRule) {
				BatchRule br = (BatchRule) r;
				//OccurrenceConstraint also those with 0 violations
				if(r.getConstraint() instanceof OccurrenceConstraint)
					tmp.add(br);
				else {
					ArrayList<SingleRule> x = new ArrayList<>();
					for(SingleRule sr : br.rules())
						if(sr.getNrOfViolations() > 0
								&& sr.getPotential() >= Config.MINIMAL_SUPPORT)
							x.add(sr);
					if(!x.isEmpty()) {
						BatchRule newBR = new BatchRule(br.getConstraint(), x.toArray(new Rule[x.size()]), null);
						tmp.add(newBR);
					}
				}

			} else if(r instanceof SingleRule) {
				throw new RuntimeException("TODO?");
				//				if(!(((SingleRule) r).getConstraint() instanceof BoundedConstraint)
				//						&& ((SingleRule) r).getViolatingTraces() > 0
				//						&& ((SingleRule) r).getPotential() >= Config.MINIMAL_SUPPORT)
				//					tmp.add(r);
			} else
				throw new RuntimeException("TODO?");
		}
		ArrayList<Rule> intermediateRules_P1 = new ArrayList<>(tmp);
		//		Collections.sort(intermediateRules_P1, new RulePrintComparator());

		//log intermediateRules_P1
		for(Rule r : intermediateRules_P1) {
			if(r instanceof SingleRule) {
				text += "\n" + ((SingleRule) r).getCSV();
				if(Config.SHOW_P1_INTERMEDIATE_RULES)
					System.out.println(r);
			} else {
				for(SingleRule sr : r.rules())
					text += "\n" + sr.getCSV() + ";" + r.toString();
				if(Config.SHOW_P1_INTERMEDIATE_RULES)
					System.out.println(((BatchRule) r).rules());
			}
		}
		Logger.getInstance().writeToFile(text.trim(), Config.FILENAME_INTERMEDIATE_P1, Config.FILE_CSV);
		Rule[] res = intermediateRules_P1.toArray(new Rule[tmp.size()]);
		return res;
	}

	public static List<SingleRule> mergeAndFilter(List<Rule> rules, Log log) {
		ArrayList<Rule> tmp = new ArrayList<>(rules);
		ArrayList<SingleRule> res = new ArrayList<>();
		//sort BatchRules with less subrules first
		RuleLengthComparator sorter = new RuleLengthComparator();
		Collections.sort(tmp, sorter);
		//merge and filter
		while(!tmp.isEmpty()) {
			Rule r1 = tmp.get(0);
			Constraint reeval = null;
			for(int i2 = 1; i2 < tmp.size(); i2++)
				if(r1.getConstraint().equals(tmp.get(i2).getConstraint(), false)) {
					Rule r2 = tmp.get(i2);
					List<Constraint> compatibles = getCompatible(r1, r2);
					if(!compatibles.isEmpty()) {
						if(reeval == null) {
							reeval = r1.getConstraint().getDecisionlessCopy();
							reeval.setActivationDecision(new Decision());
							reeval.getActivationDecision().addRules(r1.getConstraint().getActivationDecision().getRules());
						}
						reeval.getActivationDecision().addRules(r2.getConstraint().getActivationDecision().getRules());
						BatchRule br2 = (BatchRule) r2;
						if(compatibles.size() != br2.getRules().length) {//purge r2!
							ArrayList<Rule> newRs = new ArrayList<>();
							for(SingleRule sr : r2.rules()) {
								boolean isCompatible = false;
								for(Constraint c : compatibles)
									if(c.equals(sr.getConstraint(), false)) {
										isCompatible = true;
										break;
									}
								if(!isCompatible)
									newRs.add(sr);
							}
							br2.setRules(newRs.toArray(new Rule[newRs.size()]));
						} else
							tmp.remove(i2);
						i2--;
					}
				}
			if(reeval != null) {
				BatchRule br1 = (BatchRule) r1;
				BatchRule br = (BatchRule) reeval.evaluate(log, r1.getSeed());
				List<SingleRule> x = new ArrayList<>();
				for(SingleRule sr : br.rules())
					if(sr.getConformancePercentage_Traces() >= Config.MINIMAL_CONFORMANCE
					&& sr.getSupportPercentage() >= Config.MINIMAL_SUPPORT) {
						boolean isCompatible = false;
						for(Rule r : br1.getRules())
							if(r.getConstraint().equals(sr.getConstraint(), false)) {
								isCompatible = true;
								break;
							}
						if(isCompatible)
							x.add(sr);
						//						else//TODO: of gwn bijhouden (=extra info)? Er moet wel een reden zijn geweest waarom br1 deze subrule niet had...
						//							x.add(sr);
					}
				removeSubsumedRules(x);
				res.addAll(x);
				tmp.remove(0);
				Collections.sort(tmp, sorter);
			} else {
				List<SingleRule> x = new ArrayList<>();
				for(SingleRule sr : r1.rules())
					if(sr.getConformancePercentage_Traces() >= Config.MINIMAL_CONFORMANCE
					&& sr.getSupportPercentage() >= Config.MINIMAL_SUPPORT)
						x.add(sr);
				removeSubsumedRules(x);
				res.addAll(x);
				tmp.remove(0);
			}
		}
		return res;
	}

	/**
	 * Post-processing filter: Last and First constraints subsume some other constraints (Response, Precedence and AtLeast1)
	 */
	private void postProcessing(ArrayList<SingleRule> rules) {
		for(int i1 = 0; i1 < rules.size(); i1++) {
			if(rules.get(i1).getConstraint() instanceof Last) {
				Last l = (Last) rules.get(i1).getConstraint();
				AtomicExistenceExpression aee = new AtomicExistenceExpression(
						new AtLeast(null, null, l.getActivityExpression(), 1, 0, -1, false));
				for(int i2 = 0; i2 < rules.size(); i2++) {
					if(rules.get(i2).getConstraint() instanceof Response) {
						Response r = (Response) rules.get(i2).getConstraint();
						if(r.getConsequenceExpression().equals(aee)) {
							if(l.getActivationDecision() == null && r.getActivationDecision() == null) {
								rules.remove(i2);//response redundant
								if(i1 > i2)
									i1--;
								i2--;
							} else if(l.getActivationDecision() != null
									&& r.getActivationDecision() != null
									&& r.getActivationDecision().getRules().containsAll(l.getActivationDecision().getRules())) {
								r.getActivationDecision().getRules().removeAll(l.getActivationDecision().getRules());//response partially redundant
								if(r.getActivationDecision().getRules().isEmpty()) {
									rules.remove(i2);//response fully redundant
									if(i1 > i2)
										i1--;
									i2--;
								}
							}
						}
					} else if(rules.get(i2).getConstraint() instanceof AtLeast) {
						AtLeast atL = (AtLeast) rules.get(i2).getConstraint();
						if(atL.getBound() == 1 && atL.getActivityExpression().equals(l.getActivityExpression())) {
							if(l.getActivationDecision() == null && atL.getActivationDecision() == null) {
								rules.remove(i2);//response redundant
								if(i1 > i2)
									i1--;
								i2--;
							} else if(l.getActivationDecision() != null
									&& atL.getActivationDecision() != null
									&& atL.getActivationDecision().getRules().containsAll(l.getActivationDecision().getRules())) {
								atL.getActivationDecision().getRules().removeAll(l.getActivationDecision().getRules());//response partially subsumed
								if(atL.getActivationDecision().getRules().isEmpty()) {
									rules.remove(i2);//response fully redundant
									if(i1 > i2)
										i1--;
									i2--;
								}
							}
						}
					}
				}
			} else if(rules.get(i1).getConstraint() instanceof First) {
				First f = (First) rules.get(i1).getConstraint();
				AtomicExistenceExpression aee = new AtomicExistenceExpression(
						new AtLeast(null, null, f.getActivityExpression(), 1, 0, -1, false));
				for(int i2 = 0; i2 < rules.size(); i2++) {
					if(rules.get(i2).getConstraint() instanceof Precedence) {
						Precedence p = (Precedence) rules.get(i2).getConstraint();
						if(p.getConsequenceExpression().equals(aee)) {
							if(f.getActivationDecision() == null && p.getActivationDecision() == null) {
								rules.remove(i2);//precedence redundant
								if(i1 > i2)
									i1--;
								i2--;
							} else if(f.getActivationDecision() != null
									&& p.getActivationDecision() != null
									&& p.getActivationDecision().getRules().containsAll(f.getActivationDecision().getRules())) {
								p.getActivationDecision().getRules().removeAll(f.getActivationDecision().getRules());//precedence partially redundant
								if(p.getActivationDecision().getRules().isEmpty()) {
									rules.remove(i2);//precedence fully redundant
									if(i1 > i2)
										i1--;
									i2--;
								}
							}
						}
					} else if(rules.get(i2).getConstraint() instanceof AtLeast) {
						AtLeast atL = (AtLeast) rules.get(i2).getConstraint();
						if(atL.getBound() == 1 && atL.getActivityExpression().equals(f.getActivityExpression())) {
							if(f.getActivationDecision() == null && atL.getActivationDecision() == null) {
								rules.remove(i2);//response redundant
								if(i1 > i2)
									i1--;
								i2--;
							} else if(f.getActivationDecision() != null
									&& atL.getActivationDecision() != null
									&& atL.getActivationDecision().getRules().containsAll(f.getActivationDecision().getRules())) {
								atL.getActivationDecision().getRules().removeAll(f.getActivationDecision().getRules());//response partially subsumed
								if(atL.getActivationDecision().getRules().isEmpty()) {
									rules.remove(i2);//response fully redundant
									if(i1 > i2)
										i1--;
									i2--;
								}
							}
						}
					}
				}
			}
		}
	}

	private static List<Constraint> getCompatible(Rule r1, Rule r2) {
		ArrayList<Constraint> res = new ArrayList<>();
		for(SingleRule sr1 : r1.rules())
			for(SingleRule sr2 : r2.rules())
				if(sr1.getConstraint().equals(sr2.getConstraint(), false)) {
					res.add(sr1.getConstraint());
					break;
				}
		return res;
	}

	//	//TODO: useful?
	//	private static Cluster[] calculateClusters(Log log, Activity[] activitiesInScope,
	//			DataAttribute[] dataAttributesInScope) {
	//		ArrayList<ClusterPoint> cps = new ArrayList<>();
	//		for(Trace t : log)
	//			cps.add(new ClusterPoint(t));
	//		OptimizedHierarchicalKMeansInt k = new OptimizedHierarchicalKMeansInt(false);
	//		long t1 = System.currentTimeMillis();
	//		Cluster[] clusters = k.run(cps, activitiesInScope, dataAttributesInScope,
	//				Config.NUM_CLUSTERS_TOP, Config.NUM_CLUSTERS_SUB,
	//				Config.CLUSTER_SPLIT_THRESHOLD,
	//				Config.MAX_SEARCHTIME_TOP, Config.MAX_SEARCHTIME_SUB,
	//				Config.MAX_SEARCH_ITERATIONS_TOP, Config.MAX_SEARCH_ITERATIONS_SUB,
	//				Config.CLUSTER_RETRIES);
	//		long t2 = System.currentTimeMillis();
	//
	//		int nrOfTotalClusters = clusters.length;
	//		int nrOfLeafClusters = 0;
	//		for(Cluster c : clusters) {
	//			nrOfTotalClusters += c.getNumberOfSubclusters();
	//			if(c.hasSubClusters())
	//				nrOfLeafClusters += c.getNumberOfLeafSubclusters();
	//			else
	//				nrOfLeafClusters++;
	//		}
	//		System.out.println("Hierarchical clustering " + log.size() + " traces "
	//				+ "based on a " + Profile.getRandomProfile(activitiesInScope, dataAttributesInScope).getDimension()
	//				+ "-dimensional vector took " + (((double) (t2-t1))/1000) + "secs");
	//		System.out.println(nrOfTotalClusters + " (sub)clusters found");
	//		if(nrOfLeafClusters == 0)
	//			System.out.println(nrOfLeafClusters + " leaf clusters found");
	//		else
	//			System.out.println(nrOfLeafClusters + " leaf clusters found (containing "
	//					+ (log.size()/nrOfLeafClusters) + " traces on avg)");
	//		return clusters;
	//	}
}