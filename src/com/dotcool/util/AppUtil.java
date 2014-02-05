package com.dotcool.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dotcool.R;

/**
 * 应用本身的一些工具，如关于，退出...
 * @author 002666（lee）
 * date 2012/4/26
 */

public class AppUtil
{
	//feedback info
	private static EditText etUserName,etUserNum,etUserFeedback;
	
	/**
	 * 退出应用
	 * @param activity 
	 */
	public static void appExit(final Activity activity)
	{
		new AlertDialog.Builder(activity).setTitle("提示").setMessage("真的要退出吗？").setPositiveButton("确定", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				activity.finish();
			}
		}).setNegativeButton("取消", null).show();
	}
	 /** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
    public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
  
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }  
	/**
	 * 应用关于
	 * @param activity
	 */
	public static void appAbout(Activity activity)
	{
		String appInfo = "                    点酷听书\n"+
				" 提供本地语音朗读功能！预置个人挑选的古典文学和现代小说！\n"+
				" 作者：点酷科技  \n 地址： 四川成都";
		new AlertDialog.Builder(activity).setTitle("关于").setMessage(appInfo).setPositiveButton("确定",null).show();
	}
	
	/**
	 * 分享应用
	 * @param activity
	 */
	public static void appShare(Activity activity)
	{    

		Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
		intent.setType("text/plain"); // 分享发送的数据类型
		String msg = "点酷听书是款非常棒的阅读软件，推荐给大家。";
		intent.putExtra(Intent.EXTRA_TEXT, msg); // 分享的内容
		activity.startActivity(Intent.createChooser(intent, "选择分享"));// 目标应用选择对话框的标题 

	}
	
	/**
	 * 反馈对应用的意见
	 * @param activity
	 */
	public static void appFeedback(final Activity activity)
	{
		LinearLayout feedbackView = (LinearLayout)activity.getLayoutInflater().inflate(R.layout.app_feedback, null);
		etUserName = (EditText) feedbackView.findViewById(R.id.etUserName);
		etUserNum = (EditText) feedbackView.findViewById(R.id.etUserNum);
		etUserFeedback = (EditText) feedbackView.findViewById(R.id.etUserFeedback);
		
		new AlertDialog.Builder(activity).setTitle("反馈").setView(feedbackView).setPositiveButton("提交", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				String feedbackInfo = "姓名:"+etUserName.getText().toString()+
						"\n手机号码:"+etUserNum.getText().toString()+
						"\n反馈意见:"+etUserFeedback.getText().toString();
				//发送邮件
				Toast.makeText(activity, "已提交 --> \n"+feedbackInfo, Toast.LENGTH_LONG).show();
			}
		}).setNegativeButton("取消", null).show();
		
	}
	/**
	 * 获取网址内容
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String getContent(String url) throws Exception{
	    StringBuilder sb = new StringBuilder();
	    
	    HttpClient client = new DefaultHttpClient();
	    HttpParams httpParams = client.getParams();
	    //设置网络超时参数
	    HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
	    HttpConnectionParams.setSoTimeout(httpParams, 5000);
	    HttpResponse response = client.execute(new HttpGet(url));
	    HttpEntity entity = response.getEntity();
	    if (entity != null) {
	        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"), 8192);
	        
	        String line = null;
	        while ((line = reader.readLine())!= null){
	            sb.append(line + "/n");
	        }
	        reader.close();
	    }
	    return sb.toString();
	}
	public static String saveTxt(String content,String path){

        File file = new File(path);
        file.exists();
        file.mkdirs();
        String p = path+File.separator+"tmp.txt";
        FileOutputStream outputStream = null;
        try {
                        //创建文件，并写入内容
            outputStream = new FileOutputStream(new File(p));
            if(content!=null){
                outputStream.write(content.getBytes("gbk"));
            }
        } catch (FileNotFoundException e) {
           e.printStackTrace();
           return p;
        } catch (UnsupportedEncodingException e) {
           e.printStackTrace();
        } catch (IOException e) {
           e.printStackTrace();
        }finally{
           if(outputStream!=null){
               try {
                   outputStream.flush();
               } catch (IOException e) {
                   e.printStackTrace();
               }
               try {
                   outputStream.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
        }
        return p;
        } 


}
