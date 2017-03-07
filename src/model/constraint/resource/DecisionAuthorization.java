package model.constraint.resource;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.constraint.Negatable;
import model.data.Decision;
import model.expression.ResourceExpression;

public class DecisionAuthorization extends ResourceParameterConstraint implements Negatable {

	private static final long serialVersionUID = -8809426004161472233L;

	private Constraint constraint;
	private boolean isPositiveVersion;

	public DecisionAuthorization(Decision activationDecision, Decision deactivationDec,
			ResourceExpression resourceExpression, Constraint constraint,
			boolean isPositiveVersion, boolean isOptional) {
		super(activationDecision, deactivationDec, resourceExpression, isOptional);
		this.constraint = constraint;
		this.isPositiveVersion = isPositiveVersion;
	}

	public Constraint getConstraint() {
		return constraint;
	}

	public void setConstraint(Constraint constraint) {
		this.constraint = constraint;
	}

	@Override
	public boolean isPositiveVersion() {
		return isPositiveVersion;
	}

	@Override
	public void setIsPositiveVersion(boolean isPositiveVersion) {
		this.isPositiveVersion = isPositiveVersion;
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new DecisionAuthorization(null, null, getResourceExpression(), getConstraint(),
				isPositiveVersion(), isOptional());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((constraint == null) ? 0 : constraint.hashCode());
		result = prime * result + (isPositiveVersion ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj, true);
	}

	@Override
	public boolean equals(Object obj, boolean checkDecisions) {
		if (this == obj)
			return true;
		if (!super.equals(obj, checkDecisions))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DecisionAuthorization other = (DecisionAuthorization) obj;
		if (constraint == null) {
			if (other.constraint != null)
				return false;
		} else if (!constraint.equals(other.constraint))
			return false;
		if (isPositiveVersion != other.isPositiveVersion)
			return false;
		return true;
	}

	@Override
	public String toString() {
		String pre = "";
		if(!isPositiveVersion())
			pre = "Not";
		return pre + this.getClass().getSimpleName() + "(" + getResourceExpression() + ", " + getConstraint()
		+ ")" + super.toString();
	}

	@Override
	protected String getTextualConstraint() {
		String verb;
		if(isPositiveVersion())
			verb = "has to";
		else
			verb = "cannot";
		return "the decision of '" + getConstraint().getTextRepresentation() + "' " + verb + " be taken by a " + getResourceExpression();
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		// TODO Auto-generated method stub
		return null;
	}
}