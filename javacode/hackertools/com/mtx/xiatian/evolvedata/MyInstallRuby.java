package com.mtx.xiatian.evolvedata;

import java.io.IOException;
import java.io.InputStream;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class MyInstallRuby {

	public static void runIns(String szCmd, String []a)
	{
		try {
			Process p = java.lang.Runtime.getRuntime().exec(szCmd, a);
			InputStream in = p.getInputStream();
			ByteOutputStream out = new ByteOutputStream();
			byte []b = new byte[1024];
			int i = 0, j;
			while(-1 < (i = in.read(b, 0, 1024)))
			{
				out.write(b, 0, i);
			}
			in.close();
			p.destroy();
			String s = new String(out.getBytes(), "UTF-8");
			System.out.println(s);
			String s1 = "Make sure that `gem ";
			i = s.indexOf(s1);
			if(-1 < i)
			{
				j = s.indexOf("` succeeds before bundling.");
				if(-1 < j)
				{
					s = s.substring(i + s1.length(), j);
					runIns("gem", s.split(" "));
					runIns(szCmd, a);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		runIns("/Users/xiatian/.rvm/rubies/ruby-2.1.6/bin/ruby",new String[]{"/Users/xiatian/.rvm/gems/ruby-2.1.6@metasploit-framework/bin/bundle","install"});
	}

}
