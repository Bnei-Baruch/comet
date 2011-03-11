package org.BB.interactive;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.SecurityPolicy;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;

public class EncryptionSecurityPolicy implements SecurityPolicy 
{
	@Override
	public boolean canCreate(BayeuxServer server, ServerSession session,
			String arg2, ServerMessage message)
	{
		return authenticateMessage(message, session);
	}

	@Override
	public boolean canHandshake(BayeuxServer arg0, ServerSession session,
			ServerMessage message)
	{
		return authenticateMessage(message, session);
	}

	@Override
	public boolean canPublish(BayeuxServer arg0, ServerSession session,
			ServerChannel arg2, ServerMessage message)
	{
		//System.err.println("in auth:" + message.getJSON());
		return authenticateMessage(message, session);
	}

	@Override
	public boolean canSubscribe(BayeuxServer arg0, ServerSession session,
			ServerChannel arg2, ServerMessage message)
	{
		return authenticateMessage(message, session);
	}
	
	public static boolean authenticateMessage(Message message, ServerSession session)
	{
		//System.out.println("ENTERING AUTH!");

		if (session != null && session.isLocalSession())
		{
			System.err.println("Local session: return true " + session.getId() + " " 
					+ message.getChannel());
			return true;
		}
		
		if (message == null /*|| message.getExt() == null*/)
		{
			return false;
		}
		
		// Authenticate only on relevant channels!
		if (!message.getChannel().startsWith("/auth"))
		{
			
			return  true;
		}
		
		System.err.println(message.getJSON());
		
		//System.err.println(message.getClientId());
		//if (message.)
		
		//if (message.getExt() == null)
		//	return false;
		
		Object usernameObj = message.get("username");
		Object verifyObj = message.get("verify");
		
		//System.err.println(message.getData());
		//System.err.println(verifyObj);
		
		if (!(usernameObj instanceof String) || !(verifyObj instanceof String))
		{
			System.err.println("One of fields is not String");
			return false;
		}
		
		String username = (String)usernameObj;
		String verify = (String)verifyObj;
		
		System.err.print(username + "-");
		System.err.println(verify);
		
		String verifyDecrypt = decrypt(verify);
		
		System.err.println("verifyDecrypt:"+verifyDecrypt);
		
		boolean ret = verifyDecrypt.compareTo(username) == 0;
		
		System.err.println(ret);
		
		return ret;
	}

	// Change encoding and decoding to be correct!!!
	private static byte[] hexToBytes(String str)
	{
		if (str==null) {
			return null;
		} else if (str.length() < 2) {
			return null;
		} else {
			int len = str.length() / 2;
			byte[] buffer = new byte[len];

			for (int i=0; i < len; i++)
				buffer[i] = (byte) Integer.parseInt(str.substring(i*2,i*2+2),16);
			
			return buffer;
		}

	}
	
	// Input expected is hexa low case letters i.e. 7a834ff32bcd
	// They represent the byte[]
	private static String decrypt(String bytesAsString)
	{
		if (bytesAsString == null)
			return null;
		
		try {
			Cipher cipher;
		
			cipher = Cipher.getInstance("AES/CBC/NoPadding");
		
			SecretKeySpec keySpec = new SecretKeySpec("01234567890abcde".getBytes(), "AES");
			IvParameterSpec ivSpec = new IvParameterSpec("fedcba9876543210".getBytes());
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		
			byte[] bytes = hexToBytes(bytesAsString);
		
			byte[] outTextAsBytes = cipher.doFinal(bytes);
			String outText = new String(outTextAsBytes, "UTF-8").trim();
		
			System.out.println(outText);
		
			return outText;
		
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
			//kolman(heb) f0c7b11131b500d0df35489606576cf6
			//?! ac5c3404f57a5061f36a694eb5d56214
		String res = decrypt("f0c7b11131b500d0df35489606576cf6");
		
		if (res == null)
			System.out.println("Null!");
		else
			System.out.println(res);
		
		//encrypt("");
	}
}
