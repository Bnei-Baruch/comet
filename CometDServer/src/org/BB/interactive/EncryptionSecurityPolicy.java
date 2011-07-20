package org.BB.interactive;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

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
	ApplicationPool ap;

	EncryptionSecurityPolicy(ApplicationPool ap)
	{
		this.ap = ap;
	}

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
		return authenticateMessage(message, session);
	}

	@Override
	public boolean canSubscribe(BayeuxServer arg0, ServerSession session,
			ServerChannel arg2, ServerMessage message)
	{
		return authenticateMessage(message, session);
	}
	
	public boolean authenticateMessage(Message message, ServerSession session)
	{		
		System.err.println(message.getJSON());
		
		if (session != null && session.isLocalSession())
		{
			System.err.println("Local session: return true " + session.getId() + " " 
					+ message.getChannel());
			return true;
		}
		
		if (message == null)
			return false;
	
		Object appIdObj = message.get("appId");
		if (!(appIdObj instanceof String))
			return false; // Block if no appId
		String appId = (String)appIdObj;
		Application app = ap.getApp(appId);
		if (app == null) // Block if appId not valid
			return false;
			
		// Authenticate only on relevant channels!
		if (!message.getChannel().startsWith("/auth") &&
			!message.getChannel().startsWith("/service/auth"))
			return  true;

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
		
		String verifyDecryptHex = decrypt(app.secretKey, app.ivParam, verify);
		String verifyDecrypt = null;
		try {
			verifyDecrypt = new String(hexToBytes(verifyDecryptHex), "UTF-8");

			System.err.println("verifyDecrypt:"+verifyDecrypt);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return verifyDecrypt != null && verifyDecrypt.compareTo(username) == 0;
	}

	// Change encoding and decoding to be correct!!!
	public static byte[] hexToBytes(String str)
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
	
	private static char byteToHex(int b)
	{
		char r = (char)('0'+b);
		if (b >= 10)
			r = (char)('a'+(b-10));
		return r;
	}
	
	public static int toUnsignedByte(byte b)
	{
		return (int) b & 0xFF;
	}
	
	public static String bytesToHex(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < bytes.length; i++)
		{
			sb.append(byteToHex(toUnsignedByte(bytes[i]) / 16));
			sb.append(byteToHex(toUnsignedByte(bytes[i]) % 16));
		}
		return sb.toString();
	}
	
	private static String decrypt(String secretKey, String ivParam, String bytesAsString)
	{
		return doCipher(secretKey, ivParam, bytesAsString, Cipher.DECRYPT_MODE);
	}
	
	private static String encrypt(String secretKey, String ivParam, String bytesAsString)
	{
		return doCipher(secretKey, ivParam, bytesAsString, Cipher.ENCRYPT_MODE);
	}
	
	// Input expected is hexa low case letters i.e. 7a834ff32bcd
	// They represent the byte[]
	private static String doCipher(String secretKey, String ivParam, String bytesAsString, int mode)
	{
		if (bytesAsString == null)
			return null;
		
		try {
			Cipher cipher;
		
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		
			SecretKeySpec keySpec = new SecretKeySpec(hexToBytes(secretKey), "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(hexToBytes(ivParam));
			cipher.init(mode, keySpec, ivSpec);
		
			byte[] bytes = hexToBytes(bytesAsString);
		
			byte[] outTextAsBytes = cipher.doFinal(bytes);
			String outText = bytesToHex(outTextAsBytes);
		
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
		}
		
		return null;
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException
	{
		String in_test = "Hello World";
		String in_test_hex = EncryptionSecurityPolicy.bytesToHex(in_test.getBytes("UTF-8"));		
		System.out.println(in_test + " (to hex)=> " + in_test_hex);
		String in_test_back = new String(EncryptionSecurityPolicy.hexToBytes(in_test_hex), "UTF-8");
		System.out.println(in_test_hex + " (to utf-8)=> " + in_test_back);

		String encrypted = encrypt("01234567890abcde01234567890abcde",
							 "fedcba9876543210fedcba9876543210",
							 in_test_hex);
		System.out.println(in_test_hex + " (encrypt)=> " + encrypted);
		String decrypted = decrypt("01234567890abcde01234567890abcde",
				  				   "fedcba9876543210fedcba9876543210",
				  				   encrypted);
		System.out.println(encrypted + " (decrypted)=> " + decrypted);
		String result = new String(EncryptionSecurityPolicy.hexToBytes(decrypted), "UTF-8");
		System.out.println(decrypted + " (to utf-8)=> " + result);
	}
}
