package com.dotcool.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.dotcool.R;
import com.dotcool.bll.BookPageFactory;
import com.dotcool.bll.DbTags;

public class SearchActivity extends Activity
{
	private EditText etSearchContent;
	private ImageButton ibtnSearch;
	private ListView lvSearchRes;
	
	private BookPageFactory pagefactory = BookReadActivity.pagefactory;
	private Vector<String> pageLines;
	private List<String> searchResContent;
	private List<Integer> searchResBeginpositions;
	private AlertDialog searchDialog;
	private String searchContent;
	private Pattern pattern;
	private Matcher matcher;
	private int currentBeginPosition;
	private String resContent;
	private int resNum;
	private Handler searchHAndlHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);  
			if(msg.what==0)
			{
				if(searchResContent.size()>0)
				{
	//				lvSearchRes.setAdapter(new ArrayAdapter<String>(SearchActivity.this, R.layout.search_res_listview_item, R.id.tvResContent,searchResContent));
					lvSearchRes.setAdapter(new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1,searchResContent));
				}
				else
				{
					new AlertDialog.Builder(SearchActivity.this).setTitle("提示").setMessage("没有找到符合查询条件的结果!").setPositiveButton("确定", null).show();
				}
			}
			else if(msg.what==1)
			{
				searchDialog.setMessage("正在搜索，请稍后...\n已找到"+resNum+"条结果\n"+resContent);
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.search_view);
		
		etSearchContent = (EditText)findViewById(R.id.etSearchContent);
		ibtnSearch = (ImageButton)findViewById(R.id.ibtnSearch);
		lvSearchRes = (ListView)findViewById(R.id.lvSearchRes);
		lvSearchRes.setFocusable(true);
		
		lvSearchRes.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				Intent intent = new Intent(SearchActivity.this,BookReadActivity.class);
				intent.putExtra(DbTags.FIELD_BOOK_PROGRESS, "begin");
				intent.putExtra("begin", searchResBeginpositions.get(position));
				intent.putExtra(DbTags.FIELD_BOOK_PATH, getIntent().getExtras().getString(DbTags.FIELD_BOOK_PATH));
				startActivity(intent);
			}
		});
		
		ibtnSearch.setOnClickListener(new View.OnClickListener() 	
		{
			public void onClick(View v) 
			{
				pagefactory.setM_mbBufBegin(0);
				pagefactory.setM_islastPage(false);
				searchResContent = new ArrayList<String>();
				searchResBeginpositions = new ArrayList<Integer>();
    			searchDialog = new AlertDialog.Builder(SearchActivity.this).create();
    			searchDialog.setMessage("正在搜索，请稍后...");
    			searchDialog.show();
        		
    			searchContent = etSearchContent.getText().toString();
    			pattern = Pattern.compile(searchContent);
        		new Thread(new Runnable()
				{

					public void run()
					{
						System.out.println("hello");
						pagefactory.pageUp();
	            		while(!pagefactory.isM_islastPage())
	            		{
	            			pageLines= pagefactory.pageDown();
		            		if(pageLines.size()>0)
	            			{
			            		for(int i=0;i<pageLines.size();i++)
			            		{      		
			            			matcher = pattern.matcher(pageLines.get(i));
			            			if(i==0)
			            				currentBeginPosition+= pageLines.get(i).length();
			            			else
			            				currentBeginPosition+= pageLines.get(i-1).length()+pageLines.get(i).length();
			            			if(matcher.find())
			            			{
			            				searchResContent.add(pageLines.get(i));
			            				searchResBeginpositions.add(currentBeginPosition);
			            				System.out.println(i+pageLines.get(i));
			            				resContent = pageLines.get(i);
			            				resNum++;
			            				searchHAndlHandler.sendEmptyMessage(1);
			            				
			            			}
		            			}
		            		}
		            		else
		            		{
		            			pagefactory.setM_islastPage(true);
		            			searchDialog.dismiss();
		            			searchHAndlHandler.sendEmptyMessage(0);
		            			break;
		            		}
	            		}
					}
				}).start();
    		}
		});
		
	}
}
