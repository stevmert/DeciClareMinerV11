package model.constraint.relation;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.constraint.RelationConstraint;
import model.constraint.TimedConstraint;
import model.data.Decision;
import model.expression.ExistenceExpression;

//TODO?
public class AlternatePrecedence extends RelationConstraint implements TimedConstraint {

	private static final long serialVersionUID = 7172938217987354259L;

	private long timeA;
	private long timeB;

	public AlternatePrecedence(Decision activationDec, Decision deactivationDec,
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
		String name = "AlternatePrecedence";
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
		return new AlternatePrecedence(null, null, getConditionExpression(),
				getConsequenceExpression(), getTimeA(), getTimeB(), isOptional());
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		return mineAlternatePrecedence(this.getActivationDecision(), this.getConditionExpression(),
				this.getConsequenceExpression(), log);
	}

	private Rule mineAlternatePrecedence(Decision activationDecision,
			ExistenceExpression consequenceExpression, ExistenceExpression conditionExpression,
			Log log) {
		throw new IllegalArgumentException("TODO: still used?");//TODO: still used?
	}



	//	/**
	//	 * Untimed and only works for sets containing no 'AND' sets
	//	 */
	//	private ArrayList<Rule> mineAlternatePrecedence(
	//			HashSet<HashSet<Activity>> actsFrom, HashSet<HashSet<Activity>> actsTo) {
	//		ArrayList<Rule> result = new ArrayList<>();
	//		if(actsFrom.isEmpty() || actsTo.isEmpty())
	//			return result;
	//		//TODOX: This method does not always work correctly for input with 'AND', so these rules are not accepted...
	//		for(HashSet<Activity> tmp : actsFrom)
	//			if(tmp.size() > 1)
	//				return result;
	//		for(HashSet<Activity> tmp : actsTo)
	//			if(tmp.size() > 1)
	//				return result;
	//
	//		int countConfirmations_Traces = 0;
	//		int countViolations_Traces = 0;
	//		int countNrOfConfirmations = 0;
	//		int countNrOfViolations = 0;
	//		for(Trace trace : getLog()) {
	//			int index = lastIndexOf(actsTo, trace.getTraceActivities(), -1, -1);
	//			if(index > -1) {
	//				Boolean positive = null;
	//				while(true) {
	//					int indexTo = lastIndexOf(actsTo, trace.getTraceActivities().subList(0, index), -1, -1);
	//					if(indexTo == -1)
	//						break;
	//					else if(trace.getTraceActivities().get(index).getEnd()
	//							> trace.getTraceActivities().get(indexTo).getStart()) {
	//						positive = false;
	//						break;
	//					}
	//					positive = true && (positive == null || positive.booleanValue());
	//					int indexFrom = indexOf(actsFrom,
	//							trace.getTraceActivities().subList(indexTo + 1, index), -1);
	//					if(indexFrom == -1) {
	//						countNrOfViolations++;
	//						positive = false;
	//					} else
	//						countNrOfConfirmations++;
	//					index = indexTo;
	//				}
	//				if(positive != null) {
	//					if(positive)
	//						countConfirmations_Traces++;
	//					else
	//						countViolations_Traces++;
	//				}
	//			}
	//		}
	//		if(countNrOfConfirmations > 0)
	//			result.add(new Rule(new AlternatePrecedence(null, actsFrom, actsTo, 0, -1),
	//					countNrOfConfirmations, countNrOfViolations,
	//					countConfirmations_Traces, countViolations_Traces, getLog().size()));
	//		return result;
	//	}

	@Override
	public RelationConstraint getShallowCopy(ExistenceExpression conditionExpression,
			ExistenceExpression consequenceExpression) {
		return new AlternatePrecedence(getActivationDecision(), getDeactivationDecision(),
				conditionExpression, consequenceExpression, getTimeA(), getTimeB(), isOptional());
	}
}