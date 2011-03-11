package org.BB.interactive;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.SecurityPolicy;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;

public class MultipleSecurityPolicy implements SecurityPolicy {

	SecurityPolicy[] policies;
	
	MultipleSecurityPolicy(SecurityPolicy[] policies)
	{
		this.policies = policies;
	}
	
	@Override
	public boolean canCreate(BayeuxServer arg0, ServerSession arg1,
			String arg2, ServerMessage arg3) {
		for(SecurityPolicy sp : policies)
			if (!sp.canCreate(arg0, arg1, arg2, arg3))
				return false;
		
		return true;
	}

	@Override
	public boolean canHandshake(BayeuxServer arg0, ServerSession arg1,
			ServerMessage arg2) {
		for(SecurityPolicy sp : policies)
			if (!sp.canHandshake(arg0, arg1, arg2))
				return false;
		
		return true;
	}

	@Override
	public boolean canPublish(BayeuxServer arg0, ServerSession arg1,
			ServerChannel arg2, ServerMessage arg3) {
		for(SecurityPolicy sp : policies)
			if (!sp.canPublish(arg0, arg1, arg2, arg3))
				return false;
		return true;
	}

	@Override
	public boolean canSubscribe(BayeuxServer arg0, ServerSession arg1,
			ServerChannel arg2, ServerMessage arg3) {
		for(SecurityPolicy sp : policies)
			if (!sp.canSubscribe(arg0, arg1, arg2, arg3))
				return false;
		return true;
	}

}
