package com.dotcool.model;

public class BookReadSetting
{
    private int bookId;         //书籍ID
    private int bookFontsize;   //字体大小
    private String bookBgStyle;//阅读背景
    private int bookFontR;      //字体颜色RGB中的R值
    private int bookFontG;      //字体颜色RGB中的G值
    private int bookFontB;      //字体颜色RGB中的B值
    private int bookBgR;        //背景颜色RGB中的R值
    private int bookBgG;        //背景颜色RGB中的G值
    private int bookBgB;        //背景颜色RGB中的B值
    private int bookMarginWidth;//文本左右边距
    private int bookMarginHeight;//文本上下边距
    public int getBookId()
    {
        return bookId;
    }
    public void setBookId(int bookId)
    {
        this.bookId = bookId;
    }
    public int getBookFontsize()
    {
        return bookFontsize;
    }
    public void setBookFontsize(int bookFontsize)
    {
        this.bookFontsize = bookFontsize;
    }
    public String getBookBgStyle()
    {
        return bookBgStyle;
    }
    public void setBookBgStyle(String bookBgStyle)
    {
        this.bookBgStyle = bookBgStyle;
    }
    public int getBookFontR()
    {
        return bookFontR;
    }
    public void setBookFontR(int bookFontR)
    {
        this.bookFontR = bookFontR;
    }
    public int getBookFontG()
    {
        return bookFontG;
    }
    public void setBookFontG(int bookFontG)
    {
        this.bookFontG = bookFontG;
    }
    public int getBookFontB()
    {
        return bookFontB;
    }
    public void setBookFontB(int bookFontB)
    {
        this.bookFontB = bookFontB;
    }
    public int getBookBgR()
    {
        return bookBgR;
    }
    public void setBookBgR(int bookBgR)
    {
        this.bookBgR = bookBgR;
    }
    public int getBookBgG()
    {
        return bookBgG;
    }
    public void setBookBgG(int bookBgG)
    {
        this.bookBgG = bookBgG;
    }
    public int getBookBgB()
    {
        return bookBgB;
    }
    public void setBookBgB(int bookBgB)
    {
        this.bookBgB = bookBgB;
    }
    public int getBookMarginWidth()
    {
        return bookMarginWidth;
    }
    public void setBookMarginWidth(int bookMarginWidth)
    {
        this.bookMarginWidth = bookMarginWidth;
    }
    public int getBookMarginHeight()
    {
        return bookMarginHeight;
    }
    public void setBookMarginHeight(int bookMarginHeight)
    {
        this.bookMarginHeight = bookMarginHeight;
    }
}
