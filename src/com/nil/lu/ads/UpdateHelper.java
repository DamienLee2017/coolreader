package com.nil.lu.ads;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import net.youmi.android.AdManager;
import net.youmi.android.dev.AppUpdateInfo;

@SuppressLint("NewApi")
public class UpdateHelper extends AsyncTask<Void, Void, AppUpdateInfo>
{
  private AppUpdateInfo mAppUpdateInfo;
  private Context mContext;

  public UpdateHelper(Context paramContext)
  {
    this.mContext = paramContext;
  }

  protected AppUpdateInfo doInBackground(Void[] paramArrayOfVoid)
  {
    try
    {
      AppUpdateInfo localAppUpdateInfo = AdManager.getInstance(this.mContext).checkAppUpdate();
      return localAppUpdateInfo;
    }
    catch (Throwable localThrowable)
    {
      localThrowable.printStackTrace();
    }
    return null;
  }

  protected void onPostExecute(AppUpdateInfo paramAppUpdateInfo)
  {
    super.onPostExecute(paramAppUpdateInfo);
    if (paramAppUpdateInfo != null)
      try
      {
        if (paramAppUpdateInfo.getUrl() == null)
          return;
        this.mAppUpdateInfo = paramAppUpdateInfo;
        new AlertDialog.Builder(this.mContext).setTitle("发现新版本").setMessage(paramAppUpdateInfo.getUpdateTips()).setNegativeButton("马上升级", new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
          {
            Intent localIntent = new Intent("android.intent.action.VIEW", Uri.parse(UpdateHelper.this.mAppUpdateInfo.getUrl()));
            localIntent.addFlags(268435456);
            UpdateHelper.this.mContext.startActivity(localIntent);
          }
        }).setPositiveButton("下次再说", new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
          {
            paramAnonymousDialogInterface.cancel();
          }
        }).create().show();
        return;
      }
      catch (Throwable localThrowable)
      {
        localThrowable.printStackTrace();
      }
  }
}