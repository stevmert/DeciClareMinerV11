package model.constraint.existence.extra;

import java.util.ArrayList;
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
import util.IndexResult;

public class ExtremitiesConstraint extends ExistenceConstraint {

	private static final long serialVersionUID = -5897720289291982018L;

	public ExtremitiesConstraint(Decision activationDec, Decision deactivationDec,
			ActivityExpression activityExpression, boolean isOptional) {
		super(activationDec, deactivationDec, activityExpression, isOptional);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + getActivityExpression() + ")" + super.toString();
	}

	@Override
	protected String getTextualConstraint() {
		return "extremities constraint on " + getActivityExpression();
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new ExtremitiesConstraint(null, null, getActivityExpression(), isOptional());
	}

	@Override
	public BatchRule evaluate(Log log, Rule ancestor) {
		return mineFirstAndLast(this.getActivationDecision(), this.getActivityExpression(), log, ancestor);
	}

	public static BatchRule mineFirstAndLast(Decision activationDecision,
			ActivityExpression activityExpression, Log log, Rule ancestor) {
		boolean ancestor_doFirst = (ancestor == null);
		boolean ancestor_doLast = (ancestor == null);
		if(ancestor != null)
			for(SingleRule sr : ancestor.rules()) {
				if(sr.getConstraint() instanceof First)
					ancestor_doFirst = true;
				else if(sr.getConstraint() instanceof Last)
					ancestor_doLast = true;
			}

		int count_violations_first = 0;
		int count_confirmations_first = 0;
		int count_violations_last = 0;
		int count_confirmations_last = 0;
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
			//First
			if(Config.MINE_FIRST && ancestor_doFirst && activationTime == 0) {//needs to be the very first
				IndexResult indexFirst = indexOf(activityExpression, t.getActivityEvents(), -1, -1);
				boolean isBeforeAll = indexFirst != null;
				if(isBeforeAll)
					for(ActivityEvent a : t.getActivityEvents()) {
						if(a == t.getActivityEvents().get(indexFirst.getIndex_start()))
							;
						else {
							isBeforeAll = a.getStart() > t.getActivityEvents().get(indexFirst.getIndex_start()).getStart();
							break;
						}
					}
				if(isBeforeAll)
					count_confirmations_first++;
				else
					count_violations_first++;
			}
			//Last
			if(Config.MINE_LAST && ancestor_doLast && activationTime != -1) {
				List<ActivityEvent> actsRem = t.getRemainingActivityList(activationTime);
				IndexResult indexLast = lastIndexOf(activityExpression, actsRem, -1, -1);
				boolean isAfterAll = indexLast != null;
				if(isAfterAll)
					for(int ai = actsRem.size()-1; ai >= 0; ai--) {
						ActivityEvent a = actsRem.get(ai);
						if(ai == indexLast.getIndex_end())
							;
						else {
							isAfterAll = a.getStart() < t.getActivityEvents().get(indexLast.getIndex_end()).getStart();
							break;
						}
					}
				if(isAfterAll)
					count_confirmations_last++;
				else
					count_violations_last++;
			}
		}
		//remove redundant decision rules, as these are not used anyway...
		if(count_confirmations_first+count_confirmations_last > 0
				&& activationDecision != null
				&& usedDecisions.size() != activationDecision.getRules().size())
			activationDecision.setRules(usedDecisions);
		ArrayList<Rule> result = new ArrayList<>();
		if(Config.MINE_FIRST && ancestor_doFirst
				&& count_confirmations_first > 0)
			result.add(new SingleRule(new First(activationDecision, null, activityExpression, false),
					count_confirmations_first, count_violations_first,
					count_confirmations_first, count_violations_first, log.size()));
		if(Config.MINE_LAST && ancestor_doLast
				&& count_confirmations_last > 0)
			result.add(new SingleRule(new Last(activationDecision, null, activityExpression, false),
					count_confirmations_last, count_violations_last,
					count_confirmations_last, count_violations_last, log.size()));
		if(result.isEmpty())
			return null;
		else
			return new BatchRule(new ExtremitiesConstraint(activationDecision, null,
					activityExpression, false), result.toArray(new Rule[result.size()]), ancestor);
	}
}