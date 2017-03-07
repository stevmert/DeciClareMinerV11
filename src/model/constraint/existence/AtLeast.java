package model.constraint.existence;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.constraint.BoundedConstraint;
import model.constraint.ExistenceConstraint;
import model.constraint.TimedConstraint;
import model.data.Decision;
import model.expression.ActivityExpression;

public class AtLeast extends ExistenceConstraint implements TimedConstraint, BoundedConstraint {

	private static final long serialVersionUID = 1652635962318586705L;

	private int atLeast;
	private long timeA;
	private long timeB;

	public AtLeast(Decision activationDec, Decision deactivationDec,
			ActivityExpression activityExpression, int atLeast,
			long timeA, long timeB, boolean isOptional) {
		super(activationDec, deactivationDec, activityExpression, isOptional);
		if(atLeast < 1)
			throw new IllegalArgumentException();
		this.atLeast = atLeast;
		this.timeA = timeA;
		this.timeB = timeB;
	}

	@Override
	public int getBound() {
		return atLeast;
	}

	@Override
	public void setBound(int atLeast) {
		this.atLeast = atLeast;
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
		return this.getClass().getSimpleName() + "(" + getActivityExpression() + ", " + getBound()
		+ ", " + getTimeA()
		+ ", " + getTimeB() + ")" + super.toString();
	}

	@Override
	protected String getTextualConstraint() {
		String times = "time";
		if(getBound() > 1)
			times += "s";
		if(getActivityExpression().getNrOfElements() == 1)
			return getActivityExpression() + " has to be performed at least " + getBound() + " " + times + getTimedText(this);
		return getActivityExpression() + " have to be performed at least a combined " + getBound() + " " + times + getTimedText(this);
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
		AtLeast other = (AtLeast) obj;
		if (atLeast != other.atLeast)
			return false;
		return true;
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new AtLeast(null, null, getActivityExpression(),
				getBound(), getTimeA(), getTimeB(), isOptional());
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		return mineAtLeast(this.getActivationDecision(), this.getActivityExpression(), log);
	}

	public static Rule mineAtLeast(Decision activationDecision,
			ActivityExpression activityExpression, Log log) {
		throw new IllegalArgumentException("TODO: still used?");//TODO: still used?
		//		Rule r = OccurrenceConstraint.mineAtLeastAndAtMost(activationDecision, activityExpression, log);
		//		if(r == null)
		//			return null;
		//		List<SingleRule> res = r.rules();
		//		for(SingleRule sr : new ArrayList<>(res))
		//			if(!(sr.getConstraint() instanceof AtLeast))
		//				res.remove(sr);
		//		if(res.isEmpty())
		//			return null;
		//		else if(res.size() == 1)
		//			return res.get(0);
		//		return new BatchRule(new OccurrenceConstraint(activationDecision, activationDecision,
		//				activityExpression, false), res.toArray(new Rule[res.size()]));
	}

	@Override
	public boolean isRelatedTo(Constraint c) {
		if(!super.isRelatedTo(c))
			return false;
		return this.getBound() <= ((BoundedConstraint) c).getBound();
	}
}