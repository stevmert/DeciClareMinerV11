package miner.subminer;

import java.util.ArrayList;
import java.util.HashSet;

import miner.Config;
import miner.KnowledgeBaseMiner;
import miner.kb.EventOccurrenceFrequencies;
import miner.kb.KnowledgeBase;
import miner.log.Log;
import miner.rule.BatchRule;
import miner.rule.Rule;
import miner.rule.SingleRule;
import model.Activity;
import model.Constraint;
import model.constraint.ExistenceConstraint;
import model.constraint.RelationConstraint;
import model.constraint.existence.AtLeast;
import model.constraint.existence.AtLeastChoice;
import model.constraint.existence.AtMost;
import model.constraint.existence.AtMostChoice;
import model.constraint.existence.OccurrenceConstraint;
import model.constraint.existence.extra.ExtremitiesConstraint;
import model.constraint.existence.extra.First;
import model.constraint.existence.extra.Last;
import model.constraint.relation.SequenceRelationConstraint;
import model.expression.ActivityExpression;
import model.expression.AtomicActivityExpression;
import model.expression.LogicalOperator;
import model.expression.NonAtomicActivityExpression;
import util.Logger;

public class AprioriGeneralRuleMiner extends KnowledgeBaseMiner {

	//Complete search (apriori based) for all general rules
	public AprioriGeneralRuleMiner(Log log, KnowledgeBase kb) {
		super(log, kb);
	}

	@Override
	public ArrayList<Rule> execute() throws Exception {
		System.out.println("Phase 1 " + this.getClass().getSimpleName() + " mining...");
		if(Config.DO_LOGGING)
			Logger.getInstance().logP1();
		ArrayList<Rule> minedRules = new ArrayList<>();
		if(Config.MINE_EXISTENCE)
			minedRules.addAll(mineFirstLastAtLeastAtMost_GeneralRules(Config.LEVEL_SPACES));
		//		if(Config.MINE_ATLEASTCHOICE || Config.MINE_ATMOSTCHOICE)
		//			minedRules.addAll(mineAtLeastAtMostChoice_GeneralRules(Config.LEVEL_SPACES));
		if(Config.MINE_RELATION)
			minedRules.addAll(mineRelation_GeneralRules(Config.LEVEL_SPACES));
		//		if(Config.MINE_RESOURCEUSAGE || Config.MINE_RESOURCEEXCLUSION)
		//			minedRules.addAll(mineResource_GeneralRules(Config.LEVEL_SPACES));
		System.out.println("Phase 1 mining completed");
		return minedRules;
	}

	private ArrayList<Rule> mineFirstLastAtLeastAtMost_GeneralRules(String spaces) {
		String types = (Config.MINE_FIRST?" First":"")
				+ (Config.MINE_LAST?" Last":"")
				+ (Config.MINE_ATLEAST?" AtLeast":"")
				+ (Config.MINE_ATMOST?" AtMost":"");
		System.out.println(spaces + "Mining" + types + "...");
		long t_start = System.currentTimeMillis();
		ArrayList<Rule> result = new ArrayList<>();
		long nrOfEvaluations_atomic = 0;
		for(Activity act : getKB().getActivities()) {
			AtomicActivityExpression aae = new AtomicActivityExpression(act);
			if((Config.MINE_FIRST && getKB().getWasFirst().contains(act))
					|| (Config.MINE_LAST && getKB().getWasLast().contains(act))) {
				nrOfEvaluations_atomic++;
				Rule r = mineExtremitiesConstraint(aae);
				if(r != null)
					result.add(r);
			}
			if(Config.MINE_ATLEAST || Config.MINE_ATMOST) {
				EventOccurrenceFrequencies eof = getKB().getEventOccurrenceFrequencies(act.getName());
				ArrayList<Rule> rules = new ArrayList<>();
				for(int i : eof.getOccurrenceFrequencies().keySet()) {
					int totalSupport_AtLeast_Traces = 0;
					int totalSupport_AtMost_Traces = 0;
					int totalSupport_AtLeast_nrOfViolations = 0;
					int totalSupport_AtMost_nrOfViolations = 0;
					int totalSupport_AtLeast_nrOfViolations_Traces = 0;
					int totalSupport_AtMost_nrOfViolations_Traces = 0;
					for(int k : eof.getOccurrenceFrequencies().keySet()) {
						if(k >= i)
							totalSupport_AtLeast_Traces += eof.getOccurrenceFrequencies().get(k);
						if(k <= i)
							totalSupport_AtMost_Traces += eof.getOccurrenceFrequencies().get(k);
						if(k < i) {
							totalSupport_AtLeast_nrOfViolations += (i - k) * eof.getOccurrenceFrequencies().get(k);
							totalSupport_AtLeast_nrOfViolations_Traces += eof.getOccurrenceFrequencies().get(k);
						}
						if(k > i) {
							totalSupport_AtMost_nrOfViolations += (k - i) * eof.getOccurrenceFrequencies().get(k);
							totalSupport_AtMost_nrOfViolations_Traces += eof.getOccurrenceFrequencies().get(k);
						}
					}
					if(Config.MINE_ATLEAST
							&& i > 0)
						rules.add(new SingleRule(new AtLeast(null, null, aae, i, 0, -1, false),
								totalSupport_AtLeast_Traces, totalSupport_AtLeast_nrOfViolations,
								totalSupport_AtLeast_Traces, totalSupport_AtLeast_nrOfViolations_Traces, getLog().size()));
					if(Config.MINE_ATMOST)
						rules.add(new SingleRule(new AtMost(null, null, aae, i, 0, -1, false),
								totalSupport_AtMost_Traces, totalSupport_AtMost_nrOfViolations,
								totalSupport_AtMost_Traces, totalSupport_AtMost_nrOfViolations_Traces, getLog().size()));
				}
				if(rules.size() > 1)
					result.add(new BatchRule(new OccurrenceConstraint(null, null, aae, false), rules.toArray(new Rule[rules.size()]), null));
			}
		}
		int atomic = getNumberOfRules(result);
		int atomic_aggr = result.size();
		System.out.println(Config.LEVEL_SPACES + spaces + "Mined "
				+ atomic + " atomic rules ("
				+ atomic_aggr + " aggregate rules)"
				+ " in " + nrOfEvaluations_atomic + " evaluations");
		long t_end;
		if(Config.MAX_BRANCHING_LEVEL == 1)
			t_end = System.currentTimeMillis();
		else {
			System.out.println(Config.LEVEL_SPACES + spaces + "Branching atomic existence rules...");
			NR_OF_EVALUATIONS = 0;
			result = branchExistence(result);
			t_end = System.currentTimeMillis();
			long nrOfEvaluations_mix = NR_OF_EVALUATIONS;
			NR_OF_EVALUATIONS = 0;
			int branched = getNumberOfRules(result) - atomic;
			int branched_aggr = result.size() - atomic_aggr;
			if(Config.DO_LOGGING)
				Logger.getInstance().logStat_p1(types, atomic, nrOfEvaluations_atomic,
						branched, nrOfEvaluations_mix, t_end - t_start);
			System.out.println(Config.LEVEL_SPACES + spaces + "Mined "
					+ branched + " new rules by branching ("
					+ branched_aggr + " aggregate rules)"
					+ " in " + nrOfEvaluations_mix + " evaluations");
		}
		System.out.println(Config.LEVEL_SPACES + spaces + "Mined "
				+ getNumberOfRules(result) + " total rules"
				+ " (" + result.size() + " aggregate rules)");
		System.out.println(Config.LEVEL_SPACES + spaces + "Completed in "
				+ (t_end-t_start) + "ms");
		return result;
	}

