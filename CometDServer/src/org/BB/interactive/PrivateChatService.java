package org.BB.interactive;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.server.AbstractService;
import org.cometd.bayeux.server.ServerSession.RemoveListener;

public class PrivateChatService  extends AbstractService {

	BayeuxServer bayeux;
	
	private final ConcurrentMap<String, Map<String, String>> channelMembers = 
	      new ConcurrentHashMap<String, Map<String, String>>();
	
	// root channel may be "/ligdolTV"
	// or "/auth/LigdolTV" - enables authentication
	public PrivateChatService(BayeuxServer bayeux, String[] channels) {
		
		super(bayeux, "private-chat-service");

		this.bayeux = bayeux;
		
		for(String channel : channels)
		{
			addService("/service"+channel+"/privatechat", "sendPrivateChat");
			addService(channel, "trackMembers");
		}
	}
	
	public void addChannel(String channel)
	{
		addService("/service"+channel+"/privatechat", "sendPrivateChat");
		addService(channel, "trackMembers");
	}
	
	public void trackMembers(ServerSession from, Message message)
	{
		Map<String, String> membersMap = channelMembers.get(message.getChannel());
		if (membersMap == null)
		{
			Map<String, String> newMembersMap = new ConcurrentHashMap<String, String>();
			membersMap = channelMembers.putIfAbsent(message.getChannel(), newMembersMap);
			if (membersMap == null) membersMap = newMembersMap;
		}

		final String userName = (String)message.getDataAsMap().get("user");
		if (membersMap.containsKey(userName))
			return;
		
		membersMap.put(userName, from.getId());
		bayeux.getChannel(message.getChannel()).publish( 
				getServerSession(), membersMap.keySet(), null);

		final Map<String, String> members = membersMap;
		final String channelStr = message.getChannel();
		from.addListener(new RemoveListener()
		{
			public void removed(ServerSession session, boolean timedout)
			{
				members.values().remove(session.getId());
				ServerChannel channel = bayeux.getChannel(channelStr);
				if (channel != null) 
					channel.publish(session, members.keySet(), null);
			}
		});
	}
	
	//"/service"+rootChannel+"/privatechat"
	//Removes the /servise from beggining and /privatechat from the end
	private static String getRootChannel(String serviceChannel)
	{
		int from = "/service".length();
		int to = serviceChannel.length() - "/privatechat".length();
		return serviceChannel.substring(from, to);
	}
	
	public void sendPrivateChat(ServerSession from, Message message)
	{
	    String channel = getRootChannel(message.getChannel());
	    Map<String, String> membersMap = channelMembers.get(channel);
	    String[] addresse = ((String)message.getDataAsMap().get("to")).split(",");

		Map<String, Object> sendMessage = new HashMap<String, Object>();
		message.put("chat", message.getDataAsMap().get("chat"));
		message.put("user", message.getDataAsMap().get("user"));
		message.put("scope", "private");

	    for(String to : addresse)
	    {
	    	String toId = membersMap.get(to);
	    	if (toId!=null)
	    	{
	    		ServerSession toSession = bayeux.getSession(toId);
	    		if (toSession != null)
	    			toSession.deliver(getServerSession(), channel, 
	    				sendMessage, message.getId());
	    	}
	    }
		from.deliver(getServerSession(), channel, sendMessage, message.getId());
	}

	/*public void sendPrivateChat(ServerSession remote, Message message)
	{
		if (!EncryptionSecurityPolicy.authenticateMessage(message, remote))
		{
			System.err.println("Could not authenticate private chat");
			return;
		}
		else
			System.err.println("Private chat authenticated");
		
		System.err.println(message.getJSON());
		
		Map<String, String> data = (Map<String, String>)message.getData();
		
		Map<String, String> toSend = new HashMap<String, String>();
		toSend.put("from", data.get("user"));
		toSend.put("chat", data.get("chat"));
		
		String[] toArr = data.get("to").split(";");
		
		if (data.get("user").compareTo((String)message.get("username")) != 0)
		{
			System.err.println("Trying to fake private message");
			SendEmailService.sendEmail("Trying to fake private message", message.getJSON());
			return;
		}
		
		for(String to : toArr)
		{
			toSend.put("to", to);
			ServerChannel channel = bayeux.getChannel("/auth/chat/private/"+to);
			
			if (channel != null)
				channel.publish(remote,
					toSend, null);
		}
		
		//for(ServerSession session : bayeux.getSessions())
		//	session.get
	}*/

}
