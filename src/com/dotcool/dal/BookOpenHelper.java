package com.dotcool.dal;

/**
 * 创建数据库，并创建表
 * author Lee
 * date 2012/4/18
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class BookOpenHelper extends SQLiteOpenHelper
{
	private final static String DB_NAME = "ebook.db";
	
	//创建数据库
	public BookOpenHelper(Context context, String name, CursorFactory factory,
			int version)
	{
		super(context, DB_NAME, factory, version);
	}
	
	//类创建时调用，在这里主要创建table
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		String createBookInfoSql = "CREATE TABLE [book_info] ("+
				  "[book_id] INTEGER PRIMARY KEY AUTOINCREMENT,"+ 
				  "[book_name] VARCHAR,"+
				  "[book_author] VARCHAR, "+
				  "[book_path] VARCHAR, "+
				  "[book_add_time] VARCHAR,"+ 
				  "[book_open_time] VARCHAR, "+
				  "[book_category_id] INTEGER CONSTRAINT [book_category_fk] REFERENCES [book_category]([book_category_id]),"+ 
				  "[book_category_name] VARCHAR,"+
				  "[book_size] VARCHAR, "+
				  "[book_progress] VARCHAR,"+
				 "[book_begin_position] INTEGER NOT NULL DEFAULT (0));";
		String createBookCategorySql = "CREATE TABLE [book_category] ("+
				  "[book_category_id] INTEGER PRIMARY KEY AUTOINCREMENT,"+ 
				  "[book_category_name] VARCHAR, "+
				  "[book_id] VARCHAR, "+
				  "[book_name] VARCHAR);";
		String createBookMarkSql = "CREATE TABLE [book_mark] ("+
				  "[book_mark_id] INTEGER PRIMARY KEY AUTOINCREMENT,"+ 
				  "[book_id] INTEGER CONSTRAINT [book_mark_fk] REFERENCES [book_info]([book_id]),"+ 
				  "[book_name] VARCHAR, "+
				  "[book_path] VARCHAR, "+
				  "[book_mark_add_time] VARCHAR, "+
				  "[book_mark_progress] VARCHAR,"+
				  "[book_mark_detail] VARCHAR," +
				  "[book_mark_begin_position] INTEGER);";
		
		String createBookReadSettingSql = "CREATE TABLE [book_read_setting] ("+
				  "[book_id] INTEGER, "+
				  "[book_fontsize] INTEGER(2) NOT NULL,"+ 
				  "[book_bg_style] VARCHAR, "+
				  "[book_font_r] INTEGER(3) NOT NULL DEFAULT (0),"+ 
				  "[book_font_g] INTEGER(3) NOT NULL DEFAULT (0), "+
				  "[book_font_b] INTEGER(3) NOT NULL DEFAULT (0), "+
				  "[book_bg_r] INTEGER(3) NOT NULL DEFAULT (0), "+
				  "[book_bg_g] INTEGER(3) NOT NULL DEFAULT (0), "+
				  "[book_bg_b] INTEGER(3) NOT NULL DEFAULT (0), "+
				  "[book_margin_width] INTEGER(2) NOT NULL, "+
				  "[book_margin_height] INTEGER(2) NOT NULL);";
		
		String createBookChapterSql = "CREATE TABLE [book_chapter] ("+
				  "[book_id] INTEGER PRIMARY KEY, "+
				  "[book_name] VARCHAR, "+
				  "[book_chapter_name] VARCHAR,"+ 
				  "[book_chapter_begin_psition] INTEGER);";

		
		db.execSQL(createBookInfoSql);
		db.execSQL(createBookCategorySql);
		db.execSQL(createBookMarkSql);      
		db.execSQL(createBookReadSettingSql);
		db.execSQL(createBookChapterSql);
	}

	//版本升级时调用
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		String dropBookInfoSql = "drop table if exists book_info";
		String dropBookCategorySql = "drop table if exists book_category";
		String dropBookMarkSql = "drop table if exists book_mark";
		String dropBookSettingSql = "drop table if exists book_read_setting";
		String dropBookChapterSql = "drop table if exists book_chapter";
		
		db.execSQL(dropBookInfoSql);
		db.execSQL(dropBookCategorySql);
		db.execSQL(dropBookMarkSql);
		db.execSQL(dropBookSettingSql);
		db.execSQL(dropBookChapterSql);
		
		onCreate(db);
	}

}
