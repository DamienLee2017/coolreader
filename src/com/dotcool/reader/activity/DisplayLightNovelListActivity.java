package com.dotcool.reader.activity;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dotcool.reader.Constants;
import com.dotcool.reader.LNReaderApplication;
import com.dotcool.R;
import com.dotcool.reader.UIHelper;
import com.dotcool.reader.adapter.PageModelAdapter;
import com.dotcool.reader.callback.CallbackEventData;
import com.dotcool.reader.callback.ICallbackEventData;
import com.dotcool.reader.dao.NovelsDao;
import com.dotcool.reader.helper.AsyncTaskResult;
import com.dotcool.reader.model.NovelCollectionModel;
import com.dotcool.reader.model.PageModel;
import com.dotcool.reader.task.AddNovelTask;
import com.dotcool.reader.task.DownloadNovelDetailsTask;
import com.dotcool.reader.task.IAsyncTaskOwner;
import com.dotcool.reader.task.LoadNovelsTask;

/*
 * Author: Nandaka
 * Copy from: NovelsActivity.java
 */

public class DisplayLightNovelListActivity extends SherlockListActivity implements IAsyncTaskOwner, INovelListHelper {
	private static final String TAG = DisplayLightNovelListActivity.class.toString();
	private final ArrayList<PageModel> listItems = new ArrayList<PageModel>();
	private PageModelAdapter adapter;
	private LoadNovelsTask task = null;
	private DownloadNovelDetailsTask downloadTask = null;
	private AddNovelTask addTask = null;

	private boolean isInverted;
	private boolean onlyWatched = false;
	private int categoryid=0;
	String touchedForDownload;
	private String api;
	private TextView loadingText;
	private ProgressBar loadingBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UIHelper.SetTheme(this, R.layout.activity_display_light_novel_list);
		UIHelper.SetActionBarDisplayHomeAsUp(this, true);

		loadingText = (TextView) findViewById(R.id.emptyList);
		loadingBar = (ProgressBar) findViewById(R.id.empttListProgress);

		registerForContextMenu(getListView());
		onlyWatched = false;
		//如果有分类id才是
		categoryid = getIntent().getIntExtra(Constants.EXTRA_CATEGORY_ID, 0);
    	switch(categoryid) {
    		case Constants.EXTRA_HISTORY_CATEGORY_ID:
    			setTitle(getResources().getString(R.string.str_history));
    			api="/api/leixin/cat/"+categoryid;
    			break;
    		case Constants.EXTRA_XUANHUAN_CATEGORY_ID:
                setTitle(getResources().getString(R.string.str_xuanhuan));
                api="/api/leixin/cat/"+categoryid;
                break;
    		case Constants.EXTRA_KEHUAN_CATEGORY_ID:
                setTitle(getResources().getString(R.string.str_kehuan));
                api="/api/leixin/cat/"+categoryid;
                break;
    		case Constants.EXTRA_QITA_CATEGORY_ID:
                setTitle(getResources().getString(R.string.str_qita));
                api="/api/leixin/cat/"+categoryid;
                break;
    		case Constants.EXTRA_UPDATE_CATEGORY_ID:
                setTitle(getResources().getString(R.string.light_novels));
                api="/api/book/update/"+categoryid;
                break;
    		case Constants.EXTRA_MOOD_CATEGORY_ID:
                setTitle(getResources().getString(R.string.originals));
                api="/api/book/mood/"+categoryid;
                break;
    		}

		registerForContextMenu(getListView());
		isInverted = UIHelper.getColorPreferences(this);
		
