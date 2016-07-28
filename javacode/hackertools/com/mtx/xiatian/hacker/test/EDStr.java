package com.mtx.xiatian.hacker.test;

/**
 * 被加解密对象的实体
 * @author xiatian
 */
public class EDStr implements java.io.Serializable
{
    private static final long serialVersionUID = 4143130432272000474L;

    /**
     * 使用计数器
     */
    private Long count = 0L;
    /**
     * 最后一次使用时间
     */
    private Long lastTim = 0L;
    
    /**
     * 数据
     */
    private String data;
    
    public Long getCount()
    {
        return count;
    }
    public void setCount(Long count)
    {
        this.count = count;
        lastTim = System.currentTimeMillis();
    }
    public String getData()
    {
        return data;
    }
    public void setData(String data)
    {
        this.data = data;
    }

    
    public EDStr(String s,Long l)
    {
        data = s;
        count = l;
        lastTim = System.currentTimeMillis();
    }
}
