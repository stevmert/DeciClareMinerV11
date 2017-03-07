package model.constraint.resource;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.constraint.ResourceConstraint;
import model.data.Decision;
import model.resource.Resource;

public class SimultaneousCapacity extends ResourceConstraint {

	private static final long serialVersionUID = 1760081722694203060L;

	private Resource resource;
	private int simultaneousCapacity;

	public SimultaneousCapacity(Decision activationDecision, Decision deactivationDec,
			Resource resource, int simultaneousCapacity, boolean isOptional) {
		super(activationDecision, deactivationDec, isOptional);
		this.resource = resource;
		this.simultaneousCapacity = simultaneousCapacity;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public int getSimultaneousCapacity() {
		return simultaneousCapacity;
	}

	public void setSimultaneousCapacity(int simultaneousCapacity) {
		this.simultaneousCapacity = simultaneousCapacity;
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new SimultaneousCapacity(null, null, getResource(), getSimultaneousCapacity(), isOptional());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		result = prime * result + simultaneousCapacity;
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
		SimultaneousCapacity other = (SimultaneousCapacity) obj;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		if (simultaneousCapacity != other.simultaneousCapacity)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + getResource() + ", " + getSimultaneousCapacity()
		+ ")" + super.toString();
	}

	@Override
	protected String getTextualConstraint() {
		return getResource() + " can be used by up to " + getSimultaneousCapacity() + " instance(s) simultaneously";
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		// TODO Auto-generated method stub
		return null;
	}
}