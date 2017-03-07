package model.expression;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;

public class NonAtomicResourceExpression extends NonAtomicExpression implements LogicalExpression, ResourceExpression, Serializable {

	private static final long serialVersionUID = -4300530372524118215L;

	public HashSet<ResourceExpression> expressions;

	public NonAtomicResourceExpression(LogicalOperator operator, ResourceExpression... expressions) {
		this(operator, new HashSet<>(Arrays.asList(expressions)));
	}

	public NonAtomicResourceExpression(LogicalOperator operator, HashSet<ResourceExpression> expressions) {
		super(operator);
		if(expressions == null
				|| expressions.size() < 2)
			throw new IllegalArgumentException();
		this.expressions = expressions;
	}

	public HashSet<ResourceExpression> getExpressions() {
		return expressions;
	}

	public void setExpressions(HashSet<ResourceExpression> expressions) {
		this.expressions = expressions;
	}

	@Override
	public int getNrOfElements() {
		int total = 0;
		for(ResourceExpression re : expressions)
			total += re.getNrOfElements();
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
		NonAtomicResourceExpression other = (NonAtomicResourceExpression) obj;
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
		for(ResourceExpression re : expressions) {
			res += " " + getOperator() + " ";
			if(re instanceof AtomicResourceExpression)
				res += re;
			else
				res += "(" + re + ")";
		}
		return res.substring((" " + getOperator() + " ").length());
	}
}