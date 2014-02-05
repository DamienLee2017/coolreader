package com.dotcool.reader.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dotcool.R;
import com.dotcool.reader.callback.CallbackEventData;
import com.dotcool.reader.callback.DownloadCallbackEventData;
import com.dotcool.reader.callback.ICallbackEventData;
import com.dotcool.reader.callback.ICallbackNotifier;
import com.dotcool.reader.dao.NovelsDao;
import com.dotcool.reader.helper.AsyncTaskResult;
import com.dotcool.reader.model.ImageModel;

public class LoadImageTask extends AsyncTask<String, ICallbackEventData, AsyncTaskResult<ImageModel>> implements ICallbackNotifier {
	private static final String TAG = LoadImageTask.class.toString();
	public volatile IAsyncTaskOwner owner;
	private String url = "";
	private final boolean refresh;
	private final String taskId;

	public LoadImageTask(boolean refresh, IAsyncTaskOwner owner) {
		this.owner = owner;
		this.refresh = refresh;
		this.taskId = this.toString();
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
	protected AsyncTaskResult<ImageModel> doInBackground(String... params) {
		Context ctx = owner.getContext();
		this.url = params[0];
		ImageModel image = new ImageModel();
		image.setName(url);
		try {
			if (refresh) {
				publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_image_task_refreshing)));
				return new AsyncTaskResult<ImageModel>(NovelsDao.getInstance().getImageModelFromInternet(image, this));
			}
			else {
				publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_image_task_loading)));
				return new AsyncTaskResult<ImageModel>(NovelsDao.getInstance().getImageModel(image, this));
			}
		} catch (Exception e) {
			Log.e(TAG, "Error when getting image: " + e.getMessage(), e);
			publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.load_image_task_error, e.getMessage())));
			return new AsyncTaskResult<ImageModel>(e);
		}
	}

	@Override
	protected void onProgressUpdate(ICallbackEventData... values) {
		// executed on UI thread.
		owner.setMessageDialog(values[0]);
		if (values[0].getClass() == DownloadCallbackEventData.class) {
			DownloadCallbackEventData data = (DownloadCallbackEventData) values[0];
			owner.updateProgress(this.taskId, data.getPercentage(), 100, data.getMessage());
		}
		else if (values[0].getClass() == CallbackEventData.class) {
			owner.setMessageDialog(values[0]);
		}
	}

	@Override
	protected void onPostExecute(AsyncTaskResult<ImageModel> result) {
		owner.onGetResult(result, ImageModel.class);
		owner.downloadListSetup(this.taskId, null, 2, result.getError() != null ? true : false);
		owner.setMessageDialog(new CallbackEventData(owner.getContext().getResources().getString(R.string.load_image_task_complete)));
	}
}