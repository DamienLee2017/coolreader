package com.dotcool.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dotcool.model.Book;

import android.util.Log;

/**
 * 文件操作的一些工具，如计算文件的大小...
 * @author 002666(lee)
 * date 2012/4/25
 */
public class BookUtil
{
	/**
	 * 获得文件名称（不包含路径和后缀名）
	 * @param path 文件所在目录
	 * @param fileName 文件名（包含文件路径）
	 * @return 返回结果
	 */
	public static String getBookName(String path,String fileName)
	{
		String tempStr = fileName.replaceAll(path+"/", "");
		String replaceStr = "";
		Pattern p = Pattern.compile("\\.{1}[A-Za-z]+$");
		Matcher m = p.matcher(tempStr);
		if(m.find())
			replaceStr = m.group();
		return tempStr.replaceAll(replaceStr, "");
	}
	
	/**
	 * 获得文件的格式（不包含路径和后缀名）
	 * @param path 文件所在目录
	 * @param fileName 文件名（包含文件路径）
	 * @return 返回结果
	 */
	public static String getBookFormat(String fileName)
	{
		String tempStr = "";
		Pattern p = Pattern.compile("\\.{1}[A-Za-z]+$");
		Matcher m = p.matcher(fileName);
		if(m.find())
			tempStr = m.group();
		return tempStr.replace(".", "");
	}
	
	/**
	 * 计算指定路径的文件的大小
	 * @param fileName 文件名（包含文件路径）
	 * @return 返回计算的结果
	 */
	public static String getBookSize(String fileName)
	{
		File file = new File(fileName);
		long fileSize = file.length()/1024;
		if(fileSize<1024)	
		{
			return fileSize+" KB";
		}
		else
		{
			double tempSize = (double)fileSize/1024;
			BigDecimal bd = new BigDecimal(tempSize);
			return bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+" M";
		}
	}   
	
	/**
	 * 得到书籍作者
	 * @param fileName 文件名（包含文件路径）
	 * @return 返回书籍作者名
	 */
	public static String getBookAuthor(String fileName)
	{
//		作者：{1}.+\n$
		String bookAuthor = "未知";
		try
		{
			RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
			String temp = raf.readLine().toString();
			Log.i("内容",temp+"aaa");
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return bookAuthor;
	}
	
	/**
	 * 判断书架中是否已经存在此书
	 * @param bookList 书架中的全部书籍
	 * @param fileName 文件名（包含文件路径）
	 * @return 返回一个boolean型值 true为存在（不可以加入书架） false为不存在（可以加入书架）
	 */
	public static boolean isExist(ArrayList<Book> bookList,String fileName)
	{
		boolean isExist = false;
		for(int i=0;i<bookList.size();i++)
		{
			if(fileName.equals(bookList.get(i).getBookPath()))	
			{
				isExist = true;
				break;
			}
		}
		System.out.println(isExist+"2");
		return isExist;
	}
	
	public List<Book> getLatelyBookList()
	{
		return null;
	}
	
	/**
	 * 得到书籍的内容
	 * @param path 书籍路径
	 * @return
	 */
	public static StringBuilder getBookContent(String path)
	{
		StringBuilder bookContent = new StringBuilder();
		File file = new File(path);
	      
	    try
		{
	    	FileInputStream is = new FileInputStream(file);
			InputStreamReader streamReader = new InputStreamReader(is,"gb2312");
			BufferedReader br = new BufferedReader(streamReader);
			String temp = "";
			while(((temp=br.readLine())!=null))
			{
				bookContent.append(temp);
			}
		} catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 获得电子书的编码方式
	 * @param fileName 文件名，包含路径
	 * @return 返回结果
	 */
	public static String getCharsetName(String fileName)
	{
		InputStream inputStream;
		String code="";
		code="gb2312";
		try
		{
			inputStream = new FileInputStream(fileName);
			byte []head=new byte[3];
			inputStream.read(head);			
			if (head[0] == -1 && head[1] == -2 )              
				code = "UTF-16";          
			if (head[0] == -2 && head[1] == -1 )             
				code = "Unicode";          
			if(head[0]==-17 && head[1]==-69 && head[2] ==-65)             
				code = "UTF-8";		
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return code;
	}

}
