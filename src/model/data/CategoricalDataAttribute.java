package model.data;

import java.util.HashMap;
import java.util.HashSet;

import miner.log.DataEvent;
import util.xml.AtomicNode;

public class CategoricalDataAttribute extends DataAttribute {

	private static final long serialVersionUID = 4790843289629514229L;

	private final HashSet<String> values;
	private String value;

	public CategoricalDataAttribute(String name, HashSet<String> values, String value, DataRecord parent) {
		super(name, parent);
		if(values == null
				|| value == null
				|| values.isEmpty()
				|| !values.contains(value))
			throw new IllegalArgumentException();
		this.values = values;
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		if(value == null
				|| !values.contains(value))
			throw new IllegalArgumentException();
		this.value = value;
	}

	public HashSet<String> getValues() {
		return values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
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
		CategoricalDataAttribute other = (CategoricalDataAttribute) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
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
		return new AtomicNode("string", attrs);
	}

	public static DataEvent getEvent(AtomicNode node, long time,
			HashMap<String, HashSet<String>> valuesSets) {
		String name = node.getAttributes().get("key").replace("\"", "");
		String value = node.getAttributes().get("value").replace("\"", "");
		String isAct = node.getAttributes().get("isActivated");
		boolean isActivated = true;
		if(isAct != null)
			isActivated = Boolean.parseBoolean(isAct.replace("\"", ""));
		HashSet<String> values = valuesSets.get(name);
		if(values == null) {
			values = new HashSet<>();
			values.add(value);
			valuesSets.put(name, values);
		} else
			values.add(value);
		return new DataEvent(new CategoricalDataAttribute(name, values, value, null), isActivated, time);
		//TODO: need parentRecord?
	}
}