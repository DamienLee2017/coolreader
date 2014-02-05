package com.nil.lu.ads;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import net.youmi.android.AdManager;
import net.youmi.android.offers.OffersManager;
import net.youmi.android.offers.PointsChangeNotify;
import net.youmi.android.offers.PointsManager;

public class MainAdsActivity extends Activity
  implements PointsChangeNotify, View.OnClickListener
{
  private TextView mTextViewPointsBalance;

  private void initViews()
  {
    findViewById(2131165185).setOnClickListener(this);
    this.mTextViewPointsBalance = ((TextView)findViewById(2131165184));
    this.mTextViewPointsBalance.setText("经验值:" + PointsManager.getInstance(this).queryPoints());
    L.setYoumiJifen(this, PointsManager.getInstance(this).queryPoints());
  }

  public void onClick(View paramView)
  {
    switch (paramView.getId())
    {
    default:
      return;
    case 2131165185:
    }
    OffersManager.getInstance(this).showOffersWall();
  }

  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(2130903040);
    initViews();
    AdManager.getInstance(this).init("0b72edb291dc1783", "96198b6864575a0d", false);
    OffersManager.getInstance(this).onAppLaunch();
    PointsManager.getInstance(this).registerNotify(this);
    new UpdateHelper(this).execute(new Void[0]);
  }

  protected void onDestroy()
  {
    super.onDestroy();
    OffersManager.getInstance(this).onAppExit();
    PointsManager.getInstance(this).unRegisterNotify(this);
  }

  public void onPointBalanceChange(int paramInt)
  {
    this.mTextViewPointsBalance.setText("经验值:" + paramInt);
    L.setYoumiJifen(this, paramInt);
  }
}