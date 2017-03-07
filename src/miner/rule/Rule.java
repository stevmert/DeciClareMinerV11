package miner.rule;

import java.io.Serializable;
import java.util.List;

import miner.Config;
import model.Constraint;

public abstract class Rule implements Serializable {

	private static final long serialVersionUID = 8031653407753384182L;

	private Constraint constraint;
	private Rule seed;
	private int generation;

	public Rule(Constraint constraint, Rule seed) {
		super();
		this.constraint = constraint;
		this.seed = seed;
		this.generation = 0;
	}

	public Constraint getConstraint() {
		return constraint;
	}

	public void setConstraint(Constraint constraint) {
		this.constraint = constraint;
	}

	public Rule getSeed() {
		return seed;
	}

	public void setSeed(Rule seed) {
		this.seed = seed;
	}

	public int getGeneration() {
		return generation;
	}

	public void setGeneration(int generation) {
		this.generation = generation;
	}

	public abstract int getConformingTraces();

	public abstract int getViolatingTraces();

	public abstract int getNrOfConfirmations();

	public abstract int getNrOfViolations();

	public abstract List<SingleRule> rules();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((constraint == null) ? 0 : constraint.hashCode());
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
		Rule other = (Rule) obj;
		if (constraint == null) {
			if (other.constraint != null)
				return false;
		} else if (!constraint.equals(other.constraint))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return toString(false);
	}

	public String toString(boolean useTextualConstraint) {
		return (useTextualConstraint?getConstraint().getTextRepresentation():getConstraint().toString());
	}

	public static String getCSV_HEADERS() {
		return "PoI+"
				+ Config.CSV_DELIMITER + "#I+"
				+ Config.CSV_DELIMITER + "PoI-"
				+ Config.CSV_DELIMITER + "#I-"
				+ Config.CSV_DELIMITER + "PoA+"
				+ Config.CSV_DELIMITER + "#A+"
				+ Config.CSV_DELIMITER + "PoA-"
				+ Config.CSV_DELIMITER + "#A-"
				+ Config.CSV_DELIMITER + "PoIW"
				+ Config.CSV_DELIMITER + "#Traces"
				+ Config.CSV_DELIMITER + "Constraint";
	}
}