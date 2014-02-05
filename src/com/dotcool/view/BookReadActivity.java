package com.dotcool.view;

/**
 * 书籍阅读
 * author Lee
 * date 2012/4/18
 */

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import net.youmi.android.banner.AdViewLinstener;
import net.youmi.android.smart.SmartBannerManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.dotcool.R;
import com.dotcool.bll.BookPageFactory;
import com.dotcool.bll.DbDataOperation;
import com.dotcool.bll.DbTags;
import com.dotcool.bll.PageWidget;
import com.dotcool.util.AppUtil;
import com.dotcool.util.ArrayUtil;
import com.dotcool.util.BgUtil;
import com.dotcool.util.MathUtil;
import com.dotcool.util.TimeUtil;
import com.speakit.tts.foc.instance.AndroidVoiceInstance;
import com.speakit.tts.foc.instance.SettingTTSActivity;
import android.view.Menu;
import android.view.MenuItem;

public class BookReadActivity extends Activity
{
	private PageWidget mPageWidget;
	Bitmap mCurPageBitmap, mNextPageBitmap;
	Canvas mCurPageCanvas, mNextPageCanvas;
	public static BookPageFactory pagefactory;
	int screenWidth ,screenHeight;
	int bookId;
	String bookPath,bookName,bookProgress;
	String content;
	private int bookBeginPosition;
	
	//db
	private ContentResolver resolver;
	
	//tabMenu
	public final static int TITLE_USING_OFTEN = 0;
	public final static int TITLE_TOOLS = 1;
	public final static int MENU_FONT_SIZE = 0;
	public final static int MENU_READ_THEME = 1;
	public final static int MENU_FLIP_OVER = 2;
	public final static int MENU_SEARCH = 3;  
	public final static int MENU_AUDIO_BOOK = 0;
	public final static int MENU_SETTING = 1;
	public final static int MENU_BOOK_MARK = 2;
	public final static int MENU_SHARE = 3;

	
	TabMenu.MenuBodyAdapter []bodyAdapter=new TabMenu.MenuBodyAdapter[2];  
    TabMenu.MenuTitleAdapter titleAdapter;  
    TabMenu tabMenu; 
    int selTitle=0;
    PopupWindow popupWindow;
    //定义每项分页栏的内容  
    String[] title1BodyName , title2BodyName;
    int[] title1BodyIcon , title2BodyIcon;
    
    private SeekBar sbFontSize,sbBrightnessControl,sbJump;
    //menu jump
	private TextView tvCurrentProgress ;
	private ImageButton ibtnJumpUp,ibtnJumpDown;
	//flip over
	private Handler readHandler = new Handler();
	private FlipOverRunnable flipOverRunnable = new FlipOverRunnable();
	private boolean isRead = true;
    
	//menu custom theme  
    private SeekBar redForegroundSeekBar,greenForegroundSeekBar,blueForegroundSeekBar,
		redBackgroundSeekBar,greenBackgroundSeekBar,blueBackgroundSeekBar;
    int currentProgress;
    private TextView fontColorTxt,backgroundColorTxt,themeStyleTxt ;
    private int redForegoundProgress,greenForegroundProgress,blueForegroundProgress,
		redBackgoundProgress,greenBackgroundProgress,blueBackgroundProgress;
    private String foreColor,foreR="00",foreG="00",foreB="00",backColor,backR="00",backG="00",backB="00";
    private int currentForeColorId,currentBackColorId;
    
