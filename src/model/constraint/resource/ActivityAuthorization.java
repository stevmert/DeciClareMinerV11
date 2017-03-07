package model.constraint.resource;

import miner.log.Log;
import miner.rule.Rule;
import model.Activity;
import model.Constraint;
import model.constraint.Negatable;
import model.data.Decision;
import model.expression.ResourceExpression;

public class ActivityAuthorization extends ResourceParameterConstraint implements Negatable {

	private static final long serialVersionUID = -8179894975761194170L;

	private Activity activity;
	private boolean isPositiveVersion;

	public ActivityAuthorization(Decision activationDecision, Decision deactivationDec,
			ResourceExpression resourceExpression, Activity activity,
			boolean isPositiveVersion, boolean isOptional) {
		super(activationDecision, deactivationDec, resourceExpression, isOptional);
		this.activity = activity;
		this.isPositiveVersion = isPositiveVersion;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public boolean isPositiveVersion() {
		return isPositiveVersion;
	}

	@Override
	public void setIsPositiveVersion(boolean isPositiveVersion) {
		this.isPositiveVersion = isPositiveVersion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((activity == null) ? 0 : activity.hashCode());
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
		ActivityAuthorization other = (ActivityAuthorization) obj;
		if (activity == null) {
			if (other.activity != null)
				return false;
		} else if (!activity.equals(other.activity))
			return false;
		if (isPositiveVersion != other.isPositiveVersion)
			return false;
		return true;
	}

	@Override
	public String toString() {
		String name = this.getClass().getSimpleName();
		if(!isPositiveVersion())
			name = "Not" + name;
		return name + "(" + getActivity() + ", " + getResourceExpression() + ")" + super.toString();
	}

	@Override
	protected String getTextualConstraint() {
		if(getResourceExpression().getNrOfElements() == 1)
			return "a " + getResourceExpression() + " has authorization over " + getActivity();
		return "a " + getResourceExpression() + " have authorization over " + getActivity();
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new ActivityAuthorization(null, null, getResourceExpression(), activity,
				isPositiveVersion(), isOptional());
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		// TODO Auto-generated method stub
		return null;
	}
}