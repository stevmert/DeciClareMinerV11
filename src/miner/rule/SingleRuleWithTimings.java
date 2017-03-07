package miner.rule;

import model.Constraint;

public class SingleRuleWithTimings extends SingleRule {

	private static final long serialVersionUID = 4624676582960300947L;

	private double avgTime;
	private double stdevTime;
	private long minTime;
	private long maxTime;

	public SingleRuleWithTimings(Constraint constraint, int nrOfConfirmations, int nrOfViolations, int conformingTraces,
			int violatingTraces, int totalTraces, double avgTime, double stdevTime, long minTime, long maxTime) {
		super(constraint, nrOfConfirmations, nrOfViolations, conformingTraces, violatingTraces, totalTraces);
		this.avgTime = avgTime;
		this.stdevTime = stdevTime;
		this.minTime = minTime;
		this.maxTime = maxTime;
	}

	public double getAvgTime() {
		return avgTime;
	}

	public void setAvgTime(double avgTime) {
		this.avgTime = avgTime;
	}

	public double getStdevTime() {
		return stdevTime;
	}

	public void setStdevTime(double stdevTime) {
		this.stdevTime = stdevTime;
	}

	public long getMinTime() {
		return minTime;
	}

	public void setMinTime(long minTime) {
		this.minTime = minTime;
	}

	public long getMaxTime() {
		return maxTime;
	}

	public void setMaxTime(long maxTime) {
		this.maxTime = maxTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(avgTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (maxTime ^ (maxTime >>> 32));
		result = prime * result + (int) (minTime ^ (minTime >>> 32));
		temp = Double.doubleToLongBits(stdevTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		SingleRuleWithTimings other = (SingleRuleWithTimings) obj;
		if (Double.doubleToLongBits(avgTime) != Double.doubleToLongBits(other.avgTime))
			return false;
		if (maxTime != other.maxTime)
			return false;
		if (minTime != other.minTime)
			return false;
		if (Double.doubleToLongBits(stdevTime) != Double.doubleToLongBits(other.stdevTime))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return toString(false);
	}

	@Override
	public String toString(boolean useTextualConstraint) {
		return "PoI=" + getConformancePercentage_Traces() + "% (" + getConformingTraces() + "-" + getViolatingTraces() + ")" //Percentage of Instances = conformance per trace
				+ ", PoA=" + getConformancePercentage() + "% (" + getNrOfConfirmations() + "-" + getNrOfViolations() + ")" //Percentage of Activations = conformance per possible activation
				+ ", PoIW=" + getSupportPercentage() + "%"
				+ ", avgTime=" + getAvgTime() + " (stdev=" + getStdevTime() + "min/max=" + getMinTime() + "/" + getMaxTime() + ")"
				+ ": " + (useTextualConstraint?getConstraint().getTextRepresentation():getConstraint().toString());
		//Percentage of Interesting Witnesses = Support per trace
	}
}