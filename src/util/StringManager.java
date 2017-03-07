package util;

public class StringManager {

	public static String respace(String input) {
		if(input == null)
			return null;
		while(input.contains("  "))
			input = input.replaceAll("  ", " ");
		return input;
	}
}