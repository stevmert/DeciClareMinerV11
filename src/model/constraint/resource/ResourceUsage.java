package model.constraint.resource;

import model.constraint.BoundedConstraint;
import model.data.Decision;
import model.expression.ActivityExpression;
import model.expression.ResourceExpression;

public abstract class ResourceUsage extends ResourceParameterConstraint implements BoundedConstraint {

	private static final long serialVersionUID = 2860757604827713976L;

	private ActivityExpression activityExpression;
	private int bound;

	public ResourceUsage(Decision activationDecision, Decision deactivationDec, ActivityExpression activityExpression,
			ResourceExpression resourceExpression, int bound, boolean isOptional) {
		super(activationDecision, deactivationDec, resourceExpression, isOptional);
		this.activityExpression = activityExpression;
		this.bound = bound;
	}

	public ActivityExpression getActivityExpression() {
		return activityExpression;
	}

	public void setActivityExpression(ActivityExpression activityExpression) {
		this.activityExpression = activityExpression;
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
		result = prime * result + ((activityExpression == null) ? 0 : activityExpression.hashCode());
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
		ResourceUsage other = (ResourceUsage) obj;
		if (activityExpression == null) {
			if (other.activityExpression != null)
				return false;
		} else if (!activityExpression.equals(other.activityExpression))
			return false;
		if (bound != other.bound)
			return false;
		return true;
	}
}