	private int getNumberOfRules(ArrayList<Rule> rules) {
		int count = 0;
		for(Rule r : rules)
			count += r.rules().size();
		return count;
	}

	//	private ArrayList<Rule> mineAtLeastAtMostChoice_GeneralRules(String spaces) {
	//		String types = (Config.MINE_ATLEASTCHOICE?" AtLeastChoice":"")
	//				+ (Config.MINE_ATMOSTCHOICE?" AtMostChoice":"");
	//		System.out.println(spaces + "Mining" + types + "...");
	//		long t_start = System.currentTimeMillis();
	//		ArrayList<Rule> result = new ArrayList<>();
	//		ArrayList<Activity> tmp = new ArrayList<>(getActivities());
	//		long nrOfEvaluations_atomic = 0;
	//		for(int i1 = 0; i1 < tmp.size(); i1++)
	//			for(int i2 = i1+1; i2 < tmp.size(); i2++) {
	//				HashSet<Activity> acts = new HashSet<>();
	//				acts.add(tmp.get(i1));
	//				acts.add(tmp.get(i2));
	//				nrOfEvaluations_atomic++;
	//				result.addAll(mineAtLeastAndAtMostChoice(acts));
	//			}
	//		int atomic = result.size();
	//		System.out.println(Config.LEVEL_SPACES + spaces + "Mined "
	//				+ atomic + " atomic rules ("
	//				+ nrOfEvaluations_atomic + " evaluations)");
	//		long t_end;
	//		if(Config.MAX_BRANCHING_LEVEL == 1)
	//			t_end = System.currentTimeMillis();
	//		else {
	//			System.out.println(Config.LEVEL_SPACES + spaces + "Branching atomic existence rules...");
	//			NR_OF_EVALUATIONS = 0;
	//			result = branchExistence(result);
	//			t_end = System.currentTimeMillis();
	//			long nrOfEvaluations_mix = NR_OF_EVALUATIONS;
	//			NR_OF_EVALUATIONS = 0;
	//			if(Config.DO_LOGGING)
	//				Logger.getInstance().logStat_p1(types, atomic, nrOfEvaluations_atomic,
	//						result.size()-atomic, nrOfEvaluations_mix, t_end - t_start);
	//			System.out.println(Config.LEVEL_SPACES + spaces + "Mined "
	//					+ (result.size()-atomic) + " new rules by branching ("
	//					+ nrOfEvaluations_mix + " evaluations)");
	//		}
	//		System.out.println(Config.LEVEL_SPACES + spaces + "Mined "
	//				+ result.size() + " total rules");
	//		System.out.println(Config.LEVEL_SPACES + spaces + "Completed in "
	//				+ (t_end-t_start) + "ms");
	//		for(Rule r : result)
	//			if(r.getNrOfViolations() == 0
	//			&& (r.getConstraint() instanceof AtMost
	//					|| r.getConstraint() instanceof AtMostChoice))
	//				mostSeeds.add(r);
	//		return result;
	//	}

	//Cache variable for logging nr of mix evaluations
	private static long NR_OF_EVALUATIONS = 0;

