package com.mtx.xiatian.hacker;

import java.util.HashMap;

/**
 * big Map
 * @author xiatian
 */
public class MyMap  extends HashMap
{
	
    public boolean containsKey(Object arg0)
    {
	    return super.containsKey(arg0);
    }

	
    public Object get(Object s)
    {
	    String []a = String.valueOf(s).split("");
	    HashMap p = this, mC;
	    for(String k:a)
	    {
	    		if(null != (mC = (HashMap)(p == this? super.get(k) : p.get(k))))
	    			p = mC;
	    }
	    return p;
    }

	
    public Object put(String s, Object o)
    {
	    String []a = s.split("");
	    Object oT;
	    HashMap mT = null;
	    HashMap p = this;
	    for(String k: a)
	    {
	    		oT = p.get(k);
	    		if(null == oT)oT = mT= new HashMap<String,HashMap>();
	    		if(oT instanceof HashMap)
	    		{
	    			mT = (HashMap)oT;
	    		}
	    		super.put(k, mT);
	    		p = mT;
	    }
	    return o;
    }

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		;
	}

}
