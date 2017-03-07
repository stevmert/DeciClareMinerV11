package model.constraint.resource;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.data.Decision;
import model.expression.ActivityExpression;
import model.expression.ResourceExpression;

public class AtLeastUsage extends ResourceUsage {

	private static final long serialVersionUID = -8885458154311157987L;

	public AtLeastUsage(Decision activationDecision, Decision deactivationDec, ActivityExpression activityExpression,
			ResourceExpression resourceExpression, int bound, boolean isOptional) {
		super(activationDecision, deactivationDec, activityExpression, resourceExpression, bound, isOptional);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + getActivityExpression() + ", "
				+ getResourceExpression() + ", " + getBound() + ")" + super.toString();
	}

	@Override
	protected String getTextualConstraint() {
		return getActivityExpression() + " uses at least " + getBound() + " " + getResourceExpression();
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new AtLeastUsage(null, null, getActivityExpression(), getResourceExpression(),
				getBound(), isOptional());
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		// TODO Auto-generated method stub
		return null;
	}
}