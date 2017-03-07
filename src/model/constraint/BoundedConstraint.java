package model.constraint;

public interface BoundedConstraint {

	public int getBound();
	public void setBound(int newBound);
}