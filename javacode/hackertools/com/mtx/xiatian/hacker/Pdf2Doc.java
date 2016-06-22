package com.mtx.xiatian.hacker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 * pdf转换为doc
 * @author xiatian
 */
public class Pdf2Doc
{

	public static String pdftoText(String fileName)
	{
		PDFParser parser;
		String parsedText = null;
		PDFTextStripper pdfStripper = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		File file = new File(fileName);
		if (!file.isFile())
		{
			System.err.println("File " + fileName + " does not exist.");
			return null;
		}
		try
		{
			parser = new PDFParser((RandomAccessRead) new FileInputStream(file));
		} catch (Exception e)
		{
			System.err.println("Unable to open PDF Parser. " + e.getMessage());
			return null;
		}
		try
		{
			parser.parse();
			cosDoc = parser.getDocument();
			pdfStripper = new PDFTextStripper();
			pdDoc = new PDDocument(cosDoc);
			parsedText = pdfStripper.getText(pdDoc);
		} catch (Exception e)
		{
			System.err.println("An exception occured in parsing the PDF Document." + e.getMessage());
		} finally
		{
			try
			{
				if (cosDoc != null)
					cosDoc.close();
				if (pdDoc != null)
					pdDoc.close();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return parsedText;
	}

	public static void main(String args[])
	{

		try
		{
			String PDF_FILE_PATH = "./";

			String content = pdftoText(PDF_FILE_PATH);

			File file = new File("/sample/filename.txt");

			// if file doesnt exists, then create it
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();

			System.out.println("Done");

		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
