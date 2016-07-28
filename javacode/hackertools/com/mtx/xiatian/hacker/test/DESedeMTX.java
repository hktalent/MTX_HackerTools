package com.mtx.xiatian.hacker.test;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DESedeMTX
{
    public DESedeMTX()
    {}

    // 算法DESede: "DESede";
    private String Algorithm = new String(new byte[] { 68, 69, 83, 101, 100, 101 });

    // 工作模式CBC(ECB)，填充模式PKCS5Padding(NoPadding)
    // eg: DESede/CBC/PKCS5Padding, DESede/ECB/PKCS5Padding
    // 避免反编译："DESede/CBC/PKCS5Padding"
    private String Transformation = new String(
            new byte[] { 68, 69, 83, 101, 100, 101, 47, 67, 66, 67, 47, 80, 75, 67, 83, 53, 80, 97, 100, 100, 105, 110, 103 });

    // 向量iv,ECB不需要向量iv，CBC需要向量iv
    // CBC工作模式下，同样的密钥，同样的明文，使用不同的向量iv加密 会生成不同的密文
    // private final String Iv = "\0\0\0\0\0\0\0\0";
    // public final String Iv = "\0" + (char)8 + (char)0x2E + (char)0x2F+
    // (char)0x7F +"\1\2\3";
    // 必须8位长度
    // private byte []Iv = new byte[]{0, 8, 0x2E,0x2F,0x7F,1,2,3};

    private static Map<String, SecretKey> mSecretKey = new HashMap<String, SecretKey>();
    private static Map<String, IvParameterSpec> mIvParameterSpec = new HashMap<String, IvParameterSpec>();

    /**
     * 获取密码key1
     * @return
     */
    private SecretKey getSecretKey(byte[] keybyte)
    {
        // 根据给定的字节数组和算法构造一个密钥
        SecretKey deskey = null;
        if (null != key1) deskey = mSecretKey.get(key1);
        if (null == deskey)
        {
            deskey = new SecretKeySpec(keybyte, Algorithm);
            if (null != key1) mSecretKey.put(key1, deskey);
        }
        return deskey;
    }

    /**
     * 获取向量、密码key2
     * @param Iv
     * @return
     */
    private IvParameterSpec getIvParameterSpec(byte[] Iv)
    {
        IvParameterSpec iv = null;
        if (null != key2) iv = mIvParameterSpec.get(key2);
        if (null == iv)
        {
            iv = new IvParameterSpec(Iv);
            if (null != key2) mIvParameterSpec.put(key2, iv);
        }
        return iv;
    }

    public String encryptMode(byte[] keybyte, byte[] src, byte[] Iv)
    {
        Iv = getIv(Iv);
        return byte2hex(encryptModeForBytes(keybyte, src, Iv), getCode(keybyte) + getCode(Iv));
    }

    public byte[] encryptModeForBytes(byte[] keybyte, byte[] src, byte[] Iv)
    {
        Iv = getIv(Iv);
        try
        {
            // 根据给定的字节数组和算法构造一个密钥
            SecretKey deskey = getSecretKey(keybyte);
            IvParameterSpec iv = getIvParameterSpec(Iv);

            // 加密
            Cipher c1 = Cipher.getInstance(Transformation);
            c1.init(Cipher.ENCRYPT_MODE, deskey, iv);
            // [0, 1, 2, 3, 4, 8, 18, 19, 20, 27, 37, 38, 39, 40, 42, 43, 44,
            // 45, 46, 47, 41, 43, 42, 9]
            // [0, 1, 2, 3, 4, 8, 18, 19]
            return c1.doFinal(src);
        }
        catch (Throwable e1)
        {
            EncryptManager.log(e1);
        }
        return null;
    }

    private String byte2hex(byte[] b, int nCode)
    {
        if (0 == nCode) nCode = 5;// 1682547622
        String s2 = new Base64(nCode).encode(b);
        return s2;
        // String s = byte2hex1(b);
        // if(s.length() > s2.length())return s2;
        // return s; // 转成大写
    }

    private String byte2hex1(byte[] b)
    { // 一个字节的数，
      // 转成16进制字符串
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++)
        {
            // 整数转成十六进制表示
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) hs = hs + "0" + stmp;
            else hs = hs + stmp;
        }
        return hs; // 转成大写
    }

    /**
     * 通过加密的key，和向量
     * @param key
     * @param src
     * @param Iv 加密向量，通常用医院、药店的编号
     * @return
     */
    public String encryptMode(String key, String src, byte[] Iv)
    {
        try
        {
            return encryptMode(getKeyByte(key), src.getBytes(EncryptManager.charset), Iv);
        }
        catch (Exception e)
        {
            EncryptManager.log(e);
        }
        return null;
    }

    public byte[] encryptModeBytes(String key, byte[] src, String Ivs)
    {
        byte []Iv = getIv(Ivs);
        return encryptModeForBytes(getKeyByte(key), src, Iv);
    }

    private String key1, key2;

    public String encryptMode(String key, String src, String Iv)
    {
        key1 = key;
        key2 = Iv;
        return encryptMode(getKeyByte(key), src, getIv(Iv));
    }

    /**
     * 加密
     * @param key 密码
     * @param src 被加密的字符串
     * @param Iv 加密向量，通常用医院、药店的编号
     * @return
     */
    public String encryptMode(byte[] key, String src, byte[] Iv)
    {
        return encryptMode(getKeyByte(key), src.getBytes(), Iv);
    }

    /**
     * 密码数据： 1、最大允许24位（byte）长度 2、允许任何字符，包含不可见字符集，中文字符
     * @param key
     * @return
     */
    private byte[] getKeyByte(String key)
    {
        byte[] data = null;
        try
        {
            if (null != key) data = key.getBytes(EncryptManager.charset);
            return getKeyByte(data);
        }
        catch (Throwable e)
        {
            EncryptManager.log(e);
        }
        return data;
    }

    public byte[] pswd = new byte[] { 0, 8, 0x2E, 0x2F, 0x7F, 1, 2, 3, 0xC, 0x10, 0x11, 0x12, 0x13, 0x14, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2b, 0x2a,
            0x9 };

    /**
     * 密码数据： 1、最大允许24位（byte）长度 2、允许任何字符，包含不可见字符集，中文字符
     * @param key
     * @return
     */
    private byte[] getKeyByte(byte[] data)
    {
        // 加密数据必须是24位，不足补0；超出24位则只取前面的24数据
        // 默认精心制作的24个特殊初始化密码，作为掩码
        byte[] newdata = new byte[24];
        System.arraycopy(pswd, 0, newdata, 0, 24);
        if (null == data || 0 == data.length) return newdata;
        int len = data.length;
        System.arraycopy(data, 0, newdata, 0, len > 24 ? 24 : len);
        return newdata;
    }

    /**
     * 向量处理: 必须8位长度
     * @param Iv
     * @return
     */
    private byte[] getIv(byte[] Iv)
    {
        if (null != Iv && 8 == Iv.length) return Iv;
        byte[] abIv = new byte[] { 0, 8, 0x2E, 0x2F, 0x7F, 1, 2, 3 };
        if (null != Iv) System.arraycopy(Iv, 0, abIv, 0, Math.min(8, Iv.length));

        return abIv;
    }

    /**
     * 获取向量
     * @param Iv
     * @return
     */
    private byte[] getIv(String Iv)
    {
        byte[] aR = null;
        if (null == Iv || 0 == Iv.length())
        {
            aR = getIv((byte[]) null);
        }
        else
        {
            try
            {
                aR = getIv(Iv.getBytes(EncryptManager.charset));
            }
            catch (Throwable e)
            {
                EncryptManager.log(e);
            }
        }
        return aR;
    }

    public String decryptMode(String keybyte, String src, String Iv)
    {
        key1 = keybyte;
        key2 = Iv;
        byte[] b1 = getKeyByte(keybyte), b2 = getIv(Iv);
        // [0, 1, 2, 3, 4, 8, 18, 19, 20, 27, 37, 38, 39, 40, 42, 43, 44, 45,
        // 46, 47, 41, 43, 42, 9]
        // [0, 1, 2, 3, 4, 8, 18, 19]
        return decryptMode(b1, hex2byte(src, getCode(b1) + getCode(b2)), b2);
    }

    public byte[] decryptModeForBytes(String keybyte, byte[] src, String Iv)
    {
        key1 = keybyte;
        key2 = Iv;
        byte[] b1 = getKeyByte(keybyte), b2 = getIv(Iv);
        // [0, 1, 2, 3, 4, 8, 18, 19, 20, 27, 37, 38, 39, 40, 42, 43, 44, 45,
        // 46, 47, 41, 43, 42, 9]
        // [0, 1, 2, 3, 4, 8, 18, 19]
        return decryptModeForBytes(b1, src, b2);
    }

    /**
     * 解密
     * @param keybyte
     * @param src
     * @param Iv 加密向量，通常用医院、药店的编号
     * @return
     */
    public String decryptMode(byte[] keybyte, byte[] src, byte[] Iv)
    {
        try
        {
            return new String(decryptModeForBytes(keybyte, src, Iv), EncryptManager.charset);
        }
        catch (Throwable e1)
        {
            EncryptManager.log(e1);
        }
        return null;
    }

    private byte[] decryptModeForBytes(byte[] keybyte, byte[] src, byte[] Iv)
    {
        try
        {
            Iv = getIv(Iv);
            // 生成密钥
            SecretKey deskey = getSecretKey(keybyte);// new
                                                     // SecretKeySpec(keybyte,
                                                     // Algorithm);
            // 解密
            IvParameterSpec iv = getIvParameterSpec(Iv);
            Cipher c1 = Cipher.getInstance(Transformation);
            c1.init(Cipher.DECRYPT_MODE, deskey, iv);
            byte[] data = c1.doFinal(src);
            return data;
        }
        catch (Throwable e1)
        {
            EncryptManager.log(e1);
        }
        return null;
    }

    private byte[] hex2byte(byte[] b)
    {
        if ((b.length % 2) != 0) throw new IllegalArgumentException(new String(new byte[] { -23, -107, -65, -27, -70, -90, -28, -72, -115, -26, -104, -81, -27,
                -127, -74, -26, -107, -80 }));
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2)
        {
            String item = new String(b, n, 2);
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个进制字节
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }

    /**
     * 避免byte []每次不一样的hashcode
     * @param a
     * @return
     */
    private int getCode(byte[] a)
    {
        int n = 0;
        for (byte k : a)
        {
            n += k;
        }
        return n;
    }

    private byte[] hex2byte(String b, int nCode)
    {
        if (0 == nCode) nCode = 5;// -1201873912
        // if(0 == b.replaceAll("[0-9a-fA-F]", "").length()) try
        // {
        // return hex2byte(b.getBytes(EncryptManager.charset));
        // }
        // catch (Exception e)
        // {
        // EncryptManager.log(e);
        // }
        return new Base64(nCode).decodeM(b);
    }

    // public static void main(String[] args)
    // {
    // byte []a = "长度不是偶数".getBytes();
    // for(byte k:a)
    // {
    // System.out.print((int)k + ",");
    // }
    // IConst.debug(new String(new
    // byte[]{-23,-107,-65,-27,-70,-90,-28,-72,-115,-26,-104,-81,-27,-127,-74,-26,-107,-80}));
    // }
}