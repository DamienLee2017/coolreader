package com.dotcool.view;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.dotcool.R;
import com.nil.lu.ads.L;
import com.nil.lu.ads.MainAdsActivity;

public class AboutActivity extends Activity{
   private TextView titleTextV;
   private ImageView backImageV;
   private ListView aboutListV;
   private AboutActivity context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.aboutactivity);
        context=this;
        titleTextV=(TextView) findViewById(R.id.profile_header_title);
        backImageV=(ImageView)findViewById(R.id.profile_header_back);
        aboutListV=(ListView)findViewById(R.id.about_list);
        titleTextV.setText("个人中心");
        titleTextV.setTextSize(18);
        backImageV.setOnClickListener(new OnClickListener(){

            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                context.finish();
            }
            
        });
        SimpleAdapter adapter = new SimpleAdapter(this,getData(),R.layout.about_list_item,
                new String[]{"title"},
                new int[]{R.id.about_list_item_text}){
            @Override    
            public View getView(final int position, View convertView, ViewGroup parent) {  
                View view = super.getView(position, convertView, parent);  
                
                final TextView rl = (TextView)view.findViewById(R.id.about_list_item_text);  
                view.setOnClickListener(new View.OnClickListener() {  
                    public void onClick(View view) { 
                          if(rl.getText()=="积分"){
                              Ad();
                          }else if(rl.getText()=="关于"){
                              Toast.makeText(AboutActivity.this, "感谢使用，点酷听书", 0).show();
                          }else if(rl.getText()=="分享设置"){
                              Toast.makeText(AboutActivity.this, "感谢使用，点酷听书，暂未开放", 0).show();
                          }else if(rl.getText()=="登陆"){
                              Toast.makeText(AboutActivity.this, "感谢使用，点酷听书，暂未开放", 0).show();
                          }
                          
                    }  
                });  
                return view;  
            }  
        };
        aboutListV.setAdapter(adapter);
    }
    private void Ad(){
        if (L.getJifenCountAccess(AboutActivity.this)==false){
            Log.d("dots","false");
            Toast.makeText(AboutActivity.this, "请先获取经验大于0", 0).show();
            Intent localIntent1 = new Intent(AboutActivity.this, MainAdsActivity.class);
            AboutActivity.this.startActivity(localIntent1);
        }else{
            Toast.makeText(AboutActivity.this, "不需获取积分了", 0).show();
        }
       
    }
    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("title", "登陆");
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("title", "积分");
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("title", "分享设置");
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("title", "关于");
        list.add(map);
        return list;
    }
}
