package org.BB.interactive;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.SecurityPolicy;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;

public class BlackListSecurityPolicy implements SecurityPolicy 
{
	BlackList bl;
	
	// Not mandatory - only to send message to the blocked user 
	PrivateChatService pcs;
	
	BlackListSecurityPolicy(BlackList bl, PrivateChatService pcs)
	{
		this.bl = bl;
		this.pcs = pcs;
	}
	
	@Override
	public boolean canPublish(BayeuxServer arg0, ServerSession session,
			ServerChannel arg2, ServerMessage message)
	{
		return isNotInBlackList(message, session);
	}
	
	public boolean isNotInBlackList(Message message, ServerSession session)
	{
		if (session != null && session.isLocalSession())
		{
			System.err.println("Local session: return true " + session.getId() + " " 
					+ message.getChannel());
			return true;
		}
		
		if (message == null)
			return false;
		
		String appId = ApplicationPool.getApplicationId(message.getChannel());
		if (appId == null) // Block if broken channel
			return false;
		
		Object usernameObj = message.get("username");
		if (!(usernameObj instanceof String))
		{
			System.err.println("Username field is not String");
			return false;
		}
		
		String username = (String)usernameObj;
		
		boolean inBlackList = bl.exist(appId + username);
		System.err.println(username + " in black list:" + inBlackList);
		
		if (inBlackList && pcs != null)
		// Message should be generated not the same
			pcs.sendPrivateChat(session, message);
		
		return !inBlackList;
	}

	@Override
	public boolean canCreate(BayeuxServer arg0, ServerSession arg1,
			String arg2, ServerMessage arg3) {
		return true;
	}

	@Override
	public boolean canHandshake(BayeuxServer arg0, ServerSession arg1,
			ServerMessage arg2) {
		return true;
	}

	@Override
	public boolean canSubscribe(BayeuxServer arg0, ServerSession arg1,
			ServerChannel arg2, ServerMessage arg3) {
		return true;
	}

}
