package com.nil.lu.ads;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class L
{
  public static int JIFEN_MAX = 100;
  public static int JIFEN_MAX_END = 200;
  public static int SHOW_TIMES = 0;

  public static int getCount(Context paramContext)
  {
    return paramContext.getSharedPreferences("ads", 0).getInt("count", 0);
  }

  public static int getJifen(Context paramContext)
  {
    return paramContext.getSharedPreferences("ads", 0).getInt("jifen", 0);
  }

  public static boolean getJifenCountAccess(Context paramContext)
  {
    SharedPreferences localSharedPreferences = paramContext.getSharedPreferences("ads", 0);
    int i = localSharedPreferences.getInt("jifen", 0);
    if (localSharedPreferences.getInt("count", 0) <= SHOW_TIMES){
        return false;
    }else  if ((i >= JIFEN_MAX_END) || (i >= JIFEN_MAX)){        
        return false;
    }
    return true;
  }

  public static boolean getShowAds(Context paramContext)
  {
    int i = paramContext.getSharedPreferences("ads", 0).getInt("count", 0);
    int j = SHOW_TIMES;
    boolean bool = false;
    if (i >= j)
      bool = true;
    return bool;
  }

  public static void setPlayCount(Context paramContext, int paramInt)
  {
    SharedPreferences.Editor localEditor = paramContext.getSharedPreferences("ads", 0).edit();
    localEditor.putInt("count", paramInt);
    localEditor.commit();
  }

  public static void setYoumiJifen(Context paramContext, int paramInt)
  {
    SharedPreferences.Editor localEditor = paramContext.getSharedPreferences("ads", 0).edit();
    localEditor.putInt("jifen", paramInt);
    localEditor.commit();
  }
}