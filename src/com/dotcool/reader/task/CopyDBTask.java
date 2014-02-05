package com.dotcool.reader.task;

import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dotcool.reader.LNReaderApplication;
import com.dotcool.R;
import com.dotcool.reader.callback.CallbackEventData;
import com.dotcool.reader.callback.ICallbackEventData;
import com.dotcool.reader.callback.ICallbackNotifier;
import com.dotcool.reader.dao.NovelsDao;

public class CopyDBTask extends AsyncTask<Void, ICallbackEventData, Void> implements ICallbackNotifier {

	private static final String TAG = CopyDBTask.class.toString();
	private final ICallbackNotifier callback;
	private final String source;
	private final boolean makeBackup;

	public CopyDBTask(boolean makeBackup, ICallbackNotifier callback, String source) {
		this.makeBackup = makeBackup;
		this.source = source;
		this.callback = callback;
	}

	public void onCallback(ICallbackEventData message) {
		publishProgress(message);
	}

	@Override
	protected Void doInBackground(Void... params) {
		Context ctx = LNReaderApplication.getInstance().getApplicationContext();
		try {
			copyDB(makeBackup);
		} catch (IOException e) {
			String message;
			if (makeBackup) {
				message = ctx.getResources().getString(R.string.copy_db_task_backup_error);
			}
			else {
				message = ctx.getResources().getString(R.string.copy_db_task_restore_error);
			}
			publishProgress(new CallbackEventData(message));
			Log.e(TAG, message, e);
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(ICallbackEventData... values) {
		Log.d(TAG, values[0].getMessage());
		if (callback != null)
			callback.onCallback(new CallbackEventData(values[0].getMessage(), source));
	}

	private void copyDB(boolean makeBackup) throws IOException {
		Context ctx = LNReaderApplication.getInstance().getApplicationContext();
		if (makeBackup)
			publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.copy_db_task_backup_start)));
		else
			publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.copy_db_task_restore_start)));

		String filePath = NovelsDao.getInstance().copyDB(makeBackup);
		if (filePath == "null") {
			publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.database_not_found), source));
		} else {
			if (makeBackup)
				publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.copy_db_task_backup_complete, filePath)));
			else
				publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.copy_db_task_restore_complete)));
		}
	}
}
