package com.mtx.xiatian.hacker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.TreeMap;

import com.mtx.xiatian.MD5Util;

/**
 * <pre>
 * 去除重复文件
 CREATE TABLE `mydb`.`fileInfo` (
  `filename` VARCHAR(500) NOT NULL,
  `size` INT NOT NULL,
  `md5` VARCHAR(150) NOT NULL,
  PRIMARY KEY (`md5`));
 * </pre>
 * @author xiatian
 */
public class DuplicateFile extends CommonTools
{
	public DuplicateFile()
	{
		super.useMysql();
		super.hvLastScan = false;
	}
	
	private long nCnt = 1; 
	public void doDir(String szDir)
	{
		File []fs = new File(szDir).listFiles();
		String szName = null, md5;
		TreeMap<String, Object> m = new TreeMap<String, Object>(), mQ1; 
		for(File f: fs)
		{
			szName = f.getAbsolutePath();
			if(".".equals(szName) || "..".equals(szName))continue;
			if(f.isDirectory())
				doDir(szName);
			else if(!(szName.endsWith(".jpg") || szName.endsWith(".png")))
				continue;
			else
			{
				m.clear();
				md5 = md5(readFile(szName));
				mQ1 = super.querySQL("select * from fileInfo where size=" + f.length() + " and md5='" + md5 + "'");
				// 找到重复文件
				if(0 < mQ1.size())
				{
					// 如果是同一个文件，就不处理
					if(f.getName().equals(mQ1.get("filename")) 
							&& f.length() == Long.valueOf(String.valueOf(mQ1.get("size")))
							&& md5.equals(mQ1.get("md5")))
						continue;
					f.renameTo(new File("/Volumes/BOOK/pic/k/" + nCnt + szName.substring(szName.lastIndexOf("."))));
					nCnt++;
					System.out.println(f.getName() + " != " + mQ1.get("filename"));
//					System.out.println(f.length() + " != " + Long.valueOf(String.valueOf(mQ1.get("size"))));
//					System.out.println(md5 + " != " + mQ1.get("md5"));
				}
				else
				{
					m.put("filename", f.getName());
					m.put("size", f.length());
					m.put("md5", md5);
//					System.out.println(szName);
					super.insertTable("fileInfo", null, m);
				}
			}
		}
	}
	
	 /**
     * 读取文件
     * @param s
     * @return
     */
    public  byte[] readFile(String s)
    {
        InputStream in = null;
        ByteArrayOutputStream out = null;
        byte []b = null;
        try
        {
            in = new FileInputStream(new File(s));
            out = new ByteArrayOutputStream();
            b = new byte[4096];
            int i;
            while (-1 < (i = in.read(b, 0, 4096)))
            {
                out.write(b, 0, i);
            }
            out.flush();
            b = out.toByteArray();
           out.reset();
           out = null;
        }
        catch (Throwable e)
        {
        		e.printStackTrace();
        }
        finally
        {
            try
            {
                if (null != out) out.close();
            }
            catch (Throwable e)
            {
            		e.printStackTrace();
            }
            try
            {
                if (null != in) in.close();
            }
            catch (Throwable e)
            {
            	e.printStackTrace();
            }
            in = null;
            out = null;
        }
        return b;
    }
	/**
	 * 获取md5
	 * @param b
	 * @return
	 */
	public String md5(byte []b)
	{
		return MD5Util.MD5(b);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		DuplicateFile df = new DuplicateFile();
		df.doDir("/Volumes/BOOK/pic/照片 图库.photoslibrary/");
	}
}
