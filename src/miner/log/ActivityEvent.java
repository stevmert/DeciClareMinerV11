package miner.log;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

import miner.Config;
import model.Activity;
import util.StringManager;
import util.xml.AtomicNode;
import util.xml.Node;
import util.xml.ParentNode;

/**
 * time in seconds relative to trace start
 */
public class ActivityEvent extends Activity implements Comparable<ActivityEvent>, Serializable {

	private static final long serialVersionUID = -7835733201407894206L;

	private long start;
	private long end;

	public ActivityEvent(String name, long start, long end) {
		super(name);
		if(name == null
				|| start < 0
				|| end < 0
				|| start > end)
			throw new IllegalArgumentException("Invalid TraceActivity: "
					+ name + " " + start + " " + end);
		this.start = start;
		this.end = end;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public int compareTo(ActivityEvent other) {
		if(this.getStart() < other.getStart())
			return -1;
		if(this.getStart() > other.getStart())
			return 1;
		if(this.getEnd() < other.getEnd())
			return -1;
		if(this.getEnd() > other.getEnd())
			return 1;
		return 0;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + start + "," + end + ")";
	}

	public ParentNode getXesNode(ArrayList<DataEvent> dataevents,
			ArrayList<ResourceEvent> resourceevents) {
		ParentNode eventNode = new ParentNode("event", new HashMap<String, String>(),
				new ArrayList<Node>());
		{
			HashMap<String, String> attrs = new HashMap<>();
			attrs.put("key", "\"concept:name\"");
			attrs.put("value", "\""+getName()+"\"");
			eventNode.getChildNodes().add(new AtomicNode("string", attrs));
		}
		{
			HashMap<String, String> attrs = new HashMap<>();
			attrs.put("key", "\"time:timestamp\"");
			LocalDateTime time = Config.BASE_DATETIME.plusMinutes(getEnd());
			attrs.put("value", "\""+time+"\"");
			eventNode.getChildNodes().add(new AtomicNode("date", attrs));
		}
		for(int i = 0; i < dataevents.size(); i++) {
			DataEvent de = dataevents.get(i);
			if(de.getTime() > getStart()
					&& de.getTime() <= getEnd())
				eventNode.getChildNodes().add(de.getXesNode());
			else if(de.getTime() > getEnd())
				break;
		}
		for(int i = 0; i < resourceevents.size(); i++) {
			ResourceEvent re = resourceevents.get(i);
			if(re.getTime() > getStart()
					&& re.getTime() <= getEnd()) {
				eventNode.getChildNodes().add(re.getXesNode());
				break;//add resources, but can't be multivalued so only one added!
			} else if(re.getTime() > getEnd())
				break;
		}
		return eventNode;
	}

	public ParentNode getXesNode_extended(ArrayList<DataEvent> dataevents,
			ArrayList<ResourceEvent> resourceevents) {
		ParentNode eventNode = new ParentNode("event", new HashMap<String, String>(),
				new ArrayList<Node>());
		{
			HashMap<String, String> attrs = new HashMap<>();
			attrs.put("key", "\"concept:name\"");
			attrs.put("value", "\""+getName()+"\"");
			eventNode.getChildNodes().add(new AtomicNode("string", attrs));
		}
		{
			HashMap<String, String> attrs = new HashMap<>();
			attrs.put("key", "\"deci:timestamp_start\"");
			LocalDateTime time = Config.BASE_DATETIME.plusMinutes(getStart());
			attrs.put("value", "\""+time+"\"");
			eventNode.getChildNodes().add(new AtomicNode("date", attrs));
		}
		{
			HashMap<String, String> attrs = new HashMap<>();
			attrs.put("key", "\"deci:timestamp_end\"");
			LocalDateTime time = Config.BASE_DATETIME.plusMinutes(getEnd());
			attrs.put("value", "\""+time+"\"");
			eventNode.getChildNodes().add(new AtomicNode("date", attrs));
		}
		for(int i = 0; i < dataevents.size(); i++) {
			DataEvent de = dataevents.get(i);
			if(de.getTime() > getStart()
					&& de.getTime() <= getEnd())
				eventNode.getChildNodes().add(de.getXesNode());
			else if(de.getTime() > getEnd())
				break;
		}
		int count = 1;
		for(int i = 0; i < resourceevents.size(); i++) {
			ResourceEvent re = resourceevents.get(i);
			if(re.getTime() > getStart()
					&& re.getTime() <= getEnd()) {
				eventNode.getChildNodes().add(re.getXesNode_extended(count));
				count++;
			} else if(re.getTime() > getEnd())
				break;
		}
		return eventNode;
	}

	public ParentNode getXesNode_extended2() {
		ParentNode eventNode = new ParentNode("event", new HashMap<String, String>(),
				new ArrayList<Node>());
		{
			HashMap<String, String> attrs = new HashMap<>();
			attrs.put("key", "\"deci2:eventtype\"");
			attrs.put("value", "\"activity\"");
			eventNode.getChildNodes().add(new AtomicNode("string", attrs));
		}
		{
			HashMap<String, String> attrs = new HashMap<>();
			attrs.put("key", "\"concept:name\"");
			attrs.put("value", "\""+getName()+"\"");
			eventNode.getChildNodes().add(new AtomicNode("string", attrs));
		}
		{
			HashMap<String, String> attrs = new HashMap<>();
			attrs.put("key", "\"deci2:timestamp_start\"");
			LocalDateTime time = Config.BASE_DATETIME.plusMinutes(getStart());
			attrs.put("value", "\""+time+"\"");
			eventNode.getChildNodes().add(new AtomicNode("date", attrs));
		}
		{
			HashMap<String, String> attrs = new HashMap<>();
			attrs.put("key", "\"deci2:timestamp_end\"");
			LocalDateTime time = Config.BASE_DATETIME.plusMinutes(getEnd());
			attrs.put("value", "\""+time+"\"");
			eventNode.getChildNodes().add(new AtomicNode("date", attrs));
		}
		return eventNode;
	}

	public static ActivityEvent getEvent_XES(ParentNode node, LocalDateTime base_time) {
		String name = StringManager.respace(node.getValueFromNodeKey("concept:name")).trim();
		//end round up
		String dateTimeString = node.getValueFromNodeKey("time:timestamp");
		if(dateTimeString.contains("."))
			dateTimeString = dateTimeString.substring(0, dateTimeString.indexOf("."));
		LocalDateTime ldt_end = LocalDateTime.parse(dateTimeString);
		long end = (long) Math.ceil(((double) base_time.until(ldt_end, ChronoUnit.SECONDS))/60);
		//start round down
		LocalDateTime ldt_start = ldt_end.minusMinutes(1);
		long start = base_time.until(ldt_start, ChronoUnit.MINUTES);
		return new ActivityEvent(name, start, end);
	}

	public static ActivityEvent getEvent_XESExtended(ParentNode node, LocalDateTime base_time) {
		String name = node.getValueFromNodeKey("concept:name");
		//start round down
		String dateTimeString1 = node.getValueFromNodeKey("deci:timestamp_start");
		if(dateTimeString1.contains("."))
			dateTimeString1 = dateTimeString1.substring(0, dateTimeString1.indexOf("."));
		LocalDateTime ldt_start = LocalDateTime.parse(dateTimeString1);
		long start = base_time.until(ldt_start, ChronoUnit.MINUTES);
		//end round up
		String dateTimeString2 = node.getValueFromNodeKey("deci:timestamp_end");
		if(dateTimeString2.contains("."))
			dateTimeString2 = dateTimeString2.substring(0, dateTimeString2.indexOf("."));
		LocalDateTime ldt_end = LocalDateTime.parse(dateTimeString2);
		long end = (long) Math.ceil(((double) base_time.until(ldt_end, ChronoUnit.SECONDS))/60);
		return new ActivityEvent(name, start, end);
	}

	public static ActivityEvent getEvent_XESExtended2(ParentNode node, LocalDateTime base_time) {
		String name = node.getValueFromNodeKey("concept:name");
		//start round down
		String dateTimeString1 = node.getValueFromNodeKey("deci2:timestamp_start");
		if(dateTimeString1.contains("."))
			dateTimeString1 = dateTimeString1.substring(0, dateTimeString1.indexOf("."));
		LocalDateTime ldt_start = LocalDateTime.parse(dateTimeString1);
		long start = base_time.until(ldt_start, ChronoUnit.MINUTES);
		//end round up
		String dateTimeString2 = node.getValueFromNodeKey("deci2:timestamp_end");
		if(dateTimeString2.contains("."))
			dateTimeString2 = dateTimeString2.substring(0, dateTimeString2.indexOf("."));
		LocalDateTime ldt_end = LocalDateTime.parse(dateTimeString2);
		long end = (long) Math.ceil(((double) base_time.until(ldt_end, ChronoUnit.SECONDS))/60);
		return new ActivityEvent(name, start, end);
	}
}