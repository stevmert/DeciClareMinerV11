package miner.subminer.genetic;

import java.util.Comparator;

import miner.rule.RulePrintComparator;
import miner.rule.SingleRule;
import model.constraint.existence.AtLeast;
import model.constraint.existence.AtMost;

/**
 * Only works for SequenceRelationConstraints!
 * 
 * Sorts from specific to more general
 */
public class SubRuleGeneralityComparator implements Comparator<SingleRule> {

	@Override
	public int compare(SingleRule r1, SingleRule r2) {
		int o1 = RulePrintComparator.ORDER.indexOf(r1.getConstraint().getClass());
		int o2 = RulePrintComparator.ORDER.indexOf(r2.getConstraint().getClass());
		if(o1 > o2)
			return -1;
		if(o1 < o2)
			return 1;
		if(r1.getConstraint() instanceof AtLeast) {
			AtLeast a1 = (AtLeast) r1.getConstraint();
			AtLeast a2 = (AtLeast) r2.getConstraint();
			if(a1.getBound() > a2.getBound())
				return -1;
			if(a1.getBound() < a2.getBound())
				return 1;
		} if(r1.getConstraint() instanceof AtMost) {
			AtMost a1 = (AtMost) r1.getConstraint();
			AtMost a2 = (AtMost) r2.getConstraint();
			if(a1.getBound() < a2.getBound())
				return -1;
			if(a1.getBound() > a2.getBound())
				return 1;
		}
		return 0;
	}
}