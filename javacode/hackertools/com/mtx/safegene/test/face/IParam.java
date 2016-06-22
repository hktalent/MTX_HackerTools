package com.mtx.safegene.test.face;

import java.util.Map;

import org.apache.http.client.methods.HttpUriRequest;
/**
 * 测试所需方法
 * @author Administrator
 *
 */
public interface IParam {
	
	/**
	 * 总耗时
	 * @return
	 */
	public long getCurTime();
	public int getOkCnt();
	/**
	 * 红色字体处理
	 * @param s
	 */
	public String doColor(String s);
	/**
	 * 测试次数
	 * @return
	 */
	public int getCount();
	
	/**
	 * 测试用例适用框架
	 * 0表示通用
	 * 1表示适用于ta2
	 * 2表示适用于ta3
	 * 3表示适用于leaf
	 * @return
	 */
	public int getFrameType();

	/**
	 * 生成测试请求数据
	 * 
	 * @param mParams
	 * @param headers
	 * @param posts
	 *            TODO
	 * @return
	 */
	public  String makeRequestDataHs(Map<String, Object> mParams,
			Map<String, String> headers, Map<String, HttpUriRequest> posts);
	
	public boolean checkResult(Map<String, String> headers, String sContent);
	/**
	 * 检测结果
	 * @param headers
	 * @param sContent
	 */
	public void checkResultAddIndex(Map<String, String> headers, String sContent);
	/**
	 * 测试是否成功
	 * @return
	 */
	public Boolean isOk();
}
