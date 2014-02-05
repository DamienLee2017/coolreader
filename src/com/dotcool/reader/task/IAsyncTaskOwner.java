package com.dotcool.reader.task;

import android.content.Context;

import com.dotcool.reader.callback.ICallbackEventData;
import com.dotcool.reader.helper.AsyncTaskResult;

public interface IAsyncTaskOwner {
	void updateProgress(String id, int current, int total, String message);

	boolean downloadListSetup(String id, String toastText, int type, boolean hasError);

	void toggleProgressBar(boolean show);

	void setMessageDialog(ICallbackEventData message);

	void onGetResult(AsyncTaskResult<?> result, Class<?> type);

	Context getContext();
}
