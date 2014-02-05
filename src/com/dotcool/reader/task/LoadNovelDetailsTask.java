package com.dotcool.reader.task;

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
import com.dotcool.reader.model.NovelCollectionModel;
import com.dotcool.reader.model.PageModel;

public class LoadNovelDetailsTask extends AsyncTask<PageModel, ICallbackEventData, AsyncTaskResult<NovelCollectionModel>> implements ICallbackNotifier {
	private static final String TAG = LoadNovelDetailsTask.class.toString();
	private boolean refresh = false;
	public volatile IAsyncTaskOwner owner;

	public LoadNovelDetailsTask(boolean refresh, IAsyncTaskOwner owner) {
		super();
		this.owner = owner;
		this.refresh = refresh;
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
	protected AsyncTaskResult<NovelCollectionModel> doInBackground(PageModel... arg0) {
		Context ctx = LNReaderApplication.getInstance().getApplicationContext();
		PageModel page = arg0[0];
		try {
			if (refresh) {
				publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_novel_detail_task_refreshing)));
				NovelCollectionModel novelCol = NovelsDao.getInstance().getNovelDetailsFromInternet(page, this);
				return new AsyncTaskResult<NovelCollectionModel>(novelCol);
			}
			else {
				publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_novel_detail_task_loading)));
				NovelCollectionModel novelCol = NovelsDao.getInstance().getNovelDetails(page, this);
				return new AsyncTaskResult<NovelCollectionModel>(novelCol);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getClass().toString() + ": " + e.getMessage(), e);
			publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_novel_detail_task_error, e.getMessage())));
			return new AsyncTaskResult<NovelCollectionModel>(e);
		}
	}

	@Override
	protected void onProgressUpdate(ICallbackEventData... values) {
		owner.setMessageDialog(values[0]);
	}

	@Override
	protected void onPostExecute(AsyncTaskResult<NovelCollectionModel> result) {
		owner.setMessageDialog(new CallbackEventData(owner.getContext().getResources().getString(R.string.load_novel_detail_task_complete)));
		owner.onGetResult(result, NovelCollectionModel.class);
	}
}