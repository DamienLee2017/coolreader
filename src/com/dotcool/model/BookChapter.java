package com.dotcool.model;

public class BookChapter
{
    private int bookId;//书籍ID
    private String bookName;//书名
    private String bookChapterName;//章节名
    private String bookChapterBeginPosition;//章节界面文本Begin值
    public int getBookId()
    {
        return bookId;
    }
    public void setBookId(int bookId)
    {
        this.bookId = bookId;
    }
    public String getBookName()
    {
        return bookName;
    }
    public void setBookName(String bookName)
    {
        this.bookName = bookName;
    }
    public String getBookChapterName()
    {
        return bookChapterName;
    }
    public void setBookChapterName(String bookChapterName)
    {
        this.bookChapterName = bookChapterName;
    }
    public String getBookChapterBeginPosition()
    {
        return bookChapterBeginPosition;
    }
    public void setBookChapterBeginPosition(String bookChapterBeginPosition)
    {
        this.bookChapterBeginPosition = bookChapterBeginPosition;
    }
}
