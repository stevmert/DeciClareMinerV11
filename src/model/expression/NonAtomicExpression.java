package model.expression;

import java.io.Serializable;

public abstract class NonAtomicExpression implements Serializable {

	private static final long serialVersionUID = 745550802322561210L;

	private LogicalOperator operator;

	public NonAtomicExpression(LogicalOperator operator) {
		super();
		this.operator = operator;
	}

	public LogicalOperator getOperator() {
		return operator;
	}

	public void setOperator(LogicalOperator operator) {
		this.operator = operator;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((operator == null) ? 0 : operator.hashCode());
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
		NonAtomicExpression other = (NonAtomicExpression) obj;
		if (operator != other.operator)
			return false;
		return true;
	}
}