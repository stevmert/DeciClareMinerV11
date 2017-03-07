package miner.rule;

import java.util.ArrayList;
import java.util.List;

import miner.Config;
import model.Constraint;

public class SingleRule extends Rule {

	private static final long serialVersionUID = 5248236618099801281L;

	private int nrOfConfirmations;
	private int nrOfViolations;
	private int conformingTraces;
	private int violatingTraces;
	private int totalTraces;

	public SingleRule(Constraint constraint, int nrOfConfirmations, int nrOfViolations,
			int conformingTraces, int violatingTraces, int totalTraces) {
		super(constraint, null);
		this.nrOfConfirmations = nrOfConfirmations;
		this.nrOfViolations = nrOfViolations;
		this.conformingTraces = conformingTraces;
		this.violatingTraces = violatingTraces;
		this.totalTraces = totalTraces;
	}

	@Override
	public int getConformingTraces() {
		return conformingTraces;
	}

	public void setConformingTraces(int conformingTraces) {
		this.conformingTraces = conformingTraces;
	}

	@Override
	public int getViolatingTraces() {
		return violatingTraces;
	}

	public void setViolatingTraces(int violatingTraces) {
		this.violatingTraces = violatingTraces;
	}

	public int getTotalTraces() {
		return totalTraces;
	}

	public void setTotalTraces(int totalTraces) {
		this.totalTraces = totalTraces;
	}

	@Override
	public int getNrOfConfirmations() {
		return nrOfConfirmations;
	}

	public void setNrOfConfirmations(int nrOfConfirmations) {
		this.nrOfConfirmations = nrOfConfirmations;
	}

	@Override
	public int getNrOfViolations() {
		return nrOfViolations;
	}

	public void setNrOfViolations(int nrOfViolations) {
		this.nrOfViolations = nrOfViolations;
	}

	//	public boolean couldBecomeBetterThan(Rule parent) {TODO
	//		if(parent == null)
	//			return true;
	//		if(parent.getNrOfViolations() > getNrOfViolations())
	//			return couldBecomeBetterThan(parent.getParent());
	//		if(parent.getNrOfViolations() == getNrOfViolations()
	//				&& parent.getNrOfConfirmations() < getNrOfConfirmations())
	//			return couldBecomeBetterThan(parent.getParent());
	//		//is simpler version of parent
	//		if(parent.getNrOfViolations() == getNrOfViolations()
	//				&& parent.getNrOfConfirmations() == getNrOfConfirmations()
	//				&& parent.getConstraint().getActivationDecision() != null
	//				&& !parent.getConstraint().getActivationDecision().getRules().equals(
	//						getConstraint().getActivationDecision().getRules())
	//				&& parent.getConstraint().getActivationDecision().getRules().containsAll(
	//						getConstraint().getActivationDecision().getRules()))
	//			return couldBecomeBetterThan(parent.getParent());
	//		return false;
	//	}
	//
	//	public boolean couldBecomeBetterThanParents() {
	//		return couldBecomeBetterThan(getParent());
	//	}
	//
	//	public boolean isBetterThan(Rule parent) {
	//		if(parent == null)
	//			return true;
	//		if(parent.getConformancePercentage()
	//				< getConformancePercentage())
	//			return isBetterThan(parent.getParent());
	//		if(parent.getConformancePercentage() == getConformancePercentage()
	//				&& parent.getNrOfConfirmations() < getNrOfConfirmations())
	//			return isBetterThan(parent.getParent());
	//		return false;
	//	}
	//
	//	public boolean isBetterThanParents() {
	//		return isBetterThan(getParent());
	//	}

	public double getConformancePercentage_Traces() {
		if(getConformingTraces() == 0)
			return 0d;
		return (1000*getConformingTraces()/(getConformingTraces()+getViolatingTraces()))/((double) 10);
	}

	public double getViolationPercentage_Traces() {
		if(getViolatingTraces() == 0)
			return 0d;
		return (1000*getViolatingTraces()/(getConformingTraces()+getViolatingTraces()))/((double) 10);
	}

	public double getConformancePercentage() {
		if(getNrOfConfirmations() == 0)
			return 0d;
		return (1000*getNrOfConfirmations()/(getNrOfConfirmations()+getNrOfViolations()))/((double) 10);
	}

	public double getViolationPercentage() {
		if(getNrOfViolations() == 0)
			return 0d;
		return (1000*getNrOfViolations()/(getNrOfConfirmations()+getNrOfViolations()))/((double) 10);
	}

	public double getSupportPercentage() {
		return getSupportPercentage(getConformingTraces(), getViolatingTraces(), getTotalTraces());
	}

	public double getPotential() {
		return getSupportPercentage(getConformingTraces(), 0, getTotalTraces());
	}

	public static double getSupportPercentage(int conformingTraces, int violatingTraces,
			int totalTraces) {
		if(conformingTraces + violatingTraces == 0)
			return 0d;
		return (1000*(conformingTraces + violatingTraces)/totalTraces)/((double) 10);
	}

	public String getCSV() {
		return getConformancePercentage_Traces()
				+ Config.CSV_DELIMITER + getConformingTraces()
				+ Config.CSV_DELIMITER + getViolationPercentage_Traces()
				+ Config.CSV_DELIMITER + getViolatingTraces()
				+ Config.CSV_DELIMITER + getConformancePercentage()
				+ Config.CSV_DELIMITER + getNrOfConfirmations()
				+ Config.CSV_DELIMITER + getViolationPercentage()
				+ Config.CSV_DELIMITER + getNrOfViolations()
				+ Config.CSV_DELIMITER + getSupportPercentage()
				+ Config.CSV_DELIMITER + getTotalTraces()
				+ Config.CSV_DELIMITER + getConstraint();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + conformingTraces;
		result = prime * result + nrOfConfirmations;
		result = prime * result + nrOfViolations;
		result = prime * result + totalTraces;
		result = prime * result + violatingTraces;
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
		SingleRule other = (SingleRule) obj;
		if (conformingTraces != other.conformingTraces)
			return false;
		if (nrOfConfirmations != other.nrOfConfirmations)
			return false;
		if (nrOfViolations != other.nrOfViolations)
			return false;
		if (totalTraces != other.totalTraces)
			return false;
		if (violatingTraces != other.violatingTraces)
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
				+ ": " + (useTextualConstraint?getConstraint().getTextRepresentation():getConstraint().toString());
		//Percentage of Interesting Witnesses = Support per trace
	}

	@Override
	public List<SingleRule> rules() {
		ArrayList<SingleRule> res = new ArrayList<>();
		res.add(this);
		return res;
	}
}