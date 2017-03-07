package model.data;

import java.io.Serializable;
import java.util.HashSet;

public class Decision implements Serializable {

	private static final long serialVersionUID = -5884228816400622810L;

	private HashSet<DecisionRule> rules;

	public Decision() {
		this(new HashSet<DecisionRule>());
	}

	public Decision(HashSet<DecisionRule> rules) {
		super();
		this.rules = rules;
	}

	public HashSet<DecisionRule> getRules() {
		return rules;
	}

	public boolean addRule(DecisionRule dr) {
		return rules.add(dr);
	}

	public boolean addRules(HashSet<DecisionRule> drs) {
		return rules.addAll(drs);
	}

	public void setRules(HashSet<DecisionRule> rules) {
		this.rules = rules;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rules == null) ? 0 : rules.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Decision other = (Decision) obj;
		if (rules == null) {
			if (other.rules != null)
				return false;
		} else if (!rules.equals(other.rules))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return rules.toString();
	}

	public String getTextualVersion() {
		String res = null;
		for(DecisionRule dr : getRules()) {
			if(res == null)
				res = "If[";
			else
				res += " or ";
			res += dr.getTextualVersion(); 
		}
		return res + "]";
	}
}