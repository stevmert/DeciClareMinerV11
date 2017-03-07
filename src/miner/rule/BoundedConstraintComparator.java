package miner.rule;

import java.util.Comparator;

import model.constraint.BoundedConstraint;
import model.constraint.ExistenceConstraint;

public class BoundedConstraintComparator implements Comparator<SingleRule> {

	public BoundedConstraintComparator() {
		super();
	}

	@Override
	public int compare(SingleRule r1, SingleRule r2) {
		int s1 = ((ExistenceConstraint) r1.getConstraint()).getActivityExpression().getNrOfElements();
		int s2 = ((ExistenceConstraint) r2.getConstraint()).getActivityExpression().getNrOfElements();
		if(s1 < s2)
			return -1;
		if(s1 > s2)
			return 1;
		BoundedConstraint bc1 = (BoundedConstraint) r1.getConstraint();
		BoundedConstraint bc2 = (BoundedConstraint) r2.getConstraint();
		if(bc1.getBound() < bc2.getBound())
			return -1;
		if(bc1.getBound() > bc2.getBound())
			return 1;
		return 0;
	}
}