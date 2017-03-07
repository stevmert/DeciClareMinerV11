package model.constraint.existence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import miner.Config;
import miner.log.ActivityEvent;
import miner.log.DecisionActivation;
import miner.log.Log;
import miner.log.Trace;
import miner.rule.BatchRule;
import miner.rule.Rule;
import miner.rule.SingleRule;
import model.Constraint;
import model.constraint.ExistenceConstraint;
import model.data.Decision;
import model.data.DecisionRule;
import model.expression.ActivityExpression;

public class OccurrenceConstraint extends ExistenceConstraint {

	private static final long serialVersionUID = 4911724465917373269L;

	public OccurrenceConstraint(Decision activationDec, Decision deactivationDec,
			ActivityExpression activityExpression, boolean isOptional) {
		super(activationDec, deactivationDec, activityExpression, isOptional);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + getActivityExpression()
		+ ")" + super.toString();
	}

	@Override
	protected String getTextualConstraint() {
		return "occurrence constraint on " + getActivityExpression();
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new OccurrenceConstraint(null, null, getActivityExpression(), isOptional());
	}

	@Override
	public BatchRule evaluate(Log log, Rule ancestor) {
		return mineAtLeastAndAtMost(this.getActivationDecision(), this.getActivityExpression(), log, ancestor);
	}

	public static BatchRule mineAtLeastAndAtMost(Decision activationDecision,
			ActivityExpression activityExpression, Log log, Rule ancestor) {
		ArrayList<SingleRule> result = new ArrayList<>();
		if(activityExpression.getNrOfElements() < 1)
			return null;
		HashMap<Integer, Integer> counts = new HashMap<>();
		//		long minTime = -1;
		//		long maxTime = -1;
		HashSet<DecisionRule> usedDecisions = new HashSet<>();
		for(int i = 0; i < log.size(); i++) {
			Trace t = log.get(i);
			long activationTime = 0;
			if(activationDecision != null) {
				DecisionActivation decisionActivation = t.getDecisionActivation(activationDecision);
				if(decisionActivation != null) {
					activationTime = decisionActivation.getTime();
					usedDecisions.add(decisionActivation.getDecisionRule());
				} else
					activationTime = -1;
			}
			if(activationTime != -1) {
				List<ActivityEvent> actsRem = t.getRemainingActivityList(activationTime);
				int nrInTrace = count(activityExpression, actsRem);
				//				if(nrInTrace > 0) {//TODO: time
				//					HashSet<HashSet<Activity>> setOfSets = new HashSet<>();
				//					for(Activity a : activityExpression) {
				//						HashSet<Activity> s = new HashSet<>();
				//						s.add(a);
				//						setOfSets.add(s);
				//					}
				//					int index = indexOf(setOfSets, actsRem, -1, -1);
				//					if(minTime == -1 || actsRem.get(index).getStart() < minTime)
				//						minTime = actsRem.get(index).getStart();
				//					int lastIndex = lastIndexOf(setOfSets, actsRem, -1, -1);
				//					if(maxTime == -1 || actsRem.get(lastIndex).getStart() > maxTime)
				//						maxTime = actsRem.get(lastIndex).getStart();
				//				}
				if(counts.get(nrInTrace) == null)
					counts.put(nrInTrace, 0);
				counts.put(nrInTrace, counts.get(nrInTrace)+1);
			}
		}
		//remove redundant decision rules, as these are not used anyway...
		if(!counts.isEmpty()
				&& activationDecision != null
				&& usedDecisions.size() != activationDecision.getRules().size())
			activationDecision.setRules(usedDecisions);
		if(counts.isEmpty())
			return null;
		for(int i : counts.keySet()) {
			int totalSupport_AtLeast_Traces = 0;
			int totalSupport_AtMost_Traces = 0;
			int totalSupport_AtLeast_nrOfViolations = 0;
			int totalSupport_AtMost_nrOfViolations = 0;
			int totalSupport_AtLeast_nrOfViolations_Traces = 0;
			int totalSupport_AtMost_nrOfViolations_Traces = 0;
			for(int k : counts.keySet()) {
				if(k >= i)
					totalSupport_AtLeast_Traces += counts.get(k);
				if(k <= i)
					totalSupport_AtMost_Traces += counts.get(k);
				if(k < i) {
					totalSupport_AtLeast_nrOfViolations += (i - k) * counts.get(k);
					totalSupport_AtLeast_nrOfViolations_Traces += counts.get(k);
				}
				if(k > i) {
					totalSupport_AtMost_nrOfViolations += (k - i) * counts.get(k);
					totalSupport_AtMost_nrOfViolations_Traces += counts.get(k);
				}
			}
			if(Config.MINE_ATLEAST
					&& i > 0
					&& totalSupport_AtLeast_Traces > 0)
				result.add(new SingleRule(new AtLeast(activationDecision, null, activityExpression, i, 0, -1, false),
						totalSupport_AtLeast_Traces, totalSupport_AtLeast_nrOfViolations,
						totalSupport_AtLeast_Traces, totalSupport_AtLeast_nrOfViolations_Traces, log.size()));
			if(Config.MINE_ATMOST
					&& totalSupport_AtMost_Traces > 0)
				result.add(new SingleRule(new AtMost(activationDecision, null, activityExpression, i, 0, -1, false),
						totalSupport_AtMost_Traces, totalSupport_AtMost_nrOfViolations,
						totalSupport_AtMost_Traces, totalSupport_AtMost_nrOfViolations_Traces, log.size()));
		}
		if(ancestor != null && !result.isEmpty())
			purge(result, ancestor);
		if(result.isEmpty())
			return null;
		else
			return new BatchRule(new OccurrenceConstraint(activationDecision, null,
					activityExpression, false), result.toArray(new Rule[result.size()]), ancestor);
	}

	/**
	 * Removes rules that were already 100% for ancestor (=decisionless constraint)
	 * This does filter out some added information (but of minimal use)
	 */
	private static void purge(ArrayList<SingleRule> result, Rule ancestor) {
		for(SingleRule sr : ancestor.rules())
			if(sr.getNrOfViolations() == 0)
				for(int i = 0; i < result.size(); i++)
					if(sr.getConstraint().getClass().equals(result.get(i).getConstraint().getClass())
							&& ((sr.getConstraint() instanceof AtMost && ((AtMost) sr.getConstraint()).getBound() <= ((AtMost) result.get(i).getConstraint()).getBound())
									|| (sr.getConstraint() instanceof AtLeast && ((AtLeast) sr.getConstraint()).getBound() >= ((AtLeast) result.get(i).getConstraint()).getBound()))) {
						result.remove(i);
						i--;
					}
	}
}