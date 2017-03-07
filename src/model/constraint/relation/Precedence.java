package model.constraint.relation;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.constraint.RelationConstraint;
import model.constraint.TimedConstraint;
import model.data.Decision;
import model.expression.ExistenceExpression;

public class Precedence extends RelationConstraint implements TimedConstraint {

	private static final long serialVersionUID = 6201070246964641436L;

	private long timeA;
	private long timeB;

	public Precedence(Decision activationDec, Decision deactivationDec,
			ExistenceExpression consequence, ExistenceExpression condition, long timeA, long timeB,
			boolean isOptional) {
		super(activationDec, deactivationDec, condition, consequence, isOptional);
		this.timeA = timeA;
		this.timeB = timeB;
	}

	public long getTimeA() {
		return timeA;
	}

	public void setTimeA(long timeA) {
		this.timeA = timeA;
	}

	public long getTimeB() {
		return timeB;
	}

	public void setTimeB(long timeB) {
		this.timeB = timeB;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + getConsequenceExpression()
		+ ", " + getConditionExpression()
		+ ", " + getTimeA()
		+ ", " + getTimeB() + ")" + super.toString();
	}

	@Override
	protected String getTextualConstraint() {
		String verb;
		if(getConsequenceExpression().getNrOfElements() == 1)
			verb = " has to";
		else
			verb = " have to";
		return getConsequenceExpression().toTextualString() + verb + " precede "
		+ getConditionExpression().toTextualString() + getTimedText(this);
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj, true);
	}

	public boolean equals(Object obj, boolean checkDecisions) {
		if (this == obj)
			return true;
		if (!super.equals(obj, checkDecisions))
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new Precedence(null, null, getConsequenceExpression(), getConditionExpression(),
				getTimeA(), getTimeB(), isOptional());
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		return minePrecedence(this.getActivationDecision(), this.getConditionExpression(),
				this.getConsequenceExpression(), log);
	}

	public static Rule minePrecedence(Decision activationDecision,
			ExistenceExpression consequenceExpression, ExistenceExpression conditionExpression,
			Log log) {
		throw new IllegalArgumentException("TODO: still used?");//TODO: still used?
		//		int countOtherViolations_Traces = 0;
		//		int countNrOfConfirmations = 0;
		//		int countNrOfViolations = 0;
		//		long timeMin = -1;
		//		long timeMax = -1;
		//		HashSet<Integer> tracesPositive = new HashSet<>();
		//		HashSet<Integer> tracesNegative = new HashSet<>();
		//		HashSet<DecisionRule> usedDecisions = new HashSet<>();
		//		for(int i = 0; i < log.size(); i++) {
		//			Trace t = log.get(i);
		//			long activationTime = 0;
		//			if(activationDecision != null) {
		//				DecisionActivation decisionActivation = t.getDecisionActivation(activationDecision);
		//				if(decisionActivation != null) {
		//					activationTime = decisionActivation.getTime();
		//					usedDecisions.add(decisionActivation.getDecisionRule());
		//				} else
		//					activationTime = -1;
		//			}
		//			if(activationTime != -1) {
		//				List<ActivityEvent> acts = t.getRemainingActivityList(activationTime);
		//				//TODOx: correct?
		//				int indexTo = lastIndexOf(consequenceExpression, acts, -1, -1);
		//				if(indexTo > -1) {
		//					boolean restIsViolating = false;
		//					boolean hasConformed = false;
		//					while(true) {
		//						int indexFrom = lastIndexOf(conditionExpression, acts
		//								.subList(0, indexTo), -1,
		//								acts.get(indexTo).getStart());
		//						if(restIsViolating
		//								|| indexFrom == -1) {
		//							restIsViolating = true;
		//							countNrOfViolations++;
		//						} else {
		//							long countDiff = acts.get(indexTo).getStart()
		//									- acts.get(indexFrom).getEnd();
		//							if(countDiff < 0) {
		//								throw new IllegalArgumentException("countDiff < 0???");
		//							} else {
		//								countNrOfConfirmations++;
		//								hasConformed = true;
		//								if(timeMin == -1 || timeMin > countDiff)
		//									timeMin = countDiff;
		//								if(timeMax == -1 || timeMax < countDiff)
		//									timeMax = countDiff;
		//							}
		//						}
		//						indexTo = lastIndexOf(consequenceExpression, acts, indexTo, -1);
		//						if(indexTo == -1)
		//							break;
		//					}
		//					if(!restIsViolating && hasConformed)
		//						tracesPositive.add(i);
		//					else if(restIsViolating && !hasConformed)
		//						tracesNegative.add(i);
		//					else
		//						countOtherViolations_Traces++;
		//				}
		//			}
		//		}
		//		//remove redundant decision rules, as these are not used anyway...
		//		if(countNrOfConfirmations+countNrOfViolations > 0
		//				&& activationDecision != null
		//				&& usedDecisions.size() != activationDecision.getRules().size())
		//			activationDecision.setRules(usedDecisions);
		//		Rule pos = null;
		//		Rule neg = null;
		//		if(Config.MINE_PRECEDENCE)
		//			pos = new Rule(new Precedence(activationDecision, conditionExpression, consequenceExpression, timeMin, timeMax, true),
		//					countNrOfConfirmations, countNrOfViolations,
		//					tracesPositive, tracesNegative.size() + countOtherViolations_Traces,
		//					log.size());
		//		if(Config.MINE_NOTPRECEDENCE)
		//			neg = new Rule(new Precedence(activationDecision, conditionExpression, consequenceExpression, 0, -1, false),
		//					countNrOfViolations, countNrOfConfirmations,
		//					tracesNegative, tracesPositive.size() + countOtherViolations_Traces,
		//					log.size());
		//		if(pos != null && neg != null) {
		//			Rule[] result = {pos, neg};
		//			return result;
		//		} else if(pos != null && neg == null) {
		//			Rule[] result = {pos};
		//			return result;
		//		} else if(pos == null && neg != null) {
		//			Rule[] result = {neg};
		//			return result;
		//		} else
		//			return null;
	}

	@Override
	public RelationConstraint getShallowCopy(ExistenceExpression consequenceExpression,
			ExistenceExpression conditionExpression) {
		return new Precedence(getActivationDecision(), getDeactivationDecision(),
				consequenceExpression, conditionExpression, getTimeA(),
				getTimeB(), isOptional());
	}
}