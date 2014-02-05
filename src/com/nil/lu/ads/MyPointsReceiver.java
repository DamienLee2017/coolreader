package com.nil.lu.ads;
import net.youmi.android.offers.EarnPointsOrderList;
import net.youmi.android.offers.PointsReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public  class MyPointsReceiver extends PointsReceiver
{
  protected void onEarnPoints(Context paramContext, EarnPointsOrderList paramEarnPointsOrderList)
  {
    Log.d("test", "接收到赚取积分的订单");
  }

  protected void onViewPoints(Context paramContext)
  {
    Intent localIntent = new Intent(paramContext, MainAdsActivity.class);
    localIntent.addFlags(268435456);
    paramContext.startActivity(localIntent);
  }
}