package model.data;

import java.util.HashMap;

import miner.log.DataEvent;
import util.xml.AtomicNode;

public class BooleanDataAttribute extends DataAttribute {

	private static final long serialVersionUID = -2471192860058293511L;

	private boolean value;

	public BooleanDataAttribute(String name, boolean value, DataRecord parent) {
		super(name, parent);
		this.value = value;
	}

	public boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (value ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BooleanDataAttribute other = (BooleanDataAttribute) obj;
		if (value != other.value)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getName() + " = " + value;
	}

	@Override
	public AtomicNode getXesNode() {
		HashMap<String, String> attrs = new HashMap<>();
		attrs.put("key", "\""+getName()+"\"");
		attrs.put("value", "\""+getValue()+"\"");
		return new AtomicNode("boolean", attrs);
	}

	public static DataEvent getEvent(AtomicNode node, long time) {
		String name = node.getAttributes().get("key").replace("\"", "");
//		String parentRecordName = null;//TODO: need parentRecord?
		if(name.contains("->")) {
//			parentRecordName = name.substring(0, name.lastIndexOf("->"));
			name = name.substring(name.lastIndexOf("->")+2);
		}
		boolean value = Boolean.parseBoolean(node.getAttributes().get("value").replace("\"", ""));
		String tmp = node.getAttributes().get("isActivated");
		boolean isActivated;
		if(tmp == null)
			isActivated = true;
		else
			isActivated = Boolean.parseBoolean(tmp.replace("\"", ""));
		return new DataEvent(new BooleanDataAttribute(name, value, null), isActivated, time);
	}
}