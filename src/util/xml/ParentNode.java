package util.xml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ParentNode extends AttributeNode {

	public static final String DELIMITER_START_OPEN = "<";
	public static final String DELIMITER_START_CLOSE = "</";
	public static final String DELIMITER_END = ">";

	private ArrayList<Node> childNodes;

	public ParentNode(String text, HashMap<String, String> attributes,
			ArrayList<Node> childNodes) {
		super(text, attributes);
		this.childNodes = childNodes;
	}

	public ArrayList<Node> getChildNodes() {
		return childNodes;
	}

	public void setChildNodes(ArrayList<Node> childNodes) {
		this.childNodes = childNodes;
	}

	public HashSet<Node> getNodeSet(String nodeName) {
		HashSet<Node> result = new HashSet<>();
		for(Node n : getChildNodes())
			if(n.getText().equals(nodeName))
				result.add(n);
		return result;
	}

	public String getValueFromNodeKey(String key) {
		for(Node n : getChildNodes())
			if(n instanceof AttributeNode
					&& ((AttributeNode) n).getAttributes().get("key").equals("\""+key+"\""))
				return ((AttributeNode) n).getAttributes().get("value").replace("\"", "");
		return null;
	}

	public String getEndTag() {
		return DELIMITER_START_CLOSE + getText() + DELIMITER_END;
	}

	public static ParentNode parse(String input) {
		if(!input.startsWith(DELIMITER_START_OPEN)
				|| !input.endsWith(DELIMITER_END))
			throw new IllegalArgumentException("Not a ParentNode: " + input);
		String line = input.substring(DELIMITER_START_OPEN.length(), input.length()-DELIMITER_END.length()).trim();
		String[] tmp = line.split("=");
		String text = null;
		if(tmp.length > 1) {
			text = tmp[0].substring(0, tmp[0].indexOf(" "));
			tmp[0] = tmp[0].substring(text.length()).trim();
		} else {
			text = tmp[0];
			tmp[0] = "";
		}
		return new ParentNode(text, AttributeNode.parseAttributes(tmp), new ArrayList<Node>());
	}

	@Override
	public String toString() {
		return DELIMITER_START_OPEN + getText() + toString(getAttributes()) + DELIMITER_END;
	}

	@Override
	public void exportXML(BufferedWriter bw, int nrOfTabs) throws IOException {
		bw.write(getTabs(nrOfTabs) + toString() + "\n");
		for(Node n : getChildNodes())
			n.exportXML(bw, nrOfTabs+1);
		bw.write(getTabs(nrOfTabs) + getEndTag() + "\n");
	}
}