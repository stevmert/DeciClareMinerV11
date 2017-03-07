package util.xml;

import java.io.BufferedWriter;
import java.io.IOException;

public abstract class Node implements XmlExportable {

	private String text;

	public Node(String text) {
		super();
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void exportXML(BufferedWriter bw, int nrOfTabs) throws IOException {
		bw.write(getTabs(nrOfTabs) + toString() + "\n");
	}

	protected String getTabs(int nrOfTabs) {
		String result = "";
		for(int i = 0; i < nrOfTabs; i++)
			result += "\t";
		return result;
	}
}