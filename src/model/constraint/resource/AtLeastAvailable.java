package model.constraint.resource;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.data.Decision;
import model.expression.ResourceExpression;

public class AtLeastAvailable extends ResourceAvailability {

	private static final long serialVersionUID = 701752749908274232L;

	public AtLeastAvailable(Decision activationDecision, Decision deactivationDec, ResourceExpression resourceExpression,
			int atLeast, boolean isOptional) {
		super(activationDecision, deactivationDec, resourceExpression, atLeast, isOptional);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + getResourceExpression() + ", " + getBound() + ")" + super.toString();
	}

	@Override
	protected String getTextualConstraint() {
		if(getBound() == 1)
			return "there is at least " + getBound() + " " + getResourceExpression() + " available";
		return "there are at least " + getBound() + " " + getResourceExpression() + "s available";
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new AtLeastAvailable(null, null, getResourceExpression(), getBound(), isOptional());
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		// TODO Auto-generated method stub
		return null;
	}
}