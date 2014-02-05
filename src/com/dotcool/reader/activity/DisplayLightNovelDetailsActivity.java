package com.dotcool.reader.activity;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dotcool.bll.DbTags;
import com.dotcool.reader.Constants;
import com.dotcool.reader.LNReaderApplication;
import com.dotcool.R;
import com.dotcool.reader.UIHelper;
import com.dotcool.reader.adapter.BookModelAdapter;
import com.dotcool.reader.callback.ICallbackEventData;
import com.dotcool.reader.dao.NovelsDao;
import com.dotcool.reader.helper.AsyncTaskResult;
import com.dotcool.reader.model.BookModel;
import com.dotcool.reader.model.NovelCollectionModel;
import com.dotcool.reader.model.NovelContentModel;
import com.dotcool.reader.model.PageModel;
import com.dotcool.reader.parser.CommonParser;
import com.dotcool.reader.task.DownloadNovelContentTask;
import com.dotcool.reader.task.IAsyncTaskOwner;
import com.dotcool.reader.task.LoadNovelDetailsTask;
import com.dotcool.view.BookReadActivity;
import com.dotcool.view.BookshelfActivity;

public class DisplayLightNovelDetailsActivity extends SherlockActivity implements IAsyncTaskOwner {
	public static final String TAG = DisplayLightNovelDetailsActivity.class.toString();
	private PageModel page;
	private NovelCollectionModel novelCol;
	private final NovelsDao dao = NovelsDao.getInstance(this);

	private BookModelAdapter bookModelAdapter;
	private ExpandableListView expandList;

	private DownloadNovelContentTask downloadTask = null;
	private LoadNovelDetailsTask task = null;

	private TextView loadingText;
	private ProgressBar loadingBar;
	private boolean isInverted;
	private String touchedForDownload;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UIHelper.SetTheme(this, R.layout.activity_display_light_novel_details);
		UIHelper.SetActionBarDisplayHomeAsUp(this, true);

		// Get intent and message
		Intent intent = getIntent();
		page = new PageModel();
		page.setPage(intent.getStringExtra(Constants.EXTRA_PAGE));
		try {
			page = NovelsDao.getInstance(this).getPageModel(page, null);
		} catch (Exception e) {
			Log.e(TAG, "Error when getting Page Model for " + page.getPage(), e);
		}

