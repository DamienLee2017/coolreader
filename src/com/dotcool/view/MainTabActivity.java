package com.dotcool.view;

/**
 * date:2012/4/5
 * author:lee
 * 
 */

import java.io.File;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.dotcool.R;
import com.dotcool.reader.activity.MainActivity;

public class MainTabActivity extends SherlockFragmentActivity  implements OnCheckedChangeListener
{
	//主要控件声明
	public static TabHost thMain;
	private RadioButton rbtnBookshelf,rbtnbookmark,rbtnBookOnline;
	
	//thMain的tab
	public final static String TAB_BOOKSHELF = "bookshelf";
	public final static String TAB_BOOKMARK = "bookmark";
	public final static String TAB_BOOK_ONLINE = "book_online";
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tab_main);
		LocalActivityManager lam = new LocalActivityManager(this, true);  
        lam.dispatchCreate(savedInstanceState);  
        
		thMain = (TabHost)findViewById(android.R.id.tabhost);
		thMain.setup(lam);
		thMain.addTab(newTabSpec(TAB_BOOKSHELF, R.string.tab_bookshelf, R.drawable.tab_bookshelf, new Intent(this,BookshelfActivity.class)));
		thMain.addTab(newTabSpec(TAB_BOOKMARK, R.string.tab_bookmark, R.drawable.tab_bookmark, new Intent(this,BookmarkActivity.class)));
		thMain.addTab(newTabSpec(TAB_BOOK_ONLINE, R.string.tab_book_online, R.drawable.tab_book_online, new Intent(this,MainActivity.class)));
		
		rbtnBookshelf = (RadioButton)findViewById(R.id.radio_button0);
		rbtnbookmark = (RadioButton)findViewById(R.id.radio_button1);
		rbtnBookOnline = (RadioButton)findViewById(R.id.radio_button2);
		
		rbtnBookshelf.setOnCheckedChangeListener(this);
		rbtnbookmark.setOnCheckedChangeListener(this);
		rbtnBookOnline.setOnCheckedChangeListener(this);
		
		File file = new File("/mnt/sdcard/DotcoolReader");
		if(!file.exists()){
			file.mkdir();
		}
		
	}
   
	/**
	 * RadioButton的监听器方法
	 */
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if(isChecked)
		{
			if(buttonView==rbtnBookshelf)    
			{
				thMain.setCurrentTabByTag(TAB_BOOKSHELF);
			}
			else if(buttonView==rbtnbookmark)
			{
				thMain.setCurrentTabByTag(TAB_BOOKMARK);
			}
			else if(buttonView==rbtnBookOnline)
			{
				thMain.setCurrentTabByTag(TAB_BOOK_ONLINE);
			}
		}
	}      
	
	/**
	 * 创建TabSpec
	 * @param tag --> 每个tab项的标识
	 * @param resLabel--> 每个tab项的文本
	 * @param resIcon --> 每个tab项的图标
	 * @param content --> 每个tab项的内容      
	 * @return --> 返回一个TabSpec用于tabhost添加Tab
	 */
	public TabSpec newTabSpec(String tag,int resLabel,int resIcon,Intent content)
	{
		return thMain.newTabSpec(tag).setIndicator(getString(resLabel),
				getResources().getDrawable(resIcon)).setContent(content);
	}
}
