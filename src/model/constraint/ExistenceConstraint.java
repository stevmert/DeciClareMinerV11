package model.constraint;

import model.Constraint;
import model.data.Decision;
import model.expression.ActivityExpression;

public abstract class ExistenceConstraint extends Constraint {

	private static final long serialVersionUID = 9221822056339140854L;

	private ActivityExpression activityExpression;

	public ExistenceConstraint(Decision activationDec, Decision deactivationDec,
			ActivityExpression activityExpression, boolean isOptional) {
		super(activationDec, deactivationDec, isOptional);
		this.activityExpression = activityExpression;
	}

	public ActivityExpression getActivityExpression() {
		return activityExpression;
	}

	public void setActivityExpression(ActivityExpression activityExpression) {
		this.activityExpression = activityExpression;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((activityExpression == null) ? 0 : activityExpression.hashCode());
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
		ExistenceConstraint other = (ExistenceConstraint) obj;
		if (activityExpression == null) {
			if (other.activityExpression != null)
				return false;
		} else if (!activityExpression.equals(other.activityExpression))
			return false;
		return true;
	}
}