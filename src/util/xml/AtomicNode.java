package util.xml;

import java.util.HashMap;

public class AtomicNode extends AttributeNode {

	public static final String DELIMITER_START = "<";
	public static final String DELIMITER_END = "/>";

	public AtomicNode(String text, HashMap<String, String> attributes) {
		super(text, attributes);
	}

	public static AtomicNode parse(String input) {
		if(!input.startsWith(DELIMITER_START)
				|| !input.endsWith(DELIMITER_END))
			throw new IllegalArgumentException("Not a AtomicNode: " + input);
		String line = input.substring(DELIMITER_START.length(), input.length()-DELIMITER_END.length()).trim();
		String[] tmp = line.split("=");
		String text = null;
		if(tmp.length > 1) {
			text = tmp[0].substring(0, tmp[0].indexOf(" "));
			tmp[0] = tmp[0].substring(text.length()).trim();
		} else {
			text = tmp[0];
			tmp[0] = "";
		}
		return new AtomicNode(text, AttributeNode.parseAttributes(tmp));
	}

	@Override
	public String toString() {
		return DELIMITER_START + getText() + toString(getAttributes()) + DELIMITER_END;
	}
}