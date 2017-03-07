package util.xml;

import java.util.ArrayList;
import java.util.Comparator;

public class AttributeOrder implements Comparator<String> {

	private static final ArrayList<String> XML_ATTRIBUTE_ORDER = new ArrayList<>();
	static {
		XML_ATTRIBUTE_ORDER.add("version");
		XML_ATTRIBUTE_ORDER.add("encoding");
		XML_ATTRIBUTE_ORDER.add("xes.version");
		XML_ATTRIBUTE_ORDER.add("xes.features");
		XML_ATTRIBUTE_ORDER.add("xmlns");
		XML_ATTRIBUTE_ORDER.add("name");
		XML_ATTRIBUTE_ORDER.add("prefix");
		XML_ATTRIBUTE_ORDER.add("uri");
		XML_ATTRIBUTE_ORDER.add("key");
		XML_ATTRIBUTE_ORDER.add("value");
		XML_ATTRIBUTE_ORDER.add("isActivated");
	}

	@Override
	public int compare(String name1, String name2) {
		int p1 = XML_ATTRIBUTE_ORDER.indexOf(name1);
		int p2 = XML_ATTRIBUTE_ORDER.indexOf(name2);
		if(p1 == p2)
			return 0;
		if(p2 == -1)
			return -1;
		if(p1 == -1)
			return 1;
		if(p1 < p2)
			return -1;
		return 1;
	}

}
