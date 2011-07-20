package org.BB.interactive;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

public class BlackList {

	private static String blacklistFile = "blacklist";

	Set<String> blacklist;
	Set<String> removelist;
	long blacklistFileLastModified;
	
	// To know when to write/flush to file
	boolean blacklistModified;
	
	BlackList()
	{
		blacklist = new HashSet<String>();
		removelist = new HashSet<String>();
		blacklistFileLastModified = 0;
		sync();
		new Thread(new Runnable() {
			@Override public void run() {createTimeoutSync();}
		}).start();
	}
	
	private void createTimeoutSync()
	{
		new Thread(new Runnable() {
			@Override public void run() {
				try {
					// 10 seconds sleep
					Thread.sleep(10000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				sync();
			}
		}).run();
		
		createTimeoutSync();
	}
	
	private static Set<String> generateRemoveSet(Set<String> fileBlackList)
	{
		Set<String> ret = new HashSet<String>();
		
		if (fileBlackList != null)
			for(String s : fileBlackList)
				if (s.length() > 7 && s.substring(0, 7).compareTo("remove ") == 0)
				{
					ret.add(s.substring(7));
					ret.add(s);
				}

		return ret;
	}
	
	private synchronized void sync()
	{
		Set<String> newBlackList = read();
		boolean changed = blacklistFileLastModified == 0 || blacklistModified;
		
		// apply remove from black list
		Set<String> removeFromFile = generateRemoveSet(newBlackList);

		for(String s : removeFromFile)
		{
			changed = changed || newBlackList.remove(s);
			changed = changed || blacklist.remove(s);
		}
		
		if (newBlackList == null)
			newBlackList = new HashSet<String>(blacklist);
		else
			changed = changed || newBlackList.addAll(blacklist);

		changed = changed || newBlackList.removeAll(removelist);
		blacklist = newBlackList;

		if (changed)
			write();

		removelist.clear();
		blacklistModified = false;
	}
	
	// Does not change blacklist set, created new one
	// changes blacklistFileLastModified
	private Set<String> read()
	{
		InputStreamReader isr = null;
		try {
			File f = new File(blacklistFile);
			
			//System.err.println(blacklistFileLastModified + ">=" + f.lastModified());
			
			if (f.lastModified() == 0 || blacklistFileLastModified >= f.lastModified())
				return null;
			
			System.err.println("Reading " +blacklistFile+ " file.");
			
			isr = new InputStreamReader(new FileInputStream(f), "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			
			String str = null;
			
			Set<String> newBlacklist = new HashSet<String>();
			
			while((str = br.readLine()) != null)
			{
				newBlacklist.add(str);
			}
			
			blacklistFileLastModified = f.lastModified();

			isr.close();
			
			return newBlacklist;
			
		} catch (IOException e)
		{
			if (isr != null)
				try { isr.close(); } catch (IOException ex) {}
			
			blacklistFileLastModified = 0;
				
			return null;
		}
	}
	
	private void write()
	{
		System.err.println("Writing to " +blacklistFile+ " file.");
		OutputStreamWriter osw = null;
		try {
			osw = new OutputStreamWriter(new FileOutputStream(blacklistFile),"UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);
			
			for(String s : blacklist)
			{
				bw.write(s + "\n");
			}
			
			bw.flush();
			osw.close();

		} catch (IOException e) {
			if (osw != null)
				try { osw.close(); } catch (IOException ex) {}
		}
		
	}
	
	public boolean exist(String username)
	{
		return blacklist.contains(username);
	}
	
	public void addToBlackList(String username)
	{
		blacklist.add(username);
		blacklistModified = true;
	}
	
	public synchronized void removeFromBlackList(String username)
	{
		blacklist.remove(username);
		removelist.add(username);
	}
	
	public static void main(String[] args)
	{
		BlackList bl = new BlackList();
		bl.addToBlackList("kuku4");
		bl.sync();
		System.err.println(bl.exist("kuku1"));
		bl.addToBlackList("kuku3");
		bl.sync();
		System.err.println(bl.exist("kuku2"));
		bl.addToBlackList("kuku2");
		bl.sync();
		System.err.println(bl.exist("kuku3"));
		bl.addToBlackList("kuku1");
		bl.sync();
		System.err.println(bl.exist("kuku4"));
		
		bl.removeFromBlackList("kuku2");
		bl.removeFromBlackList("kuku4");
		
		while(true)
		{
			
			try {
				Thread.sleep(3000L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			System.err.println(bl.exist("kuku5"));
		}
	}
	
}
