package miner.kb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import miner.Config;
import miner.log.ActivityEvent;
import miner.log.DataEvent;
import miner.log.Log;
import miner.log.ResourceEvent;
import miner.log.Trace;
import model.Activity;
import model.data.DataAttribute;
import model.resource.Resource;

//TODO: really useful? More efficiently extracts the info needed, but uses more memory.
//	==> allow releasing info when not used anymore!
public class KnowledgeBase implements Serializable {

	private static final long serialVersionUID = 8431787356409049561L;

	//TODO: more stored statistics?
	private final int nrOfTraces;
	private final HashSet<Activity> activities;
	private final HashSet<Resource> resources;
	private final ArrayList<DataAttribute> dataElements;
	private final HashSet<DataAttribute> dataElementsWithAllUsedValues;
	private final HashMap<String, EventOccurrenceFrequencies> eofs;
	private final HashSet<Activity> wasFirst;
	private final HashSet<Activity> wasLast;
	private final HashMap<Activity, HashSet<Activity>> wasBefore;//just an indication, simultaneous acts will not be interpreted correctly
	private final HashMap<Activity, HashSet<Activity>> wasAfter;//just an indication, simultaneous acts will not be interpreted correctly

	public KnowledgeBase(Log log) {
		this.nrOfTraces = log.size();
		this.activities = new HashSet<>();
		this.resources = new HashSet<>();
		this.dataElements = new ArrayList<>();
		this.dataElementsWithAllUsedValues = new HashSet<>();
		if(Config.MINE_ATLEAST || Config.MINE_ATMOST || Config.MINE_RELATION)
			this.eofs = new HashMap<>();
		else
			this.eofs = null;
		if(Config.MINE_FIRST)
			this.wasFirst = new HashSet<>();
		else
			this.wasFirst = null;
		if(Config.MINE_LAST)
			this.wasLast = new HashSet<>();
		else
			this.wasLast = null;
		if(Config.MINE_RELATION) {
			this.wasBefore = new HashMap<>();
			this.wasAfter = new HashMap<>();
		} else {
			this.wasBefore = null;
			this.wasAfter = null;
		}
		HashSet<String> names = new HashSet<>();
		for(Trace t : log) {
			HashSet<String> aes = new HashSet<>();
			for(int i = 0; i < t.getActivityEvents().size(); i++) {
				ActivityEvent ae = t.getActivityEvents().get(i);
				aes.add(ae.getName());
				if(!activities.contains(ae)) {
					activities.add(new Activity(ae.getName()));//activities
					if(Config.MINE_RELATION) {
						wasBefore.put(ae, new HashSet<>());
						wasAfter.put(ae, new HashSet<>());
					}
				}
				if(Config.MINE_FIRST
						&& ae.getStart() == 0)
					wasFirst.add(ae);//first
				if(Config.MINE_RELATION) {
					if(i > 0)
						wasBefore.get(ae).addAll(t.getActivityEvents().subList(0, i));
					if(i < t.getActivityEvents().size()-1)
						wasAfter.get(ae).addAll(t.getActivityEvents().subList(i+1, t.getActivityEvents().size()));
				}
			}
			if(Config.MINE_FIRST)
				for(int i = t.getActivityEvents().size()-1; i >= 0; i--) {
					if(t.getActivityEvents().get(i).getEnd()
							== t.getActivityEvents().get(t.getActivityEvents().size()-1).getEnd())
						wasLast.add(t.getActivityEvents().get(i));//last
					else
						break;
				}
			for(ResourceEvent re : t.getResourceEvents())//resources
				resources.add(re.getResource());
			for(DataEvent de : t.getDataEvents()) {//dataElements
				dataElementsWithAllUsedValues.add(de.getDataElement());
				if(names.add(de.getDataElement().getName()))
					dataElements.add(de.getDataElement());
			}
			if(Config.MINE_ATLEAST || Config.MINE_ATMOST || Config.MINE_RELATION)
				for(String n : aes) {//eofs
					int count = 0;
					for(ActivityEvent ae : t.getActivityEvents())
						if(ae.getName().equals(n))
							count++;
					if(eofs.containsKey(n))
						eofs.get(n).addOccurrenceCount(count);
					else {
						EventOccurrenceFrequencies eof = new EventOccurrenceFrequencies();
						eof.addOccurrenceCount(count);
						eofs.put(n, eof);
					}
				}
		}
		if(Config.MINE_ATLEAST || Config.MINE_ATMOST || Config.MINE_RELATION)
			for(EventOccurrenceFrequencies eof : eofs.values()) {
				int count = 0;
				for(int v : eof.getOccurrenceFrequencies().values())
					count += v;
				if(nrOfTraces > count)
					eof.getOccurrenceFrequencies().put(0, nrOfTraces - count);
			}
	}

	public int getNrOfTraces() {
		return nrOfTraces;
	}

	public HashSet<Activity> getActivities() {
		return activities;
	}

	public HashSet<Resource> getResources() {
		return resources;
	}

	public ArrayList<DataAttribute> getDataElements() {
		return dataElements;
	}

	public HashSet<DataAttribute> getDataElementsWithAllUsedValues() {
		return dataElementsWithAllUsedValues;
	}

	public HashMap<String, EventOccurrenceFrequencies> getEventOccurrenceFrequencies() {
		return eofs;
	}

	public EventOccurrenceFrequencies getEventOccurrenceFrequencies(String eventName) {
		return eofs.get(eventName);
	}

	public HashSet<Activity> getWasFirst() {
		return wasFirst;
	}

	public HashSet<Activity> getWasLast() {
		return wasLast;
	}

	public HashMap<Activity, HashSet<Activity>> getWasBefore() {
		return wasBefore;
	}

	public HashMap<Activity, HashSet<Activity>> getWasAfter() {
		return wasAfter;
	}
}