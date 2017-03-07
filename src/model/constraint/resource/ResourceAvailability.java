package model.constraint.resource;

import model.constraint.BoundedConstraint;
import model.data.Decision;
import model.expression.ResourceExpression;

public abstract class ResourceAvailability extends ResourceParameterConstraint implements BoundedConstraint {

	private static final long serialVersionUID = -2713283081883433661L;

	private int bound;

	public ResourceAvailability(Decision activationDecision, Decision deactivationDec,
			ResourceExpression resourceExpression, int bound, boolean isOptional) {
		super(activationDecision, deactivationDec, resourceExpression, isOptional);
		this.bound = bound;
	}

	@Override
	public int getBound() {
		return bound;
	}

	@Override
	public void setBound(int bound) {
		this.bound = bound;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + bound;
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
		ResourceAvailability other = (ResourceAvailability) obj;
		if (bound != other.bound)
			return false;
		return true;
	}
}