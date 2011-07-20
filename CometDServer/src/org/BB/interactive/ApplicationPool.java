package org.BB.interactive;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ApplicationPool {

	Map<String, Application> appMap;

	public ApplicationPool(String filename) throws IOException {
		appMap = new HashMap<String, Application>();

		BufferedReader in = new BufferedReader(new FileReader(filename));
		Application app = null;
	    while ((app = Application.readFromBuffer(in))!=null) {
	    	appMap.put(app.id, app);
	    }
	    in.close();
	}
	
	public int size() {
		return appMap.size();
	}
	
	ApplicationPool(Application[] apps)
	{
		appMap = new HashMap<String, Application>();
		
		for(Application app : apps)
			appMap.put(app.id, app);
	}
	
	// Input may be a channel or appId!!!
	public Application getApp(String channel_or_appId)
	{
		String appId = getApplicationId(channel_or_appId);
		
		if (appId == null || appId.isEmpty())
			return null;
		
		return appMap.get(appId);
	}
	
	public static String getApplicationId(String channel)
	{
		if (channel == null)
			return null;

		int from = 0;
		if (channel.startsWith("/service/auth"))
			from = "/service/auth".length();
		else if (channel.startsWith("/auth"))
			from = "/auth".length();
		int to = channel.indexOf("/", from);
		
		if (to == -1)
			to = channel.length();

		return channel.substring(from, to);
	}
	
}
