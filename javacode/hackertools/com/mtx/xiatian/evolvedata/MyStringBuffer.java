package com.mtx.xiatian.evolvedata;

public class MyStringBuffer {

	private StringBuffer sb = new StringBuffer();
	
	public MyStringBuffer append(String s)
	{
		sb.append(s);
//		System.out.print(s);
		return this;
	}

	public String toString() {
		return sb.toString();
	}
}
