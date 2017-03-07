package model.expression;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;

import model.Activity;

public class NonAtomicActivityExpression extends NonAtomicExpression implements LogicalExpression, ActivityExpression, Serializable {

	private static final long serialVersionUID = 360797720045791851L;

	public HashSet<ActivityExpression> expressions;

	public NonAtomicActivityExpression(LogicalOperator operator, ActivityExpression... expressions) {
		this(operator, new HashSet<>(Arrays.asList(expressions)));
	}

	public NonAtomicActivityExpression(LogicalOperator operator, Activity... activities) {
		this(operator, convertToExpressions(activities));
	}

	public NonAtomicActivityExpression(LogicalOperator operator, HashSet<ActivityExpression> expressions) {
		super(operator);
		if(expressions == null
				|| expressions.size() < 2)
			throw new IllegalArgumentException();
		this.expressions = expressions;
	}

	private static HashSet<ActivityExpression> convertToExpressions(Activity... activities) {
		HashSet<ActivityExpression> tmp = new HashSet<>();
		for(Activity a : activities)
			tmp.add(new AtomicActivityExpression(a));
		return tmp;
	}

	public HashSet<ActivityExpression> getExpressions() {
		return expressions;
	}

	public void setExpressions(HashSet<ActivityExpression> expressions) {
		this.expressions = expressions;
	}

	@Override
	public int getNrOfElements() {
		int total = 0;
		for(ActivityExpression ae : expressions)
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
		NonAtomicActivityExpression other = (NonAtomicActivityExpression) obj;
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
		for(ActivityExpression ae : expressions) {
			res += " " + getOperator() + " ";
			if(ae instanceof AtomicActivityExpression)
				res += ae;
			else
				res += "(" + ae + ")";
		}
		return res.substring((" " + getOperator() + " ").length());
	}
}