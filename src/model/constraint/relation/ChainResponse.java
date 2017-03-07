package model.constraint.relation;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.constraint.RelationConstraint;
import model.constraint.TimedConstraint;
import model.data.Decision;
import model.expression.ExistenceExpression;

public class ChainResponse extends RelationConstraint implements TimedConstraint {

	private static final long serialVersionUID = -4833816453287114292L;

	private long timeA;
	private long timeB;

	public ChainResponse(Decision activationDec, Decision deactivationDec,
			ExistenceExpression condition,
			ExistenceExpression consequence, long timeA, long timeB,
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
		return this.getClass().getSimpleName() + "(" + getConditionExpression()
		+ ", " + getConsequenceExpression()
		+ ", " + getTimeA()
		+ ", " + getTimeB() + ")" + super.toString();
	}

	@Override
	protected String getTextualConstraint() {
		String verb;
		if(getConditionExpression().getNrOfElements() == 1)
			verb = " has to";
		else
			verb = " have to";
		return getConditionExpression().toTextualString() + verb + " be directly followed by "
		+ getConsequenceExpression().toTextualString() + getTimedText(this);
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
		return new ChainResponse(null, null, getConditionExpression(), getConsequenceExpression(),
				getTimeA(), getTimeB(), isOptional());
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		return mineChainResponse(this.getActivationDecision(), this.getConditionExpression(),
				this.getConsequenceExpression(), log);
	}

	public static Rule mineChainResponse(Decision activationDecision,
			ExistenceExpression conditionExpression, ExistenceExpression consequenceExpression,
			Log log) {
		throw new IllegalArgumentException("TODO: still used?");//TODO: still used?
		//		HashSet<Integer> tracesPositive = new HashSet<>();
		//		HashSet<Integer> tracesNegative = new HashSet<>();
		//		int countOtherViolations_Traces = 0;
		//		int countNrOfConfirmations = 0;
		//		int countNrOfViolations = 0;
		//		//		long timeMin = -1;
		//		//		long timeMax = -1;
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
		//					boolean hasViolated = false;
		//					while(true) {
		//						int indexTo = indexOf(consequenceExpression, acts
		//								.subList(indexFrom + 1, acts.size()), -1,
		//								acts.get(indexFrom).getEnd());
		//						if(restIsViolating
		//								|| indexTo == -1) {
		//							restIsViolating = true;
		//							hasViolated = true;
		//							countNrOfViolations++;
		//						} else {
		//							long firstTimingAfter = -1;
		//							for(int j = indexFrom+1; j < acts.size(); j++) {
		//								if(acts.get(j).getStart()
		//										>= acts.get(indexFrom).getEnd()) {
		//									firstTimingAfter = acts.get(j).getStart();
		//									break;
		//								}
		//							}
		//							if(firstTimingAfter == -1) {
		//								restIsViolating = true;
		//								hasViolated = true;
		//								countNrOfViolations++;
		//							} else {
		//								indexTo += indexFrom + 1;
		//								if(acts.get(indexTo).getStart() == firstTimingAfter) {
		//									long countDiff = acts.get(indexTo).getStart()
		//											- acts.get(indexFrom).getEnd();
		//									if(countDiff < 0) {
		//										throw new IllegalArgumentException("countDiff < 0???");
		//									} else {
		//										countNrOfConfirmations++;
		//										hasConformed = true;
		//										//										if(timeMin == -1 || timeMin > countDiff)
		//										//											timeMin = countDiff;
		//										//										if(timeMax == -1 || timeMax < countDiff)
		//										//											timeMax = countDiff;
		//									}
		//								} else {
		//									hasViolated = true;
		//									countNrOfViolations++;
		//								}
		//							}
		//						}
		//						indexFrom = indexOf(conditionExpression, acts, indexFrom+1, -1);
		//						if(indexFrom == -1)
		//							break;
		//					}
		//					if(!hasViolated && hasConformed)
		//						tracesPositive.add(i);
		//					else if(hasViolated && !hasConformed)
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
		//		if(Config.MINE_CHAINRESPONSE)
		//			pos = new Rule(new ChainResponse(activationDecision, null, conditionExpression, consequenceExpression, 0, -1, true),
		//					countNrOfConfirmations, countNrOfViolations,
		//					tracesPositive, tracesNegative.size() + countOtherViolations_Traces,
		//					log.size());
		//		if(Config.MINE_NOTCHAINRESPONSE)
		//			neg = new Rule(new ChainResponse(activationDecision, null, conditionExpression,
		//					consequenceExpression.getNegation(), 0, -1, false),
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
	public RelationConstraint getShallowCopy(ExistenceExpression condition,
			ExistenceExpression consequence) {
		return new ChainResponse(getActivationDecision(), getDeactivationDecision(), condition, consequence, getTimeA(),
				getTimeB(), isOptional());
	}
}