	//checks all combinations for AtLeast/AtMost(Choice) as pruning would only be useful in very specific cases
	//(this is because AtLeast/AtMost and AtLeastChoice/AtMostChoice are each mined in just 1 step for all different bounds
	private ArrayList<Rule> branchExistence(ArrayList<Rule> atomicRules) {
		ArrayList<Rule> result = new ArrayList<>(atomicRules);
		ArrayList<Rule> newRules_approved_k_1 = atomicRules;
		int branchLevel = 2;
		while(true) {
			ArrayList<Rule> newRules_approved_k = new ArrayList<>();
			for(int i1 = 0; i1 < newRules_approved_k_1.size(); i1++)
				//				if(newRules_approved_k_1.get(i1).getViolatingTraces() > 0)
				for(int i2 = i1+1; i2 < newRules_approved_k_1.size(); i2++)
					//						if(newRules_approved_k_1.get(i2).getViolatingTraces() > 0)
					if(areMergableExistence((BatchRule) newRules_approved_k_1.get(i1), (BatchRule) newRules_approved_k_1.get(i2))) {
						BatchRule r1 = (BatchRule) newRules_approved_k_1.get(i1);
						BatchRule r2 = (BatchRule) newRules_approved_k_1.get(i2);
						ExistenceConstraint c1 = (ExistenceConstraint) r1.getConstraint();
						ExistenceConstraint c2 = (ExistenceConstraint) r2.getConstraint();

						NonAtomicActivityExpression ae = mergeExistence(c1.getActivityExpression(),
								c2.getActivityExpression());
						if(ae == null)
							throw new IllegalArgumentException("Error when merging existence constraints: "
									+ r1 + " and " + r2);
						if(!isAlreadyMinedRule(newRules_approved_k, ae, c1)) {
							NR_OF_EVALUATIONS++;
							BatchRule tmp = null;
							if(c1 instanceof AtLeast
									|| c1 instanceof AtMost
									|| c1 instanceof OccurrenceConstraint)
								tmp = mineOccurrenceConstraint(ae);
							//							else if(c1 instanceof AtLeastChoice
							//									|| c1 instanceof AtMostChoice)
							//								tmp = mineAtLeastAndAtMostChoice(ae);
							else if(c1 instanceof First
									|| c1 instanceof Last
									|| c1 instanceof ExtremitiesConstraint)//TODO: do based on KB? -> not always correct (if process starts with 2 acts at the same time...)
								tmp = mineExtremitiesConstraint(ae);
							else
								throw new IllegalArgumentException("TODO??");
							if(tmp != null) {
								//checks support and for improvement (does nothing for OccurrenceConstraints!)
								tmp.purge(r1, r2, Config.MINIMAL_SUPPORT);
								if(tmp.getRules().length > 0)
									newRules_approved_k.add(tmp);
							}
						}
					}
			result.addAll(newRules_approved_k);
			branchLevel++;
			if(newRules_approved_k.isEmpty()
					|| newRules_approved_k.size() == 1
					|| (Config.MAX_BRANCHING_LEVEL != 0 && branchLevel > Config.MAX_BRANCHING_LEVEL))
				break;
			newRules_approved_k_1 = newRules_approved_k;
		}
		return result;
	}

	private boolean areMergableExistence(BatchRule br1, BatchRule br2) {
		ExistenceConstraint e1 = (ExistenceConstraint) br1.getConstraint();
		ExistenceConstraint e2 = (ExistenceConstraint) br2.getConstraint();
		if(!e1.getClass().equals(e2.getClass()))
			return false;
		if(e1 instanceof ExtremitiesConstraint) {
			boolean hasFirst1 = false;
			boolean hasLast1 = false;
			for(SingleRule sr : br1.rules()) {
				if(sr.getConstraint() instanceof First)
					hasFirst1 = true;
				if(sr.getConstraint() instanceof Last)
					hasLast1 = true;
			}
			boolean hasFirst2 = false;
			boolean hasLast2 = false;
			for(SingleRule sr : br2.rules()) {
				if(sr.getConstraint() instanceof First)
					hasFirst2 = true;
				if(sr.getConstraint() instanceof Last)
					hasLast2 = true;
			}
			if((!hasFirst1 || !hasFirst2)
					&& (!hasLast1 || !hasLast2))
				return false;
		}
		if(e1.getActivityExpression() instanceof AtomicActivityExpression
				&& e2.getActivityExpression() instanceof AtomicActivityExpression) {
			if(((AtomicActivityExpression) e1.getActivityExpression()).getActivity().equals(
					((AtomicActivityExpression) e2.getActivityExpression()).getActivity()))
				return false;
			else
				return true;
		}
		if(e1.getActivityExpression() instanceof NonAtomicActivityExpression
				&& e2.getActivityExpression() instanceof NonAtomicActivityExpression) {
			if(!((NonAtomicActivityExpression) e1.getActivityExpression()).getOperator().equals(
					((NonAtomicActivityExpression) e2.getActivityExpression()).getOperator()))
				return false;
			if(((NonAtomicActivityExpression) e1.getActivityExpression()).getExpressions().size() != 
					((NonAtomicActivityExpression) e2.getActivityExpression()).getExpressions().size())
				return false;
			int countDiffs = 0;
			for(ActivityExpression ae : ((NonAtomicActivityExpression) e1.getActivityExpression()).getExpressions()) {
				if(ae instanceof NonAtomicActivityExpression)
					throw new IllegalArgumentException("TODO?");
				if(!((NonAtomicActivityExpression) e2.getActivityExpression()).getExpressions().contains(ae))
					countDiffs++;
			}
			if(countDiffs == 1)
				return true;
			else
				return false;
		}
		return false;
	}

