package model.expression;

import java.io.Serializable;

import model.Activity;

public class AtomicActivityExpression implements LogicalExpression, ActivityExpression, Serializable {

	private static final long serialVersionUID = -113923986088206399L;

	private Activity activity;

	public AtomicActivityExpression(Activity activity) {
		super();
		this.activity = activity;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	@Override
	public int getNrOfElements() {
		return 1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activity == null) ? 0 : activity.hashCode());
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
		AtomicActivityExpression other = (AtomicActivityExpression) obj;
		if (activity == null) {
			if (other.activity != null)
				return false;
		} else if (!activity.equals(other.activity))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return activity.toString();
	}
}