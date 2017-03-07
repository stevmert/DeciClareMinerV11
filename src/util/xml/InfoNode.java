package util.xml;

public class InfoNode extends Node {

	public static final String DELIMITER_START = "<!-- ";
	public static final String DELIMITER_END = " -->";

	public InfoNode(String text) {
		super(text);
	}

	public static InfoNode parse(String input) {
		if(!input.startsWith(DELIMITER_START)
				|| !input.endsWith(DELIMITER_END))
			throw new IllegalArgumentException("Not a InfoNode: " + input);
		String line = input.substring(DELIMITER_START.length(), input.length()-DELIMITER_END.length()).trim();
		return new InfoNode(line);
	}

	@Override
	public String toString() {
		return DELIMITER_START + getText() + DELIMITER_END;
	}
}