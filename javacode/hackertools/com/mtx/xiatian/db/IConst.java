package com.mtx.xiatian.db;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class IConst
{
	public static String charset = "UTF-8";
	public static void debug(Throwable e)
	{
		e.printStackTrace();;
	}
	
	public static void log(Object... e)
    {
        if (null != e)
        {
            if (1 == e.length && e[0] instanceof Throwable)
            {
                log((Throwable) e[0]);
                return;
            }
            StringBuffer buf = new StringBuffer();
            for (int i = 0, j = e.length; i < j; i++)
            {
                if (e[i] instanceof Throwable) log((Throwable) e[i]);
                else buf.append(String.valueOf(e[i]));
            }
            if (0 < buf.length())
            {
            		System.out.println(buf.toString());
            }
        }
    }
	public static void debug(Object... e)
    {
        log(e);
    }
	
	public static InputStream getResourceAsStream(String resource)
			throws IOException {
//	    return IConst.getResourceAsStream(resource);
		InputStream in = null;
		ClassLoader loader = null;
		try {
			// if(!resource.startsWith("/"))resource = "/" + resource;
			loader = IConst.class.getClassLoader();
		} catch (Throwable e) {
		    IConst.debug(e);
		}
		try {
			if (null != loader)
				in = loader.getResourceAsStream(resource);
		} catch (Throwable e) {IConst.debug(e);
		}
		try {
			if (in == null)
				in = ClassLoader.getSystemResourceAsStream(resource);
		} catch (Throwable e) {IConst.debug(e);
		}
		try {
			if (null == in
					&& null != (loader = Thread.currentThread()
							.getContextClassLoader()))
				in = loader.getResourceAsStream(resource);
		} catch (Throwable e) {IConst.debug(e);
		}
		try {
			if (null == in)
				in = IConst.class.getResourceAsStream(resource);
		} catch (Throwable e) {IConst.debug(e);
		}
		try {
			if (in == null)
				in = new FileInputStream(resource);
			// if (in == null) throw new IOException("未找到资源 " + resource);
		}catch(FileNotFoundException e)
		{
		    IConst.debug("找不到资源：",  resource);
		} catch (Throwable e) {
		    IConst.debug(e);
		}
		if (null == in && resource.startsWith("/"))
			return getResourceAsStream(resource.substring(1));
		return in;
	}
	
	public static String readFile(String s)
    {
        InputStream in = null;
        ByteArrayOutputStream out = null;
        try
        {
            in = getResourceAsStream(s);
            if (null == in) return null;
            out = new ByteArrayOutputStream();
            byte[] b = new byte[4096];
            int i;
            while (-1 < (i = in.read(b, 0, 4096)))
            {
                out.write(b, 0, i);
            }
            out.flush();
            return new String(out.toByteArray(), charset);
        }
        catch (Throwable e)
        {
            debug(e);
        }
        finally
        {
            try
            {
                if (null != out) out.close();
            }
            catch (Throwable e)
            {
                debug(e);
            }
            try
            {
                if (null != in) in.close();
            }
            catch (Throwable e)
            {
                debug(e);
            }
            in = null;
            out = null;
        }
        return null;
    }
}