	private NonAtomicActivityExpression mergeExistence(ActivityExpression ae1,
			ActivityExpression ae2) {
		if(ae1 instanceof AtomicActivityExpression
				&& ae2 instanceof AtomicActivityExpression)
			return new NonAtomicActivityExpression(LogicalOperator.OR, ae1, ae2);
		if(ae1 instanceof NonAtomicActivityExpression
				&& ae2 instanceof NonAtomicActivityExpression) {
			HashSet<ActivityExpression> expressions = new HashSet<>();
			for(ActivityExpression ae : ((NonAtomicActivityExpression) ae1).getExpressions()) {
				if(!(ae instanceof AtomicActivityExpression))
					throw new IllegalArgumentException("TODO?");
				expressions.add(ae);
			}
			for(ActivityExpression ae : ((NonAtomicActivityExpression) ae2).getExpressions()) {
				if(!(ae instanceof AtomicActivityExpression))
					throw new IllegalArgumentException("TODO?");
				expressions.add(ae);
			}
			NonAtomicActivityExpression res = new NonAtomicActivityExpression(LogicalOperator.OR, expressions);
			if(res.getNrOfElements() != ae1.getNrOfElements() + 1)
				throw new IllegalArgumentException("Wrong merge (not mergable?)!");
			return res;
		}
		return null;
	}

	private static boolean isAlreadyMinedRule(ArrayList<Rule> rules,
			NonAtomicActivityExpression ae,
			ExistenceConstraint baseConstraint) {
		boolean isAtLeastAtMost_base = baseConstraint instanceof AtLeast || baseConstraint instanceof AtMost;
		boolean isAtLeastAtMostChoice_base = baseConstraint instanceof AtLeastChoice || baseConstraint instanceof AtMostChoice;
		for(Rule r : rules) {
			boolean isAtLeastAtMost_r = r.getConstraint() instanceof AtLeast || r.getConstraint() instanceof AtMost;
			boolean isAtLeastAtMostChoice_r = r.getConstraint() instanceof AtLeastChoice || r.getConstraint() instanceof AtMostChoice;
			if(((isAtLeastAtMost_base && isAtLeastAtMost_r)
					|| (isAtLeastAtMostChoice_base && isAtLeastAtMostChoice_r)
					|| r.getConstraint().getClass().equals(baseConstraint.getClass()))
					&& ((ExistenceConstraint) r.getConstraint()).getActivityExpression().equals(ae))
				return true;
		}
		return false;
	}

	private ArrayList<Rule> mineRelation_GeneralRules(String spaces) {
		String types = (Config.MINE_RESPONDEDPRESENCE?" RespondedPresence":"")
				+ (Config.MINE_NOTRESPONDEDPRESENCE?" NotRespondedPresence":"")
				+ (Config.MINE_RESPONSE?" Response":"")
				+ (Config.MINE_NOTRESPONSE?" NotResponse":"")
				+ (Config.MINE_CHAINRESPONSE?" ChainResponse":"")
				+ (Config.MINE_NOTCHAINRESPONSE?" NotChainResponse":"")
				//				+ (Config.MINE_ALTERNATERESPONSE?" AlternateResponse":"")
				+ (Config.MINE_PRECEDENCE?" Precedence":"")
				+ (Config.MINE_NOTPRECEDENCE?" NotPrecedence":"")
				+ (Config.MINE_CHAINPRECEDENCE?" ChainPrecedence":"")
				+ (Config.MINE_NOTCHAINPRECEDENCE?" NotChainPrecedence":"");
		System.out.println(spaces + "Mining" + types + "...");
		long t_start = System.currentTimeMillis();
		ArrayList<Rule> result = new ArrayList<>();
		Activity[] acts = getKB().getActivities().toArray(new Activity[getKB().getActivities().size()]);
		long nrOfEvaluations_atomic = 0;
		for(int i1 = 0; i1 < acts.length; i1++)
			for(int i2 = 0; i2 < acts.length; i2++)
				if(i1 != i2) {
					//TODO: no support for AtLeast more than one!
					//TODO: no support for AtMost at from side!
					//TODO: no support for AtMost more than zero at to side!
					boolean theyCoOccur = getKB().getWasBefore().get(acts[i1]).contains(acts[i2])
							|| getKB().getWasAfter().get(acts[i1]).contains(acts[i2]);
					AtomicActivityExpression cond = new AtomicActivityExpression(acts[i1]);
					AtomicActivityExpression conseq = new AtomicActivityExpression(acts[i2]);
					if(Config.MINE_RESPONDEDPRESENCE || Config.MINE_NOTRESPONDEDPRESENCE
							|| Config.MINE_RESPONSE || Config.MINE_NOTRESPONSE
							|| Config.MINE_CHAINRESPONSE || Config.MINE_NOTCHAINRESPONSE
							|| Config.MINE_PRECEDENCE || Config.MINE_NOTPRECEDENCE
							|| Config.MINE_CHAINPRECEDENCE || Config.MINE_NOTCHAINPRECEDENCE)
						if(theyCoOccur
								|| Config.MINE_NOTRESPONDEDPRESENCE
								|| Config.MINE_NOTRESPONSE
								|| Config.MINE_NOTCHAINRESPONSE
								|| Config.MINE_NOTPRECEDENCE
								|| Config.MINE_NOTCHAINPRECEDENCE) {//only then search...
							MineRelationProfile mrp = new MineRelationProfile(new MineExistenceProfile(true, false),
									new MineExistenceProfile(true, true));
							if(!Config.MINE_NOTRESPONDEDPRESENCE
									&& !Config.MINE_NOTRESPONSE
									&& !Config.MINE_NOTCHAINRESPONSE
									&& !Config.MINE_NOTPRECEDENCE
									&& !Config.MINE_NOTCHAINPRECEDENCE)
								mrp.getTo().setAtMost(false);
							else
								mrp.getTo().setAtMost_UpperBound(0);
							mrp.getFrom().setAtLeast_UpperBound(1);
							mrp.getTo().setAtLeast_UpperBound(1);
							//TODO: use eof to calculate real bounds
							//							HashMap<Integer, Integer> eof = getKB().getEventOccurrenceFrequencies(acts[i1].getName()).getOccurrenceFrequencies();
							//							eof.;
							Rule r = mineSequenceRelationConstraint(cond, conseq, mrp);
							if(r != null)
								result.add(r);
							nrOfEvaluations_atomic++;
						}
				}
		int atomic = getNumberOfRules(result);
		int atomic_aggr = result.size();
		System.out.println(Config.LEVEL_SPACES + spaces + "Mined "
				+ atomic + " atomic rules ("
				+ atomic_aggr + " aggregate rules)"
				+ " in " + nrOfEvaluations_atomic + " evaluations");
		long t_end;
		if(Config.MAX_BRANCHING_LEVEL == 1)
			t_end = System.currentTimeMillis();
		else {
			System.out.println(Config.LEVEL_SPACES + spaces + "Branching atomic relation rules...");
			NR_OF_EVALUATIONS = 0;
			result = branchRelations_ConditionSide(result);
			result = branchRelations_ConsequenceSide(result);
			t_end = System.currentTimeMillis();
			long nrOfEvaluations_mix = NR_OF_EVALUATIONS;
			int branched = getNumberOfRules(result) - atomic;
			int branched_aggr = result.size() - atomic_aggr;
			NR_OF_EVALUATIONS = 0;
			System.out.println(Config.LEVEL_SPACES + spaces + "Mined "
					+ branched + " new rules by branching ("
					+ branched_aggr + " aggregate rules)"
					+ " in " + nrOfEvaluations_mix + " evaluations");
			if(Config.DO_LOGGING)
				Logger.getInstance().logStat_p1(types, atomic, nrOfEvaluations_atomic, 
						result.size()-atomic, nrOfEvaluations_mix, t_end - t_start);
		}
		System.out.println(Config.LEVEL_SPACES + spaces + "Mined " + result.size() + " total rules");
		System.out.println(Config.LEVEL_SPACES + spaces + "Completed in "
				+ (t_end-t_start) + "ms");
		return result;
	}

