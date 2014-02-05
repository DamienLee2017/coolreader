package com.dotcool.model;

/**
 * 书籍的基本信息
 * @author 002666(Lee)
 * date 2012/4/19
 */

public class Book
{
    private int bookId;                 //书籍ID
    private String bookName;            //书名
    private String bookAuthor;          //作者
    private String bookImgUrl;
    private String bookSummary;         //简介
    private long bookCreateTime;      //创建时间
    private long bookUpdateTime;      //更新时间
    private int bookHits;            //查看总数
    private int bookChapterCount;    //章节总数
    private String bookLastChapterTitle;//最后一张标题
    private String bookLastChapterID;   //最后一张ID
    private String bookPath;            //书籍路径
    private String bookAddTime;         //添加时间
    private String bookOpenTime;        //最后打开时间
    private int bookCategoryId;         //所属类别ID
    private String bookCategroyName;    //所属类别名
    private String bookSize;            //书籍大小
    private String bookProgress;        //最后阅读进度
    private int bookBeginPosition;
    public int getBookBeginPosition()
    {
        return bookBeginPosition;
    }
    public void setBookBeginPosition(int bookBeginPosition)
    {
        this.bookBeginPosition = bookBeginPosition;
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
    public String getBookAuthor()
    {
        return bookAuthor;
    }
    public void setBookAuthor(String bookAuthor)
    {
        this.bookAuthor = bookAuthor;
    }
    public String getBookPath()
    {
        return bookPath;
    }
    public void setBookPath(String bookPath)
    {
        this.bookPath = bookPath;
    }
    public String getBookAddTime()
    {
        return bookAddTime;
    }
    public void setBookAddTime(String bookAddTime)
    {
        this.bookAddTime = bookAddTime;
    }
    public String getBookOpenTime()
    {
        return bookOpenTime;
    }
    public void setBookOpenTime(String bookOpenTime)
    {
        this.bookOpenTime = bookOpenTime;
    }
    public int getBookCategoryId()
    {
        return bookCategoryId;
    }
    public void setBookCategoryId(int bookCategoryId)
    {
        this.bookCategoryId = bookCategoryId;
    }
    public String getBookCategroyName()
    {
        return bookCategroyName;
    }
    public void setBookCategroyName(String bookCategroyName)
    {
        this.bookCategroyName = bookCategroyName;
    }
    public String getBookSize()
    {
        return bookSize;
    }
    public void setBookSize(String bookSize)
    {
        this.bookSize = bookSize;
    }
    public String getBookProgress()
    {
        return bookProgress;
    }
    public void setBookProgress(String bookProgress)
    {
        this.bookProgress = bookProgress;
    }
    public String getBookImgUrl() {
        return bookImgUrl;
    }
    public void setBookImgUrl(String bookImgUrl) {
        this.bookImgUrl = bookImgUrl;
    }
    public String getBookSummary() {
        return bookSummary;
    }
    public void setBookSummary(String bookSummary) {
        this.bookSummary = bookSummary;
    }
    public long getBookCreateTime() {
        return bookCreateTime;
    }
    public void setBookCreateTime(long l) {
        this.bookCreateTime = l;
    }
    public long getBookUpdateTime() {
        return bookUpdateTime;
    }
    public void setBookUpdateTime(long bookUpdateTime) {
        this.bookUpdateTime = bookUpdateTime;
    }
    public int getBookHits() {
        return bookHits;
    }
    public void setBookHits(int bookHits) {
        this.bookHits = bookHits;
    }
    public int getBookChapterCount() {
        return bookChapterCount;
    }
    public void setBookChapterCount(int bookChapterCount) {
        this.bookChapterCount = bookChapterCount;
    }
    public String getBookLastChapterTitle() {
        return bookLastChapterTitle;
    }
    public void setBookLastChapterTitle(String bookLastChapterTitle) {
        this.bookLastChapterTitle = bookLastChapterTitle;
    }
    public String getBookLastChapterID() {
        return bookLastChapterID;
    }
    public void setBookLastChapterID(String bookLastChapterID) {
        this.bookLastChapterID = bookLastChapterID;
    }
}
