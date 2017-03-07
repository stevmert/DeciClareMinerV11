package model.expression;

public enum LogicalOperator {

	AND, OR;

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}