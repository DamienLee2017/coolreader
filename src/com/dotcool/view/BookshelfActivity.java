package com.dotcool.view;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dotcool.R;
import com.dotcool.bll.DbDataOperation;
import com.dotcool.bll.DbTags;
import com.dotcool.bll.FileBrowser;
import com.dotcool.bll.OnFileBrowserListener;
import com.dotcool.model.Book;
import com.dotcool.util.AppUtil;
import com.dotcool.util.BookUtil;
import com.dotcool.util.TimeUtil;
public class BookshelfActivity extends Activity implements OnFileBrowserListener,OnItemClickListener
													
{
	private ListView lvBookshelf;

	private static boolean isDelFile;
	private static CheckBox checkBox;
	private MenuItem miOpenBook,miDeleteBook,miBookUpload,miBookDetail;
	private CustomAdapter adapter  ;
	private ImageButton handle;
	private FileBrowser fileBrowser = null;
	private RelativeLayout ll=null;
	//db
	private ContentResolver resolver;
	public static ArrayList<Book> bookList = new ArrayList<Book>();
	private Book book;
	
	private String currentFileName;
	private String currentPath = "/mnt/sdcard/";
	private int bookPosition;
    private ImageView backImageV;
    private TextView titleTextV;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bookshelf);
		
		init();         
	}     
    
	@Override
	protected void onResume()
	{   
		super.onResume();
		bookList = DbDataOperation.getBookInfo(resolver);
		adapter.notifyDataSetChanged();
	}

	public void init()   
	{
		resolver = getContentResolver();
		bookList = DbDataOperation.getBookInfo(resolver);
		
		lvBookshelf = (ListView)findViewById(R.id.lvBookshelf);
		fileBrowser = (FileBrowser)findViewById(R.id.filebrowser);
		fileBrowser.setOnFileBrowserListener(this);
		lvBookshelf.setOnItemClickListener(this);
		ll=(RelativeLayout)findViewById(R.id.content);
		
		adapter = new CustomAdapter();
		lvBookshelf.setAdapter(adapter);
		lvBookshelf.setOnCreateContextMenuListener(new OnCreateContextMenuListener()
		{
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo)
			{
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
				bookPosition = info.position;
				menu.add(Menu.NONE,R.id.menu_open,0,"打开");
				menu.add(Menu.NONE, R.id.menu_del, 1, "删除 " ); 
				menu.add(Menu.NONE, R.id.menu_des, 1, "详细 " ); 
			}
		});
        titleTextV=(TextView)findViewById(R.id.profile_header_title);
        titleTextV.setText("本地文件");
        backImageV=(ImageView)findViewById(R.id.profile_header_back);
        backImageV.setOnClickListener(new OnClickListener(){

                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    ll.setVisibility(View.INVISIBLE);
                }
                
            });
	}



	class CustomAdapter extends BaseAdapter
	{
		TextView tvBookNameCover,tvBookName,tvBookAuthor,tvBookAddTime,tvBookProgress;
		
		public int getCount()
		{
			return bookList.size();
		}

		public Object getItem(int position)
		{
			return null;
		}

		public long getItemId(int position)
		{
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			book = bookList.get(position);
			
			if(convertView==null)
				convertView = (LinearLayout)getLayoutInflater().inflate(R.layout.bookshelf_item_listview, null);
			tvBookNameCover = (TextView)convertView.findViewById(R.id.tvBookNameCover);
			tvBookName = (TextView)convertView.findViewById(R.id.tvBookName);
			tvBookAuthor = (TextView)convertView.findViewById(R.id.tvBookAuthor);
			tvBookAddTime = (TextView)convertView.findViewById(R.id.tvBookTime);
			tvBookProgress = (TextView)convertView.findViewById(R.id.tvBookProgress);
			
			//tvBookNameCover.setText(book.getBookName());
			String tmp=book.getBookPath();
			if(tmp.indexOf(".txt")!=-1){
			    tvBookNameCover.setBackgroundDrawable(getResources().getDrawable(R.drawable.cover_txt));
			}else if(tmp.indexOf(".chm")!=-1){
                tvBookNameCover.setBackgroundDrawable(getResources().getDrawable(R.drawable.cover_chm));
            }else if(tmp.indexOf(".ebk")!=-1){
                tvBookNameCover.setBackgroundDrawable(getResources().getDrawable(R.drawable.cover_ebk));
            }else if(tmp.indexOf(".epub")!=-1){
                tvBookNameCover.setBackgroundDrawable(getResources().getDrawable(R.drawable.cover_epub));
            }else if(tmp.indexOf(".html")!=-1){
                tvBookNameCover.setBackgroundDrawable(getResources().getDrawable(R.drawable.cover_html));
            }else if(tmp.indexOf(".pdb")!=-1){
                tvBookNameCover.setBackgroundDrawable(getResources().getDrawable(R.drawable.cover_pdb));
            }else if(tmp.indexOf(".umd")!=-1){
                tvBookNameCover.setBackgroundDrawable(getResources().getDrawable(R.drawable.cover_umd));
            }else {
                tvBookNameCover.setBackgroundDrawable(getResources().getDrawable(R.drawable.cover_txt));
            }
			tvBookName.setText(book.getBookName());
			tvBookAuthor.setText(book.getBookAuthor());
			tvBookAddTime.setText(book.getBookAddTime());
			tvBookProgress.setText(book.getBookProgress());
			
			return convertView;
		}
	}
   
	/**
	 * 重写列表项点击事件监听器
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id)
	{
		Intent intent = new Intent(BookshelfActivity.this,BookReadActivity.class);
		intent.putExtra(DbTags.FIELD_BOOK_PATH, bookList.get(position).getBookPath());
		intent.putExtra(DbTags.FIELD_BOOK_ID, bookList.get(position).getBookId());
		intent.putExtra(DbTags.FIELD_BOOK_PROGRESS, bookList.get(position).getBookProgress());
		intent.putExtra(DbTags.FIELD_BOOK_NAME, bookList.get(position).getBookName());
		intent.putExtra(DbTags.FIELD_BOOK_BEGIN_POSITION, bookList.get(position).getBookBeginPosition());
		System.out.println(bookList.get(position).getBookPath());
		startActivity(intent);
	}
	public boolean onCreateOptionsMenu(android.view.Menu menu) { 
	    // TODO Auto-generated method stub 
	    super .onCreateOptionsMenu(menu); 
    	    menu.add(0, R.id.menu_share, 1, "分享 " ).setIcon(R.drawable.menu_share); 
    	    menu.add(0, R.id.menu_feedback, 2, "反馈 " ).setIcon(R.drawable.menu_feedback); 
    	    menu.add(1, R.id.menu_exit, 3, "关于 " ).setIcon(R.drawable.menu_about); 
    	    menu.add(1, R.id.menu_about, 4, "本地 " ).setIcon(R.drawable.menu_exit); 
	    return true ;

	    }
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_share:
            AppUtil.appShare(this);
            return true;
        case R.id.menu_feedback:
            AppUtil.appFeedback(this);
            return true;
        case R.id.menu_about:
            ll.setVisibility(View.VISIBLE);
            return true;
        case R.id.menu_exit:
            Intent intent = new Intent(BookshelfActivity.this,AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	   @Override
	    public boolean onContextItemSelected(android.view.MenuItem item) {
		
		 if(item.getItemId() ==R.id.menu_open)
		{
			Intent intent = new Intent(BookshelfActivity.this,BookReadActivity.class);
			intent.putExtra(DbTags.FIELD_BOOK_PATH, bookList.get(bookPosition).getBookPath());
			intent.putExtra(DbTags.FIELD_BOOK_ID, bookList.get(bookPosition).getBookId());
			intent.putExtra(DbTags.FIELD_BOOK_NAME, bookList.get(bookPosition).getBookName());
			intent.putExtra(DbTags.FIELD_BOOK_PROGRESS, bookList.get(bookPosition).getBookProgress());
			intent.putExtra(DbTags.FIELD_BOOK_BEGIN_POSITION, bookList.get(bookPosition).getBookBeginPosition());
			startActivity(intent);
			ll.setVisibility(View.INVISIBLE);
		}
		else if(item.getItemId()==R.id.menu_del)
		{
			checkBox = new CheckBox(this);
			checkBox.setText("同时删除本地文件");
			new AlertDialog.Builder(this).setTitle("删除").setMessage("真的要删除吗？").setView(checkBox).setPositiveButton("确定", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					isDelFile = checkBox.isChecked();
					if(isDelFile)
					{
						File file = new File(bookList.get(bookPosition).getBookPath());
						file.delete();
					}
					DbDataOperation.deleteBook(resolver, bookList.get(bookPosition).getBookId());
					bookList = DbDataOperation.getBookInfo(resolver);
					adapter.notifyDataSetChanged();
					ll.setVisibility(View.INVISIBLE);
				}
			}).setNegativeButton("取消", null).show();
		}
		else if(item.getItemId()==R.id.menu_des)
		{
			String bookDetail = "书名："+bookList.get(bookPosition).getBookName()+
					"\n格式："+BookUtil.getBookFormat(bookList.get(bookPosition).getBookPath())+
					"\n大小："+bookList.get(bookPosition).getBookSize()+
					"\n进度："+bookList.get(bookPosition).getBookProgress()+
					"\n路径："+bookList.get(bookPosition).getBookPath();
			
			new AlertDialog.Builder(this).setTitle("详细信息").setMessage(bookDetail).setPositiveButton("确定", null).show();
		}else if(item==miBookUpload){
		    
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if (keyCode == KeyEvent.KEYCODE_MENU) {
	        super.openOptionsMenu();
        }else if(keyCode==KeyEvent.KEYCODE_BACK)
		{
		    if(ll.getVisibility()==View.VISIBLE){
		        ll.setVisibility(View.INVISIBLE);
		        return true;
		    }else{
		        AppUtil.appExit(this);
		        return super.onKeyDown(keyCode, event);
		    }
		}
		 return super.onKeyDown(keyCode, event);
		
        
		
	}

	public void onFileItemClick(final String fileName)
	{
		currentFileName = fileName;	
		new AlertDialog.Builder(this).setItems(new String[]{"直接阅读","加入书架","详细信息"}, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				if(which==0)
				{
					BookUtil.getBookContent(currentFileName);
					Intent intent = new Intent(BookshelfActivity.this,BookReadActivity.class);
					intent.putExtra(DbTags.FIELD_BOOK_PATH, fileName);
					intent.putExtra(DbTags.FIELD_BOOK_PROGRESS, "onlyRead");
					startActivity(intent);
				}
				else if(which==1)
				{
//					Toast.makeText(BookshelfActivity.this, BookUtil.isExist(bookList, fileName)+"", Toast.LENGTH_LONG).show();
					if(BookUtil.isExist(bookList, fileName)==true)
					{
						new AlertDialog.Builder(BookshelfActivity.this).setTitle("提示").setMessage("此书在书架中已存在，无需继续添加！").setPositiveButton("确定", null).show();
					}
					else
					{
						DbDataOperation.insertToBookInfo(resolver, BookUtil.getBookName(currentPath, fileName), "未知", fileName, TimeUtil.getCurrentTime(),
								TimeUtil.getCurrentTime(), 0, "未分类",BookUtil.getBookSize(fileName), "0.0%");
						bookList = DbDataOperation.getBookInfo(resolver);
						adapter.notifyDataSetChanged();
					}
				}
				else if(which==2)
				{
					String detail = "文件名："+fileName.replaceAll(currentPath+"/", "")+"\n"
							+"文件路径："+fileName+"\n"
							+"文件大小："+BookUtil.getBookSize(fileName);
					
					new AlertDialog.Builder(BookshelfActivity.this).setTitle("详细信息").setMessage(detail).setPositiveButton("确定", null).show();
				}
			}
		}).show();
	}

	public void onDirItemClick(String path)
	{
		currentPath = path;
	}
}
