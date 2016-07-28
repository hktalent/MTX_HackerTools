package com.mtx.xiatian.hacker.test;

import java.util.Map;

import com.mtx.xiatian.evolvedata.MyHashMap;




/**
 * 加、解密工具类
 * @author xiatian
 */
public class EncryptManager
{
    /**
     * 单实例加密工具
     */
    private static DESedeMTX des = new DESedeMTX();
    public static String charset = "UTF-8";

    /**
     * 牺牲内存提高效率
     */
    private static MyHashMap mEnc = new MyHashMap();
    private static MyHashMap mDec = new MyHashMap();

    /**
     * 日志输出
     * @param e
     */
    public static void log(Throwable e)
    {
        if (null != e) e.printStackTrace();
    }

    /**
     * 加密函数故意 加密数据s，
     * @param key1 密码1 例如授权给医院的特定密码
     * @param key2 密码2 例如医院编号
     * @param s
     * @return
     */
    public static synchronized String encStr(String key1, String key2, String s)
    {
        String sRst = null;
        EDStr d = (EDStr) mEnc.get(s);
        if (null == d)
        {
            sRst = des.encryptMode(key1, s, key2);
            if(MyHashMap.useCache)
                mEnc.put(s, new EDStr(sRst, 0L));
        }
        else
        {
            sRst = d.getData();
            d.setCount(d.getCount() + 1);
        }
        clearnData(mEnc);
        return sRst;
    }
    /**
     * 加密二进制
     * @param key1
     * @param key2
     * @param s
     * @return
     */
    public static synchronized byte[] encForBytes(String key1, String key2, byte[] s)
    {
        return des.encryptModeBytes(key1, s, key2);
    }
    
    /**
     * 清理不常用的非热点数据 1、当缓存数据小于10000时不清理 2、缓存数据大于10000-条时清理掉： 3、使用次数低于10次的数据
     * 4、长时间未使用的数据
     * @param m
     */
    private static void clearnData(Map<String, EDStr> m)
    {}


    /**
     * 解密字符串s 1、当密码不传递时默认使用内部的密码 2、强烈建议传入双密码
     * @param key1 第一个主密码
     * @param key2 第二个密码，例如医院编号
     * @param s
     * @return
     */
    public static synchronized String decStr(String key1, String key2, String s)
    {
        String sRst = null;
        EDStr d = (EDStr) mDec.get(s);
        if (null == d)
        {
            sRst = des.decryptMode(key1, s, key2);
            if(MyHashMap.useCache)mDec.put(s, new EDStr(sRst, 0L));
        }
        else
        {
            sRst = d.getData();
            d.setCount(d.getCount() + 1);
        }
        clearnData(mDec);
        return sRst;
    }
    
    /**
     * 对二进制进行加密
     * @param key1
     * @param key2
     * @param s
     * @return
     */
    public static synchronized byte[] decForBytes(String key1, String key2, byte[] s)
    {
        return des.decryptModeForBytes(key1, s, key2);
    }

}
