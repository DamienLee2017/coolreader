package com.dotcool.bll;

/**
 * 对数据库的具体操作，如：返回数据集、添加、删除...
 */

import java.util.ArrayList;

import com.dotcool.model.Book;
import com.dotcool.model.BookMark;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class DbDataOperation
{
	/**
	 * 得到书籍的详细信息
	 * @param resolver 
	 * @return 返回添加了数据的book对象
	 */
	public static ArrayList<Book> getBookInfo(ContentResolver resolver)
	{
		ArrayList<Book> bookList = new ArrayList<Book>();
		Book book;
		
		Cursor cursor = resolver.query(Uri.parse(DbTags.URI_TABLE_BOOK_INFO), null, null, null, null);
		while(cursor.moveToNext())
		{
			book = new Book();
			book.setBookId(Integer.parseInt(getFieldContent(cursor, DbTags.FIELD_BOOK_ID)));
			book.setBookName(getFieldContent(cursor, DbTags.FIELD_BOOK_NAME));
			book.setBookAuthor(getFieldContent(cursor, DbTags.FIELD_BOOK_AUTHOR));
			book.setBookPath(getFieldContent(cursor, DbTags.FIELD_BOOK_PATH));
			book.setBookAddTime(getFieldContent(cursor, DbTags.FIELD_BOOK_ADD_TIME));
			book.setBookOpenTime(getFieldContent(cursor, DbTags.FIELD_BOOK_OPEN_TIME));
			book.setBookCategoryId(Integer.parseInt(getFieldContent(cursor, DbTags.FIELD_BOOK_CATEGORY_ID)));
			book.setBookCategroyName(getFieldContent(cursor, DbTags.FIELD_BOOK_CATEGORY_NAME));
			book.setBookSize(getFieldContent(cursor, DbTags.FIELD_BOOK_SIZE));
			book.setBookProgress(getFieldContent(cursor, DbTags.FIELD_BOOK_PROGRESS));
			book.setBookBeginPosition(Integer.parseInt(getFieldContent(cursor, DbTags.FIELD_BOOK_BEGIN_POSITION)));
			bookList.add(book);
		}
		cursor.close();
		
		return bookList;   
	}
	
	/**
	 * 得到书签的详细信息
	 * @param resolver 
	 * @return 返回添加了数据的bookmark对象列表
	 */
	public static ArrayList<BookMark> getBookMark(ContentResolver resolver)
	{
		ArrayList<BookMark> bookMarkList = new ArrayList<BookMark>();
		BookMark bookMark;
		
		Cursor cursor = resolver.query(Uri.parse(DbTags.URI_TABLE_BOOK_MARK), null, null, null, null);
		while(cursor.moveToNext())
		{
			bookMark = new BookMark();
			bookMark.setBookMarkId(Integer.parseInt(getFieldContent(cursor, DbTags.FIELD_BOOK_MARK_ID)));
			bookMark.setBookId(Integer.parseInt(getFieldContent(cursor, DbTags.FIELD_BOOK_ID)));
			bookMark.setBookName(getFieldContent(cursor, DbTags.FIELD_BOOK_NAME));
			bookMark.setBookPath(getFieldContent(cursor, DbTags.FIELD_BOOK_PATH));
			bookMark.setBookMarkAddTime(getFieldContent(cursor, DbTags.FIELD_BOOK_MARK_ADD_TIME));
			bookMark.setBookMarkProgress(getFieldContent(cursor, DbTags.FIELD_BOOK_MARK_PROGRESS));
			bookMark.setBookMarkBeginPosition(Integer.parseInt(getFieldContent(cursor, DbTags.FIELD_BOOK_MARK_BEGIN_POSITION)));
			bookMark.setBookMarkDetail(getFieldContent(cursor, DbTags.FIELD_BOOK_MARK_DETAIL));
			bookMarkList.add(bookMark);
		}
		cursor.close();
		
		return bookMarkList;   
	}
	
	/**
	 * 将书籍信息添加到书籍信息表
	 * @param resolver
	 * @param bookId 书籍ID
	 * @param bookName书籍名
	 * @param bookAuthor 书籍作者
	 * @param BookPath 书籍存放路径
	 * @param bookAddTime 书籍加入书架时间
	 * @param bookOpenTime 书籍最近一次打开时间
	 * @param bookCategoryId 书籍所属类别ID
	 * @param bookCategoryName 书籍所属类别名
	 * @param bookSize 书籍大小
	 * @param bookProgress 书籍最近一次阅读进度     
	 */
	public static void insertToBookInfo(ContentResolver resolver,String bookName,String bookAuthor,String bookPath,String bookAddTime,
									String bookOpenTime,int bookCategoryId,String bookCategoryName,String bookSize,String bookProgress)
	{
		ContentValues values = new ContentValues();
		values.put(DbTags.FIELD_BOOK_NAME, bookName);
		values.put(DbTags.FIELD_BOOK_AUTHOR, bookAuthor);
		values.put(DbTags.FIELD_BOOK_PATH, bookPath);
		values.put(DbTags.FIELD_BOOK_ADD_TIME, bookAddTime);
		values.put(DbTags.FIELD_BOOK_OPEN_TIME, bookOpenTime);
		values.put(DbTags.FIELD_BOOK_CATEGORY_ID, bookCategoryId);
		values.put(DbTags.FIELD_BOOK_CATEGORY_NAME, bookCategoryName);
		values.put(DbTags.FIELD_BOOK_SIZE, bookSize);
		values.put(DbTags.FIELD_BOOK_PROGRESS, bookProgress);
		resolver.insert(Uri.parse(DbTags.URI_TABLE_BOOK_INFO), values);
	}
	
	public static void updateValuesToTable(ContentResolver contentResolver,Uri uri,ContentValues values,String where,String[] selectionArgs)
	{
		contentResolver.update(uri, values, where, selectionArgs);
	}
	
	/**
	 * 根据表中字段名获得该字段的内容
	 * @param cursor 数据库游标
	 * @param fieldName 字段名
	 * @return 返回字段内容
	 */
	public static String getFieldContent(Cursor cursor,String fieldName)
	{
		return cursor.getString(cursor.getColumnIndex(fieldName));
	}
	
	public static void deleteBook(ContentResolver resolver,int bookId)
	{
		resolver.delete(Uri.parse(DbTags.URI_TABLE_BOOK_INFO), DbTags.FIELD_BOOK_ID+"=?", new String[]{bookId+""});
	}
}
