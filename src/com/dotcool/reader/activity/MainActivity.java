package com.dotcool.reader.activity;

import java.util.Iterator;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.dotcool.R;
import com.dotcool.reader.AlternativeLanguageInfo;
import com.dotcool.reader.Constants;
import com.dotcool.reader.UIHelper;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.toString();
	private boolean isInverted;
	private final Context ctx = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UIHelper.setLanguage(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			UIHelper.SetTheme(this, R.layout.activity_main);
		else {
			UIHelper.SetTheme(this, R.layout.activity_main_no_tab);
		}
		UIHelper.SetActionBarDisplayHomeAsUp(this, false);
		isInverted = UIHelper.getColorPreferences(this);
	}




	@Override
	public void onBackPressed() {
		// always exit if pressing back on Main Activity.
		finish();
	}

	public void openNovelList(View view) {
		String ui = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PREF_UI_SELECTION, "0");
		if (ui.equalsIgnoreCase("0")) {
			Intent intent = new Intent(this, DisplayNovelPagerActivity.class);
			intent.putExtra(Constants.EXTRA_CATEGORY_ID, Constants.EXTRA_UPDATE_CATEGORY_ID);
			startActivity(intent);
		} 
	}

	public void openNovelListNoTab(View view) {
		Intent intent = new Intent(this, DisplayLightNovelListActivity.class);
		intent.putExtra(Constants.EXTRA_CATEGORY_ID, Constants.EXTRA_UPDATE_CATEGORY_ID);
		startActivity(intent);
	}

	public void openTeaserList(View view) {
		Intent intent = new Intent(this, DisplayTeaserListActivity.class);
		startActivity(intent);
	}

	public void openOriginalsList(View view) {
	    Intent intent = new Intent(this, DisplayLightNovelListActivity.class);
        intent.putExtra(Constants.EXTRA_CATEGORY_ID, Constants.EXTRA_MOOD_CATEGORY_ID);
        startActivity(intent);
	}

	public void openDownloadsList(View view) {
		Intent intent = new Intent(this, DownloadListActivity.class);
		startActivity(intent);
	}


	public void openBookmarks(View view) {
		Intent bookmarkIntent = new Intent(this, DisplayBookmarkActivity.class);
		startActivity(bookmarkIntent);
	}

	public void openHistoryList(View view) {
        Intent intent = new Intent(this, DisplayLightNovelListActivity.class);
        intent.putExtra(Constants.EXTRA_CATEGORY_ID, Constants.EXTRA_HISTORY_CATEGORY_ID);
        startActivity(intent);
    }
    public void openXuanhuanList(View view) {
            Intent intent = new Intent(this, DisplayLightNovelListActivity.class);
            intent.putExtra(Constants.EXTRA_CATEGORY_ID, Constants.EXTRA_XUANHUAN_CATEGORY_ID);
            startActivity(intent);
        }
    public void openKehuanList(View view) {
            Intent intent = new Intent(this, DisplayLightNovelListActivity.class);
            intent.putExtra(Constants.EXTRA_CATEGORY_ID, Constants.EXTRA_KEHUAN_CATEGORY_ID);
            startActivity(intent);
        }
    public void openQitaList(View view) {
            Intent intent = new Intent(this, DisplayLightNovelListActivity.class);
            intent.putExtra(Constants.EXTRA_CATEGORY_ID, Constants.EXTRA_QITA_CATEGORY_ID);
            startActivity(intent);
        }

	public void openWatchList(View view) {
		Intent intent = new Intent(this, DisplayLightNovelListActivity.class);
		intent.putExtra(Constants.EXTRA_ONLY_WATCHED, true);
		startActivity(intent);
	}



	/* Open An activity to select alternative language */
	public void openAlternativeNovelList(View view) {
		selectAlternativeLanguage();
	}

	/**
	 * Create a dialog for alternative language selection
	 */

	public void selectAlternativeLanguage() {

		/* Counts number of selected Alternative Language */
		int selection = 0;

		/* Checking number of selected languages */
		Iterator<Entry<String, AlternativeLanguageInfo>> it = AlternativeLanguageInfo.getAlternativeLanguageInfo().entrySet().iterator();
		while (it.hasNext()) {
			AlternativeLanguageInfo info = it.next().getValue();
			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(info.getLanguage(), true))
				selection++;
			it.remove();
		}

		if (selection == 0) {
			/* Build an AlertDialog */
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
			/* Title for AlertDialog */
			alertDialogBuilder.setMessage(getResources().getString(R.string.no_selected_language));
			alertDialogBuilder.setCancelable(false);
			alertDialogBuilder.setPositiveButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
			/* Create alert dialog */
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		} else {
			/* Start next Activity */
			Intent intent = new Intent(ctx, DisplayAlternativeNovelPagerActivity.class);
			startActivity(intent);
		}

	}

}
