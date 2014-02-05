package com.dotcool.view;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dotcool.R;
import com.dotcool.bll.DbDataOperation;
import com.dotcool.bll.DbTags;
import com.dotcool.model.BookMark;
import com.dotcool.util.AppUtil;

public class BookmarkActivity extends ListActivity 
{
    private ListView lvBookMark;
    private ContentResolver resolver;
    private ArrayList<BookMark> bookMarks = new ArrayList<BookMark>();
    private BookMark bookMark;
    CustomAdapter adapter = new CustomAdapter();
    
    private int currentPosition;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        lvBookMark = getListView();
        resolver = getContentResolver();
        bookMarks = DbDataOperation.getBookMark(resolver);
        lvBookMark.setBackgroundResource(R.drawable.bg_listview);
        lvBookMark.setAdapter(adapter);
        lvBookMark.setDivider(null);
        lvBookMark.setCacheColorHint(000000);
        lvBookMark.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                currentPosition = position;
                intentToRead();
            }
        });
        lvBookMark.setOnCreateContextMenuListener(new OnCreateContextMenuListener()
        {
            public void onCreateContextMenu(ContextMenu menu, View v,
                    ContextMenuInfo menuInfo)
            {
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
                currentPosition = info.position;
                menu.add(Menu.NONE,R.id.menu_open,0,"打开");
                menu.add(Menu.NONE, R.id.menu_del, 1, "删除 " ); 
                menu.add(Menu.NONE, R.id.menu_des, 1, "详细 " ); 
  
            }
        });
    }
    
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_open:
            intentToRead();
            return true;
        case R.id.menu_del:
            new AlertDialog.Builder(this).setTitle("删除").setMessage("真的要删除吗？").setPositiveButton("确定", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    resolver.delete(Uri.parse(DbTags.URI_TABLE_BOOK_MARK), DbTags.FIELD_BOOK_MARK_ID+"=?", new String[]{bookMarks.get(currentPosition).getBookMarkId()+""});
                    updateView();
                }
            }).setNegativeButton("取消", null).show();
            return true;
        case R.id.menu_des:
            String bookDetail = "书名："+bookMarks.get(currentPosition).getBookName()+
            "\n进度："+bookMarks.get(currentPosition).getBookMarkProgress()+
            "\n添加时间："+bookMarks.get(currentPosition).getBookMarkAddTime();
    
             new AlertDialog.Builder(this).setTitle("详细信息").setMessage(bookDetail).setPositiveButton("确定", null).show();
            return true;
        
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
                AppUtil.appExit(this);
        }
        return super.onKeyDown(keyCode, event);
        
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        updateView();
    }

    class CustomAdapter extends BaseAdapter
    {
        ImageView lvBookMark;
        TextView tvBookName,tvBookMarkProgress,tvBookMarkDetail;
        
        public int getCount()
        {
            return bookMarks.size();
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
            bookMark = bookMarks.get(position);
            
            if(convertView==null)
                convertView = (LinearLayout)getLayoutInflater().inflate(R.layout.bookmark_lv_item, null);
            lvBookMark = (ImageView)convertView.findViewById(R.id.ivBookMark);
            tvBookName = (TextView)convertView.findViewById(R.id.tvBookName);
            tvBookMarkDetail = (TextView)convertView.findViewById(R.id.tvBookMarkDetail);
            tvBookMarkProgress = (TextView)convertView.findViewById(R.id.tvBookMarkProgress);
                 
            tvBookName.setText(bookMark.getBookName());
            tvBookMarkDetail.setText(bookMark.getBookMarkDetail());
            tvBookMarkProgress.setText(bookMark.getBookMarkProgress());
            
            return convertView;
        }
        
    }
   
    
    
   /*
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // Get the item that was clicked
        BookMark page = (BookMark) adapter.getItem(position);
        // Create new intent
       
    }*/
    /**
     * 跳转到阅读界面
     */
    public void intentToRead()
    {
        Intent intent = new Intent(BookmarkActivity.this,BookReadActivity.class);
        intent.putExtra(DbTags.FIELD_BOOK_PATH, bookMarks.get(currentPosition).getBookPath());
        intent.putExtra(DbTags.FIELD_BOOK_ID, bookMarks.get(currentPosition).getBookId());
        intent.putExtra(DbTags.FIELD_BOOK_PROGRESS, bookMarks.get(currentPosition).getBookMarkProgress());
        intent.putExtra(DbTags.FIELD_BOOK_NAME, bookMarks.get(currentPosition).getBookName());
        intent.putExtra(DbTags.FIELD_BOOK_BEGIN_POSITION, bookMarks.get(currentPosition).getBookMarkBeginPosition());
        startActivity(intent);
    }
    
    /**
     * 更新界面
     */
    public void updateView()
    {
        bookMarks = DbDataOperation.getBookMark(resolver);
        adapter.notifyDataSetChanged();
    }


}
