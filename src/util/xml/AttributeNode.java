package util.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public abstract class AttributeNode extends Node {

	private HashMap<String, String> attributes;

	public AttributeNode(String text, HashMap<String, String> attributes) {
		super(text);
		this.attributes = attributes;
	}

	public HashMap<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(HashMap<String, String> attributes) {
		this.attributes = attributes;
	}

	protected static HashMap<String, String> parseAttributes(String input) {
		String[] tmp = input.trim().split("=");
		return parseAttributes(tmp);
	}

	protected static HashMap<String, String> parseAttributes(String[] input) {
		HashMap<String, String> attributes = new HashMap<>();
		for(int i = 1; i < input.length; i++) {
			String attrName = input[i-1];
			String attrValue;
			if(i+1 == input.length)
				attrValue = input[i];
			else {
				attrValue = input[i].substring(0, input[i].lastIndexOf(" "));
				input[i] = input[i].substring(attrValue.length()+1);
			}
			attributes.put(attrName, attrValue);
		}
		return attributes;
	}

	private static final AttributeOrder ATTRIBUTE_ORDER = new AttributeOrder();
	public static String toString(HashMap<String, String> attrs) {
		if(attrs.isEmpty())
			return "";
		String result = "";
		ArrayList<String> keys = new ArrayList<>(attrs.keySet());
		Collections.sort(keys, ATTRIBUTE_ORDER);
		for(String key : keys)
			result += " " + key + "=" + attrs.get(key);
		return result;
	}
}