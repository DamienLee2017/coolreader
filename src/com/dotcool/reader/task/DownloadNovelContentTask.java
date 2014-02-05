package com.dotcool.reader.task;

import java.util.ArrayList;

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

public class DownloadNovelContentTask extends AsyncTask<Void, ICallbackEventData, AsyncTaskResult<NovelContentModel>> implements ICallbackNotifier {
	private static final String TAG = DownloadNovelContentTask.class.toString();
	private final BookModel chapters;
	public volatile IAsyncTaskOwner owner;
	private int currentChapter = 0;
	private final String taskId;

	public DownloadNovelContentTask(BookModel chapters, IAsyncTaskOwner owner) {
		this.chapters = chapters;
		this.owner = owner;
		this.taskId = this.toString();
	}

	@Override
	protected void onPreExecute() {
		// executed on UI thread.
		// owner.toggleProgressBar(true);
		boolean exists = false;
		exists = owner.downloadListSetup(this.taskId, null, 0, false);
		if (exists)
			this.cancel(true);
	}

	public void onCallback(ICallbackEventData message) {
		publishProgress(message);
	}

	@Override
	protected AsyncTaskResult<NovelContentModel> doInBackground(Void... params) {
		Context ctx = owner.getContext();
		ArrayList<Exception> exceptionList = new ArrayList<Exception>();
		NovelContentModel contents = new NovelContentModel();
		try {
			
				/*NovelContentModel oldContent = NovelsDao.getInstance().getNovelContent(chapters, true, null);
				String message = ctx.getResources().getString(R.string.download_novel_content_task_progress, chapters.getTitle());
				if (oldContent != null) {
					message = ctx.getResources().getString(R.string.download_novel_content_task_update, chapters.getTitle());
				}
				Log.i(TAG, message);
				publishProgress(new CallbackEventData(message));
				*/
				try {
					NovelContentModel temp = NovelsDao.getInstance().getNovelContentFromInternet(chapters, this);
					contents = temp;
				} catch (Exception e) {
					Log.e(TAG, String.format("Error when downloading: %s", chapters.getTitle()), e);
					publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.download_novel_content_task_error, e.getMessage())));
					exceptionList.add(e);
				}


			if (exceptionList.size() > 0) {
				return new AsyncTaskResult<NovelContentModel>(exceptionList.get(exceptionList.size() - 1));
			}
			return new AsyncTaskResult<NovelContentModel>(contents);
		} catch (Exception e) {
			Log.e(TAG, String.format("Error when downloading: %s", chapters.getPage(), e));
			//publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.download_novel_content_task_error, chapters.getPage(), e.getMessage())));
			//return new AsyncTaskResult<NovelContentModel>(e);
			return new AsyncTaskResult<NovelContentModel>(contents);
		}
	}

	@Override
	protected void onProgressUpdate(ICallbackEventData... values) {
		// executed on UI thread.
		owner.setMessageDialog(values[0]);
		owner.updateProgress(this.taskId, currentChapter, 5, values[0].getMessage());
	}

	@Override
	protected void onPostExecute(AsyncTaskResult<NovelContentModel> result) {
		owner.setMessageDialog(new CallbackEventData(owner.getContext().getResources().getString(R.string.download_novel_content_task_complete)));
		owner.onGetResult(result, NovelContentModel.class);
		owner.downloadListSetup(this.taskId, null, 2, result.getError() != null ? true : false);
	}
}