    //chapter
    Pattern pattern = Pattern.compile("第{1}.+章{1}.+");
	Matcher matcher;
	List<String> chapterName = new ArrayList<String>();
	List<Integer> chapterBeginPositions = new ArrayList<Integer>();
	Vector<String> lines;
	int chapterNum = 0;
	private AlertDialog chapterDialog;
	private String currentChapterName = "wu";
	private int currentBeginPosition = 0;
	private Context context;
	private StringBuilder src;
	Handler chapterHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);   
			Log.d("dots", ""+msg.what);
			if(msg.what == 0)
			{
//				updateView(); 
				showChapterDialog();
			}
			else if(msg.what == 1)
			{
				chapterDialog.setMessage("正在提取章节，请稍后...\n已发现章节：["+chapterNum+"] "+currentChapterName);
			}
			else if(msg.what == 2)
			{
        		mPageWidget.invalidate();
			}else if(msg.what==3){
			   
			      SharedPreferences localSharedPreferences  =context.getSharedPreferences("audio", 0);
			        if(localSharedPreferences.getInt("flag", 0)==1){
			           
			           src=new StringBuilder();
			           for(String item:pagefactory.getM_lines()){
			               src.append(item);
			           }
			           AndroidVoiceInstance.Instance().playVoiceString(BookReadActivity.this, src.toString());
			        }
			     
			}
		}
	};
	
	//pre and next chapter
	private String currentChapter;
	private int currentChapterIndex;
	private boolean preOrNext = true;
	Handler preHandler = new Handler(){
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			for(int i=0;i<chapterName.size();i++)
			{
				if(chapterName.get(i).equals(currentChapter))
				{
					currentChapterIndex = i;
					Log.i("chapter"+i+"-->",currentChapterIndex+"");
					break;
				}
			}
			if(currentChapterIndex==0)
			{
				new AlertDialog.Builder(BookReadActivity.this).setTitle("提示").setMessage("已是第一章！").setPositiveButton("确定", null).show();
			}
			else
			{
				pagefactory.setM_mbBufBegin(chapterBeginPositions.get(currentChapterIndex-1));
				updateView();
			}
			
		}
	};
	Handler nextHandler = new Handler(){
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			for(int i=0;i<chapterName.size();i++)
			{
				Log.i("name"+i,chapterName.get(i));
				if(chapterName.get(i).equals(currentChapter))
				{
					currentChapterIndex = i;
					Log.i("chapter"+i+"-->",currentChapterIndex+"");
					break;
				}
			}
			if(currentChapterIndex==(chapterName.size()-1))
			{
				new AlertDialog.Builder(BookReadActivity.this).setTitle("提示").setMessage("已是最后一章！").setPositiveButton("确定", null).show();
			}
			else
			{
				pagefactory.setM_mbBufBegin(chapterBeginPositions.get(currentChapterIndex+1));
				updateView();
			}
		}
	};
	
	
	@Override   
	public void onCreate(Bundle savedInstanceState) 
	{    
		super.onCreate(savedInstanceState);
		resolver = getContentResolver();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.act_reader);
		LinearLayout adLayout = (LinearLayout) findViewById(R.id.adLayout);
        AdView adView = new AdView(this, AdSize.FIT_SCREEN);
        adLayout.addView(adView);
        adView.setAdListener(new AdViewLinstener() {
            public void onSwitchedAd(AdView arg0) {
                Log.i("YoumiSample", "广告条切换");
            }
            
            public void onReceivedAd(AdView arg0) {
                Log.i("YoumiSample", "请求广告成功");
                
            }
            
            public void onFailedToReceivedAd(AdView arg0) {
                Log.i("YoumiSample", "请求广告失败");
            }
        });
        SmartBannerManager.init(this);
        // 调用展示飘窗
        SmartBannerManager.show(BookReadActivity.this);

		mPageWidget = (PageWidget)findViewById(R.id.pagewidget);
		AndroidVoiceInstance.Instance().initData(BookReadActivity.this, "000000");
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels-AppUtil.dip2px(this, 50);   
		context=this;
		mCurPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
		mNextPageBitmap = Bitmap
				.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);

		mCurPageCanvas = new Canvas(mCurPageBitmap);
		mNextPageCanvas = new Canvas(mNextPageBitmap);
		pagefactory = new BookPageFactory(screenWidth, screenHeight);
		
		SharedPreferences localSharedPreferences  =context.getSharedPreferences("audio", 0);
        int tmp=localSharedPreferences.getInt("theme", 0);
       
        int[] bgs = new int[]{R.drawable.read_bg_1,R.drawable.read_bg_2,R.drawable.read_bg_3,R.drawable.read_bg_6,R.drawable.read_bg_7};
		pagefactory.setBgBitmap(BgUtil.getSuitableBg(this,screenWidth,screenHeight,R.drawable.read_bg_3));
		pagefactory.setM_backColor(0x000000);
        pagefactory.setBgBitmap(BgUtil.getSuitableBg(BookReadActivity.this,screenWidth,screenHeight,bgs[tmp]));
       
		try 
		{
		    if(getIntent().getExtras().getInt("net")==1){
	            //网上下载构建
		        content=getIntent().getExtras().getString("net_content");
		        bookPath=AppUtil.saveTxt(content, this.getFilesDir().getAbsolutePath());
		        bookProgress="onlyRead";
		        bookId = getIntent().getExtras().getInt("net_book_id");
		        bookBeginPosition=0;
		        pagefactory.openbook(bookPath);
	        }else{
	            //一般构建
    			bookPath = getIntent().getExtras().getString(DbTags.FIELD_BOOK_PATH);
    			bookName = getIntent().getExtras().getString(DbTags.FIELD_BOOK_NAME);
    			bookId = getIntent().getExtras().getInt(DbTags.FIELD_BOOK_ID);
    			bookProgress = getIntent().getExtras().getString(DbTags.FIELD_BOOK_PROGRESS);
    			bookBeginPosition = getIntent().getExtras().getInt(DbTags.FIELD_BOOK_BEGIN_POSITION);
    			pagefactory.openbook(bookPath);
	        }
			if(bookProgress==null||bookProgress.equals("onlyRead"))
			{
				pagefactory.Draw(mCurPageCanvas);
			}
			else if(bookProgress.equals("begin"))
			{
				pagefactory.setM_mbBufBegin(getIntent().getIntExtra("begin", 0));
				updateView();
				System.out.println("2");
			}
			else
			{
     	       	pagefactory.setM_mbBufBegin(bookBeginPosition);
     	       	System.out.println("3");
				updateView();
			}
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
			Toast.makeText(this, "电子书不存在!!!",
					Toast.LENGTH_SHORT).show();
		}
		mPageWidget.setBitmaps(mCurPageBitmap, mCurPageBitmap);
		mPageWidget.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent e) 
			{
				boolean ret=false;
				if(popupWindow==null||!popupWindow.isShowing())
				{
					if (v == mPageWidget) 
					{
						if (e.getAction() == MotionEvent.ACTION_DOWN) {
							mPageWidget.abortAnimation();
							mPageWidget.calcCornerXY(e.getX(), e.getY());
							pagefactory.Draw(mCurPageCanvas);
							if (mPageWidget.DragToRight()) 
							{
								try 
								{
									pagefactory.prePage();
								} 
								catch (IOException e1)
								{
									e1.printStackTrace();
								}						
								if(pagefactory.isfirstPage())
								{
//									Toast.makeText(BookReadActivity.this, "已到第一页！", 200).show();
									return false;
								}
								pagefactory.Draw(mNextPageCanvas);
							     chapterHandler.sendEmptyMessage(3);
							} else 
							{
								try 
								{
									pagefactory.nextPage();
								}
								catch (IOException e1) 
								{
									e1.printStackTrace();
								}
								if(pagefactory.islastPage())
								{
//									Toast.makeText(BookReadActivity.this, "已到最后一页！", 200).show();
									return false;
								}
								pagefactory.Draw(mNextPageCanvas);
							     chapterHandler.sendEmptyMessage(3);
							}
							mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
						}
						 ret = mPageWidget.doTouchEvent(e);
						return ret;
					}
				}
				return false;
			}
		});
		
		//设置分页栏的标题  
        titleAdapter = new TabMenu.MenuTitleAdapter(this, new String[] { "常用",  
                "工具",}, 16, 0xFF222222,Color.LTGRAY,Color.BLACK);  
        //定义每项分页栏的内容  
        title1BodyName = new String[] { "字体",  "主题","滚动","搜索"};
        title1BodyIcon = new int[] { R.drawable.menu_fontsize,R.drawable.menu_extract,R.drawable.menu_play,R.drawable.menu_search};
        title2BodyName = new String[] { "朗诵","设置", "书签", "分享"};
        title2BodyIcon = new int[] { R.drawable.menu_play, R.drawable.menu_setting, R.drawable.menu_bookmark, R.drawable.menu_share};
       
        bodyAdapter[0]=new TabMenu.MenuBodyAdapter(this,title1BodyName,title1BodyIcon,12, 0xFF000000);  
        bodyAdapter[1]=new TabMenu.MenuBodyAdapter(this,title2BodyName,title2BodyIcon,12, 0xFF000000);    
           
           
        tabMenu=new TabMenu(this,  
                 new TitleClickEvent(),  
                 new BodyClickEvent(),  
                 titleAdapter,  
                 0xFFFFFFFF,//TabMenu的背景颜色  
                 R.style.PopupAnimation);//出现与消失的动画  
         tabMenu.update();  
         tabMenu.setTitleSelect(0);  
         tabMenu.SetBodyAdapter(bodyAdapter[0]);  
         SharedPreferences.Editor localEditor = context.getSharedPreferences("AndroidVoice", 0).edit();
         localEditor.putInt("isUpdate", 0);
         localEditor.commit();
    
	}
	
	class TitleClickEvent implements OnItemClickListener
	{  
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  
                long arg3) 
        {  
            selTitle=arg2;  
            tabMenu.setTitleSelect(arg2);  
            tabMenu.SetBodyAdapter(bodyAdapter[arg2]);  
        }  
    }  
      
    class BodyClickEvent implements OnItemClickListener ,View.OnClickListener
    {     		
    	MySeekBarChangedListener seekBarChangedListener = new MySeekBarChangedListener();
    	
    	//Bg Drawable
    	int[] bgs = new int[]{R.drawable.read_bg_1,R.drawable.read_bg_2,R.drawable.read_bg_3,R.drawable.read_bg_6,R.drawable.read_bg_7};
    	int[] styles = new int[]{R.drawable.style01,R.drawable.style02,R.drawable.style03,R.drawable.style06,R.drawable.style07};
    	String[] styleName = new String[]{"羊皮纸","粉红回忆","蓝色幻想","咖啡时光","水墨江南"};
    	GridView gvReadTheme;
    	
    	//search
    	EditText etSearchContent;
    	ImageButton ibtnSearch,ibtnSearchBack,ibtnSearchForward,ibtnSearchClear;
    	
        public void onItemClick(AdapterView<?> arg0, View arg1, int position,  
                long arg3)
        {  
            tabMenu.setBodyClick(position,Color.GRAY);  
            if(selTitle==TITLE_USING_OFTEN)
            {
            	if(position==MENU_FONT_SIZE)
            	{
            		LinearLayout fontSizeView = null;
    				if(fontSizeView==null)
    					fontSizeView = (LinearLayout)getLayoutInflater().inflate(R.layout.fontsize_view, null);
    				
    				showPopupWindowAtBottom(fontSizeView,LayoutParams.FILL_PARENT,   
    	                    100, Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM,0,0);
    				popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_listview));
        	        
        	        sbFontSize = (SeekBar) fontSizeView.findViewById(R.id.fontSizeProgress);
        	        sbFontSize.setProgress(pagefactory.getM_fontSize());
        	        sbFontSize.setOnSeekBarChangeListener(seekBarChangedListener);
            	}
            	
            	else if(position==MENU_READ_THEME)
                {    
                    LinearLayout readThemeView = null;
                    if(readThemeView==null)
                        readThemeView = (LinearLayout)getLayoutInflater().inflate(R.layout.read_theme_view, null);
                    gvReadTheme = (GridView) readThemeView.findViewById(R.id.gvReadTheme);
                    
                    List<Map<String,Integer>> themeList = new ArrayList<Map<String,Integer>>();
                    for(int i=0;i<styles.length;i++)
                    {
                        Map<String,Integer> themeMap = new HashMap<String,Integer> ();
                        themeMap.put("themeStyle", styles[i]);
                        themeList.add(themeMap);   
                    }
                    SimpleAdapter simpleAdapter = new SimpleAdapter(BookReadActivity.this,themeList,R.layout.theme_imageview,
                            new String[]{"themeStyle"},new int[]{R.id.themeStyle});
                    gvReadTheme.setAdapter(simpleAdapter);
                    
                    gvReadTheme.setOnItemClickListener(new OnItemClickListener()
                    {
                        public void onItemClick(AdapterView<?> parent,
                                View view, int position, long id)
                        {

                            SharedPreferences.Editor localEditor = context.getSharedPreferences("audio", 0).edit();
                            localEditor.putInt("theme", position);
                            localEditor.commit();
                            
                            //Toast.makeText(BookReadActivity.this, styleName[position], 50).show();
                            pagefactory.setM_backColor(0x000000);
                            pagefactory.setBgBitmap(BgUtil.getSuitableBg(BookReadActivity.this,screenWidth,screenHeight,bgs[position]));
                            updateView();
                        }
                    });
                    
                    showPopupWindowAtBottom(readThemeView,LayoutParams.FILL_PARENT,   
                            110, Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM,0,0);
                    popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_listview));
                    popupWindow.setFocusable(true);
                }
            	else if(position==MENU_FLIP_OVER)
            	{
            		if(isRead)
            		{
            			readHandler.postDelayed(flipOverRunnable, 1000);
            			isRead = false;
            			title1BodyName[MENU_FLIP_OVER] = "停止阅读";
            			title1BodyIcon[MENU_FLIP_OVER] = R.drawable.menu_stop;
            			//Toast.makeText(BookReadActivity.this, "开始阅读", 500).show();
            		}
            		else 
            		{
            			readHandler.removeCallbacks(flipOverRunnable);
            			isRead = true;
            			title1BodyName[MENU_FLIP_OVER] = "开始阅读";
            			title1BodyIcon[MENU_FLIP_OVER] = R.drawable.menu_play;
            			//Toast.makeText(BookReadActivity.this, "停止阅读", 500).show();
            		}
            		bodyAdapter[0]=new TabMenu.MenuBodyAdapter(BookReadActivity.this,title1BodyName,title1BodyIcon,12, 0xFF000000); 
        			tabMenu.SetBodyAdapter(bodyAdapter[0]);
            	}
            	
            	else if(position==MENU_SEARCH)
            	{
            		//方式1
            		Intent intent = new Intent(BookReadActivity.this,SearchActivity.class);
            		intent.putExtra(DbTags.FIELD_BOOK_PATH, bookPath);
            		startActivity(intent);
            		
            		//方式2
//            		LinearLayout bookSearchView = null;
//    				if(bookSearchView==null)
//    					bookSearchView = (LinearLayout)getLayoutInflater().inflate(R.layout.search_view, null);  
//    				etSearchContent = (EditText)bookSearchView.findViewById(R.id.etSearchContent);
//    				ibtnSearch = (ImageButton)bookSearchView.findViewById(R.id.ibtnSearch);
//    				ibtnSearch.setOnClickListener(this);
//    				showPopupWindowAtBottom(bookSearchView,LayoutParams.FILL_PARENT,   
//    						70, Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM,0,pagefactory.getmWidth());
            		
            	}
            	
            }
            else if(selTitle==TITLE_TOOLS)
            {
            	 if(position==MENU_AUDIO_BOOK)
            	{
            	    
            	    SharedPreferences localSharedPreferences  =context.getSharedPreferences("audio", 0);
            	    int tmp=0;
            	    if (localSharedPreferences.getInt("flag", 0)==0){
            	        tmp=1;
            	        chapterHandler.sendEmptyMessage(3);
            	    }else{
            	        tmp=0;
            	    }
            	    SharedPreferences.Editor localEditor = context.getSharedPreferences("audio", 0).edit();
            	    localEditor.putInt("flag", tmp);
            	    localEditor.commit();
            	    
            	    
            	}
            	
            	
            	else if(position==MENU_BOOK_MARK)
            	{
            		if(bookProgress.equals("onlyRead"))
            		{
            			new AlertDialog.Builder(BookReadActivity.this).setTitle("提示").setMessage("电子书书未添加到书架，无法添加书签").setPositiveButton("确定", null).show();
            		}
            		else
            		{
            			
            			ContentValues values = new ContentValues();
                		values.put(DbTags.FIELD_BOOK_ID, bookId);
                		values.put(DbTags.FIELD_BOOK_NAME, bookName);
                		System.out.println("name -->"+bookName);
                		values.put(DbTags.FIELD_BOOK_PATH, bookPath);
                		values.put(DbTags.FIELD_BOOK_MARK_ADD_TIME, TimeUtil.getCurrentTime());
                		values.put(DbTags.FIELD_BOOK_MARK_PROGRESS, pagefactory.getStrPercent());
                		values.put(DbTags.FIELD_BOOK_MARK_BEGIN_POSITION, pagefactory.getM_mbBufBegin());
                		
                		try
						{
							pagefactory.nextPage();
						} catch (IOException e)
						{
							e.printStackTrace();
						}
            			Vector<String> details = pagefactory.pageUp();
                		values.put(DbTags.FIELD_BOOK_MARK_DETAIL, details.get(0));
                		
                		resolver.insert(Uri.parse(DbTags.URI_TABLE_BOOK_MARK), values);
                		
                		//Toast.makeText(BookReadActivity.this, "添加书签成功", 200).show();
            		}
            	}
            	else if(position==MENU_SETTING)
            	{
            	    //AndroidVoiceInstance.stopVoiceString();
                    Intent localIntent2 = new Intent(context, SettingTTSActivity.class);
                    startActivity(localIntent2);
            	}
            	else if(position==MENU_SHARE)
            	{
            	    Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
                    intent.setType("text/plain"); // 分享发送的数据类型
                    String msg = "点酷听书是款非常棒的阅读软件，推荐给大家。\n";
                    if(src.toString().length()>100){
                        msg+=src.toString().substring(0, 100);
                    }else{
                        msg+=src.toString().substring(0,src.toString().length()-1);
                    }
                    intent.putExtra(Intent.EXTRA_TEXT, msg); // 分享的内容
                    context.startActivity(Intent.createChooser(intent, "选择分享"));// 目标应用选择对话框的标题 
            	}

            }  
              
        }

		public void onClick(View v) 
		{
			if(v==ibtnSearch)
			{
				popupWindow.dismiss();
				popupWindow = null;
				String searchContent = etSearchContent.getText().toString();
				
				pagefactory.pageDown();
				Vector<String> currentPageLines = pagefactory.pageUp();
				for(int i=0;i<currentPageLines.size();i++)
				{
					if(currentPageLines.get(i).contains(searchContent))
					{
						
					}
				}
				
				LinearLayout searchControlView = null;
				if(searchControlView==null)
					searchControlView = (LinearLayout)getLayoutInflater().inflate(R.layout.search_control, null);  
				ibtnSearchBack = (ImageButton)searchControlView.findViewById(R.id.ibtnSearchBack);
				ibtnSearchForward = (ImageButton)searchControlView.findViewById(R.id.ibtnSearchForward);	
				ibtnSearchClear = (ImageButton)searchControlView.findViewById(R.id.ibtnSearchClear);
				ibtnSearchBack.setOnClickListener(this);
				ibtnSearchForward.setOnClickListener(this);
				ibtnSearchClear.setOnClickListener(this);
				
				showPopupWindowAtBottom(searchControlView,LayoutParams.FILL_PARENT,   
						100, Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM,0,0);
			}
			else if(v==ibtnSearchBack)
			{
				 
			}
			else if(v==ibtnSearchForward)
			{
				
			}
			else if(v==ibtnSearchClear)
			{
				
			}
		}  
          
    }  
    @Override  
    /** 
     * 创建MENU 
     */  
    public boolean onCreateOptionsMenu(Menu menu) {  
        menu.add("menu");// 必须创建一项  
        return super.onCreateOptionsMenu(menu);  
    } 
    @Override  
    /** 
     * 拦截MENU 
     */  
    public boolean onMenuOpened(int featureId, Menu menu) {  
        if (tabMenu != null) {  
            if (tabMenu.isShowing())  
                tabMenu.dismiss();  
            else {  
                tabMenu.showAtLocation(findViewById(R.id.rl_reader),  
                        Gravity.BOTTOM, 0, 0);  
            }  
        }  
        return false;// 返回为true 则显示系统menu  
    }  
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode==KeyEvent.KEYCODE_BACK)
		{
			if(popupWindow!=null&&popupWindow.isShowing())
			{
				popupWindow.dismiss();
				popupWindow = null;
				return true;
			}	
			
			if(!bookProgress.equals("onlyRead"))
    		{
				ContentValues values = new ContentValues();
				values.put(DbTags.FIELD_BOOK_PROGRESS, pagefactory.getStrPercent());
				values.put(DbTags.FIELD_BOOK_BEGIN_POSITION, pagefactory.getM_mbBufBegin());
				DbDataOperation.updateValuesToTable(resolver,(Uri.parse(DbTags.URI_TABLE_BOOK_INFO)),values, DbTags.FIELD_BOOK_ID+"=?", new String[]{bookId+""});
    		}
    	}
		else if(keyCode==KeyEvent.KEYCODE_MENU)
		{
			if(tabMenu!=null&&tabMenu.isShowing())
			{
				tabMenu.dismiss();
				tabMenu = null;
				return true;
			}
			if(popupWindow!=null&&popupWindow.isShowing())
			{
				popupWindow.dismiss();
				popupWindow = null;
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if(popupWindow!=null&&popupWindow.isShowing())
		{
			popupWindow.dismiss();
			popupWindow = null;
		}
		return super.onTouchEvent(event);
	}
	
	//SeekBar事件监听器
	class MySeekBarChangedListener implements OnSeekBarChangeListener
	{
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser)
		{
			currentProgress = seekBar.getProgress();
			if(seekBar==sbFontSize)
			{
				pagefactory.setM_fontSize(currentProgress);
				pagefactory.getmPaint().setTextSize(currentProgress);
				pagefactory.setmLineCount((int)pagefactory.getmVisibleHeight()/pagefactory.getM_fontSize());
				//Toast.makeText(BookReadActivity.this, currentProgress+" 号", 50).show();
				updateView();
			}
			else if(seekBar==sbBrightnessControl)
			{
				//Toast.makeText(BookReadActivity.this, "亮度："+currentProgress, 100).show();
				android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, currentProgress);
				currentProgress = Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS,-1);
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				if(0<=currentProgress&&currentProgress<=255)
					lp.buttonBrightness = currentProgress;
				getWindow().setAttributes(lp);
				Log.i("亮度-->",""+lp.buttonBrightness);
			}
			else if(seekBar==sbJump)
			{
				//计算并设置tvCurrentProgress显示当前进度
				double progressD = ((double)currentProgress/1000)*100 ;
				BigDecimal bd = new BigDecimal(progressD);
				String progressStr = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()+" %";
				tvCurrentProgress.setText(progressStr);
				
				//跳转进度
				pagefactory.setM_mbBufBegin((int) (pagefactory.getM_mbBufLen()*progressD/100));
				updateView();
				}
			else if(seekBar==redForegroundSeekBar)
			{
				redForegoundProgress = redForegroundSeekBar.getProgress();
				foreR = MathUtil.DtoX(redForegoundProgress);
				foreColor = foreR+foreG+foreB;
				currentForeColorId = 0xFF000000+Integer.parseInt(foreColor, 16);
				themeStyleTxt.setTextColor(currentForeColorId);
				fontColorTxt.setText("字体颜色：R:"+redForegoundProgress+",G:"+greenForegroundProgress+",B:"+blueForegroundProgress+"  -->#"+foreColor);
			}
			else if(seekBar==greenForegroundSeekBar)
			{
				greenForegroundProgress = greenForegroundSeekBar.getProgress();
				foreG = MathUtil.DtoX(greenForegroundProgress);
				foreColor = foreR+foreG+foreB;
				currentForeColorId = 0xFF000000+Integer.parseInt(foreColor, 16);
				themeStyleTxt.setTextColor(currentForeColorId);
				fontColorTxt.setText("字体颜色：R:"+redForegoundProgress+",G:"+greenForegroundProgress+",B:"+blueForegroundProgress+"  -->#"+foreColor);
			}
			else if(seekBar==blueForegroundSeekBar)
			{
				blueForegroundProgress = blueForegroundSeekBar.getProgress();
				foreB = MathUtil.DtoX(blueForegroundProgress);
				foreColor = foreR+foreG+foreB;
				currentForeColorId = 0xFF000000+Integer.parseInt(foreColor, 16);
				themeStyleTxt.setTextColor(currentForeColorId);
				fontColorTxt.setText("字体颜色：R:"+redForegoundProgress+",G:"+greenForegroundProgress+",B:"+blueForegroundProgress+"  -->#"+foreColor);
			}
			else if(seekBar==redBackgroundSeekBar)
			{
				redBackgoundProgress = redBackgroundSeekBar.getProgress();
				backR = MathUtil.DtoX(redBackgoundProgress);
				backColor = backR+backG+backB;
				currentBackColorId = 0xFF000000+Integer.parseInt(backColor, 16);
				themeStyleTxt.setBackgroundColor(currentBackColorId);
				backgroundColorTxt.setText("背景颜色：R:"+redBackgoundProgress+",G:"+greenBackgroundProgress+",B:"+blueBackgroundProgress+"  -->#"+backColor);
			}
			else if(seekBar==greenBackgroundSeekBar)
			{
				greenBackgroundProgress = greenBackgroundSeekBar.getProgress();
				backG = MathUtil.DtoX(greenBackgroundProgress);
				backColor = backR+backG+backB;
				currentBackColorId = 0xFF000000+Integer.parseInt(backColor, 16);
				themeStyleTxt.setBackgroundColor(currentBackColorId);
				backgroundColorTxt.setText("背景颜色：R:"+redBackgoundProgress+",G:"+greenBackgroundProgress+",B:"+blueBackgroundProgress+"  -->#"+backColor);
			}
			else if(seekBar==blueBackgroundSeekBar)
			{
				blueBackgroundProgress = blueBackgroundSeekBar.getProgress();
				backB = MathUtil.DtoX(blueBackgroundProgress);
				backColor = backR+backG+backB;
				currentBackColorId = 0xFF000000+Integer.parseInt(backColor, 16);
				themeStyleTxt.setBackgroundColor(currentBackColorId);
				backgroundColorTxt.setText("背景颜色：R:"+redBackgoundProgress+",G:"+greenBackgroundProgress+",B:"+blueBackgroundProgress+"  -->#"+backColor);
			}
		}
		
		public void onStartTrackingTouch(SeekBar seekBar)
		{
		}
		public void onStopTrackingTouch(SeekBar seekBar)
		{
		}
	}
	
	
	/**
	 * 根据新的设置更新界面
	 */
	public void updateView()
	{
	    
		mPageWidget.invalidate();
		try
		{
			pagefactory.prePage();
			pagefactory.nextPage();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		pagefactory.Draw(mCurPageCanvas);	
		pagefactory.Draw(mNextPageCanvas);
		
	}
	
	/**
	 * 初始化，并根据view显示响应的Popupwindow
	 * @param view Popupwindow中的内容
	 */
	public void showPopupWindowAtBottom(View view,int width,int height,final int gravity,final int x,final int y)
	{
		popupWindow = new PopupWindow(view, width,   
				height);
		
		Handler handler = null;
		if(handler==null)
			handler =  new Handler();
        handler.postDelayed(new Runnable()
		{
			public void run()
			{
				popupWindow.showAtLocation(mPageWidget,gravity, x, y);
			}
		}, 250);
	}
	
	/**
	 * 自动阅读
	 * @author 002666
	 *
	 */
	class FlipOverRunnable implements Runnable
	{
	    
		public void run()
		{
			try
			{	
				pagefactory.nextPage();
				readHandler.postDelayed(this, 3000);
			} 
    		catch (IOException e)
			{
				e.printStackTrace();
			}
			
    		updateView();
		}
	}
	
	/**
	 * 显示章节目录
	 */
	public void showChapterDialog()
	{
		
		new AlertDialog.Builder(BookReadActivity.this).setItems(ArrayUtil.getStringArray(chapterName), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
//				for(int i=0;i<chapterBeginPositions.size())
				pagefactory.setM_mbBufBegin(chapterBeginPositions.get(which));
				updateView();
			}
		}).show();
	}
	
	@Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        SharedPreferences.Editor localEditor = context.getSharedPreferences("audio", 0).edit();
        localEditor.putInt("flag", 0);
        localEditor.commit();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    public void getCureentChapter()
	{	
		new Thread(new Runnable()
		{
			public void run()
			{
				boolean isFind = true;
				pagefactory.pageUp();
        		while(isFind)
        		{
            		lines= pagefactory.pageDown();
            		if(lines.size()>0)
        			{
	            		for(int i=0;i<lines.size();i++)
	            		{      		
	            			matcher = pattern.matcher(lines.get(i));
	            			if(matcher.find())
	            			{
	            				currentChapter = matcher.group();
	            				Log.i("chapter"+i+"-->",matcher.group());
	            				isFind = false;
	            				break;	
	            			}
            			}
            		}
        		}
        		if(preOrNext)
        			preHandler.sendEmptyMessage(1);
        		else
        			nextHandler.sendEmptyMessage(1);
			}
		}).start();
	}
}