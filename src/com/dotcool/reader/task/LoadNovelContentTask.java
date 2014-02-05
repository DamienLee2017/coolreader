package com.dotcool.reader.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dotcool.R;
import com.dotcool.reader.callback.CallbackEventData;
import com.dotcool.reader.callback.ICallbackEventData;
import com.dotcool.reader.callback.ICallbackNotifier;
import com.dotcool.reader.dao.NovelsDao;
import com.dotcool.reader.helper.AsyncTaskResult;
import com.dotcool.reader.model.BookModel;
import com.dotcool.reader.model.NovelContentModel;
import com.dotcool.reader.model.PageModel;

public class LoadNovelContentTask extends AsyncTask<BookModel, ICallbackEventData, AsyncTaskResult<NovelContentModel>> implements ICallbackNotifier {
	private static final String TAG = LoadNovelContentTask.class.toString();
	public volatile IAsyncTaskOwner owner;
	private final boolean refresh;

	public LoadNovelContentTask(boolean isRefresh, IAsyncTaskOwner owner) {
		super();
		this.refresh = isRefresh;
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
	protected AsyncTaskResult<NovelContentModel> doInBackground(BookModel... params) {
		Context ctx = owner.getContext();
		try {
			BookModel p = params[0];
			if (refresh) {
				publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_novel_content_task_refreshing)));
				return new AsyncTaskResult<NovelContentModel>(NovelsDao.getInstance().getNovelContentFromInternet(p, this));
			}
			else {
				publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_novel_content_task_loading)));
				return new AsyncTaskResult<NovelContentModel>(NovelsDao.getInstance().getNovelContent(p, true, this));
			}
		} catch (Exception e) {
			Log.e(TAG, "Error when getting novel content: " + e.getMessage(), e);
			publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_novel_content_task_error, e.getMessage())));
			return new AsyncTaskResult<NovelContentModel>(e);
		}
	}

	@Override
	protected void onProgressUpdate(ICallbackEventData... values) {
		// executed on UI thread.
		owner.setMessageDialog(values[0]);
	}

	@Override
	protected void onPostExecute(AsyncTaskResult<NovelContentModel> result) {
		owner.setMessageDialog(new CallbackEventData(owner.getContext().getResources().getString(R.string.load_novel_content_task_complete)));
		owner.onGetResult(result, NovelContentModel.class);
	}
}