package miner.kb;

import java.io.Serializable;

public class OccurenceFrequency implements Comparable<OccurenceFrequency>, Serializable {

	private static final long serialVersionUID = 6584720124641947108L;

	private int nrOfOccurrences;
	private int frequency;

	public OccurenceFrequency(int nrOfOccurrences, int frequency) {
		super();
		this.nrOfOccurrences = nrOfOccurrences;
		this.frequency = frequency;
	}

	public int getNrOfOccurrences() {
		return nrOfOccurrences;
	}

	public void setNrOfOccurrences(int nrOfOccurrences) {
		this.nrOfOccurrences = nrOfOccurrences;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	@Override
	public String toString() {
		return getNrOfOccurrences() + ":" + getFrequency();
	}

	@Override
	public int compareTo(OccurenceFrequency o) {
		if(this.getNrOfOccurrences() < o.getNrOfOccurrences())
			return -1;
		if(this.getNrOfOccurrences() > o.getNrOfOccurrences())
			return 1;
		return 0;
	}
}