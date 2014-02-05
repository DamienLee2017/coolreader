package com.dotcool.reader.task;

import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dotcool.reader.LNReaderApplication;
import com.dotcool.R;
import com.dotcool.reader.callback.CallbackEventData;
import com.dotcool.reader.callback.ICallbackEventData;
import com.dotcool.reader.callback.ICallbackNotifier;
import com.dotcool.reader.dao.NovelsDao;
import com.dotcool.reader.helper.AsyncTaskResult;
import com.dotcool.reader.model.PageModel;

public class LoadNovelsTask extends AsyncTask<Void, ICallbackEventData, AsyncTaskResult<PageModel[]>> implements ICallbackNotifier {
	private static final String TAG = LoadNovelsTask.class.toString();
	private boolean refreshOnly = false;
	private boolean onlyWatched = false;
	private boolean alphOrder = false;
	private String api="";
	public volatile IAsyncTaskOwner owner;

	public LoadNovelsTask(IAsyncTaskOwner owner, boolean refreshOnly, boolean onlyWatched, boolean alphOrder,String api) {
		this.refreshOnly = refreshOnly;
		this.onlyWatched = onlyWatched;
		this.alphOrder = alphOrder;
		this.owner = owner;
		this.api=api;
	}

	public void onCallback(ICallbackEventData message) {
		publishProgress(message);
	}

	@Override
	protected void onPreExecute() {
		// executed on UI thread.
		owner.toggleProgressBar(true);
	}

	@Override
	protected AsyncTaskResult<PageModel[]> doInBackground(Void... arg0) {
		Context ctx = LNReaderApplication.getInstance().getApplicationContext();
		// different thread from UI
		try {
			ArrayList<PageModel> novels = new ArrayList<PageModel>();
			if (onlyWatched) {
				publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_novels_task_watched)));
				novels = NovelsDao.getInstance().getWatchedNovel();
			}
			else {
				if (refreshOnly) {
					publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_novels_task_refreshing)));
					novels = NovelsDao.getInstance().getNovelsFromInternet(this,api);
				}
				else {
					publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_novels_task_loading)));
					novels = NovelsDao.getInstance().getNovels(this, alphOrder,api);
				}
			}
			return new AsyncTaskResult<PageModel[]>(novels.toArray(new PageModel[novels.size()]));
		} catch (Exception e) {
			Log.e(TAG, "Error when getting novel list: " + e.getMessage(), e);
			publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_novels_task_error, e.getMessage())));
			return new AsyncTaskResult<PageModel[]>(e);
		}
	}

	@Override
	protected void onProgressUpdate(ICallbackEventData... values) {
		owner.setMessageDialog(values[0]);
	}

	@Override
	protected void onPostExecute(AsyncTaskResult<PageModel[]> result) {
		// executed on UI thread.
		if (onlyWatched) {
			owner.setMessageDialog(new CallbackEventData(owner.getContext().getResources().getString(R.string.load_novels_task_watched_complete)));
		} else {
			owner.setMessageDialog(new CallbackEventData(owner.getContext().getResources().getString(R.string.load_novels_task_complete)));
		}
		owner.onGetResult(result, PageModel[].class);
	}
}