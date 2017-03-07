package util.xml;

import java.util.HashMap;

public class VersionNode extends AttributeNode {

	public static final String DELIMITER_START = "<?xml";
	public static final String DELIMITER_END = "?>";

	public VersionNode(String text, HashMap<String, String> attributes) {
		super(text, attributes);
	}

	public static VersionNode parse(String input) {
		if(!input.startsWith(DELIMITER_START)
				|| !input.endsWith(DELIMITER_END))
			throw new IllegalArgumentException("Not a VersionNode: " + input);
		String line = input.substring(DELIMITER_START.length(), input.length()-DELIMITER_END.length()).trim();
		return new VersionNode("", AttributeNode.parseAttributes(line));
	}

	@Override
	public String toString() {
		return DELIMITER_START + toString(getAttributes()) + " " + DELIMITER_END;
	}
}