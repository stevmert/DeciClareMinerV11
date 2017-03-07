package model.constraint.data;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.constraint.DataConstraint;
import model.data.Decision;
import model.expression.ActivityExpression;
import model.expression.DataExpression;

public class Read extends DataConstraint {

	private static final long serialVersionUID = 5554621792342529695L;

	public Read(Decision activationDecision, Decision deactivationDec, DataExpression dataExpression,
			ActivityExpression actExpression, boolean isPositiveVersion, boolean isOptional) {
		super(activationDecision, deactivationDec, dataExpression, actExpression, isPositiveVersion, isOptional);
	}

	@Override
	public String toString() {
		return toString(this.getClass().getSimpleName());
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new Read(null, null, getDataExpression(), getActExpression(), isPositiveVersion(), isOptional());
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getTextualConstraintVerb() {
		return "read";
	}
}