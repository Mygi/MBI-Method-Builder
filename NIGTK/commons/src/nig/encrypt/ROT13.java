package nig.encrypt;

/**
 * Class to implement simple ROT13 reverible encryption
 * 
 * @author ssadeghis
 *
 */
public class ROT13 {


	public static String encodeString (String str) {
		return rot13(str);
	}
	
	public static String decodeString (String str) {
		return rot13(str);
	}
	
	
	private static String rot13(String message)
	{
		String coded = "";
		for (int x = 0; x < message.length(); x++)
		{
			char c = message.charAt(x);
			
			if (Character.isLowerCase(c))
			{
				c += 13;
				if (c > 'z') c -= ('z' - 'a') + 1;
			}
			if (Character.isUpperCase(c))
			{
				c += 13;
				if (c > 'Z') c -= ('Z' - 'A') + 1;
			}
			coded += c;
		}
		return coded;
	}
}
