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

public class LoadTeasersTask extends AsyncTask<Void, ICallbackEventData, AsyncTaskResult<PageModel[]>> implements ICallbackNotifier {
	private static final String TAG = LoadTeasersTask.class.toString();
	private boolean refreshOnly = false;
	private boolean alphOrder = false;
	public volatile IAsyncTaskOwner owner;

	public LoadTeasersTask(IAsyncTaskOwner owner, boolean refreshOnly, boolean alphOrder) {
		this.refreshOnly = refreshOnly;
		this.alphOrder = alphOrder;
		this.owner = owner;
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
			if (refreshOnly) {
				publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_teaser_task_refreshing)));
				novels = NovelsDao.getInstance().getTeaserFromInternet(this);
			}
			else {
				publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_teaser_task_loading)));
				novels = NovelsDao.getInstance().getTeaser(this, alphOrder);
			}
			return new AsyncTaskResult<PageModel[]>(novels.toArray(new PageModel[novels.size()]));
		} catch (Exception e) {
			Log.e(TAG, "Error when getting teaser list: " + e.getMessage(), e);
			publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_teaser_task_error, e.getMessage())));
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
		owner.setMessageDialog(new CallbackEventData(owner.getContext().getResources().getString(R.string.load_teaser_task_complete)));
		owner.onGetResult(result, PageModel[].class);
	}
}