package model.expression;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;

public class NonAtomicDataExpression extends NonAtomicExpression implements LogicalExpression, DataExpression, Serializable {

	private static final long serialVersionUID = 8102081596777488317L;

	public HashSet<DataExpression> expressions;
	
	public NonAtomicDataExpression(LogicalOperator operator, DataExpression... expressions) {
		this(operator, new HashSet<>(Arrays.asList(expressions)));
	}

	public NonAtomicDataExpression(LogicalOperator operator, HashSet<DataExpression> expressions) {
		super(operator);
		if(expressions == null
				|| expressions.size() < 2)
			throw new IllegalArgumentException();
		this.expressions = expressions;
	}

	public HashSet<DataExpression> getExpressions() {
		return expressions;
	}

	public void setExpressions(HashSet<DataExpression> expressions) {
		this.expressions = expressions;
	}

	@Override
	public int getNrOfElements() {
		int total = 0;
		for(DataExpression ae : expressions)
			total += ae.getNrOfElements();
		return total;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((expressions == null) ? 0 : expressions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NonAtomicDataExpression other = (NonAtomicDataExpression) obj;
		if (expressions == null) {
			if (other.expressions != null)
				return false;
		} else if (!expressions.equals(other.expressions))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String res = "";
		for(DataExpression ae : expressions) {
			res += " " + getOperator() + " ";
			if(ae instanceof AtomicDataExpression)
				res += ae;
			else
				res += "(" + ae + ")";
		}
		return res.substring((" " + getOperator() + " ").length());
	}
}