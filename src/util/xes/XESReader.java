package util.xes;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import miner.log.ActivityEvent;
import miner.log.DataEvent;
import miner.log.Log;
import miner.log.ResourceEvent;
import miner.log.Trace;
import util.xml.AtomicNode;
import util.xml.Node;
import util.xml.ParentNode;
import util.xml.XML;

public class XESReader {

	public static Log getLog(File f) throws Exception {
		XML xml = XML.parse(f);
		return getLog(f.getName(), xml);
	}

	public static Log getLog(String name, XML xml) {
		ParentNode logNode = (ParentNode) xml.getNode("log");
		for(Node n : logNode.getNodeSet("extension")) {
			AtomicNode ext = (AtomicNode) n;
			if(ext.getAttributes().get("prefix").equals("\"deci\""))
				return getLog_XESExtended(name, logNode.getNodeSet("trace"));
			if(ext.getAttributes().get("prefix").equals("\"deci2\""))
				return getLog_XESExtended2(name, logNode.getNodeSet("trace"));
		}
		return getLog_XES(name, logNode.getNodeSet("trace"));
	}

	private static Log getLog_XES(String name, HashSet<Node> traceNodes) {
		Log log = new Log(name);
		HashMap<String, HashSet<String>> valuesSetsCategoricalDataElements = new HashMap<>();
		for(Node traceNode : traceNodes) {
			ParentNode tn = (ParentNode) traceNode;
			LocalDateTime base = null;
			for(Node n : tn.getNodeSet("event")) {
				ParentNode en = (ParentNode) n;
				String dateTimeString = en.getValueFromNodeKey("time:timestamp");
				if(dateTimeString != null) {
					if(dateTimeString.contains("."))
						dateTimeString = dateTimeString.substring(0, dateTimeString.indexOf("."));
					LocalDateTime ldt = LocalDateTime.parse(dateTimeString);
					if(base == null || ldt.isBefore(base))
						base = ldt;
				}
			}
			if(base != null) {//non empty trace
				base = base.minusMinutes(1);
				ArrayList<ActivityEvent> actEvents = new ArrayList<>();
				ArrayList<DataEvent> dataEvents = new ArrayList<>();
				ArrayList<ResourceEvent> resourceEvents = new ArrayList<>();
				for(Node n : tn.getChildNodes()) {
					if(n instanceof AtomicNode)
						dataEvents.add(DataEvent.getEvent((AtomicNode) n, 0,
								valuesSetsCategoricalDataElements));
					else {
						ActivityEvent actEvent = ActivityEvent.getEvent_XES((ParentNode) n, base);
						actEvents.add(actEvent);
						for(Node c : ((ParentNode) n).getChildNodes()) {
							AtomicNode an = (AtomicNode) c;
							if(!an.getAttributes().get("key").equals("\"concept:name\"")
									&& !an.getAttributes().get("key").equals("\"time:timestamp\"")) {
								if(an.getAttributes().get("key").equals("\"org:role\"")
										|| an.getAttributes().get("key").equals("\"org:resource\""))//resource
									resourceEvents.add(ResourceEvent.getEvent_XES((ParentNode) n, actEvent.getEnd()));
								else//data
									dataEvents.add(DataEvent.getEvent(an, actEvent.getEnd(),
											valuesSetsCategoricalDataElements));
							}
						}
					}
				}
				Collections.sort(actEvents);
				Collections.sort(dataEvents);
				Collections.sort(resourceEvents);
				log.add(new Trace(actEvents, dataEvents, resourceEvents));
			}
		}
		return log;
	}