	//TODO: add support for other relation constraints (now only SequenceRelationConstraint)
	private ArrayList<Rule> branchRelations_ConditionSide(ArrayList<Rule> atomicRules) {
		ArrayList<Rule> result = new ArrayList<>(atomicRules);
		ArrayList<Rule> newRules_approved_k_1 = atomicRules;
		ArrayList<Rule> newRules_disapproved_k_1 = new ArrayList<>();
		int branchLevel = 2;
		while(true) {
			ArrayList<Rule> newRules_approved_k = new ArrayList<>();
			ArrayList<Rule> newRules_disapproved_k = new ArrayList<>();
			HashSet<Constraint> constraintsDone = new HashSet<>();
			for(int i1 = 0; i1 < newRules_approved_k_1.size(); i1++)
				if(newRules_approved_k_1.get(i1).getViolatingTraces() > 0)
					for(int i2 = i1+1; i2 < newRules_approved_k_1.size(); i2++)
						if(newRules_approved_k_1.get(i2).getViolatingTraces() > 0)
							if(haveMergableTemplates(newRules_approved_k_1.get(i1), newRules_approved_k_1.get(i2)))
								if(haveMergableConditionSides(((SequenceRelationConstraint) newRules_approved_k_1.get(i1).getConstraint()),
										((SequenceRelationConstraint) newRules_approved_k_1.get(i2).getConstraint()))) {
									BatchRule r1 = (BatchRule) newRules_approved_k_1.get(i1);
									BatchRule r2 = (BatchRule) newRules_approved_k_1.get(i2);
									SequenceRelationConstraint c1 = (SequenceRelationConstraint) r1.getConstraint();
									SequenceRelationConstraint c2 = (SequenceRelationConstraint) r2.getConstraint();

									ActivityExpression[] conds = getMergedExpressions(c1.getConditionExpression(), c2.getConditionExpression(), true);
									ActivityExpression conseq = c1.getConsequenceExpression();

									for(ActivityExpression cond : conds) {
										boolean alreadyDone = constraintsDone.contains(c1.getShallowCopy(cond, conseq));
										boolean aprioriPropertySatisfied = checkApriori_conditionSide(cond, conseq, c1, newRules_disapproved_k_1);

										if(!alreadyDone && aprioriPropertySatisfied) {
											NR_OF_EVALUATIONS ++;
											MineRelationProfile mrp = new MineRelationProfile(new MineExistenceProfile(true, false),
													new MineExistenceProfile(true, true));
											if(!Config.MINE_NOTRESPONDEDPRESENCE
													&& !Config.MINE_NOTRESPONSE
													&& !Config.MINE_NOTCHAINRESPONSE
													&& !Config.MINE_NOTPRECEDENCE
													&& !Config.MINE_NOTCHAINPRECEDENCE)
												mrp.getTo().setAtMost(false);
											else
												mrp.getTo().setAtMost_UpperBound(0);
											mrp.getFrom().setAtLeast_UpperBound(1);
											mrp.getTo().setAtLeast_UpperBound(1);
											BatchRule tmp = mineSequenceRelationConstraint(cond, conseq, mrp);
											if(tmp == null)
												constraintsDone.add(c1.getShallowCopy(cond, conseq));
											else {
												constraintsDone.add(tmp.getConstraint());
												tmp.purge(r1, r2, Config.MINIMAL_SUPPORT);//checks support and for improvement
												if(tmp.getRules().length == 0)
													newRules_disapproved_k.add(tmp);
												else
													newRules_approved_k.add(tmp);
											}
										}
									}
								}
			result.addAll(newRules_approved_k);
			branchLevel++;
			if(newRules_approved_k.isEmpty()
					|| newRules_approved_k.size() == 1
					|| (Config.MAX_BRANCHING_LEVEL != 0 && branchLevel > Config.MAX_BRANCHING_LEVEL))
				break;
			newRules_approved_k_1 = newRules_approved_k;
			newRules_disapproved_k_1 = newRules_disapproved_k;
		}
		return result;
	}

