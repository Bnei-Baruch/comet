package org.BB.interactive;

import java.util.HashMap;
import java.util.Map;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.server.AbstractService;

public class PrivateChatService  extends AbstractService {

	BayeuxServer bayeux;
	
	public PrivateChatService(BayeuxServer bayeux) {
		
		super(bayeux, "private-chat-service");

		this.bayeux = bayeux;
		
		addService("/service/privatechat", "sendPrivateChat");

	}
	
	public void sendPrivateChat(ServerSession remote, Message message)
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
	}

}
