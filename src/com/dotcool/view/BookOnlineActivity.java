package com.dotcool.view;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dotcool.R;
import com.dotcool.bll.DbDataOperation;
import com.dotcool.bll.Downloader;
import com.dotcool.model.LoadInfo;
import com.dotcool.util.AppUtil;
import com.dotcool.util.BookUtil;
import com.dotcool.util.TimeUtil;

public class BookOnlineActivity extends ListActivity 
{
    private ListView lvBookOnline;
    private ContentResolver resolver;
    
    ImageView ivbookCover ;
    TextView tvbookName,tvbookDetail;
    Button btnDownload;

    private int threadcount;
    //Begin-change by Gank
    private int[] bookIconRes = new int[]{R.drawable.p39113,R.drawable.p39143,R.drawable.p39534,
            R.drawable.p39726};
    private String[] bookNames = new String[]{"深圳情人","本色","天使不在线","我的天使我的爱"};
    private String[] bookDetails = new String[]{
            "刘雪婷慵懒地靠在浅绿色布艺沙发上，修长笔直的双腿随意搁在圆皮脚凳上，哈欠连天地看着手机里连绵不断的贺年短信。",
            "两个人，像田地中的两只鼹鼠，你觅食，我守窝，你守窝，我觅食，在一起互相温暖着，照料着，度过每一个白天和夜晚，每一个春夏秋冬。",
            "辟辟啪啪，打上这最后一行字，文字已经把电脑的屏幕塞得满满的，再也没有任何缝隙。键盘敲打的声音突然停止，四周重新陷入一片寂静中。",
            "在我安静的小屋里有着星点的光，那是我的烟。月光从窗口洒下，光影里有隐约飘散的烟雾。慢慢地腾升，绕着墙上的十字绣画袅袅而上。"};
    private final static String URL = "";
    private String[] bookUrls = new String[]{
            "http://storezhang-upload.stor.sinaapp.com/fiction/txt/c76b0495-8e4e-4dde-b6d4-ece72c53c2d9.txt",
            "http://storezhang-upload.stor.sinaapp.com/fiction/txt/aa371da1-7368-4f81-a036-0897a87e04a8.txt",
            "http://storezhang-upload.stor.sinaapp.com/fiction/txt/21243553-5e6c-4f59-8e3b-b3eafebddfba.txt",
            "http://storezhang-upload.stor.sinaapp.com/fiction/txt/8eae5c2d-3c0c-43d1-8728-82bed11babb7.txt"};
    //End
    private static final String SD_PATH = "/mnt/sdcard/DotcoolReader/";
    // 存放各个下载器
    private Map<String, Downloader> downloaders = new HashMap<String, Downloader>();
    // 存放与下载器对应的进度条
    private Map<String, ProgressBar> ProgressBars = new HashMap<String, ProgressBar>();
    
