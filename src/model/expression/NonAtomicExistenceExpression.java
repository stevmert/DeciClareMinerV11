package model.expression;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;

public class NonAtomicExistenceExpression extends NonAtomicExpression implements LogicalExpression, ExistenceExpression, Serializable {

	private static final long serialVersionUID = -4719533727029500747L;

	private HashSet<ExistenceExpression> expressions;

	public NonAtomicExistenceExpression(LogicalOperator operator, ExistenceExpression... expressions) {
		this(operator, new HashSet<>(Arrays.asList(expressions)));
	}

	public NonAtomicExistenceExpression(LogicalOperator operator, HashSet<ExistenceExpression> expressions) {
		super(operator);
		if(expressions == null
				|| expressions.size() < 2)
			throw new IllegalArgumentException();
		this.expressions = expressions;
	}

	public HashSet<ExistenceExpression> getExpressions() {
		return expressions;
	}

	public void setExpressions(HashSet<ExistenceExpression> expressions) {
		this.expressions = expressions;
	}

	@Override
	public int getNrOfElements() {
		int total = 0;
		for(ExistenceExpression e : expressions)
			total += e.getNrOfElements();
		return total;
	}

	@Override
	public ExistenceExpression getNegation() {
		LogicalOperator op;
		if(getOperator().equals(LogicalOperator.AND))
			op = LogicalOperator.OR;
		else
			op = LogicalOperator.AND;
		HashSet<ExistenceExpression> newExpressions = new HashSet<>();
		for(ExistenceExpression e : getExpressions())
			newExpressions.add(e.getNegation());
		return new NonAtomicExistenceExpression(op, newExpressions);
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
		NonAtomicExistenceExpression other = (NonAtomicExistenceExpression) obj;
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
		for(ExistenceExpression e : expressions) {
			res += " " + getOperator() + " ";
			if(e instanceof AtomicExistenceExpression)
				res += e;
			else
				res += "(" + e + ")";
		}
		return res.substring((" " + getOperator() + " ").length());
	}

	@Override
	public String toTextualString() {
		String res = "";
		for(ExistenceExpression e : expressions) {
			res += " " + getOperator() + " ";
			if(e instanceof AtomicExistenceExpression)
				res += e.toTextualString();
			else
				res += "(" + e.toTextualString() + ")";
		}
		res = res.substring((" " + getOperator() + " ").length());
		if(!res.startsWith("(") || !res.endsWith(")"))
			res = "(" + res + ")";
		return res;
	}

	@Override
	public boolean hasAtMost() {
		for(ExistenceExpression x : getExpressions())
			if(x.hasAtMost())
				return true;
		return false;
	}
}