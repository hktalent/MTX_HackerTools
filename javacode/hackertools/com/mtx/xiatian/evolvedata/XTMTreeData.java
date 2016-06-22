package com.mtx.xiatian.evolvedata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mtx.face.IMyResultHandler;

public class XTMTreeData implements Serializable{
	private static final long serialVersionUID = 1L;
	private XTMTreeData parent = null;
	private char k = 0;
	// 多个子组件
	private List<XTMTreeData> child = null;
	
	private List<XTMTreeData> relation = null;
	
	/**
	 * 添加关联对象，例如个人编号和姓名对象关联
	 * @param n
	 * @return
	 */
	public XTMTreeData andRelationXTMTreeData(XTMTreeData n)
	{
		if(null == relation)relation =  new ArrayList<XTMTreeData>();
		relation.add(n);
		return this;
	}
	
	
	/**
	 * 添加一个节点，并设置parent
	 * @param node
	 * @return
	 */
	public XTMTreeData addXTMTreeData(XTMTreeData node)
	{
		if(null == child)
			child = new ArrayList<XTMTreeData>();
		child.add(node);
		node.parent = this;
		return node;
	}
	
	/**
	 * 根据key获取并返回节点，如果该节点不存在就创建她
	 * @param s
	 * @return
	 */
	public XTMTreeData getNodeByKey(char s)
	{
		if(null != child)
		for(XTMTreeData n:child)
		   if(s == n.k)
			   return n;
		XTMTreeData	xta = new XTMTreeData(s);
		addXTMTreeData(xta);
		return xta;
	}
	
	/**
	 * 根据路径，返回最终的末节点
	 * @param s
	 * @return
	 */
	public XTMTreeData getNodeAndCreate(String s)
	{
		char []a = s.toCharArray();
		XTMTreeData curNode = this;
		for(int i = 0, j = a.length; i < j; i++)
		{
			if(0 == a[i])continue;
			curNode = curNode.getNodeByKey(a[i]);
		}
		return curNode;
	}
	
	/**
	 * 获取根节点，根节点没有数据
	 * @return
	 */
	public XTMTreeData getRoot()
	{
		if(null == parent)return this;
		XTMTreeData p = parent;
		while(null != p.parent)
			p = p.parent;
		return p;
	}

	/**
	 * 返回当前节点的key
	 * @return
	 */
	public char getKey()
	{
		return k;
	}

	public XTMTreeData(char key){
		k = key;
	}

	/**
	 * 当前节点转换出路径信息
	 */
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		char szS = 0;
		if(0 != k)
			sb.append(k);
		
		XTMTreeData p = parent;
		while(null != p)
		{
			szS = p.getKey();
			if(0 != szS)
			{
				sb.insert(0, szS);
				p = p.parent;
				continue;
			}
			break;
		}
		return sb.toString();
	}

	public static void main(String []arg)
	{
		final XTMTreeData root = new XTMTreeData((char)0);
		final List <Map<String, Object>> lst = new ArrayList<Map<String, Object>>();
		// 判断存在否？
		MyQDataInfo.sdb.queryForList("SELECT * FROM ac01  limit 0,1000000", new IMyResultHandler(){
					public void doResult(Map<String, Object> m) {
//						root.getNodeAndCreate(String.valueOf(m.get("aac003")))
//						.andRelationXTMTreeData(
//						root.getNodeAndCreate(String.valueOf(m.get("aac002")))
//						).andRelationXTMTreeData(
//								root.getNodeAndCreate(String.valueOf(m.get("aac001")))
//								);
					}});
		MyQDataInfo.writeObject("10万缓存", root);
		
	}
}
