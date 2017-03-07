package model.constraint.relation;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.constraint.RelationConstraint;
import model.constraint.TimedConstraint;
import model.data.Decision;
import model.expression.ExistenceExpression;

public class Response extends RelationConstraint implements TimedConstraint {

	private static final long serialVersionUID = 1764224344303636237L;

	private long timeA;
	private long timeB;

	public Response(Decision activationDec, Decision deactivationDec, ExistenceExpression condition,
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
		return getConditionExpression().toTextualString() + verb + " be followed by "
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
		return new Response(null, null, getConditionExpression(), getConsequenceExpression(),
				getTimeA(), getTimeB(), isOptional());
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		return mineResponse(this.getActivationDecision(), this.getConditionExpression(),
				this.getConsequenceExpression(), log);
	}

	public static Rule mineResponse(Decision activationDecision,
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
		//				IndexResult indexFrom = indexOf(conditionExpression, acts, -1, -1);
		//				if(indexFrom != null) {
		//					boolean restIsViolating = false;
		//					boolean hasConformed = false;
		//					while(true) {
		//						IndexResult indexTo = indexOf(consequenceExpression, acts
		//								.subList(indexFrom.getIndex_end() + 1, acts.size()), -1,
		//								acts.get(indexFrom.getIndex_end()).getEnd());
		//						if(restIsViolating
		//								|| indexTo == null) {
		//							restIsViolating = true;
		//							countNrOfViolations++;
		//						} else {
		//							//							indexTo += indexFrom + 1;
		//							//							long countDiff = acts.get(indexTo).getStart()
		//							//									- acts.get(indexFrom).getEnd();
		//							//							if(countDiff < 0) {
		//							//								throw new IllegalArgumentException("countDiff < 0???");
		//							//							} else {
		//							countNrOfConfirmations++;
		//							hasConformed = true;
		//							//								if(timeMin == -1 || timeMin > countDiff)
		//							//									timeMin = countDiff;
		//							//								if(timeMax == -1 || timeMax < countDiff)
		//							//									timeMax = countDiff;
		//							//							}
		//						}
		//						indexFrom = indexOf(conditionExpression, acts, indexFrom.getIndex_start()+1, -1);
		//						if(indexFrom == null)
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
		//			pos = new Rule(new Response(activationDecision, null, conditionExpression, consequenceExpression, 0, -1, false),
		//					countNrOfConfirmations, countNrOfViolations,
		//					tracesPositive, tracesNegative.size() + countOtherViolations_Traces,
		//					log.size());
		//		//TODOx: negative?
		//		if(Config.MINE_NOTRESPONSE)
		//			neg = new Rule(new Response(activationDecision, null, conditionExpression,
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

	//	public static Rule[] mineResponse(Decision activationDecision,
	//			ActivityExpression conditionExpression, ActivityExpression consequenceExpression,
	//			MineRelationProfile mrp, Log log) {
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
	//				IndexResult indexFrom = indexOf(conditionExpression, acts, -1, -1);
	//				if(indexFrom != null) {
	//					boolean restIsViolating = false;
	//					boolean hasConformed = false;
	//					while(true) {
	//						IndexResult indexTo = indexOf(consequenceExpression, acts
	//								.subList(indexFrom.getIndex_end() + 1, acts.size()), -1,
	//								acts.get(indexFrom.getIndex_end()).getEnd());
	//						if(restIsViolating
	//								|| indexTo == null) {
	//							restIsViolating = true;
	//							countNrOfViolations++;
	//						} else {
	//							//							indexTo += indexFrom + 1;
	//							//							long countDiff = acts.get(indexTo).getStart()
	//							//									- acts.get(indexFrom).getEnd();
	//							//							if(countDiff < 0) {
	//							//								throw new IllegalArgumentException("countDiff < 0???");
	//							//							} else {
	//							countNrOfConfirmations++;
	//							hasConformed = true;
	//							//								if(timeMin == -1 || timeMin > countDiff)
	//							//									timeMin = countDiff;
	//							//								if(timeMax == -1 || timeMax < countDiff)
	//							//									timeMax = countDiff;
	//							//							}
	//						}
	//						indexFrom = indexOf(conditionExpression, acts, indexFrom.getIndex_start()+1, -1);
	//						if(indexFrom == null)
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
	//			pos = new Rule(new Response(activationDecision, null, conditionExpression, consequenceExpression, 0, -1, false),
	//					countNrOfConfirmations, countNrOfViolations,
	//					tracesPositive, tracesNegative.size() + countOtherViolations_Traces,
	//					log.size());
	//		//TODOx: negative?
	//		if(Config.MINE_NOTRESPONSE)
	//			neg = new Rule(new Response(activationDecision, null, conditionExpression,
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
	//	}

	@Override
	public RelationConstraint getShallowCopy(ExistenceExpression conditionExpression,
			ExistenceExpression consequenceExpression) {
		return new Response(getActivationDecision(), getDeactivationDecision(), conditionExpression,
				consequenceExpression, getTimeA(),
				getTimeB(), isOptional());
	}
}