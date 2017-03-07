package model.constraint.resource;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.constraint.Negatable;
import model.data.Decision;
import model.expression.ResourceExpression;

public class ResourceAvailabilitySchedule extends ResourceParameterConstraint implements Negatable {

	private static final long serialVersionUID = 707236037076366298L;

	public ResourceAvailabilitySchedule(Decision activationDecision, Decision deactivationDec,
			ResourceExpression resourceExpression, boolean isOptional) {
		super(activationDecision, deactivationDec, resourceExpression, isOptional);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isPositiveVersion() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setIsPositiveVersion(boolean isPositiveVersion) {
		// TODO Auto-generated method stub

	}

	@Override
	public Constraint getDecisionlessCopy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getTextualConstraint() {
		// TODO Auto-generated method stub
		return null;
	}
}