		// setup listener
		expandList = (ExpandableListView) findViewById(R.id.chapter_list);
		registerForContextMenu(expandList);
		expandList.setOnChildClickListener(new OnChildClickListener() {
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				if (novelCol != null) {
					PageModel chapter = bookModelAdapter.getChild(groupPosition, childPosition);
					String bookName = novelCol.getBookCollections().get(groupPosition).getTitle();
					touchedForDownload = bookName ;
					//loadChapter(chapter);
				}
				return false;
			}
		});
		expandList.setOnGroupClickListener(new OnGroupClickListener(){

            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition,
                    long id) {
                if (novelCol != null) {
                    BookModel chapter = bookModelAdapter.getGroup(groupPosition);
                    loadCotent(chapter);
                  
                }
                    
                return false;
            }
		    
		});
		setTitle(page.getTitle());
		isInverted = UIHelper.getColorPreferences(this);

		loadingText = (TextView) findViewById(R.id.emptyList);
		loadingBar = (ProgressBar) findViewById(R.id.empttListProgress);

		executeTask(page, false);
	}
	private void loadCotent(BookModel chapter) {

        downloadTask = new DownloadNovelContentTask(chapter, DisplayLightNovelDetailsActivity.this);
        downloadTask.execute();
  
	}


	@Override
	protected void onRestart() {
		super.onRestart();
		if (isInverted != UIHelper.getColorPreferences(this)) {
			UIHelper.Recreate(this);
		}
		if (bookModelAdapter != null) {
			bookModelAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "OnResume: " + task.getStatus().toString());
	}

	@Override
	public void onStop() {

		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_display_light_novel_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			super.onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

		MenuInflater inflater = getMenuInflater();
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			inflater.inflate(R.menu.novel_details_volume_context_menu, menu);
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			inflater.inflate(R.menu.novel_details_chapter_context_menu, menu);
		}
	}

	
	@SuppressLint("NewApi")
	private void executeTask(PageModel pageModel, boolean willRefresh) {
		task = new LoadNovelDetailsTask(willRefresh, this);
		String key = TAG + Constants.KEY_LOAD_CHAPTER + pageModel.getPage();
		boolean isAdded = LNReaderApplication.getInstance().addTask(key, task);

		if (isAdded) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new PageModel[] { pageModel });
			else
				task.execute(new PageModel[] { pageModel });
		} else {
			LoadNovelDetailsTask tempTask = (LoadNovelDetailsTask) LNReaderApplication.getInstance().getTask(key);
			if (tempTask != null) {
				task = tempTask;
				task.owner = this;
				toggleProgressBar(true);
			}
		}
	}

	@SuppressLint("NewApi")
	private void executeDownloadTask(BookModel chapters, boolean isAll) {
		if (page != null) {
			downloadTask = new DownloadNovelContentTask(chapters, this);
			String key = TAG + Constants.KEY_DOWNLOAD_CHAPTER + page.getPage();
			if (isAll) {
				key = TAG + Constants.KEY_DOWNLOAD_ALL_CHAPTER + page.getPage();
			}
			boolean isAdded = LNReaderApplication.getInstance().addTask(key, task);

			if (isAdded) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				else
					downloadTask.execute();
			} else {
				DownloadNovelContentTask tempTask = (DownloadNovelContentTask) LNReaderApplication.getInstance().getTask(key);
				if (tempTask != null) {
					downloadTask = tempTask;
					downloadTask.owner = this;
				}
			}
		}
	}

	public boolean downloadListSetup(String id, String toastText, int type, boolean hasError) {
		boolean exists = false;
		String name = page.getTitle() + " " + touchedForDownload;
		if (type == 0) {
			if (LNReaderApplication.getInstance().checkIfDownloadExists(name)) {
				exists = true;
				Toast.makeText(this, getResources().getString(R.string.download_on_queue), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, getResources().getString(R.string.toast_downloading, name), Toast.LENGTH_SHORT).show();
				LNReaderApplication.getInstance().addDownload(id, name);
			}
		} else if (type == 1) {
			Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
		} else if (type == 2) {
			String message = getResources().getString(R.string.toast_download_finish, page.getTitle(), LNReaderApplication.getInstance().getDownloadDescription(id));
			if (hasError)
				message = getResources().getString(R.string.toast_download_finish_with_error, page.getTitle(), LNReaderApplication.getInstance().getDownloadDescription(id));
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
			LNReaderApplication.getInstance().removeDownload(id);
		}
		return exists;
	}

	public void updateProgress(String id, int current, int total, String message) {
		double cur = current;
		double tot = total;
		double result = (cur / tot) * 100;
		LNReaderApplication.getInstance().updateDownload(id, (int) result, message);
		if (loadingBar != null && loadingBar.getVisibility() == View.VISIBLE) {
			loadingBar.setIndeterminate(false);
			loadingBar.setMax(total);
			loadingBar.setProgress(current);
			loadingBar.setProgress(0);
			loadingBar.setProgress(current);
			loadingBar.setMax(total);
		}
	}

	public void toggleProgressBar(boolean show) {
		if (show) {
			loadingText.setText("加载中，请稍后...");
			loadingText.setVisibility(TextView.VISIBLE);
			loadingBar.setVisibility(ProgressBar.VISIBLE);
			loadingBar.setIndeterminate(true);
			expandList.setVisibility(ListView.GONE);
		} else {
			loadingText.setVisibility(TextView.GONE);
			loadingBar.setVisibility(ProgressBar.GONE);
			expandList.setVisibility(ListView.VISIBLE);
		}
	}

	public void setMessageDialog(ICallbackEventData message) {
		if (loadingText.getVisibility() == View.VISIBLE) {
			loadingText.setText(message.getMessage());
		}
	}

	@SuppressLint("NewApi")
	public void onGetResult(AsyncTaskResult<?> result, Class<?> t) {
		Exception e = result.getError();

		if (e == null) {
			// from DownloadNovelContentTask
			if (t == NovelContentModel.class) {
				NovelContentModel content = (NovelContentModel) result.getResult();
				if (content != null) {
				    Intent intent = new Intent(DisplayLightNovelDetailsActivity.this ,BookReadActivity.class);
			        intent.putExtra("net", 1);
			        intent.putExtra("net_content", content.getContent());
			        intent.putExtra("net_book_id", content.getId());
			        startActivity(intent);
					bookModelAdapter.notifyDataSetChanged();
				}
			}
			// from LoadNovelDetailsTask
			else if (t == NovelCollectionModel.class) {
				novelCol = (NovelCollectionModel) result.getResult();
				if(novelCol==null){
				      return;
				}
				expandList = (ExpandableListView) findViewById(R.id.chapter_list);
				// now add the volume and chapter list.
				try {
					// Prepare header
					if (expandList.getHeaderViewsCount() == 0) {
						page = novelCol.getPageModel();
						LayoutInflater layoutInflater = getLayoutInflater();
						View synopsis = layoutInflater.inflate(R.layout.activity_display_synopsis, null);
						TextView textViewTitle = (TextView) synopsis.findViewById(R.id.title);
						TextView textViewSynopsis = (TextView) synopsis.findViewById(R.id.synopsys);
						textViewTitle.setTextSize(20);
						textViewSynopsis.setTextSize(16);
						String title = page.getTitle();
						if (page.isTeaser()) {
							title += " (" + getResources().getString(R.string.teaser_project) + ")";
						}
						if (page.isStalled()) {
							title += "\nStatus: " + getResources().getString(R.string.project_stalled);
						}
						if (page.isAbandoned()) {
							title += "\nStatus: " + getResources().getString(R.string.project_abandonded);
						}
						if (page.isPending()) {
							title += "\nStatus: " + getResources().getString(R.string.project_pending_authorization);
						}

						textViewTitle.setText(title);
						textViewSynopsis.setText(novelCol.getPageModel().getSummary());

						CheckBox isWatched = (CheckBox) synopsis.findViewById(R.id.isWatched);
						isWatched.setChecked(page.isWatched());
						isWatched.setOnCheckedChangeListener(new OnCheckedChangeListener() {

							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
								updateWatchStatus(isChecked, page);
							}
						});

						ImageView ImageViewCover = (ImageView) synopsis.findViewById(R.id.cover);
						if (novelCol.getCoverBitmap() == null) {
							// IN app test, is returning empty bitmap
							Toast.makeText(this, getResources().getString(R.string.toast_err_bitmap_empty), Toast.LENGTH_LONG).show();
						} else {

							if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) && UIHelper.getStrechCoverPreference(this)) {
								Drawable coverDrawable = new BitmapDrawable(getResources(), novelCol.getCoverBitmap());
								int coverHeight = novelCol.getCoverBitmap().getHeight();
								int coverWidth = novelCol.getCoverBitmap().getWidth();
								int screenWidth = (int) (UIHelper.getScreenHeight(this) * 0.9);
								int finalHeight = coverHeight * (screenWidth / coverWidth);
								ImageViewCover.setBackground(coverDrawable);
								ImageViewCover.getLayoutParams().height = finalHeight;
								ImageViewCover.getLayoutParams().width = screenWidth;
							} else {
								Log.d(TAG, "Non Stretch");
								ImageViewCover.setImageBitmap(novelCol.getCoverBitmap());
								ImageViewCover.getLayoutParams().height = novelCol.getCoverBitmap().getHeight();
								ImageViewCover.getLayoutParams().width = novelCol.getCoverBitmap().getWidth();
							}
						}

						expandList.addHeaderView(synopsis);
					}
					bookModelAdapter = new BookModelAdapter(DisplayLightNovelDetailsActivity.this, novelCol.getBookCollections());
					expandList.setAdapter(bookModelAdapter);
					Log.d(TAG, "Loaded: " + novelCol.getPage());
				} catch (Exception e2) {
					Log.e(TAG, "Error when setting up chapter list: " + e2.getMessage(), e2);
					Toast.makeText(DisplayLightNovelDetailsActivity.this, getResources().getString(R.string.error_setting_chapter_list, e2.getMessage()), Toast.LENGTH_SHORT).show();
				}

				if (novelCol == null) {
					Log.e(TAG, "Empty Novel Collection: " + getIntent().getStringExtra(Constants.EXTRA_PAGE));
				}
			}
		} else {
			Log.e(TAG, e.getClass().toString() + ": " + e.getMessage(), e);
			Toast.makeText(this, e.getClass().toString() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}

		toggleProgressBar(false);
	}

	private void updateWatchStatus(boolean isChecked, PageModel page) {
		if (isChecked) {
			Toast.makeText(this, getResources().getString(R.string.toast_add_watch, page.getTitle()), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, getResources().getString(R.string.toast_remove_watch, page.getTitle()), Toast.LENGTH_SHORT).show();
		}
		// update the db!
		page.setWatched(isChecked);
		NovelsDao dao = NovelsDao.getInstance(this);
		dao.updatePageModel(page);
	}


	public Context getContext() {
		return this;
	}
}
