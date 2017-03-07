package model.expression;

public interface ExistenceExpression {
	public int getNrOfElements();
	public String toTextualString();
	public ExistenceExpression getNegation();
	public boolean hasAtMost();
}