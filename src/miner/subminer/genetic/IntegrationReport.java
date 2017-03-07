package miner.subminer.genetic;

public class IntegrationReport {

	private int batchRulesAdded;
	private int batchRulesRemoved;
	private int singleRulesAdded;
	private int singleRulesRemoved;

	public IntegrationReport(int batchRulesAdded, int batchRulesRemoved, int singleRulesAdded, int singleRulesRemoved) {
		super();
		this.batchRulesAdded = batchRulesAdded;
		this.batchRulesRemoved = batchRulesRemoved;
		this.singleRulesAdded = singleRulesAdded;
		this.singleRulesRemoved = singleRulesRemoved;
	}

	public IntegrationReport() {
		this(0, 0, 0, 0);
	}

	public int getBatchRulesAdded() {
		return batchRulesAdded;
	}

	public void setBatchRulesAdded(int batchRulesAdded) {
		this.batchRulesAdded = batchRulesAdded;
	}

	public void addBatchRulesAdded(int batchRulesAdded) {
		this.batchRulesAdded += batchRulesAdded;
	}

	public void addBatchRulesAdded() {
		this.batchRulesAdded++;
	}

	public int getBatchRulesRemoved() {
		return batchRulesRemoved;
	}

	public void setBatchRulesRemoved(int batchRulesRemoved) {
		this.batchRulesRemoved = batchRulesRemoved;
	}

	public void addBatchRulesRemoved(int batchRulesRemoved) {
		this.batchRulesRemoved += batchRulesRemoved;
	}

	public void addBatchRulesRemoved() {
		this.batchRulesRemoved++;
	}

	public int getSingleRulesAdded() {
		return singleRulesAdded;
	}

	public void setSingleRulesAdded(int singleRulesAdded) {
		this.singleRulesAdded = singleRulesAdded;
	}

	public void addSingleRulesAdded(int singleRulesAdded) {
		this.singleRulesAdded += singleRulesAdded;
	}

	public int getSingleRulesRemoved() {
		return singleRulesRemoved;
	}

	public void setSingleRulesRemoved(int singleRulesRemoved) {
		this.singleRulesRemoved = singleRulesRemoved;
	}

	public void addSingleRulesRemoved(int singleRulesRemoved) {
		this.singleRulesRemoved += singleRulesRemoved;
	}

	public boolean hadEffect() {
		//they should never be negative so this should be correct
		return batchRulesAdded+batchRulesRemoved+singleRulesAdded+singleRulesRemoved != 0;
	}

	public void add(IntegrationReport other) {
		this.addBatchRulesAdded(other.getBatchRulesAdded());
		this.addBatchRulesRemoved(other.getBatchRulesRemoved());
		this.addSingleRulesAdded(other.getSingleRulesAdded());
		this.addSingleRulesRemoved(other.getSingleRulesRemoved());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + batchRulesAdded;
		result = prime * result + batchRulesRemoved;
		result = prime * result + singleRulesAdded;
		result = prime * result + singleRulesRemoved;
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
		IntegrationReport other = (IntegrationReport) obj;
		if (batchRulesAdded != other.batchRulesAdded)
			return false;
		if (batchRulesRemoved != other.batchRulesRemoved)
			return false;
		if (singleRulesAdded != other.singleRulesAdded)
			return false;
		if (singleRulesRemoved != other.singleRulesRemoved)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IntegrationReport [BRAdded=" + batchRulesAdded + ", BRRemoved=" + batchRulesRemoved
				+ ", SRAdded=" + singleRulesAdded + ", SRRemoved=" + singleRulesRemoved + "]";
	}
}