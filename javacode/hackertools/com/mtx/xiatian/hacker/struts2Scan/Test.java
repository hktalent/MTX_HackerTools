package com.mtx.xiatian.hacker.struts2Scan;

public class Test
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		String s = "sudo nmap --system-dns -Pn -sS -A -oX Target%d.xml 192.168.%d.0/24";
		
		for(int i = 1; i < 256; i++)
		{
			System.out.println(String.format(s, i,i));
		}
	}

}
