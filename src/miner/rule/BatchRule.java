package miner.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.Constraint;
import model.constraint.BoundedConstraint;
import model.constraint.existence.OccurrenceConstraint;

public class BatchRule extends Rule implements Serializable {

	private static final long serialVersionUID = -8705139028635552327L;

	private Rule[] rules;

	public BatchRule(Constraint constraint, Rule[] rules, Rule seed) {
		super(constraint, seed);
		this.rules = rules;
	}

	public Rule[] getRules() {
		return rules;
	}

	public void setRules(Rule[] rules) {
		this.rules = rules;
	}

	@Override
	public int getConformingTraces() {
		int c = 0;
		for(Rule r : getRules())
			c += r.getConformingTraces();
		return c;
	}

	@Override
	public int getViolatingTraces() {
		int c = 0;
		for(Rule r : getRules())
			c += r.getViolatingTraces();
		return c;
	}

	@Override
	public int getNrOfConfirmations() {
		int c = 0;
		for(Rule r : getRules())
			c += r.getNrOfConfirmations();
		return c;
	}

	@Override
	public int getNrOfViolations() {
		int c = 0;
		for(Rule r : getRules())
			c += r.getNrOfViolations();
		return c;
	}

	@Override
	public List<SingleRule> rules() {
		ArrayList<SingleRule> res = new ArrayList<>();
		for(Rule r : getRules())
			res.addAll(r.rules());
		return res;
	}

	public void purge(BatchRule parent1, BatchRule parent2, double minSupport) {
		if(this.getConstraint() instanceof OccurrenceConstraint)
			return;
		ArrayList<Rule> newRules = new ArrayList<>();
		List<SingleRule> rules = rules();
		for(SingleRule s : rules) {
			SingleRule s1 = getParentRule(s, parent1);
			SingleRule s2 = getParentRule(s, parent2);
			if(s1 != null && s2 != null
					&& s.getNrOfViolations() < s1.getNrOfViolations()
					&& s.getNrOfViolations() < s2.getNrOfViolations()
					&& s.getPotential() >= minSupport)
				newRules.add(s);
		}
		if(newRules.size() != rules.size())
			setRules(newRules.toArray(new Rule[newRules.size()]));
	}

	private SingleRule getParentRule(SingleRule s, BatchRule parent) {
		//closest related for bounded!
		if(s.getConstraint() instanceof BoundedConstraint) {
			SingleRule closestRelated = null;
			for(SingleRule s2 : parent.rules())
				if(s.getConstraint().isRelatedTo(s2.getConstraint())
						&& (closestRelated == null
						|| s2.getConstraint().isRelatedTo(closestRelated.getConstraint()))) {
					closestRelated = s2;
					if(((BoundedConstraint) s.getConstraint()).getBound() == ((BoundedConstraint) closestRelated.getConstraint()).getBound())
						break;
				}
			return closestRelated;
		} else {
			for(SingleRule s2 : parent.rules())
				if(s.getConstraint().isRelatedTo(s2.getConstraint()))
					return s2;
			return null;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(rules);
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
		BatchRule other = (BatchRule) obj;
		if (!Arrays.equals(rules, other.rules))
			return false;
		return true;
	}
}