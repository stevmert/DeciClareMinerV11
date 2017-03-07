package miner.log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import model.data.BooleanDataAttribute;
import model.data.DataAttribute;
import model.data.Decision;
import model.data.DecisionRule;
import util.xml.Node;
import util.xml.ParentNode;

public class Trace implements Serializable {

	private static final long serialVersionUID = 3045580127162156661L;

	private ArrayList<ActivityEvent> activityEvents;
	private ArrayList<DataEvent> dataEvents;
	private ArrayList<ResourceEvent> resourceEvents;

	public Trace(ArrayList<ActivityEvent> activityEvents, ArrayList<DataEvent> dataEvents,
			ArrayList<ResourceEvent> resourceEvents) {
		super();
		if(activityEvents == null
				|| dataEvents == null
				|| activityEvents.isEmpty())
			throw new IllegalArgumentException();
		this.activityEvents = activityEvents;
		this.resourceEvents = resourceEvents;
		this.dataEvents = dataEvents;
	}

	public Trace(ArrayList<ActivityEvent> activityEvents) {
		this(activityEvents, new ArrayList<DataEvent>(), new ArrayList<ResourceEvent>());
	}

	public ArrayList<ActivityEvent> getActivityEvents() {
		return activityEvents;
	}

	public void setActivityEvents(ArrayList<ActivityEvent> activityEvents) {
		this.activityEvents = activityEvents;
	}

	public List<ActivityEvent> getRemainingActivityList(long activationTime) {
		if(activationTime == 0)
			return getActivityEvents();
		ArrayList<ActivityEvent> result = new ArrayList<>();
		for(ActivityEvent ta : getActivityEvents())
			if(ta.getEnd() >= activationTime) //if(ta.getStart() >= activationTime)
				result.add(ta);
		return result;
	}

	public ArrayList<DataEvent> getDataEvents() {
		return dataEvents;
	}

	public void setDataEvents(ArrayList<DataEvent> dataEvents) {
		this.dataEvents = dataEvents;
	}

	public ArrayList<ResourceEvent> getResourceEvents() {
		return resourceEvents;
	}

	public void setResourceEvents(ArrayList<ResourceEvent> resourceEvents) {
		this.resourceEvents = resourceEvents;
	}

	public DecisionActivation getDecisionActivation(Decision dec) {
		HashSet<DataAttribute> currentSituation = new HashSet<DataAttribute>();
		for(DataEvent de : getDataEvents()) {
			if(de.isActivated()) {
				if(!currentSituation.contains(de.getDataElement())) {
					if(de.getDataElement() instanceof BooleanDataAttribute) {
						DataAttribute toDelete = null;
						for(DataAttribute x : currentSituation)
							if(x instanceof BooleanDataAttribute
									&& x.getName().equals(de.getDataElement().getName()))
								toDelete = x;
						if(toDelete != null)
							currentSituation.remove(toDelete);
					}
					currentSituation.add(de.getDataElement());
					for(DecisionRule dr : dec.getRules())
						if(currentSituation.containsAll(dr.getDataValues()))
							return new DecisionActivation(dr, de.getTime());
				}
			} else
				currentSituation.remove(de.getDataElement());
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activityEvents == null) ? 0 : activityEvents.hashCode());
		result = prime * result + ((dataEvents == null) ? 0 : dataEvents.hashCode());
		result = prime * result + ((resourceEvents == null) ? 0 : resourceEvents.hashCode());
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
		Trace other = (Trace) obj;
		if (activityEvents == null) {
			if (other.activityEvents != null)
				return false;
		} else if (!activityEvents.equals(other.activityEvents))
			return false;
		if (dataEvents == null) {
			if (other.dataEvents != null)
				return false;
		} else if (!dataEvents.equals(other.dataEvents))
			return false;
		if (resourceEvents == null) {
			if (other.resourceEvents != null)
				return false;
		} else if (!resourceEvents.equals(other.resourceEvents))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "<" + getActivityEvents().toString() + " && " + getDataEvents().toString()
				+ " && " + getResourceEvents().toString() + ">";
	}

	public ParentNode getXesNode() {
		ParentNode newNode = new ParentNode("trace", new HashMap<String, String>(),
				new ArrayList<Node>());
		for(DataEvent de : getDataEvents()) {
			if(de.getTime() == 0)
				newNode.getChildNodes().add(de.getXesNode());
			else
				break;
		}
		for(ActivityEvent ae : getActivityEvents())
			newNode.getChildNodes().add(ae.getXesNode(getDataEvents(), getResourceEvents()));
		return newNode;
	}

	public ParentNode getXesNode_extended() {
		ParentNode newNode = new ParentNode("trace", new HashMap<String, String>(),
				new ArrayList<Node>());
		for(DataEvent de : getDataEvents()) {
			if(de.getTime() == 0)
				newNode.getChildNodes().add(de.getXesNode());
			else
				break;
		}
		for(ActivityEvent ae : getActivityEvents())
			newNode.getChildNodes().add(ae.getXesNode_extended(getDataEvents(), getResourceEvents()));
		return newNode;
	}

	public ParentNode getXesNode_extended2() {
		ParentNode newNode = new ParentNode("trace", new HashMap<String, String>(),
				new ArrayList<Node>());
		for(DataEvent de : getDataEvents()) {
			if(de.getTime() == 0)
				newNode.getChildNodes().add(de.getXesNode());
			else
				break;
		}
		for(ActivityEvent ae : getActivityEvents())
			newNode.getChildNodes().add(ae.getXesNode_extended2());
		for(DataEvent de : getDataEvents())
			if(de.getTime() > 0)
				newNode.getChildNodes().add(de.getXesNode_extended2());
		for(ResourceEvent re : getResourceEvents())
			newNode.getChildNodes().add(re.getXesNode_extended2());
		return newNode;
	}
}