		// Encapsulated in updateContent
		updateContent(false, onlyWatched,api);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// Get the item that was clicked
		PageModel o = adapter.getItem(position);
		String novel = o.toString();
		// Create new intent
		Intent intent = new Intent(this, DisplayLightNovelDetailsActivity.class);
		intent.putExtra(Constants.EXTRA_NOVEL, novel);
		intent.putExtra(Constants.EXTRA_PAGE, o.getPage());
		intent.putExtra(Constants.EXTRA_ID, o.getId());
		//intent.putExtra(Constants.EXTRA_SUMMARY, o.getBookitem().getBookSummary());
		intent.putExtra(Constants.EXTRA_TITLE, o.getTitle());
		intent.putExtra(Constants.EXTRA_ONLY_WATCHED, getIntent().getBooleanExtra(Constants.EXTRA_ONLY_WATCHED, false));
		startActivity(intent);
		Log.d("DisplayLightNovelsActivity", o.getId() + " (" + o.getTitle() + ")");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_display_light_novel_list, menu);
		return true;
	}

	@Override
	protected void onStop() {

		super.onStop();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (isInverted != UIHelper.getColorPreferences(this)) {
			UIHelper.Recreate(this);
		}
		if (adapter != null)
			adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh_novel_list:
			refreshList();
			return true;
		case R.id.invert_colors:
			UIHelper.ToggleColorPref(this);
			UIHelper.Recreate(this);
			return true;
		case android.R.id.home:
			super.onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void refreshList() {
		boolean onlyWatched = getIntent().getBooleanExtra(Constants.EXTRA_ONLY_WATCHED, false);
		updateContent(true, onlyWatched,api);
		Toast.makeText(this, "刷新中，请稍后...", Toast.LENGTH_SHORT).show();
	}

	public void downloadAllNovelInfo() {
		if (onlyWatched)
			touchedForDownload = "Watched Light Novels information";
		else
			touchedForDownload = "All Main Light Novels information";
		executeDownloadTask(listItems);
	}

	public void manualAdd() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getResources().getString(R.string.add_novel_main));
		// alert.setMessage("Message");
		LayoutInflater factory = LayoutInflater.from(this);
		View inputView = factory.inflate(R.layout.layout_add_new_novel, null);
		final EditText inputName = (EditText) inputView.findViewById(R.id.page);
		final EditText inputTitle = (EditText) inputView.findViewById(R.id.title);
		alert.setView(inputView);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (whichButton == DialogInterface.BUTTON_POSITIVE) {
					handleOK(inputName, inputTitle);
				}
			}
		});
		alert.setNegativeButton(getResources().getString(R.string.cancel), null);
		alert.show();
	}

	private void handleOK(EditText input, EditText inputTitle) {
		String novel = input.getText().toString();
		String title = inputTitle.getText().toString();
		if (novel != null && novel.length() > 0 && inputTitle != null && inputTitle.length() > 0) {
			PageModel temp = new PageModel();
			temp.setPage(novel);
			temp.setTitle(title);
			temp.setType(PageModel.TYPE_NOVEL);
			temp.setParent("Main_Page");
			executeAddTask(temp);
		} else {
			Toast.makeText(this, "沒有輸入", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.novel_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.add_to_watch:
			/*
			 * Implement code to toggle watch of this novel
			 */
			if (info.position > -1) {
				PageModel novel = listItems.get(info.position);
				if (novel.isWatched()) {
					novel.setWatched(false);
					Toast.makeText(this, getResources().getString(R.string.removed_from_watchlist) + ": " + novel.getTitle(), Toast.LENGTH_SHORT).show();
				} else {
					novel.setWatched(true);
					Toast.makeText(this, getResources().getString(R.string.added_to_watchlist) + ": " + novel.getTitle(), Toast.LENGTH_SHORT).show();
				}
				NovelsDao.getInstance(this).updatePageModel(novel);
				adapter.notifyDataSetChanged();
			}
			return true;
		case R.id.download_novel:
			/*
			 * Implement code to download novel synopsis
			 */
			if (info.position > -1) {
				PageModel novel = listItems.get(info.position);
				ArrayList<PageModel> novels = new ArrayList<PageModel>();
				novels.add(novel);
				touchedForDownload = novel.getTitle() + "'s information";
				executeDownloadTask(novels);
			}
			return true;
		case R.id.delete_novel:
			if (info.position > -1) {
				toggleProgressBar(true);
				PageModel novel = listItems.get(info.position);
				boolean result = NovelsDao.getInstance(this).deleteNovel(novel);
				if (result) {
					listItems.remove(novel);
					adapter.notifyDataSetChanged();
				}
				toggleProgressBar(false);
			}
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void updateContent(boolean isRefresh, boolean onlyWatched,String api) {
		try {
			// Check size
			int resourceId = R.layout.novel_list_item;
			if (UIHelper.IsSmallScreen(this)) {
				resourceId = R.layout.novel_list_item_small;
			}
			if (adapter != null) {
				adapter.setResourceId(resourceId);
			} else {
				adapter = new PageModelAdapter(this, resourceId, listItems);
			}
			boolean alphOrder = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.PREF_ALPH_ORDER, false);
			executeTask(isRefresh, onlyWatched, alphOrder,api);
			setListAdapter(adapter);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			Toast.makeText(this, getResources().getString(R.string.error_update) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@SuppressLint("NewApi")
	private void executeTask(boolean isRefresh, boolean onlyWatched, boolean alphOrder,String api) {
		task = new LoadNovelsTask(this, isRefresh, onlyWatched, alphOrder,api);
		String key = TAG + ":Main+Page";
		boolean isAdded = LNReaderApplication.getInstance().addTask(key, task);
		if (isAdded) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			else
				task.execute();
		} else {
			Log.i(TAG, "Continue execute task: " + key);
			LoadNovelsTask tempTask = (LoadNovelsTask) LNReaderApplication.getInstance().getTask(key);
			if (tempTask != null) {
				task = tempTask;
				task.owner = this;
			}
			toggleProgressBar(true);
		}
	}

	public boolean downloadListSetup(String id, String toastText, int type, boolean hasError) {
		boolean exists = false;
		String name = touchedForDownload;
		if (type == 0) {
			if (LNReaderApplication.getInstance().checkIfDownloadExists(name)) {
				exists = true;
				Toast.makeText(this, getResources().getString(R.string.download_on_queue), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, getResources().getString(R.string.downloading) + name + ".", Toast.LENGTH_SHORT).show();
				LNReaderApplication.getInstance().addDownload(id, name);
			}
		} else if (type == 1) {
			Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
		} else if (type == 2) {
			String message = String.format("%s's 下载完毕!", LNReaderApplication.getInstance().getDownloadDescription(id));
			if (hasError)
				message = String.format("%s's 下载错误!", LNReaderApplication.getInstance().getDownloadDescription(id));

			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
			LNReaderApplication.getInstance().removeDownload(id);
		}
		return exists;
	}

	public void updateProgress(String id, int current, int total, String messString) {
		double cur = current;
		double tot = total;
		double result = (cur / tot) * 100;
		LNReaderApplication.getInstance().updateDownload(id, (int) result, messString);
		if (loadingBar != null && loadingBar.getVisibility() == View.VISIBLE) {
			loadingBar.setIndeterminate(false);
			loadingBar.setMax(total);
			loadingBar.setProgress(current);
			loadingBar.setProgress(0);
			loadingBar.setProgress(current);
			loadingBar.setMax(total);
		}
	}

	@SuppressLint("NewApi")
	private void executeDownloadTask(ArrayList<PageModel> novels) {
		downloadTask = new DownloadNovelDetailsTask(this);
		if (novels == null || novels.size() == 0) {
			Log.w(TAG, getResources().getString(R.string.error_empty_download_array));
			return;
		}
		String key = DisplayLightNovelDetailsActivity.TAG + ":" + novels.get(0).getPage();
		if (novels.size() > 1) {
			key = DisplayLightNovelDetailsActivity.TAG + ":All_Novels";
		}
		boolean isAdded = LNReaderApplication.getInstance().addTask(key, downloadTask);
		if (isAdded) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, novels.toArray(new PageModel[novels.size()]));
			else
				downloadTask.execute(novels.toArray(new PageModel[novels.size()]));
		} else {
			Log.i(TAG, "继续系在任务: " + key);
			DownloadNovelDetailsTask tempTask = (DownloadNovelDetailsTask) LNReaderApplication.getInstance().getTask(key);
			if (tempTask != null) {
				downloadTask = tempTask;
				downloadTask.owner = this;
			}
			toggleProgressBar(true);
		}
	}

	@SuppressLint("NewApi")
	private void executeAddTask(PageModel novel) {
		addTask = new AddNovelTask(this);
		String key = DisplayLightNovelDetailsActivity.TAG + ":Add:" + novel.getPage();
		boolean isAdded = LNReaderApplication.getInstance().addTask(key, addTask);
		if (isAdded) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				addTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new PageModel[] { novel });
			else
				addTask.execute(new PageModel[] { novel });
		} else {
			Log.i(TAG, "Continue Add task: " + key);
			AddNovelTask tempTask = (AddNovelTask) LNReaderApplication.getInstance().getTask(key);
			if (tempTask != null) {
				addTask = tempTask;
				addTask.owner = this;
			}
			toggleProgressBar(true);
		}
	}

	public void toggleProgressBar(boolean show) {
		if (show) {
			loadingText.setText("正在加载中，请稍后...");
			loadingText.setVisibility(TextView.VISIBLE);
			loadingBar.setVisibility(ProgressBar.VISIBLE);
			loadingBar.setIndeterminate(true);
			getListView().setVisibility(ListView.GONE);
		} else {
			loadingText.setVisibility(TextView.GONE);
			loadingBar.setVisibility(ProgressBar.GONE);
			getListView().setVisibility(ListView.VISIBLE);
		}
	}

	public void setMessageDialog(ICallbackEventData message) {
		if (loadingText.getVisibility() == TextView.VISIBLE)
			loadingText.setText(message.getMessage());
	}

	public void onGetResult(AsyncTaskResult<?> result, Class<?> t) {
		Exception e = result.getError();
		if (e == null) {
			// from LoadNovelsTask
			if (t == PageModel[].class) {
				PageModel[] list = (PageModel[]) result.getResult();
				toggleProgressBar(false);
				if (list != null && list.length > 0) {
					adapter.clear();
					adapter.addAll(list);

					// Show message if watch list is empty
					if (onlyWatched) {
						if (list.length == 0) {
							Log.d(TAG, "WatchList result set message empty");
							TextView tv = (TextView) findViewById(R.id.emptyList);
							tv.setVisibility(TextView.VISIBLE);
							tv.setText("列表为空.");
						}
					}
				}
				else {
					TextView tv = (TextView) findViewById(R.id.emptyList);
					tv.setVisibility(TextView.VISIBLE);
					tv.setText("列表为空.");
					Log.w(TAG, "列表为空!");
				}
			}
			// from DownloadNovelDetailsTask
			else if (t == NovelCollectionModel[].class) {
				setMessageDialog(new CallbackEventData("Download complete."));
				NovelCollectionModel[] list = (NovelCollectionModel[]) result.getResult();
				if (list.length > 0) {
					for (NovelCollectionModel novelCol : list) {
						try {
							PageModel page = novelCol.getPageModel();
							boolean found = false;
							for (PageModel temp : adapter.data) {
								if (temp.getPage().equalsIgnoreCase(page.getPage())) {
									found = true;
									break;
								}
							}
							if (!found) {
								adapter.data.add(page);
							}
						} catch (Exception e1) {
							Log.e(TAG, e1.getClass().toString() + ": " + e1.getMessage(), e1);
						}
					}
				}
				adapter.notifyDataSetChanged();
				toggleProgressBar(false);
			} else {
				Log.e(TAG, "Unknown ResultType: " + t.getName());
			}
		} else {
			Log.e(TAG, e.getClass().toString() + ": " + e.getMessage(), e);
			Toast.makeText(this, e.getClass().toString() + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	public Context getContext() {
		return this;
	}
}