    private LinearLayout layout;
    private NotificationManager notificationManager;
    private int notificationId = 1;
    private int currentPosition;
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                String url = (String) msg.obj;
                int length = msg.arg1;
                ProgressBar bar = ProgressBars.get(url);
                if (bar != null) {
                    // 设置进度条按读取的length长度更新
                    bar.incrementProgressBy(length);
                    if (bar.getProgress() == bar.getMax()) {
                        notificationManager.cancel(notificationId);
                        btnDownload.setText("下载");
                        // 下载完成后清除进度条并将map中的数据清空
                        LinearLayout layout = (LinearLayout) bar.getParent();
                        layout.removeView(bar);
                        ProgressBars.remove(url);
                        downloaders.get(url).delete(url);
                        downloaders.get(url).reset();
                        downloaders.remove(url);
                        
                        new AlertDialog.Builder(BookOnlineActivity.this).setTitle("提示").setMessage("下载完成，是否将《"+bookNames[currentPosition]+"》加入书架?")
                        .setPositiveButton("加入", new DialogInterface.OnClickListener()
                        {
                            
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(BookUtil.isExist( DbDataOperation.getBookInfo(resolver), SD_PATH+bookNames[currentPosition]+".txt")==true)
                                {
                                    new AlertDialog.Builder(BookOnlineActivity.this).setTitle("提示").setMessage("此书在书架中已存在，无需继续添加！").setPositiveButton("确定", null).show();
                                }
                                else
                                {
                                    DbDataOperation.insertToBookInfo(resolver, bookNames[currentPosition], "未知", SD_PATH+bookNames[currentPosition]+".txt", TimeUtil.getCurrentTime(),
                                            TimeUtil.getCurrentTime(), 0, "未分类",BookUtil.getBookSize(SD_PATH+bookNames[currentPosition]+".txt"), "0.0%");
                                    MainTabActivity.thMain.setCurrentTabByTag(MainTabActivity.TAB_BOOKSHELF);
                                    }
                            }
                        }).setNegativeButton("取消", null).show();
                    }
                }
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        resolver = getContentResolver();
        lvBookOnline = getListView();
        lvBookOnline.setCacheColorHint(000000);
        lvBookOnline.setDivider(null);
        lvBookOnline.setBackgroundResource(R.drawable.bg_listview);
        lvBookOnline.setFocusable(true);
        lvBookOnline.setAdapter(new CustomAdapter());
        
        
        
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
    class CustomAdapter extends BaseAdapter
    {
        public int getCount()
        {
            return bookNames.length;
        }

        public Object getItem(int position)
        {
            return null;
        }

        public long getItemId(int position)
        {
            return 0;
        }

        public View getView(final int position, View convertView, ViewGroup parent)
        {       
            layout = (LinearLayout) getLayoutInflater().inflate(R.layout.book_online_lv_item, null);
            ivbookCover = (ImageView)layout.findViewById(R.id.ivBookCover);
            tvbookName = (TextView)layout.findViewById(R.id.tvBookName);;
            tvbookDetail = (TextView)layout.findViewById(R.id.tvBookDetail);
            btnDownload = (Button)layout.findViewById(R.id.btnDownload);
                 
            ivbookCover.setBackgroundResource(bookIconRes[position]);
            tvbookName.setText(bookNames[position]);
            tvbookDetail.setText(bookDetails[position]);
            btnDownload.setOnClickListener(new OnClickListener()
            {
                public void onClick(View v)
                {
                    currentPosition = position;
                    startDownload(layout);
                }
            });
                 
            return layout;
        }
        
    }
    
    /**
     * 显示一个自定义内容的notification
     * @param iconId 图标资源id
     * @param tickerText 状态栏的标题
     * @param contentTitle 通知的标题
     * @param contentText 通知的内容
     * @param id 通知的id
     */
    public void showNotification(int iconId,String tickerText,String contentTitle,String contentText,int id)
    {
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification(iconId, tickerText, System.currentTimeMillis());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, getIntent(), 0);
        notification.setLatestEventInfo(this, contentTitle, contentText, pendingIntent);
        notificationManager.notify(id, notification);
    }
    
    /**
     * 响应开始下载按钮的点击事件
     */
    public void startDownload(View v) 
    {
        if(btnDownload.getText().equals("下载")||btnDownload.getText().equals("继续"))
        {
            btnDownload.setText("暂停");
            Toast.makeText(BookOnlineActivity.this, "开始下载", 500).show();
            showNotification(R.drawable.online_download, "《"+bookNames[currentPosition]+"》开始下载", "正在下载", "《"+bookNames[currentPosition]+"》下载中...",notificationId);
            String urlstr = URL + bookUrls[currentPosition] ;
            String localfile = SD_PATH + bookNames[currentPosition]+".txt";
            // 设置下载线程数为4
            threadcount = 4;
            // 初始化一个downloader下载器
            Downloader downloader = downloaders.get(urlstr);
            if (downloader == null) {
                downloader = new Downloader(urlstr, localfile, threadcount, this,
                        mHandler);
                downloaders.put(urlstr, downloader);
            }
            if (downloader.isdownloading())
                return;
            // 得到下载信息类的个数组成集合
            LoadInfo loadInfo = downloader.getDownloaderInfors();
            //       显示进度条
            showProgress(loadInfo, urlstr, v);
            // 调用方法开始下载
            downloader.download();
        }
        else 
        {
            btnDownload.setText("继续");
            pauseDownload(v);
        }
    }
    
    /**
     * 响应暂停下载按钮的点击事件
     */
    public void pauseDownload(View v)
    {
        String urlstr = URL + bookUrls[currentPosition];
        downloaders.get(urlstr).pause();
    }

    /**
     * 显示进度条
     */
    private void showProgress(LoadInfo loadInfo, String url, View v)
    {
        ProgressBar bar = ProgressBars.get(url);
        if (bar == null) 
        {
            bar = new ProgressBar(this, null,
                    android.R.attr.progressBarStyleHorizontal);
            bar.setMax(loadInfo.getFileSize());
            bar.setProgress(loadInfo.getComplete());
            System.out.println(loadInfo.getFileSize()+"--"+loadInfo.getComplete());
            ProgressBars.put(url, bar);
            LinearLayout.LayoutParams params = new LayoutParams(
                    LayoutParams.FILL_PARENT, 5);
             ((LinearLayout) v).addView(bar, params);
        }
    }

   

}
