package model.constraint.resource;

import java.util.HashSet;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.constraint.Negatable;
import model.constraint.ResourceConstraint;
import model.data.Decision;

//TODO: can only mine these rules if the resources are uniquely defined...
public class ResourceEquality extends ResourceConstraint implements Negatable {

	private static final long serialVersionUID = 2949801547270807016L;

	private HashSet<Resource_ActivityExpressionPair> pairs;
	private boolean isSame;

	public ResourceEquality(Decision activationDec, Decision deactivationDec,
			HashSet<Resource_ActivityExpressionPair> pairs, boolean isSame, boolean isOptional) {
		super(activationDec, deactivationDec, isOptional);
		if(pairs == null || pairs.size() < 2)
			throw new IllegalArgumentException();
		this.pairs = pairs;
		this.isSame = isSame;
	}

	public HashSet<Resource_ActivityExpressionPair> getPairs() {
		return pairs;
	}

	public void setPairs(HashSet<Resource_ActivityExpressionPair> pairs) {
		this.pairs = pairs;
	}

	public boolean isSame() {
		return isSame;
	}

	public void setIsSame(boolean isSame) {
		this.isSame = isSame;
	}

	@Override
	public boolean isPositiveVersion() {
		return isSame();
	}

	@Override
	public void setIsPositiveVersion(boolean isPositiveVersion) {
		setIsSame(isPositiveVersion);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isSame ? 1231 : 1237);
		result = prime * result + ((pairs == null) ? 0 : pairs.hashCode());
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
		ResourceEquality other = (ResourceEquality) obj;
		if (isSame != other.isSame)
			return false;
		if (pairs == null) {
			if (other.pairs != null)
				return false;
		} else if (!pairs.equals(other.pairs))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String name = "Resource";
		if(isSame())
			name += "Equality";
		else
			name += "Inequality";
		String tmp = "";
		for(Resource_ActivityExpressionPair rp : getPairs())
			tmp += ", " + rp;
		tmp = tmp.substring(2);
		return name + "(" + tmp + ")";
	}

	@Override
	protected String getTextualConstraint() {
		String verb;
		if(isSame())
			verb = "have to";
		else
			verb = "cannot";
		String tmp = "";
		for(Resource_ActivityExpressionPair rp : getPairs())
			tmp += ", " + rp;
		tmp = tmp.substring(2);
		return "the following resources " + verb + " be equal: " + tmp;
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new ResourceEquality(null, null, getPairs(), isSame(), isOptional());
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		// TODO Auto-generated method stub
		return null;
	}
}