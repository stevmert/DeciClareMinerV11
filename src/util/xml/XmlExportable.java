package util.xml;

import java.io.BufferedWriter;
import java.io.IOException;

public interface XmlExportable {

	public void exportXML(BufferedWriter bw, int nrOfTabs) throws IOException;
}