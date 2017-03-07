package miner.log;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import miner.Config;
import model.data.BooleanDataAttribute;
import model.data.CategoricalDataAttribute;
import model.data.DataAttribute;
import util.xml.AtomicNode;
import util.xml.Node;
import util.xml.ParentNode;

public class DataEvent implements Comparable<DataEvent>, Serializable {

	private static final long serialVersionUID = -5686671569572138789L;
	
	private DataAttribute dataElement;
	private boolean isActivated;
	private long time;

	public DataEvent(DataAttribute dataElement, boolean isActivated, long time) {
		super();
		this.dataElement = dataElement;
		this.isActivated = isActivated;
		this.time = time;
	}

	public DataAttribute getDataElement() {
		return dataElement;
	}

	public void setDataElement(DataAttribute dataElement) {
		this.dataElement = dataElement;
	}

	public boolean isActivated() {
		return isActivated;
	}

	public void setActivated(boolean isActivated) {
		this.isActivated = isActivated;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataElement == null) ? 0 : dataElement.hashCode());
		result = prime * result + (isActivated ? 1231 : 1237);
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
		DataEvent other = (DataEvent) obj;
		if (dataElement == null) {
			if (other.dataElement != null)
				return false;
		} else if (!dataElement.equals(other.dataElement))
			return false;
		if (isActivated != other.isActivated)
			return false;
		if (time != other.time)
			return false;
		return true;
	}

	@Override
	public int compareTo(DataEvent other) {
		if(this.getTime() < other.getTime())
			return -1;
		if(this.getTime() > other.getTime())
			return 1;
		if(!this.isActivated() && other.isActivated())
			return -1;
		if(!other.isActivated() && this.isActivated())
			return 1;
		return 0;
	}

	@Override
	public String toString() {
		return time + ": " + dataElement + " (" + (isActivated()?"+":"-") + ")";
	}

	public AtomicNode getXesNode() {
		AtomicNode an = getDataElement().getXesNode();
		//		LocalDateTime time = Config.BASE_DATETIME.plusMinutes(getTime());
		//		an.getAttributes().put("time:timestamp", "\""+time+"\"");
		//		if(getDataElement() instanceof CategoricalDataElement)

		//TODO
		//		an.getAttributes().put("isActivated", "\""+isActivated()+"\"");
		return an;
	}

	public ParentNode getXesNode_extended2() {
		ParentNode eventNode = new ParentNode("event", new HashMap<String, String>(),
				new ArrayList<Node>());
		{
			HashMap<String, String> attrs = new HashMap<>();
			attrs.put("key", "\"deci2:eventtype\"");
			attrs.put("value", "\"data\"");
			eventNode.getChildNodes().add(new AtomicNode("string", attrs));
		}
		eventNode.getChildNodes().add(getXesNode());
		{
			HashMap<String, String> attrs = new HashMap<>();
			attrs.put("key", "\"deci2:timestamp_end\"");
			LocalDateTime time = Config.BASE_DATETIME.plusMinutes(getTime());
			attrs.put("value", "\""+time+"\"");
			eventNode.getChildNodes().add(new AtomicNode("date", attrs));
		}
		return eventNode;
	}

	public static DataEvent getEvent(AtomicNode node, long time,
			HashMap<String, HashSet<String>> valuesSets) {
		if(node.getText().equals("boolean"))
			return BooleanDataAttribute.getEvent(node, time);
		else
			return CategoricalDataAttribute.getEvent(node, time, valuesSets);
	}

	public static DataEvent getEvent(ParentNode node, LocalDateTime base_time,
			HashMap<String, HashSet<String>> valuesSets) {
		String dateTimeString = node.getValueFromNodeKey("deci2:timestamp_end");
		if(dateTimeString.contains("."))
			dateTimeString = dateTimeString.substring(0, dateTimeString.indexOf("."));
		LocalDateTime ldt_end = LocalDateTime.parse(dateTimeString);
		long time = base_time.until(ldt_end, ChronoUnit.MINUTES);
		for(Node n : node.getChildNodes()) {
			AtomicNode an = (AtomicNode) n;
			if(!an.getAttributes().get("key").equals("\"deci2:eventtype\"")
					&& !an.getAttributes().get("key").equals("\"deci2:timestamp_end\""))
				return getEvent(an, time, valuesSets);
		}
		throw new IllegalArgumentException();
	}
}