	//check if they contain at least one similar template
	private boolean haveMergableTemplates(Rule r1, Rule r2) {
		for(SingleRule sr1 : r1.rules())
			for(SingleRule sr2 : r2.rules())
				if(sr1.getClass().equals(sr2.getClass())
						&& ((RelationConstraint) sr1.getConstraint()).hasAtMostConsequence()
						== ((RelationConstraint) sr2.getConstraint()).hasAtMostConsequence())
					return true;
		return false;
	}

	//TODO: add support for other relation constraints (now only SequenceRelationConstraint)
	private ArrayList<Rule> branchRelations_ConsequenceSide(ArrayList<Rule> atomicRules) {
		ArrayList<Rule> result = new ArrayList<>(atomicRules);
		ArrayList<Rule> newRules_approved_k_1 = atomicRules;
		ArrayList<Rule> newRules_disapproved_k_1 = new ArrayList<>();
		int branchLevel = 2;
		while(true) {
			ArrayList<Rule> newRules_approved_k = new ArrayList<>();
			ArrayList<Rule> newRules_disapproved_k = new ArrayList<>();
			HashSet<Constraint> constraintsDone = new HashSet<>();
			for(int i1 = 0; i1 < newRules_approved_k_1.size(); i1++)
				if(newRules_approved_k_1.get(i1).getViolatingTraces() > 0)
					for(int i2 = i1+1; i2 < newRules_approved_k_1.size(); i2++)
						if(newRules_approved_k_1.get(i2).getViolatingTraces() > 0)
							if(haveMergableTemplates(newRules_approved_k_1.get(i1), newRules_approved_k_1.get(i2)))
								if(haveMergableConsequenceSides(((SequenceRelationConstraint) newRules_approved_k_1.get(i1).getConstraint()),
										((SequenceRelationConstraint) newRules_approved_k_1.get(i2).getConstraint()))) {
									BatchRule r1 = (BatchRule) newRules_approved_k_1.get(i1);
									BatchRule r2 = (BatchRule) newRules_approved_k_1.get(i2);
									SequenceRelationConstraint c1 = (SequenceRelationConstraint) r1.getConstraint();
									SequenceRelationConstraint c2 = (SequenceRelationConstraint) r2.getConstraint();

									ActivityExpression cond = c1.getConditionExpression();
									ActivityExpression[] conseqs = getMergedExpressions(c1.getConsequenceExpression(), c2.getConsequenceExpression(), false);

									for(ActivityExpression conseq : conseqs) {
										boolean alreadyDone = constraintsDone.contains(c1.getShallowCopy(cond, conseq));
										boolean aprioriPropertySatisfied = checkApriori_consequenceSide(cond, conseq, c1, newRules_disapproved_k_1);

										if(!alreadyDone && aprioriPropertySatisfied) {
											NR_OF_EVALUATIONS ++;
											MineRelationProfile mrp = new MineRelationProfile(new MineExistenceProfile(true, false),
													new MineExistenceProfile(true, true));
											if(!Config.MINE_NOTRESPONDEDPRESENCE
													&& !Config.MINE_NOTRESPONSE
													&& !Config.MINE_NOTCHAINRESPONSE
													&& !Config.MINE_NOTPRECEDENCE
													&& !Config.MINE_NOTCHAINPRECEDENCE)
												mrp.getTo().setAtMost(false);
											else
												mrp.getTo().setAtMost_UpperBound(0);
											mrp.getFrom().setAtLeast_UpperBound(1);
											mrp.getTo().setAtLeast_UpperBound(1);
											BatchRule tmp = mineSequenceRelationConstraint(cond, conseq, mrp);
											if(tmp == null)
												constraintsDone.add(c1.getShallowCopy(cond, conseq));
											else {
												constraintsDone.add(tmp.getConstraint());
												tmp.purge(r1, r2, Config.MINIMAL_SUPPORT);//checks support and for improvement
												if(tmp.getRules().length == 0)
													newRules_disapproved_k.add(tmp);
												else
													newRules_approved_k.add(tmp);
											}
										}
									}
								}
			result.addAll(newRules_approved_k);
			branchLevel++;
			if(newRules_approved_k.isEmpty()
					|| newRules_approved_k.size() == 1
					|| (Config.MAX_BRANCHING_LEVEL != 0 && branchLevel > Config.MAX_BRANCHING_LEVEL))
				break;
			newRules_approved_k_1 = newRules_approved_k;
			newRules_disapproved_k_1 = newRules_disapproved_k;
		}
		return result;
	}

	private boolean haveMergableConditionSides(SequenceRelationConstraint c1, SequenceRelationConstraint c2) {
		if(!c1.getClass().equals(c2.getClass()))
			return false;
		if(!c1.getConsequenceExpression().equals(c2.getConsequenceExpression()))
			return false;
		if(!isCompatibleForMerge(c1.getConditionExpression(), c2.getConditionExpression()))
			return false;
		return true;
	}

