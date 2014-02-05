package com.dotcool.bll;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.Vector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.Log;

public class BookPageFactory {

	private File book_file = null;
	private MappedByteBuffer m_mbBuf = null;
	private int m_mbBufLen = 0;
	private int m_mbBufBegin = 0;
	private int m_mbBufEnd = 0;
	private String m_strCharsetName = "GBK";
	private Bitmap m_book_bg = null;
	private int mWidth;
	private int mHeight;

	private Vector<String> m_lines = new Vector<String>();

	private int m_fontSize = 25;
	private int m_textColor = Color.BLACK;
	private int m_backColor = 0xffff9e85; // 背景颜色
	private int marginWidth = 15; // 左右与边缘的距离
	private int marginHeight = 20; // 上下与边缘的距离

	private int mLineCount; // 每页可以显示的行数
	private float mVisibleHeight; // 绘制内容的高
	private float mVisibleWidth; // 绘制内容的宽
	private boolean m_isfirstPage,m_islastPage;
	
	private String strPercent;//进度
	private String bookName;//书名
	
//	 private int m_nLineSpaceing = 5;

	private Paint mPaint;
	
	private int currentBeginPosition;

	public BookPageFactory(int w, int h) {
		mWidth = w;
		mHeight = h;
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextAlign(Align.LEFT);
		mPaint.setTextSize(m_fontSize);
		mPaint.setColor(m_textColor);
		mVisibleWidth = mWidth - marginWidth * 2;
		mVisibleHeight = mHeight - marginHeight * 2;
		mLineCount = (int) (mVisibleHeight / m_fontSize); // 可显示的行数
	}

	public void openbook(String strFilePath) throws IOException {
		book_file = new File(strFilePath);
		long lLen = book_file.length();
		m_mbBufLen = (int) lLen;
		m_mbBuf = new RandomAccessFile(book_file, "r").getChannel().map(
				FileChannel.MapMode.READ_ONLY, 0, lLen);
	}


	protected byte[] readParagraphBack(int nFromPos) {
		int nEnd = nFromPos;
		int i;
		byte b0, b1;
		if (m_strCharsetName.equals("UTF-16LE")) {
			i = nEnd - 2;
			while (i > 0) {
				b0 = m_mbBuf.get(i);
				b1 = m_mbBuf.get(i + 1);
				if (b0 == 0x0a && b1 == 0x00 && i != nEnd - 2) {
					i += 2;
					break;
				}
				i--;
			}

		} else if (m_strCharsetName.equals("UTF-16BE")) {
			i = nEnd - 2;
			while (i > 0) {
				b0 = m_mbBuf.get(i);
				b1 = m_mbBuf.get(i + 1);
				if (b0 == 0x00 && b1 == 0x0a && i != nEnd - 2) {
					i += 2;
					break;
				}
				i--;
			}
		} else {
			i = nEnd - 1;
			while (i > 0) {
				b0 = m_mbBuf.get(i);
				if (b0 == 0x0a && i != nEnd - 1) {
					i++;
					break;
				}
				i--;
			}
		}
		if (i < 0)
			i = 0;
		int nParaSize = nEnd - i;
		int j;
		byte[] buf = new byte[nParaSize];
		for (j = 0; j < nParaSize; j++) {
			buf[j] = m_mbBuf.get(i + j);
		}
		return buf;
	}


	// 读取上一段落
	protected byte[] readParagraphForward(int nFromPos) {
		int nStart = nFromPos;
		int i = nStart;
		byte b0, b1;
		// 根据编码格式判断换行
		if (m_strCharsetName.equals("UTF-16LE")) {
			while (i < m_mbBufLen - 1) {
				b0 = m_mbBuf.get(i++);
				b1 = m_mbBuf.get(i++);
				if (b0 == 0x0a && b1 == 0x00) {
					break;
				}
			}
		} else if (m_strCharsetName.equals("UTF-16BE")) {
			while (i < m_mbBufLen - 1) {
				b0 = m_mbBuf.get(i++);
				b1 = m_mbBuf.get(i++);
				if (b0 == 0x00 && b1 == 0x0a) {
					break;
				}
			}
		} else {
			while (i < m_mbBufLen) {
				b0 = m_mbBuf.get(i++);
				if (b0 == 0x0a) {
					break;
				}
			}
		}
		int nParaSize = i - nStart;
		byte[] buf = new byte[nParaSize];
		for (i = 0; i < nParaSize; i++) {
			buf[i] = m_mbBuf.get(nFromPos + i);
		}
		return buf;
	}

