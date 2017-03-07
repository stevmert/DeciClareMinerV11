package miner.log;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

import miner.Config;
import model.resource.DirectResource;
import model.resource.Resource;
import model.resource.ResourceRole;
import util.xml.AtomicNode;
import util.xml.Node;
import util.xml.ParentNode;

public class ResourceEvent implements Comparable<ResourceEvent>, Serializable {

	private static final long serialVersionUID = -2682207810555521960L;

	private Resource resource;
	private long time;

	public ResourceEvent(Resource resource, long time) {
		super();
		this.resource = resource;
		this.time = time;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
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
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
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
		ResourceEvent other = (ResourceEvent) obj;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		if (time != other.time)
			return false;
		return true;
	}

	@Override
	public int compareTo(ResourceEvent other) {
		if(this.getTime() < other.getTime())
			return -1;
		if(this.getTime() > other.getTime())
			return 1;
		return 0;
	}

	@Override
	public String toString() {
		return getTime() + ": " + getResource();
	}

	public Node getXesNode() {
		HashMap<String, String> attrs = new HashMap<>();
		if(getResource() instanceof DirectResource)
			attrs.put("key", "\"org:resource\"");
		else
			attrs.put("key", "\"org:role\"");
		attrs.put("value", "\""+getResource().getName()+"\"");
		//		LocalDateTime time = Config.BASE_DATETIME.plusMinutes(getTime());
		//		attrs.put("time:timestamp", "\""+time+"\"");
		return new AtomicNode("string", attrs);
	}

	public Node getXesNode_extended(int count) {
		HashMap<String, String> attrs = new HashMap<>();
		if(getResource() instanceof DirectResource)
			attrs.put("key", "\"deci:resource" + count + "\"");
		else
			attrs.put("key", "\"deci:role" + count + "\"");
		attrs.put("value", "\""+getResource().getName()+"\"");
		//		LocalDateTime time = Config.BASE_DATETIME.plusMinutes(getTime());
		//		attrs.put("time:timestamp", "\""+time+"\"");
		return new AtomicNode("string", attrs);
	}

	public ParentNode getXesNode_extended2() {
		ParentNode eventNode = new ParentNode("event", new HashMap<String, String>(),
				new ArrayList<Node>());
		{
			HashMap<String, String> attrs = new HashMap<>();
			attrs.put("key", "\"deci2:eventtype\"");
			attrs.put("value", "\"resource\"");
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

	public static ResourceEvent getEvent_XES(ParentNode node, long time) {
		String resourceName = node.getValueFromNodeKey("org:resource");//DirectResource
		Resource resource = null;
		if(resourceName != null)
			resource = new DirectResource(resourceName);
		else {
			resourceName = node.getValueFromNodeKey("org:role");
			//			//availability
			//			String avString = null;
			//			for(Node n : node.getChildNodes()) {
			//				AttributeNode an = (AttributeNode) n;
			//				if(an.getAttributes().get("availability") != null) {
			//					avString = an.getAttributes().get("availability").replace("\"", "");
			//					break;
			//				}
			//			}
			//			int availability = 1;//default availability
			//			if(avString != null)
			//				availability = Integer.parseInt(avString);
			resource = new ResourceRole(resourceName);//ResourceRole
		}
		return new ResourceEvent(resource, time);
	}

	public static ResourceEvent getEvent_XESExtended(AtomicNode node, long time) {
		Resource resource = null;
		if(node.getAttributes().get("key").startsWith("\"deci:resource"))//DirectResource
			resource = new DirectResource(node.getAttributes().get("value").replace("\"", ""));
		else {//ResourceRole
			//			String avString = node.getAttributes().get("availability");
			//			int availability = 1;//default availability
			//			if(avString != null)
			//				availability = Integer.parseInt(avString.replace("\"", ""));
			resource = new ResourceRole(node.getAttributes().get("value").replace("\"", ""));
		}
		return new ResourceEvent(resource, time);
	}

	public static ResourceEvent getEvent_XESExtended2(ParentNode node, LocalDateTime base_time) {
		String dateTimeString = node.getValueFromNodeKey("deci2:timestamp_end");
		if(dateTimeString.contains("."))
			dateTimeString = dateTimeString.substring(0, dateTimeString.indexOf("."));
		LocalDateTime ldt_end = LocalDateTime.parse(dateTimeString);
		long time = base_time.until(ldt_end, ChronoUnit.MINUTES);
		return getEvent_XES(node, time);
	}
}