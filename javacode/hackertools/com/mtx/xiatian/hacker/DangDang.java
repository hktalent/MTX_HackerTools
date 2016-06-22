package com.mtx.xiatian.hacker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * <pre>
 * 处理网络上流传的“当当”泄露的数据 
 * CREATE TABLE `mydb`.`dangdang` (
  `email` VARCHAR(50) NOT NULL COMMENT '邮箱', 
  `xm` VARCHAR(45) NULL COMMENT '姓名',
   `dz` VARCHAR(300) NULL COMMENT
  '地址', `bh1` INT NULL COMMENT '编号',
   `bh2` INT NULL COMMENT '编号', `tel2`
  VARCHAR(45) NULL COMMENT '座机',
   `tel` VARCHAR(45) NULL COMMENT '手机',
    `jg` DECIMAL(5,2) NULL COMMENT '价格', 
    PRIMARY KEY (`email`));
</pre>
 * @author xiatian
 * 
 */
public class DangDang extends CommonTools
{
	
	public void doOneExcel(String excelFilePath, final List<TreeMap<String, Object>> list)
	{
		XSSFWorkbook workbook = null;
		XSSFSheet firstSheet = null;
		FileInputStream inputStream = null;
		try
		{
			long lnCnt = 0;
			TreeMap<String, Object> map = null;

			System.out.println("开始打开文件...");
			System.out.println(excelFilePath);
			inputStream = new FileInputStream(new File(excelFilePath));
			workbook = new XSSFWorkbook(inputStream);
			firstSheet = workbook.getSheetAt(0);
			Iterator<Row> iterator = firstSheet.iterator();
			String[] a =
			{ "email", "xm", "dz", "bh1", "bh2", "tel2", "tel", "jg" };
			int nC = 0;
			Row nextRow = null;
			Iterator<Cell> cellIterator = null;
			Cell cell = null;
			String szCol = "";
			// 行处理
			while (iterator.hasNext())
			{
				nextRow = iterator.next();
				lnCnt++;
				// if(lnCnt < 275501)continue;
				if (0L == lnCnt % 30000)
				{
					System.out.println("开始处理数据行：" + lnCnt);
					System.gc();
				}
				cellIterator = nextRow.cellIterator();
				nC = 0;
				map = new TreeMap<String, Object>();
				// 列
				while (cellIterator.hasNext())
				{
					// 避免越界
					if (nC >= a.length)
						break;

					cell = cellIterator.next();
					switch (cell.getCellType())
					{
						case Cell.CELL_TYPE_STRING:
							szCol = cell.getStringCellValue();
							break;
						case Cell.CELL_TYPE_BOOLEAN:
							szCol = String.valueOf(cell.getBooleanCellValue());
							break;
						case Cell.CELL_TYPE_NUMERIC:
							double dV = cell.getNumericCellValue();
							BigDecimal bd = new BigDecimal(dV);
							szCol = bd.toBigInteger().toString();
							break;
					}
					if (szCol.startsWith("receiver_"))
						break;
					// if(6 == nC)
					// System.out.println(szCol);
					map.put(a[nC++], szCol.trim());
				}
				if (0 == map.size())
					continue;
				list.add(map);
				// 通知有数据
				synchronized (list)
				{
					list.notifyAll();
					// System.out.println("已经通知其他线程有数据");
					// // 等待取走数据
					// System.out.println("等待取走数据");
				}
				synchronized (list)
				{
					while (0 < list.size())
					{
						list.wait();
					}
				}
			}
			list.add(null);
			synchronized (list)
			{
				list.notifyAll();
			}
			System.out.println("文件处理完毕，共行数： " + lnCnt);
			System.gc();
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if (null != workbook)
					workbook.close();
				inputStream.close();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 处理文本文件
	 * @param excelFilePath
	 * @param list
	 */
	public void doOneText(String excelFilePath, final List<TreeMap<String, Object>> list)
	{
		System.out.println("开始打开文件...");
		System.out.println(excelFilePath);
		String[] a =
		{ "email", "xm", "dz", "bh1", "bh2", "tel2", "tel", "jg" };
		String[] a1 = null;
		long lnCnt = 0;
		TreeMap<String, Object> map = null;
		BufferedReader reader = null;
		int i, j;
		try
		{
			reader = new BufferedReader(new FileReader(new File(excelFilePath)));
			String s = null;
			while (null != (s = reader.readLine()))
			{
				s = s.trim();
				if (0 == s.length())
					continue;
				a1 = s.split("\t|       ");
				map = new TreeMap<String, Object>();
				lnCnt++;
				if (0L == lnCnt % 30000)
				{
					System.out.println("开始处理数据行：" + lnCnt);
					System.gc();
				}
				for (i = 0, j = Math.min(a1.length, a.length); i < j; i++)
				{
					map.put(a[i], a1[i]);
				}
				list.add(map);
				// 通知有数据
				synchronized (list)
				{
					list.notifyAll();
				}
				synchronized (list)
				{
					while (0 < list.size())
					{
						list.wait();
					}
				}
			}
			list.add(null);
			synchronized (list)
			{
				list.notifyAll();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void doOneFile(String excelFilePath)
	{
		final List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>();
		System.out.println("开始insert...");
		// 必须以并行线程进行
		new Thread(new Runnable()
		{
			public void run()
			{
				insert("dangdang", list);
			}
		}).start();
		if (excelFilePath.endsWith(".txt"))
			doOneText(excelFilePath, list);
		else
			doOneExcel(excelFilePath, list);

	}

	public void doOneFile1(String excelFilePath)
	{
		XSSFWorkbook workbook = null;
		XSSFSheet firstSheet = null;
		FileInputStream inputStream = null;
		try
		{
			System.out.println(excelFilePath);
			long lnCnt = 0;
			super.useMysql();
			super.hvLastScan = false;
			final List<TreeMap<String, Object>> list = new ArrayList<TreeMap<String, Object>>();
			TreeMap<String, Object> map = null;

			System.out.println("开始打开文件...");
			inputStream = new FileInputStream(new File(excelFilePath));
			workbook = new XSSFWorkbook(inputStream);
			firstSheet = workbook.getSheetAt(0);
			Iterator<Row> iterator = firstSheet.iterator();
			System.out.println("开始insert...");
			// 必须以并行线程进行
			new Thread(new Runnable()
			{
				public void run()
				{
					insert("dangdang", list);
				}
			}).start();
			String[] a =
			{ "email", "xm", "dz", "bh1", "bh2", "tel2", "tel", "jg" };
			int nC = 0;
			Row nextRow = null;
			Iterator<Cell> cellIterator = null;
			Cell cell = null;
			String szCol = "";
			// 行处理
			while (iterator.hasNext())
			{
				nextRow = iterator.next();
				lnCnt++;
				// if(lnCnt < 275501)continue;
				if (0L == lnCnt % 30000)
				{
					System.out.println("开始处理数据行：" + lnCnt);
					System.gc();
				}
				cellIterator = nextRow.cellIterator();
				nC = 0;
				map = new TreeMap<String, Object>();
				// 列
				while (cellIterator.hasNext())
				{
					// 避免越界
					if (nC >= a.length)
						break;

					cell = cellIterator.next();
					switch (cell.getCellType())
					{
						case Cell.CELL_TYPE_STRING:
							szCol = cell.getStringCellValue();
							break;
						case Cell.CELL_TYPE_BOOLEAN:
							szCol = String.valueOf(cell.getBooleanCellValue());
							break;
						case Cell.CELL_TYPE_NUMERIC:
							double dV = cell.getNumericCellValue();
							BigDecimal bd = new BigDecimal(dV);
							szCol = bd.toBigInteger().toString();
							break;
					}
					if (szCol.startsWith("receiver_"))
						break;
					// if(6 == nC)
					// System.out.println(szCol);
					map.put(a[nC++], szCol.trim());
				}
				if (0 == map.size())
					continue;
				list.add(map);
				// 通知有数据
				synchronized (list)
				{
					list.notifyAll();
					// System.out.println("已经通知其他线程有数据");
					// // 等待取走数据
					// System.out.println("等待取走数据");
				}
				synchronized (list)
				{
					while (0 < list.size())
					{
						list.wait();
					}
				}
			}
			list.add(null);
			synchronized (list)
			{
				list.notifyAll();
			}
			System.out.println("文件处理完毕，共行数： " + lnCnt);
			System.gc();
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if (null != workbook)
					workbook.close();
				inputStream.close();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// 01.xlsx 02.xlsx 03.xlsx 04.xlsx 05.xlsx 06.xlsx 07.xlsx 09.xlsx
		// 10.xlsx 11.xlsx 13.xlsx 14.xlsx 15.xlsx 16.xlsx 17.xlsx 18.xlsx
		// 19.xlsx 20.xlsx 21.xlsx 22.xlsx 23.xlsx 24.xlsx 25.xlsx 26.xlsx
		// 27.xlsx
		final String[] aFs = "DD_SH.txt".split(" ");
		// 文件存在的目录
		final String path = "./data/dangdang/";
		final DangDang dd = new DangDang();
		for (String s : aFs)
		{
			dd.doOneFile(path + s.trim());
		}
	}

}
