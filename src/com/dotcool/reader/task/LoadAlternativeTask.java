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

/*
 * Modified by : freedomofkeima
 */

public class LoadAlternativeTask extends AsyncTask<Void, ICallbackEventData, AsyncTaskResult<PageModel[]>> implements ICallbackNotifier {
	private static final String TAG = LoadAlternativeTask.class.toString();
	private boolean refreshOnly = false;
	private boolean alphOrder = false;
	private String language = null;
	public volatile IAsyncTaskOwner owner;

	public LoadAlternativeTask(IAsyncTaskOwner owner, boolean refreshOnly, boolean alphOrder, String language) {
		this.refreshOnly = refreshOnly;
		this.alphOrder = alphOrder;
		this.owner = owner;
		this.language = language;
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
		Context ctx = owner.getContext();
		// different thread from UI
		try {
			ArrayList<PageModel> novels = new ArrayList<PageModel>();
			if (refreshOnly) {
				publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_novel_alt_task_refreshing, language)));
				novels = NovelsDao.getInstance().getAlternativeFromInternet(this, language);

			}
			else {
				publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_novel_alt_task_loading, language)));
				novels = NovelsDao.getInstance().getAlternative(this, alphOrder, language);
			}
			return new AsyncTaskResult<PageModel[]>(novels.toArray(new PageModel[novels.size()]));
		} catch (Exception e) {
			Log.e(TAG, "Error when getting " + language + " list: " + e.getMessage(), e);
			publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_novel_alt_task_error, language, e.getMessage())));
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
		owner.setMessageDialog(new CallbackEventData(LNReaderApplication.getInstance().getApplicationContext().getResources().getString(R.string.load_novel_alt_task_complete, language)));
		owner.onGetResult(result, PageModel[].class);
	}
}