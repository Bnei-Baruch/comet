package org.BB.interactive;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.server.AbstractService;

import javax.mail.MessagingException;
import javax.mail.internet.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.mail.PasswordAuthentication;

public class SendEmailService extends AbstractService {

	class MessageAbuseData
	{
		Set<String> users;
		long time;
		String abuser;
	}
	
	Map<String,MessageAbuseData> abuseMessages;
	
	private static int NUM_OF_USERS_TO_ABUSE = 2;
	private static long MESSAGE_ABUSE_TIMEOUT = 5*60*1000;
	
	private BlackList blacklist;
	
	public SendEmailService(BayeuxServer bayeux, BlackList blacklist) {
		
		super(bayeux, "send-email-service");

		abuseMessages = new HashMap<String, SendEmailService.MessageAbuseData>();
		this.blacklist = blacklist;

		addService("/service/abuse", "abuseReported");
	}
	
	private void handleAbuseReported(Message message)
	{
		updateAbuseReported(message);
		
		for(Entry<String, MessageAbuseData> e : abuseMessages.entrySet())
			if (e.getValue().users.size() >= NUM_OF_USERS_TO_ABUSE)
			{  // Send abuse email
				if (blacklist != null)
					blacklist.addToBlackList(e.getValue().abuser);
				sendEmail("abuse report", "Reporters:" + e.getValue().users
						+ "\n" + e.getKey());
				abuseMessages.remove(e.getKey());
			} else if (System.currentTimeMillis() - e.getValue().time >= MESSAGE_ABUSE_TIMEOUT)
				// Remove old
				abuseMessages.remove(e.getKey());
	}
	
	private void updateAbuseReported(Message message)
	{
		String reporter = (String)message.get("username");
		Map<String, String> data = (Map<String, String>)message.getData();

		String abuser = data.get("user");
		String key = data.get("message");
		
		MessageAbuseData value = abuseMessages.get(key);
		
		if (value == null)
		{
			value = new MessageAbuseData();
			value.users = new HashSet<String>();
			abuseMessages.put(key, value);
		}
		
		value.users.add(reporter);
		value.abuser = abuser;
		value.time = System.currentTimeMillis();
	}

	public void abuseReported(ServerSession remote, Message message)
	{
		System.err.println("handling abuseReported");

		if (!EncryptionSecurityPolicy.authenticateMessage(message, remote))
		{
			System.err.println("Not authenticated mail send");
			return;
		}
		
		System.err.println("authenticated, handleAbuseReported...");

		handleAbuseReported(message);
	}
	
	private static class Authenticator extends javax.mail.Authenticator {
		private PasswordAuthentication authentication;

		public Authenticator() {
			String username = "ligdoltv@ligdoltv.com";
			String password = "children1";
			authentication = new PasswordAuthentication(username, password);
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			return authentication;
		}
	}
	
	public static void sendEmail(String subject, String body)
    {
		boolean isMailAuth = true;
		Authenticator authenticator = new Authenticator();
		Properties props = new Properties();
		props.setProperty("mail.smtp.submitter", "root");
		props.setProperty("mail.smtp.auth", "true");		
	    props.setProperty("mail.transport.protocol", "smtp");
	    if (isMailAuth)
	    	props.setProperty("mail.smtp.protocol.auth", "true");
	    props.setProperty("mail.host", "localhost");
	    props.setProperty("mail.user", "root");
	    props.setProperty("mail.password", "children1");

	    try {
	    	  javax.mail.Session mailSession = null;
	    	  if (isMailAuth)
	    		  mailSession = javax.mail.Session.getDefaultInstance(props, authenticator);
	    	  else
	    		  mailSession = javax.mail.Session.getDefaultInstance(props);
	    	  
		      javax.mail.Transport transport = mailSession.getTransport("smtp");
	
		      MimeMessage emailMessage = new MimeMessage(mailSession);
		      emailMessage.setSubject(subject, "UTF-8");
		      emailMessage.setText(body, "UTF-8");
		      emailMessage.setHeader("Content-Type", "text/plain; charset=UTF-8");
	
		      emailMessage.addFrom(new InternetAddress[] {new InternetAddress("ligdoltv@ligdoltv.com")});
		      emailMessage.addRecipient(javax.mail.Message.RecipientType.TO,
		      		  new InternetAddress("kolmanv@gmail.com"));
		      
		      //javax.mail.Address[] rec = {new InternetAddress("abuse@ligdoltv.com")};
	
		      transport.connect();//"ligdoltv.com","abuse","abuseesuba");
		      emailMessage.saveChanges();
		      transport.sendMessage(emailMessage,
		    		  emailMessage.getRecipients(javax.mail.Message.RecipientType.TO)
		    		  );
		      transport.close();
	      
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public static void main(String[] args)
	{
		//new SendEmailService(null).sendEmail();
	}

}