	private boolean haveMergableConsequenceSides(SequenceRelationConstraint c1, SequenceRelationConstraint c2) {
		if(!c1.getClass().equals(c2.getClass()))
			return false;
		if(!c1.getConditionExpression().equals(c2.getConditionExpression()))
			return false;
		if(!isCompatibleForMerge(c1.getConsequenceExpression(), c2.getConsequenceExpression()))
			return false;
		return true;
	}

	private boolean isCompatibleForMerge(ActivityExpression ex1,
			ActivityExpression ex2) {
		return getMergedExpressions(ex1, ex2, true) != null;
	}

	private ActivityExpression[] getMergedExpressions(ActivityExpression ex1,
			ActivityExpression ex2, boolean onlyAND) {
		if(ex1 instanceof AtomicActivityExpression
				&& ex2 instanceof AtomicActivityExpression) {
			if(onlyAND) {
				ActivityExpression[] res = {new NonAtomicActivityExpression(LogicalOperator.AND, ex1, ex2)};
				return res;
			} else {
				ActivityExpression[] res = {new NonAtomicActivityExpression(LogicalOperator.AND, ex1, ex2),
						new NonAtomicActivityExpression(LogicalOperator.OR, ex1, ex2)};
				return res;
			}
		} else if(ex1 instanceof NonAtomicActivityExpression
				&& ex2 instanceof NonAtomicActivityExpression
				&& differsInOne((NonAtomicActivityExpression) ex1, (NonAtomicActivityExpression) ex2)) {
			HashSet<ActivityExpression> a = new HashSet<>(((NonAtomicActivityExpression) ex1).getExpressions());
			a.addAll(((NonAtomicActivityExpression) ex2).getExpressions());
			if(a.size() != ((NonAtomicActivityExpression) ex1).getExpressions().size()+1
					|| a.size() != ((NonAtomicActivityExpression) ex2).getExpressions().size()+1)
				throw new IllegalArgumentException();
			ActivityExpression[] res = {new NonAtomicActivityExpression(((NonAtomicActivityExpression) ex1).getOperator(), a)};
			return res;
		} else
			return null;
	}

	private boolean checkApriori_conditionSide(ActivityExpression cond, ActivityExpression conseq,
			SequenceRelationConstraint baseConstraint, ArrayList<Rule> newRules_disapproved_k_1) {
		for(Rule r : newRules_disapproved_k_1) {
			if(r.getConstraint().getClass().equals(baseConstraint.getClass())
					&& ((SequenceRelationConstraint) r.getConstraint()).getConsequenceExpression().equals(conseq)
					&& isSubset((NonAtomicActivityExpression) ((SequenceRelationConstraint) r.getConstraint()).getConditionExpression(),
							(NonAtomicActivityExpression) cond))
				return false;
		}
		return true;
	}

	private boolean checkApriori_consequenceSide(ActivityExpression cond, ActivityExpression conseq,
			SequenceRelationConstraint baseConstraint, ArrayList<Rule> newRules_disapproved_k_1) {
		for(Rule r : newRules_disapproved_k_1) {
			if(r.getConstraint().getClass().equals(baseConstraint.getClass())
					&& ((SequenceRelationConstraint) r.getConstraint()).getConditionExpression().equals(cond)
					&& isSubset((NonAtomicActivityExpression) ((SequenceRelationConstraint) r.getConstraint()).getConsequenceExpression(),
							(NonAtomicActivityExpression) conseq))
				return false;
		}
		return true;
	}

	private boolean differsInOne(NonAtomicActivityExpression parent1, NonAtomicActivityExpression parent2) {
		if(parent1.getOperator().equals(parent2.getOperator())
				&& parent1.getNrOfElements() == parent2.getNrOfElements()) {
			int diffs = 0;
			for(ActivityExpression a : parent1.getExpressions())
				if(!parent2.getExpressions().contains(a))
					diffs++;
			return diffs == 1;
		}
		return false;
	}

	private boolean isSubset(NonAtomicActivityExpression parent, NonAtomicActivityExpression child) {
		if(parent.getOperator().equals(child.getOperator())
				&& parent.getNrOfElements() == child.getNrOfElements() - 1) {
			int diffs = 0;
			for(ActivityExpression a : child.getExpressions())
				if(!parent.getExpressions().contains(a))
					diffs++;
			return diffs == 1;
		}
		return false;
	}

