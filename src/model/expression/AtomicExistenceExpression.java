package model.expression;

import java.io.Serializable;

import model.constraint.ExistenceConstraint;
import model.constraint.existence.AtLeast;
import model.constraint.existence.AtMost;

public class AtomicExistenceExpression implements LogicalExpression, ExistenceExpression, Serializable {

	private static final long serialVersionUID = -4086217898179295567L;

	private ExistenceConstraint exCon;

	public AtomicExistenceExpression(ExistenceConstraint exCon) {
		super();
		//TODO: check if allowed existence template
		//TODO: not null!
		//TODO: no decisions!!!
		this.exCon = exCon;
	}

	public ExistenceConstraint getExistenceConstraint() {
		return exCon;
	}

	public void setExistenceConstraint(ExistenceConstraint exCon) {
		this.exCon = exCon;
	}

	@Override
	public int getNrOfElements() {
		return 1;
	}

	@Override
	public ExistenceExpression getNegation() {
		if(exCon instanceof AtLeast)
			return new AtomicExistenceExpression(
					new AtMost(null, null, exCon.getActivityExpression(), ((AtLeast) exCon).getBound()-1,
							0, -1, false));
		else if(exCon instanceof AtMost)
			return new AtomicExistenceExpression(
					new AtLeast(null, null, exCon.getActivityExpression(), ((AtMost) exCon).getBound()+1,
							0, -1, false));
		else//TODO: other existence expressions
			throw new IllegalArgumentException("TODO");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((exCon == null) ? 0 : exCon.hashCode());
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
		AtomicExistenceExpression other = (AtomicExistenceExpression) obj;
		if (exCon == null) {
			if (other.exCon != null)
				return false;
		} else if (!exCon.equals(other.exCon))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return exCon.toString();
	}

	@Override
	public String toTextualString() {
		return "(" + exCon.getTextRepresentation() + ")";
	}

	@Override
	public boolean hasAtMost() {
		return this.getExistenceConstraint() instanceof AtMost;
	}
}