package util.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class XML {

	private ArrayList<Node> nodes;

	public XML(ArrayList<Node> nodes) {
		super();
		this.nodes = nodes;
	}

	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}

	public Node getNode(String nodeName) {
		for(Node n : getNodes())
			if((n instanceof ParentNode || n instanceof AtomicNode)
					&& n.getText().equals(nodeName))
				return n;
		return null;
	}

	public static XML parse(String input) {
		return new XML(getChildNodes(input.trim()));
	}

	public static XML parse(File f) throws IOException {
		XML xml = new XML(getChildNodes(f));
		return xml;
	}

	private static ArrayList<Node> getChildNodes(String input) {
		String xml = input.trim();
		ArrayList<Node> nodes = new ArrayList<>();
		while(xml.length() > 0) {
			String line;
			if(xml.contains("\n")) {
				line = xml.substring(0, xml.indexOf("\n")).trim();
				xml = xml.substring(line.length()+1).trim();
			} else {
				line = xml.trim();
				xml = "";
			}
			Node n = null;
			try {
				n = VersionNode.parse(line);
			} catch(Exception e1) {
				try {
					n = InfoNode.parse(line);
				} catch(Exception e2) {
					try {
						n = AtomicNode.parse(line);
					} catch(Exception e3) {
						try {
							n = ParentNode.parse(line);
							String endTag = ((ParentNode) n).getEndTag();
							String children = xml.substring(0, xml.indexOf(endTag));
							xml = xml.substring(children.length() + endTag.length()).trim();
							((ParentNode) n).getChildNodes().addAll(getChildNodes(children));
						} catch(Exception e4) {}
					}
				}
			}
			if(n == null)
				throw new IllegalArgumentException("Unknown XML-node: " + line);
			nodes.add(n);
		}
		return nodes;
	}

	private static ArrayList<Node> getChildNodes(File f) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
			return getChildNodes(reader, null);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(reader != null)
				reader.close();
		}
		return null;
	}

	private static ArrayList<Node> getChildNodes(BufferedReader reader, String endTag) throws IOException {
		String line = reader.readLine().trim();
		ArrayList<Node> nodes = new ArrayList<>();
		while(line != null) {
			line = line.trim();
			if(line.equals(endTag))
				return nodes;
			Node n = null;
			try {
				n = VersionNode.parse(line);
			} catch(Exception e1) {
				try {
					n = InfoNode.parse(line);
				} catch(Exception e2) {
					try {
						n = AtomicNode.parse(line);
					} catch(Exception e3) {
						try {
							n = ParentNode.parse(line);
							((ParentNode) n).getChildNodes().addAll(getChildNodes(reader, ((ParentNode) n).getEndTag()));
						} catch(Exception e4) {}
					}
				}
			}
			if(n == null)
				throw new IllegalArgumentException("Unknown XML-node: " + line);
			nodes.add(n);
			line = reader.readLine();
		}
		return nodes;
	}

	public void exportXML(BufferedWriter bw) throws IOException {
		for(Node n : getNodes())
			n.exportXML(bw, 0);
		bw.close();
	}
}