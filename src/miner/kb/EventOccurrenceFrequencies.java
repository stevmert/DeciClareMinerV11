package miner.kb;

import java.io.Serializable;
import java.util.HashMap;

public class EventOccurrenceFrequencies implements Serializable {

	private static final long serialVersionUID = -7805868997716838223L;

	private HashMap<Integer, Integer> ofs;

	public EventOccurrenceFrequencies() {
		this(new HashMap<Integer, Integer>());
	}

	public EventOccurrenceFrequencies(HashMap<Integer, Integer> ofs) {
		this.ofs = ofs;
	}

	public HashMap<Integer, Integer> getOccurrenceFrequencies() {
		return ofs;
	}

	public void setOccurrenceFrequencies(HashMap<Integer, Integer> ofs) {
		this.ofs = ofs;
	}

	public void addOccurrenceCount(int count) {
		if(ofs.get(count) == null)
			ofs.put(count, 1);
		else
			ofs.put(count, ofs.get(count)+1);
	}

	@Override
	public String toString() {
		return ""+getOccurrenceFrequencies();
	}
}