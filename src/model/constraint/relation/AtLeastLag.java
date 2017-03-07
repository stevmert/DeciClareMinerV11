package model.constraint.relation;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.constraint.BoundedConstraint;
import model.constraint.RelationConstraint;
import model.data.Decision;
import model.expression.ExistenceExpression;

public class AtLeastLag extends RelationConstraint implements BoundedConstraint {

	private static final long serialVersionUID = -5405965970890966719L;

	private int atLeast;

	public AtLeastLag(Decision activationDecision, Decision deactivationDec, ExistenceExpression conditionExpression,
			ExistenceExpression consequenceExpression, int atLeast, boolean isOptional) {
		super(activationDecision, deactivationDec, conditionExpression, consequenceExpression, isOptional);
		this.atLeast = atLeast;
	}

	@Override
	public int getBound() {
		return atLeast;
	}

	@Override
	public void setBound(int atLeast) {
		this.atLeast = atLeast;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + getConditionExpression()
		+ ", " + getConsequenceExpression()
		+ ", " + getBound() + ")";
	}

	@Override
	protected String getTextualConstraint() {
		return "before " + getConsequenceExpression().toTextualString() + " can be executed after the execution of "
				+ getConditionExpression().toTextualString() + " at least " + getTime(getBound()) + " have to have elapsed";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + atLeast;
		return result;
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
		AtLeastLag other = (AtLeastLag) obj;
		if (atLeast != other.atLeast)
			return false;
		return true;
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new AtLeastLag(null, null, getConditionExpression(), getConsequenceExpression(),
				getBound(), isOptional());
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		return mineAtLeastLag(this.getActivationDecision(), this.getConditionExpression(),
				this.getConsequenceExpression(), getBound(), log);
	}

	public static Rule mineAtLeastLag(Decision activationDecision,
			ExistenceExpression consequenceExpression, ExistenceExpression conditionExpression,
			int atLeast, Log log) {
		throw new IllegalArgumentException("TODO: still used?");//TODO: still used?
		//		HashSet<Integer> tracesPositive = new HashSet<>();
		//		HashSet<Integer> tracesNegative = new HashSet<>();
		//		int countOtherViolations_Traces = 0;
		//		int countNrOfConfirmations = 0;
		//		int countNrOfViolations = 0;
		//		long timeMin = -1;
		//		long timeMax = -1;
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
		//				int indexFrom = indexOf(conditionExpression, acts, -1, -1);
		//				if(indexFrom > -1) {
		//					boolean restIsViolating = false;
		//					boolean hasConformed = false;
		//					while(true) {
		//						int indexTo = indexOf(consequenceExpression, acts
		//								.subList(indexFrom + 1, acts.size()), -1,
		//								acts.get(indexFrom).getEnd());
		//						if(restIsViolating
		//								|| indexTo == -1) {
		//							restIsViolating = true;
		//							countNrOfViolations++;
		//						} else {
		//							indexTo += indexFrom + 1;
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
		//						indexFrom = indexOf(conditionExpression, acts, indexFrom+1, -1);
		//						if(indexFrom == -1)
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
		//		if(Config.MINE_RESPONSE)
		//			pos = new Rule(new AtLeastLag(activationDecision, conditionExpression, consequenceExpression, timeMin, timeMax, true),
		//					countNrOfConfirmations, countNrOfViolations,
		//					tracesPositive, tracesNegative.size() + countOtherViolations_Traces,
		//					log.size());
		//		if(Config.MINE_NOTRESPONSE)
		//			neg = new Rule(new AtLeastLag(activationDecision, conditionExpression, consequenceExpression, 0, -1, false),
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
	public RelationConstraint getShallowCopy(ExistenceExpression conditionExpression,
			ExistenceExpression consequenceExpression) {
		return new AtLeastLag(getActivationDecision(), getDeactivationDecision(), conditionExpression,
				consequenceExpression, getBound(), isOptional());
	}
}