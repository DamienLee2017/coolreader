package com.dotcool.util;

/**
 * 处理背景图片的一些工具，如放大 缩小...
 * author lee
 * date 2012/4/25
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class BgUtil
{
	/**
	 * 使背景图片适应手机屏幕分辨率
	 * @param context 上下文环境
	 * @param screenWidth 手机屏幕宽度
	 * @param screenHeight 手机屏幕高度
	 * @param resId 背景图片资源ID
	 * @return 返回一个新的适应手机屏幕分辨率的图片
	 */
	public static Bitmap getSuitableBg(Context context,int screenWidth,int screenHeight,int resId)
	{
		Bitmap bg = BitmapFactory.decodeResource(
				context.getResources(), resId);
		int bgWidth = bg.getWidth();
		int bgHeight = bg.getHeight();
		float scaleWidth = (float)screenWidth/bgWidth;
		float scaleHeight = (float)screenHeight/bgHeight;		
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newBg = Bitmap.createBitmap(bg, 0, 0, bgWidth, bgHeight, matrix, true);
		return newBg;
	}
}