	//	private ArrayList<Rule> mineResource_GeneralRules(String spaces) {
	//		String types = (Config.MINE_RESOURCEUSAGE?" ResourceUsage":"")
	//				+ (Config.MINE_RESOURCEEXCLUSION?" ResourceExclusion":"");
	//		System.out.println(spaces + "Mining" + types + "...");
	//		long t_start = System.currentTimeMillis();
	//		System.out.println(spaces + "Mining"
	//				+ (Config.MINE_RESOURCEUSAGE?" ResourceUsage":"")
	//				+ (Config.MINE_RESOURCEEXCLUSION?" ResourceExclusion":"") + "...");
	//		ArrayList<Rule> result = new ArrayList<>();
	//		long nrOfEvaluations_atomic = 0;
	//		for(Activity act : getActivities())
	//			for(Resource res : getResources()) {
	//				HashSet<Resource> resources = new HashSet<>();
	//				resources.add(res);
	//				if(Config.MINE_RESOURCEUSAGE || Config.MINE_RESOURCEEXCLUSION) {
	//					nrOfEvaluations_atomic++;
	//					Rule r = mineUsage(act, resources);
	//					if(r != null)
	//						result.add(r);
	//				}
	//			}
	//		int atomic = result.size();
	//		System.out.println(Config.LEVEL_SPACES + spaces + "Mined "
	//				+ atomic + " atomic rules ("
	//				+ nrOfEvaluations_atomic + " evaluations)");
	//		long t_end;
	//		if(Config.MAX_BRANCHING_LEVEL == 1)
	//			t_end = System.currentTimeMillis();
	//		else {
	//			System.out.println(Config.LEVEL_SPACES + spaces + "Branching atomic resource rules...");
	//			NR_OF_EVALUATIONS = 0;
	//			result = branchResourceUsages(result);
	//			t_end = System.currentTimeMillis();
	//			long nrOfEvaluations_mix = NR_OF_EVALUATIONS;
	//			NR_OF_EVALUATIONS = 0;
	//			if(Config.DO_LOGGING)
	//				Logger.getInstance().logStat_p1(types, atomic, nrOfEvaluations_atomic,
	//						result.size()-atomic, nrOfEvaluations_mix, t_end - t_start);
	//			System.out.println(Config.LEVEL_SPACES + spaces + "Mined "
	//					+ (result.size()-atomic) + " new rules by branching ("
	//					+ nrOfEvaluations_mix + " evaluations)");
	//		}
	//		System.out.println(Config.LEVEL_SPACES + spaces + "Mined "
	//				+ result.size() + " total rules");
	//		System.out.println(Config.LEVEL_SPACES + spaces + "Completed in "
	//				+ (t_end-t_start) + "ms");
	//		return result;
	//	}
	//
	//	private ArrayList<Rule> branchResourceUsages(ArrayList<Rule> atomicRules) {
	//		ArrayList<Rule> result = new ArrayList<>(atomicRules);
	//		ArrayList<Rule> newRules_approved_k_1 = atomicRules;
	//		int branchLevel = 2;
	//		while(true) {
	//			ArrayList<Rule> newRules_approved_k = new ArrayList<>();
	//			for(int i1 = 0; i1 < newRules_approved_k_1.size(); i1++)
	//				if(newRules_approved_k_1.get(i1).getViolatingTraces() > 0)
	//					for(int i2 = i1+1; i2 < newRules_approved_k_1.size(); i2++)
	//						if(newRules_approved_k_1.get(i2).getViolatingTraces() > 0)
	//							if(areMergableResources(((ResourceUsagex) newRules_approved_k_1.get(i1).getConstraint()),
	//									((ResourceUsagex) newRules_approved_k_1.get(i2).getConstraint()))) {
	//								Rule r1 = newRules_approved_k_1.get(i1);
	//								Rule r2 = newRules_approved_k_1.get(i2);
	//								ResourceUsagex ru1 = (ResourceUsagex) r1.getConstraint();
	//								ResourceUsagex ru2 = (ResourceUsagex) r2.getConstraint();
	//								HashSet<Resource> resources = new HashSet<>();
	//								resources.addAll(ru1.getResources());
	//								resources.addAll(ru2.getResources());
	//								if(!isAlreadyMinedResourceRule(newRules_approved_k_1, resources, ru1)) {
	//									NR_OF_EVALUATIONS++;
	//									Rule r = mineUsage(ru1.getActivity(), resources);
	//									if(r != null
	//											&& r.getNrOfViolations() < r1.getNrOfViolations()
	//											&& r.getNrOfViolations() < r2.getNrOfViolations())
	//										newRules_approved_k.add(r);
	//								}
	//							}
	//			result.addAll(newRules_approved_k);
	//			branchLevel++;
	//			if(newRules_approved_k.isEmpty()
	//					|| newRules_approved_k.size() == 1
	//					|| (Config.MAX_BRANCHING_LEVEL != 0 && branchLevel > Config.MAX_BRANCHING_LEVEL))
	//				break;
	//			newRules_approved_k_1 = newRules_approved_k;
	//		}
	//		return result;
	//	}
	//
	//	private boolean areMergableResources(ResourceUsagex r1, ResourceUsagex r2) {
	//		if(!r1.getActivity().equals(r2.getActivity()))
	//			return false;
	//		if(!isCompatible_OR(r1.getResources(), r2.getResources()))
	//			return false;
	//		return true;
	//	}
	//
	//	private static boolean isAlreadyMinedResourceRule(ArrayList<Rule> rules,
	//			HashSet<Resource> resources,
	//			ResourceUsagex baseConstraint) {
	//		for(Rule r : rules)
	//			if(r.getConstraint().getClass().equals(baseConstraint.getClass())
	//					&& ((ResourceUsagex) r.getConstraint()).getActivity().equals(baseConstraint.getActivity())
	//					&& ((ResourceUsagex) r.getConstraint()).getResources().equals(resources))
	//				return true;
	//		return false;
	//	}

	private BatchRule mineExtremitiesConstraint(ActivityExpression ae) {
		return ExtremitiesConstraint.mineFirstAndLast(null, ae, getLog(), null);
	}

	private BatchRule mineOccurrenceConstraint(ActivityExpression ae) {
		return OccurrenceConstraint.mineAtLeastAndAtMost(null, ae, getLog(), null);
	}

	private BatchRule mineSequenceRelationConstraint(ActivityExpression cond,
			ActivityExpression conseq, MineRelationProfile mrp) {
		return SequenceRelationConstraint.mineSequenceRelationConstraint(null, cond, conseq,
				mrp, getLog(), null);
	}
}