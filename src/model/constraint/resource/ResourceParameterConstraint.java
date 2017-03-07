package model.constraint.resource;

import model.constraint.ResourceConstraint;
import model.data.Decision;
import model.expression.ResourceExpression;

public abstract class ResourceParameterConstraint extends ResourceConstraint {

	private static final long serialVersionUID = 3487228237223219164L;

	private ResourceExpression resourceExpression;

	public ResourceParameterConstraint(Decision activationDecision, Decision deactivationDec,
			ResourceExpression resourceExpression, boolean isOptional) {
		super(activationDecision, deactivationDec, isOptional);
		this.resourceExpression = resourceExpression;
	}

	public ResourceExpression getResourceExpression() {
		return resourceExpression;
	}

	public void setResourceExpression(ResourceExpression resourceExpression) {
		this.resourceExpression = resourceExpression;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((resourceExpression == null) ? 0 : resourceExpression.hashCode());
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
		ResourceParameterConstraint other = (ResourceParameterConstraint) obj;
		if (resourceExpression == null) {
			if (other.resourceExpression != null)
				return false;
		} else if (!resourceExpression.equals(other.resourceExpression))
			return false;
		return true;
	}
}