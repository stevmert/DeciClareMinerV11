package model.constraint.resource;

import java.io.Serializable;

import model.expression.ActivityExpression;
import model.expression.ResourceExpression;

public class Resource_ActivityExpressionPair implements Serializable {

	private static final long serialVersionUID = 5734796090619813866L;

	private ResourceExpression resourceExpression;
	private ActivityExpression activityExpression;

	public Resource_ActivityExpressionPair(ResourceExpression resourceExpression,
			ActivityExpression activityExpression) {
		super();
		this.resourceExpression = resourceExpression;
		this.activityExpression = activityExpression;
	}

	public ResourceExpression getResourceExpression() {
		return resourceExpression;
	}

	public void setResourceExpression(ResourceExpression resourceExpression) {
		this.resourceExpression = resourceExpression;
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
		int result = 1;
		result = prime * result + ((activityExpression == null) ? 0 : activityExpression.hashCode());
		result = prime * result + ((resourceExpression == null) ? 0 : resourceExpression.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Resource_ActivityExpressionPair other = (Resource_ActivityExpressionPair) obj;
		if (activityExpression == null) {
			if (other.activityExpression != null)
				return false;
		} else if (!activityExpression.equals(other.activityExpression))
			return false;
		if (resourceExpression == null) {
			if (other.resourceExpression != null)
				return false;
		} else if (!resourceExpression.equals(other.resourceExpression))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return resourceExpression + " in " + activityExpression;
	}
}