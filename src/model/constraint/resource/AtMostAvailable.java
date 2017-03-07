package model.constraint.resource;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.data.Decision;
import model.expression.ResourceExpression;

public class AtMostAvailable extends ResourceAvailability {

	private static final long serialVersionUID = 7367613864327365904L;

	public AtMostAvailable(Decision activationDecision, Decision deactivationDec,
			ResourceExpression resourceExpression, int atMost, boolean isOptional) {
		super(activationDecision, deactivationDec, resourceExpression, atMost, isOptional);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + getResourceExpression()
		+ ", " + getBound() + ")" + super.toString();
	}

	@Override
	protected String getTextualConstraint() {
		if(getBound() == 1)
			return "there is at most " + getBound() + " " + getResourceExpression() + " available";
		return "there are at most " + getBound() + " " + getResourceExpression() + "s available";
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new AtMostAvailable(null, null, getResourceExpression(), getBound(), isOptional());
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		// TODO Auto-generated method stub
		return null;
	}
}