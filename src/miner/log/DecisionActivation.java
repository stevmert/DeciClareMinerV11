package miner.log;

import java.io.Serializable;

import model.data.DecisionRule;

public class DecisionActivation implements Serializable {

	private static final long serialVersionUID = 8599058477912419642L;

	private final DecisionRule decisionRule;
	private final long time;
	
	public DecisionActivation(DecisionRule decisionRule, long time) {
		super();
		this.decisionRule = decisionRule;
		this.time = time;
	}

	public DecisionRule getDecisionRule() {
		return decisionRule;
	}

	public long getTime() {
		return time;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((decisionRule == null) ? 0 : decisionRule.hashCode());
		result = prime * result + (int) (time ^ (time >>> 32));
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
		DecisionActivation other = (DecisionActivation) obj;
		if (decisionRule == null) {
			if (other.decisionRule != null)
				return false;
		} else if (!decisionRule.equals(other.decisionRule))
			return false;
		if (time != other.time)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return time + ": " + decisionRule;
	}
}