	private static Log getLog_XESExtended(String name, HashSet<Node> traceNodes) {
		Log log = new Log(name);
		HashMap<String, HashSet<String>> valuesSetsCategoricalDataElements = new HashMap<>();
		for(Node traceNode : traceNodes) {
			ParentNode tn = (ParentNode) traceNode;
			LocalDateTime base = null;
			for(Node n : tn.getNodeSet("event")) {
				ParentNode en = (ParentNode) n;
				String dateTimeString = en.getValueFromNodeKey("deci:timestamp_start");
				if(dateTimeString != null) {
					if(dateTimeString.contains("."))
						dateTimeString = dateTimeString.substring(0, dateTimeString.indexOf("."));
					LocalDateTime ldt = LocalDateTime.parse(dateTimeString);
					if(base == null || ldt.isBefore(base))
						base = ldt;
				}
			}
			if(base != null) {//non empty trace
				ArrayList<ActivityEvent> actEvents = new ArrayList<>();
				ArrayList<DataEvent> dataEvents = new ArrayList<>();
				ArrayList<ResourceEvent> resourceEvents = new ArrayList<>();
				for(Node n : tn.getChildNodes()) {
					if(n instanceof AtomicNode)
						dataEvents.add(DataEvent.getEvent((AtomicNode) n, 0,
								valuesSetsCategoricalDataElements));
					else {
						ActivityEvent actEvent = ActivityEvent.getEvent_XESExtended((ParentNode) n, base);
						actEvents.add(actEvent);
						for(Node c : ((ParentNode) n).getChildNodes()) {
							AtomicNode an = (AtomicNode) c;
							if(!an.getAttributes().get("key").equals("\"concept:name\"")
									&& !an.getAttributes().get("key").equals("\"deci:timestamp_start\"")
									&& !an.getAttributes().get("key").equals("\"deci:timestamp_end\"")) {
								if(an.getAttributes().get("key").startsWith("\"deci:role")
										|| an.getAttributes().get("key").startsWith("\"deci:resource"))//resource
									resourceEvents.add(ResourceEvent.getEvent_XESExtended(an, actEvent.getEnd()));
								else//data
									dataEvents.add(DataEvent.getEvent(an, actEvent.getEnd(),
											valuesSetsCategoricalDataElements));
							}
						}
					}
				}
				Collections.sort(actEvents);
				Collections.sort(dataEvents);
				Collections.sort(resourceEvents);
				log.add(new Trace(actEvents, dataEvents, resourceEvents));
			}
		}
		return log;
	}

	private static Log getLog_XESExtended2(String name, HashSet<Node> traceNodes) {
		Log log = new Log(name);
		HashMap<String, HashSet<String>> valuesSetsCategoricalDataElements = new HashMap<>();
		for(Node traceNode : traceNodes) {
			ParentNode tn = (ParentNode) traceNode;
			LocalDateTime base = null;
			for(Node n : tn.getNodeSet("event")) {
				ParentNode en = (ParentNode) n;
				String dateTimeString = en.getValueFromNodeKey("deci2:timestamp_start");
				if(dateTimeString != null) {
					if(dateTimeString.contains("."))
						dateTimeString = dateTimeString.substring(0, dateTimeString.indexOf("."));
					LocalDateTime ldt = LocalDateTime.parse(dateTimeString);
					if(base == null || ldt.isBefore(base))
						base = ldt;
				}
			}
			if(base != null) {//non empty trace
				ArrayList<ActivityEvent> actEvents = new ArrayList<>();
				ArrayList<DataEvent> dataEvents = new ArrayList<>();
				ArrayList<ResourceEvent> resourceEvents = new ArrayList<>();
				for(Node n : tn.getChildNodes()) {
					if(n instanceof AtomicNode)
						dataEvents.add(DataEvent.getEvent((AtomicNode) n, 0,
								valuesSetsCategoricalDataElements));
					else {
						String type = ((ParentNode) n).getValueFromNodeKey("deci2:eventtype");
						if(type.equals("activity"))
							actEvents.add(ActivityEvent.getEvent_XESExtended2((ParentNode) n, base));
						else if(type.equals("data"))
							dataEvents.add(DataEvent.getEvent((ParentNode) n, base,
									valuesSetsCategoricalDataElements));
						else if(type.equals("resource"))
							resourceEvents.add(ResourceEvent.getEvent_XESExtended2((ParentNode) n, base));
					}
				}
				Collections.sort(actEvents);
				Collections.sort(dataEvents);
				Collections.sort(resourceEvents);
				log.add(new Trace(actEvents, dataEvents, resourceEvents));
			}
		}
		return log;
	}
}