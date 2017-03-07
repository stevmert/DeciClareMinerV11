package model.constraint.relation;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.constraint.RelationConstraint;
import model.constraint.TimedConstraint;
import model.data.Decision;
import model.expression.ExistenceExpression;

//TODO: not correct!!!
public class AlternateResponse extends RelationConstraint implements TimedConstraint {

	private static final long serialVersionUID = -1910114881037599154L;

	private long timeA;
	private long timeB;

	public AlternateResponse(Decision activationDec, Decision deactivationDec,
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
		String name = "AlternateResponse";
		return name + "(" + getConditionExpression()
		+ ", " + getConsequenceExpression()
		+ ", " + getTimeA()
		+ ", " + getTimeB() + ")" + super.toString();
	}

	@Override
	protected String getTextualConstraint() {
		throw new IllegalArgumentException("TODO");
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new AlternateResponse(null, null, getConditionExpression(),
				getConsequenceExpression(), getTimeA(), getTimeB(), isOptional());
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		return mineAlternateResponse(this.getActivationDecision(), this.getConditionExpression(),
				this.getConsequenceExpression(), log);
	}

	/**
	 * Untimed and only works for sets containing no 'AND' sets
	 */
	public static Rule mineAlternateResponse(Decision activationDecision,
			ExistenceExpression consequenceExpression, ExistenceExpression conditionExpression,
			Log log) {
		throw new IllegalArgumentException("TODO: still used?");//TODO: still used?
		//		//TODOx: This method does not always work correctly for input with 'AND', so these rules are not accepted...
		//		//[[A, B, X]] - > [[C, D]]
		//		//ABX CD ABX	OK
		//		//ABX AB CD X	OK
		//		//AABBX CD X	OK
		//		//AABX CD X		WRONG!
		//		for(HashSet<Activity> tmp : actsFrom)
		//			if(tmp.size() > 1)
		//				return null;
		//		for(HashSet<Activity> tmp : actsTo)
		//			if(tmp.size() > 1)
		//				return null;
		//
		//		int countViolations_Traces = 0;
		//		int countNrOfConfirmations = 0;
		//		int countNrOfViolations = 0;
		//		HashSet<DecisionRule> usedDecisions = new HashSet<>();
		//		HashSet<Integer> traces = new HashSet<>();
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
		//				int index = indexOf(actsFrom, acts, -1, -1);
		//				if(index > -1) {
		//					Boolean positive = null;
		//					while(true) {
		//						int indexFrom = indexOf(actsFrom, acts, index, -1);
		//						if(indexFrom == -1)
		//							break;
		//						else if(acts.get(index).getEnd()
		//								> acts.get(indexFrom).getStart()) {
		//							positive = false;
		//							break;
		//						}
		//						positive = true && (positive == null || positive.booleanValue());
		//						int indexTo = indexOf(actsTo,
		//								acts.subList(index + 1, indexFrom), -1,
		//								acts.get(index).getEnd());
		//						if(indexTo == -1) {
		//							countNrOfViolations++;
		//							positive = false;
		//						} else {
		//							indexTo += index + 1;
		//							if(acts.get(indexTo).getEnd()
		//									> acts.get(indexFrom).getStart()) {
		//								countNrOfViolations++;
		//								positive = false;
		//							} else
		//								countNrOfConfirmations++;
		//						}
		//						index = indexFrom;
		//					}
		//					if(positive != null) {
		//						if(positive)
		//							traces.add(i);
		//						else
		//							countViolations_Traces++;
		//					}
		//				}
		//			}
		//		}
		//		//remove redundant decision rules, as these are not used anyway...
		//		if(countNrOfConfirmations > 0
		//				&& activationDecision != null
		//				&& usedDecisions.size() != activationDecision.getRules().size())
		//			activationDecision.setRules(usedDecisions);
		//		Rule[] result = {new Rule(new AlternateResponse(activationDecision, actsFrom, actsTo, 0, -1),
		//				countNrOfConfirmations, countNrOfViolations,
		//				traces, countViolations_Traces, log.size())};
		//		return result;
	}

	@Override
	public RelationConstraint getShallowCopy(ExistenceExpression conditionExpression,
			ExistenceExpression consequenceExpression) {
		return new AlternateResponse(getActivationDecision(), getDeactivationDecision(),
				conditionExpression, consequenceExpression, getTimeA(), getTimeB(), isOptional());
	}
}