	public Vector<String> pageDown() {
		String strParagraph = "";
		Vector<String> lines = new Vector<String>();
		while (lines.size() < mLineCount && m_mbBufEnd < m_mbBufLen) {
			byte[] paraBuf = readParagraphForward(m_mbBufEnd); // 读取一个段落
			m_mbBufEnd += paraBuf.length;
			try {
				strParagraph = new String(paraBuf, m_strCharsetName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			String strReturn = "";
			if (strParagraph.indexOf("\r\n") != -1) {
				strReturn = "\r\n";
				strParagraph = strParagraph.replaceAll("\r\n", "");
			} else if (strParagraph.indexOf("\n") != -1) {
				strReturn = "\n";
				strParagraph = strParagraph.replaceAll("\n", "");
			}    

			if (strParagraph.length() == 0) {
				lines.add(strParagraph);
			}
			while (strParagraph.length() > 0) {
				int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth,
						null);
				lines.add(strParagraph.substring(0, nSize));
				strParagraph = strParagraph.substring(nSize);
				if (lines.size() >= mLineCount) {
					break;
				}
			}
			if (strParagraph.length() != 0) {
				try {
					m_mbBufEnd -= (strParagraph + strReturn)
							.getBytes(m_strCharsetName).length;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return lines;
	}

	public Vector<String> pageUp() {
		if (m_mbBufBegin < 0)
			m_mbBufBegin = 0;
		Vector<String> lines = new Vector<String>();
		String strParagraph = "";
		while (lines.size() < mLineCount && m_mbBufBegin > 0) {
			Vector<String> paraLines = new Vector<String>();
			byte[] paraBuf = readParagraphBack(m_mbBufBegin);
			m_mbBufBegin -= paraBuf.length;
			try {
				strParagraph = new String(paraBuf, m_strCharsetName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			strParagraph = strParagraph.replaceAll("\r\n", "");
			strParagraph = strParagraph.replaceAll("\n", "");

			if (strParagraph.length() == 0) {
				paraLines.add(strParagraph);
			}
			while (strParagraph.length() > 0) {
				int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth,
						null);
				paraLines.add(strParagraph.substring(0, nSize));
				strParagraph = strParagraph.substring(nSize);
			}
			lines.addAll(0, paraLines);
		}
		while (lines.size() > mLineCount) {
			try {
				m_mbBufBegin += lines.get(0).getBytes(m_strCharsetName).length;
				lines.remove(0);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		m_mbBufEnd = m_mbBufBegin;
		return lines;
	}

	public void prePage() throws IOException {
		if (m_mbBufBegin <= 0) {
			m_mbBufBegin = 0;
			m_isfirstPage=true;
			return;
		}else m_isfirstPage=false;
		m_lines.clear();
		pageUp();
		m_lines = pageDown();
	}

	public void nextPage() throws IOException {
		if (m_mbBufEnd >= m_mbBufLen) {
			m_islastPage=true;
			return;
		}else m_islastPage=false;
		m_lines.clear();
		m_mbBufBegin = m_mbBufEnd;
		m_lines = pageDown();
	}

	public void Draw(Canvas c) {
		if (m_lines.size() == 0)
			m_lines = pageDown();
		if (m_lines.size() > 0) {
			if (m_book_bg == null)
				c.drawColor(m_backColor);
			else
				c.drawBitmap(m_book_bg, 0, 0, null);
			int y = marginHeight;
			for (String strLine : m_lines) {
				y += m_fontSize;
				//Log.d("dots", strLine);
				c.drawText(strLine, marginWidth, y, mPaint);
			}
		}
		float fPercent = (float) (m_mbBufBegin * 1.0 / m_mbBufLen);
		DecimalFormat df = new DecimalFormat("#0.0");
		strPercent = df.format(fPercent * 100) + "%";
		int nPercentWidth = (int) mPaint.measureText("999.9%") + 1;
		mPaint.setTextSize(13);
		c.drawText(strPercent, mWidth - nPercentWidth, mHeight - 5, mPaint);
		mPaint.setTextSize(m_fontSize);
//		bookName = "墨门飞甲";
//		c.drawText(bookName, mWidth/2-mPaint.getTextSize(), mHeight - 5, mPaint);
	}

	public void setBgBitmap(Bitmap BG) {
		m_book_bg = BG;
	}
	
	public boolean isfirstPage() {
		return m_isfirstPage;
	}
	public boolean islastPage() {
		return m_islastPage;
	}

	public int getM_fontSize()
	{
		return m_fontSize;
	}

	public void setM_fontSize(int m_fontSize)
	{
		this.m_fontSize = m_fontSize;
	}

	public Paint getmPaint()
	{
		return mPaint;
	}

	public void setmPaint(Paint mPaint)
	{
		this.mPaint = mPaint;
	}
	
	public float getmVisibleHeight()
	{
		return mVisibleHeight;
	}

	public void setmVisibleHeight(float mVisibleHeight)
	{
		this.mVisibleHeight = mVisibleHeight;
	}

	public int getmLineCount()
	{
		return mLineCount;
	}

	public void setmLineCount(int mLineCount)
	{
		this.mLineCount = mLineCount;
	}
	
	public int getM_textColor()
	{
		return m_textColor;
	}

	public void setM_textColor(int m_textColor)
	{
		this.m_textColor = m_textColor;
	}

	public int getM_backColor()
	{
		return m_backColor;
	}

	public void setM_backColor(int m_backColor)
	{
		this.m_backColor = m_backColor;
	}

	public Bitmap getM_book_bg()
	{
		return m_book_bg;
	}

	public void setM_book_bg(Bitmap m_book_bg)
	{
		this.m_book_bg = m_book_bg;
	}

	public MappedByteBuffer getM_mbBuf()
	{
		return m_mbBuf;
	}

	public void setM_mbBuf(MappedByteBuffer m_mbBuf)
	{
		this.m_mbBuf = m_mbBuf;
	}

	public int getM_mbBufLen()
	{
		return m_mbBufLen;
	}

	public void setM_mbBufLen(int m_mbBufLen)
	{
		this.m_mbBufLen = m_mbBufLen;
	}

	public int getM_mbBufBegin()
	{
		return m_mbBufBegin;
	}

	public void setM_mbBufBegin(int m_mbBufBegin)
	{
		this.m_mbBufBegin = m_mbBufBegin;
	}

	public String getM_strCharsetName()
	{
		return m_strCharsetName;
	}

	public void setM_strCharsetName(String m_strCharsetName)
	{
		this.m_strCharsetName = m_strCharsetName;
	}

	public Vector<String> getM_lines()
	{
		return m_lines;
	}

	public void setM_lines(Vector<String> m_lines)
	{
		this.m_lines = m_lines;
	}

	public int getMarginWidth()
	{
		return marginWidth;
	}

	public void setMarginWidth(int marginWidth)
	{
		this.marginWidth = marginWidth;
	}

	public int getMarginHeight()
	{
		return marginHeight;
	}

	public void setMarginHeight(int marginHeight)
	{
		this.marginHeight = marginHeight;
	}

	public float getmVisibleWidth()
	{
		return mVisibleWidth;
	}

	public void setmVisibleWidth(float mVisibleWidth)
	{
		this.mVisibleWidth = mVisibleWidth;
	}

	public boolean isM_isfirstPage()
	{
		return m_isfirstPage;
	}

	public void setM_isfirstPage(boolean m_isfirstPage)
	{
		this.m_isfirstPage = m_isfirstPage;
	}

	public boolean isM_islastPage()
	{
		return m_islastPage;
	}

	public void setM_islastPage(boolean m_islastPage)
	{
		this.m_islastPage = m_islastPage;
	}

	public String getStrPercent()
	{
		return strPercent;
	}

	public void setStrPercent(String strPercent)
	{
		this.strPercent = strPercent;
	}

	public String getBookName()
	{
		return bookName;
	}

	public void setBookName(String bookName)
	{
		this.bookName = bookName;
	}

	public int getM_mbBufEnd()
	{
		return m_mbBufEnd;
	}

	public void setM_mbBufEnd(int m_mbBufEnd)
	{
		this.m_mbBufEnd = m_mbBufEnd;
	}

	public File getBook_file() {
		return book_file;
	}

	public void setBook_file(File book_file) {
		this.book_file = book_file;
	}

	public int getmWidth() {
		return mWidth;
	}

	public void setmWidth(int mWidth) {
		this.mWidth = mWidth;
	}

	public int getmHeight() {
		return mHeight;
	}

	public void setmHeight(int mHeight) {
		this.mHeight = mHeight;
	}

	public int getCurrentBeginPosition()
	{
		return currentBeginPosition;
	}

	public void setCurrentBeginPosition(int currentBeginPosition)
	{
		this.currentBeginPosition = currentBeginPosition;
	}
}
