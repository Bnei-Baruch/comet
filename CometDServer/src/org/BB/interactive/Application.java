package org.BB.interactive;

import java.io.BufferedReader;
import java.io.IOException;

public class Application {
	public String id;

	//boolean hasSecureChannels;
	//boolean hasPrivateUserChannels;
	//boolean hasBlacklist;
	
	// if has secure channels, than not null!
	// For AES
	public String secretKey;
	public String ivParam;
	
	public static String stripSpaces(String in)
	{
		return in.replaceAll(" ", "");
	}
	
	public static Application readFromBuffer(BufferedReader in) throws IOException
	{
		Application ret = new Application();
		String line = null;
		int read = 0;
		while((line = in.readLine()) != null && read < 3)
		{
			String[] words = line.split("=");
			if (line.isEmpty()) {
				return ret;
			} else if (stripSpaces(words[0]).compareTo("id") == 0) {
				read++;
				ret.id = stripSpaces(words[1]);
			} else if (stripSpaces(words[0]).compareTo("secretKey") == 0) {
				read++;
				ret.secretKey = stripSpaces(words[1]);
			} else if (stripSpaces(words[0]).compareTo("ivParam") == 0) {
				read++;
				ret.ivParam = stripSpaces(words[1]);
			}
		}
		
		if (read == 3)
			return ret;

		return null;
	}
}
