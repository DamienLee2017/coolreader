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
import com.dotcool.reader.model.NovelCollectionModel;
import com.dotcool.reader.model.PageModel;

public class AddNovelTask extends AsyncTask<PageModel, ICallbackEventData, AsyncTaskResult<NovelCollectionModel>> implements ICallbackNotifier {
	public volatile IAsyncTaskOwner owner;

	public AddNovelTask(IAsyncTaskOwner displayLightNovelListActivity) {
		this.owner = displayLightNovelListActivity;
	}

	public void onCallback(ICallbackEventData message) {
		onProgressUpdate(message);
	}

	@Override
	protected AsyncTaskResult<NovelCollectionModel> doInBackground(PageModel... params) {
		Context ctx = owner.getContext();
		PageModel page = params[0];
		try {
			publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.add_novel_task_check, page.getPage())));
			page = NovelsDao.getInstance().getUpdateInfo(page, this);
			if (page.isMissing()) {
				return new AsyncTaskResult<NovelCollectionModel>(new Exception(ctx.getResources().getString(R.string.add_novel_task_missing, page.getPage())));
			}

			NovelCollectionModel novelCol = NovelsDao.getInstance().getNovelDetailsFromInternet(page, this);
			Log.d("AddNovelTask", "Downloaded: " + novelCol.getPage());
			return new AsyncTaskResult<NovelCollectionModel>(novelCol);
		} catch (Exception e) {
			Log.e("AddNovelTask", e.getClass().toString() + ": " + e.getMessage(), e);
			publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.add_novel_task_error, page.getPage(), e.getMessage())));
			return new AsyncTaskResult<NovelCollectionModel>(e);
		}
	}

	@Override
	protected void onProgressUpdate(ICallbackEventData... values) {
		// executed on UI thread.
		owner.setMessageDialog(values[0]);
	}

	@Override
	protected void onPostExecute(AsyncTaskResult<NovelCollectionModel> result) {
		owner.setMessageDialog(new CallbackEventData(owner.getContext().getResources().getString(R.string.add_novel_task_complete)));
		owner.onGetResult(result, NovelCollectionModel.class);
	}
}
