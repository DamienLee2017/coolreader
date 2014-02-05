package com.dotcool.model;

public class BookMark
{
    private int bookMarkId;//书签ID
    private int bookId;//书籍ID
    private String bookName;//书名
    private String bookPath;//书籍路径
    private String bookMarkAddTime;//书签添加时间
    private String bookMarkProgress;//书签进度
    private String bookMarkDetail;//书签描述
    private int bookMarkBeginPosition;//书签添加时文本Begin值
    public String getBookMarkDetail()
    {
        return bookMarkDetail;
    }
    public void setBookMarkDetail(String bookMarkDetail)
    {
        this.bookMarkDetail = bookMarkDetail;
    }
    public int getBookMarkBeginPosition()
    {
        return bookMarkBeginPosition;
    }
    public void setBookMarkBeginPosition(int bookMarkBeginPosition)
    {
        this.bookMarkBeginPosition = bookMarkBeginPosition;
    }
    public int getBookMarkId()
    {
        return bookMarkId;
    }
    public void setBookMarkId(int bookMarkId)
    {
        this.bookMarkId = bookMarkId;
    }
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
    public String getBookMarkProgress()
    {
        return bookMarkProgress;
    }
    public void setBookMarkProgress(String bookMarkProgress)
    {
        this.bookMarkProgress = bookMarkProgress;
    }
    public String getBookPath()
    {
        return bookPath;
    }
    public void setBookPath(String bookPath)
    {
        this.bookPath = bookPath;
    }
    public String getBookMarkAddTime()
    {
        return bookMarkAddTime;
    }
    public void setBookMarkAddTime(String bookMarkAddTime)
    {
        this.bookMarkAddTime = bookMarkAddTime;
    }
    
}
