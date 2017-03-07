package model.constraint;

import java.io.Serializable;

import model.Constraint;
import model.data.Decision;
import model.expression.ActivityExpression;
import model.expression.DataExpression;

public abstract class DataConstraint extends Constraint implements Negatable, Serializable {

	private static final long serialVersionUID = 45566251456440544L;

	private DataExpression dataExpression;
	private ActivityExpression actExpression;
	private boolean isPositiveVersion;

	public DataConstraint(Decision activationDecision, Decision deactivationDec,
			DataExpression dataExpression, ActivityExpression actExpression, boolean isPositiveVersion,
			boolean isOptional) {
		super(activationDecision, deactivationDec, isOptional);
		this.dataExpression = dataExpression;
		this.actExpression = actExpression;
		this.isPositiveVersion = isPositiveVersion;
	}

	public DataExpression getDataExpression() {
		return dataExpression;
	}

	public void setDataExpression(DataExpression dataExpression) {
		this.dataExpression = dataExpression;
	}

	public ActivityExpression getActExpression() {
		return actExpression;
	}

	public void setActExpression(ActivityExpression actExpression) {
		this.actExpression = actExpression;
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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((actExpression == null) ? 0 : actExpression.hashCode());
		result = prime * result + ((dataExpression == null) ? 0 : dataExpression.hashCode());
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
		DataConstraint other = (DataConstraint) obj;
		if (actExpression == null) {
			if (other.actExpression != null)
				return false;
		} else if (!actExpression.equals(other.actExpression))
			return false;
		if (dataExpression == null) {
			if (other.dataExpression != null)
				return false;
		} else if (!dataExpression.equals(other.dataExpression))
			return false;
		if (isPositiveVersion != other.isPositiveVersion)
			return false;
		return true;
	}

	protected String toString(String name) {
		String pre = "Required";
		if(!isPositiveVersion())
			pre = "Prohibited";
		return pre + name + "(" + getActExpression() + ", " + getDataExpression()
		+ ")" + super.toString();
	}

	@Override
	protected String getTextualConstraint() {
		String verb;
		if(isPositiveVersion()) {
			if(getActExpression().getNrOfElements() == 1)
				verb = " has to";
			else
				verb = " have to";
		} else
			verb = " cannot ";
		return getActExpression() + verb + " " + getTextualConstraintVerb() + " '" + getDataExpression() + "'";
	}

	protected abstract String getTextualConstraintVerb();
}