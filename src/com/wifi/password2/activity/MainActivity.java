package com.wifi.password2.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.wifi.password2.R;
import com.wifi.password2.activity.MainActivity;
import com.wifi.password2.utils.AppUtils;
import com.wifi.password2.utils.ROOTShellUtils;
import com.wifi.password2.utils.WifiUtils;
import com.wifi.password2.wifi.WIFIinfo;
import com.wifi.password2.wifi.WifiManage;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import peter1303.material.MaterialDesignDialog.MaterialDialog;

public class MainActivity extends Activity implements OnClickListener
{
	//public static String UPDATE="https://raw.githubusercontent.com/Peter1303/WIFIPasswordLite/master/update/update.txt";
	//public static String UPDATE="https://coding.net/u/DevelopTeam/p/WIFIPD.io/git/raw/master/update.txt";
	public static String THANKS="https://coding.net/u/DevelopTeam/p/WIFIPD.io/git/raw/master/List.txt";
	public static String FEEDBACK="https://github.com/Peter1303/WIFIPasswordLite/issues";
	public static String UPDATELINK="https://www.pgyer.com/WIFIPasswordLite";
	public static String GITHUB="https://github.com/Peter1303/WIFIPasswordLite";
	
	private String SSID="SSID";
	private String PSK="PSK";
	
	private int BN_COLOR;
	
	private int OK=android.R.string.ok;
	private int CANCEL=android.R.string.cancel;
	//数据储存
	private SharedPreferences sp;
    private Editor editor;
	
	//主布局
	private TextView titleTip;
	private TextView mainEmpty;
	private ListView WIFIList;
	private ImageView menu,refresh;
	private LinearLayout ToolbarLayout;
	
	private LinearLayout snackbar_layout;
	private TextView snackbar_text;
	
	private WifiAdapter adapter;
	//WIFI
	private WifiManage wifiManage;
	//其他
	private Context con=this;
	private List<WIFIinfo> wifiinfos =new ArrayList<WIFIinfo>();
	private WifiUtils localWifiUtils;
	//数值
	
	private String thanks_list="";
	
	private boolean isConnected=false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		
		init();
		actions();
		setUpListView();
		loadWIFIList();
		update_report();
		if(isConnected){
			new UpdateAsyncTask().execute();
		}
		
    }
	
	private void init(){
		mainEmpty=(TextView)findViewById(R.id.mainEmpty);
		WIFIList=(ListView)findViewById(R.id.main_listview);
		ToolbarLayout=(LinearLayout)findViewById(R.id.appbar_LinearLayout);
		titleTip=(TextView)findViewById(R.id.app_title);
		menu=(ImageView)findViewById(R.id.action_menu);
		refresh=(ImageView)findViewById(R.id.action_refresh);
		
		snackbar_layout=(LinearLayout)findViewById(R.id.snackbar_layout);
		snackbar_text=(TextView)findViewById(R.id.snackbar_TextView);
	}
	
	private void actions(){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			//透明状态栏
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			//透明导航栏
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
		
		menu.setOnClickListener(this);
		refresh.setOnClickListener(this);
		isConnected=AppUtils.isWifiConnected(MainActivity.this);
		BN_COLOR=getResources().getColor(R.color.colorPrimary);
		localWifiUtils=new WifiUtils(con);
		//检测权限
		if (!ROOTShellUtils.checkRootPermission()) {
			final MaterialDialog dialog=new MaterialDialog(MainActivity.this);
			dialog.setDialogBGColorResource(BN_COLOR);
			dialog.setMessage("本程序不能在没有ROOT权限的设备上使用 ：(");
			dialog.setPositiveButton("退出", new OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						finish();
						dialog.dismiss();
					}
				})
				.setNegativeButton("重试", new OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						actions();
						dialog.dismiss();
					}
				})
				.setCanceledOnTouchOutside(false).show();
		}
		ToolbarLayout.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					WIFIList.smoothScrollToPosition(0);
				}
			});
	}
	
	@Override
	public void onClick(View v)
	{
		switch(v.getId()){
			case R.id.action_menu:
				final MaterialDialog dialog=new MaterialDialog(MainActivity.this);
				dialog.setDialogBGColorResource(BN_COLOR);
                List<String> list=new ArrayList<>();
                list.add("完整版下载");
				list.add("备份恢复");
				list.add("更新日志");
				list.add("软件反馈");
				list.add("分享软件");
				list.add("关于我们");
                dialog.setItems(list, new OnItemClickListener(){

                        @Override
                        public void onItemClick(AdapterView<?> p1, View p2, int pos, long p4)
                        {
							if(pos==0){
								Uri uri = Uri.parse("https://www.pgyer.com/WIFIPassword");
								Intent intent=new Intent(Intent.ACTION_VIEW,uri);
								startActivity(intent);
							}
							if(pos==1){
								showBKorRECDialog();
							}
							if(pos==2){
								dialogShow("更新日志",getResources().getString(R.string.update_report),"确定","");
							}
							if(pos==3){
								webIntent(FEEDBACK);
							}
							if(pos==4){
								share();
							}
							if(pos==5){
								String s=AppUtils.Version(MainActivity.this)+"\n\n";
								if(!thanks_list.isEmpty()){
									s+="感谢你们：\n"+thanks_list+"\n\n";
								}
								s+=getResources().getString(R.string.about);
								final MaterialDialog dialog=new MaterialDialog(MainActivity.this);
								dialog.setDialogBGColorResource(BN_COLOR);
								dialog.setTitle("关于我们")
								.setMessage(s)
									.setPositiveButton("确定", new OnClickListener(){

										@Override
										public void onClick(View p1)
										{
											dialog.dismiss();
										}
									})
									.setNegativeButton("开源", new OnClickListener(){

										@Override
										public void onClick(View p1)
										{
											webIntent(GITHUB);
										}
									})
								.setCanceledOnTouchOutside(false).show();
							}
                            dialog.dismiss();
                        }
                    }).show();
				
				break;
			case R.id.action_refresh:
				loadWIFIList();
				break;
		}
	}
	
	private void setUpListView(){
		adapter = new WifiAdapter();
		WIFIList.setAdapter(adapter);
	}
	
	private void loadWIFIList(){
		wifiManage = new WifiManage();
		try {
			wifiinfos=wifiManage.Read(AppUtils.LOAD_WIFI_LIST);
			if(wifiinfos.size()==0){
				WIFIList.setVisibility(View.GONE);
				mainEmpty.setVisibility(View.VISIBLE);
			} else{
				WIFIList.setVisibility(View.VISIBLE);
				mainEmpty.setVisibility(View.GONE);
				snackbar("共找到"+wifiinfos.size()+"个结果");
			}
		} catch (Exception e) {
		}
		adapter.notifyDataSetChanged();
	}
	
	private void snackbar(String s){
		showSnackbar(s);
		if(delayRun!=null){
			handler.removeCallbacks(delayRun);
		}
		//延迟800ms
		handler.postDelayed(delayRun, 3000);
	}
	private Handler handler = new Handler();
	private Runnable delayRun = new Runnable() {
		
        @Override
        public void run() {
			if(snackbar_layout.getVisibility()==View.VISIBLE){
				snackbar_layout.setVisibility(View.GONE);
			}
        }
    };
	private void showSnackbar(String s){
		snackbar_text.setText(s);
		snackbar_layout.setVisibility(View.VISIBLE);
	}
	
	private void showErrorSnackbar(String s){
		showSnackbar("发生错误："+s);
	}
	
	private class WifiAdapter extends BaseAdapter {
		public WifiAdapter() {
		}

		public final class WifiLayout { 
			public TextView wifi_ssid;  
			public TextView wifi_psk;
			public TextView wifi_sec;
			public LinearLayout layout;
		}

		@Override
		public int getCount() {
			return wifiinfos.size();
		}

		@Override
		public Object getItem(int position) {
			return wifiinfos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			WifiLayout wifi=null;
			if (convertView == null){
				wifi = new WifiLayout();
				//获得组件，实例化组件
				convertView = LayoutInflater.from(con).inflate(R.layout.wifi_item, null);
				wifi.wifi_ssid = (TextView)convertView.findViewById(R.id.WIFISSID);  
				wifi.wifi_psk = (TextView)convertView.findViewById(R.id.WIFIPSK);
				wifi.wifi_sec = (TextView)convertView.findViewById(R.id.WIFIMGMT);
				wifi.layout=(LinearLayout)convertView.findViewById(R.id.WIFILAYOUT);
				convertView.setTag(wifi);  
			} else{  
				wifi = (WifiLayout)convertView.getTag();  
			}
			
			//得到字符串
			//放到ListView
			String WIFI_SSID=wifiinfos.get(position).SSID;
			String WIFI_PSK=wifiinfos.get(position).PSK;
			String WIFI_SEC=wifiinfos.get(position).SEC;

			//绑定数据
			wifi.wifi_ssid.setText(WIFI_SSID);  
			wifi.wifi_psk.setText(WIFI_PSK);
			wifi.wifi_sec.setText(WIFI_SEC);

			//复制事件
			wifi.layout.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						//得到位置
						String s="名称：",p="密码：";
						final String Wifi_SSID=s + wifiinfos.get(position).SSID;
						final String WIFI_PSK=p + wifiinfos.get(position).PSK;
						final String WIFI_SP=Wifi_SSID + "\n" + WIFI_PSK;
						final MaterialDialog dialog=new MaterialDialog(MainActivity.this);
						dialog.setDialogBGColorResource(BN_COLOR);
						final List<String> list=new ArrayList<>();
						list.add("复制WIFI名称");
						if(!wifiinfos.get(position).PSK.isEmpty()){
							list.add("复制WIFI密码");
							list.add("复制WIFI名称和密码");
						}
						dialog.setItems(list, new OnItemClickListener(){

								@Override
								public void onItemClick(AdapterView<?> p1, View p2, int i, long p4)
								{
									if(i==0){
										Clickboard(Wifi_SSID);
										snackbar("已复制名称 :)");
									}
									if(i==1){
										Clickboard(WIFI_PSK);
										snackbar("已复制密码 :)");
									}
									if(i==2){
										Clickboard(WIFI_SP);
										snackbar("已复制名称和密码 :)");
									}
									dialog.dismiss();
								}
							}).show();
						}
					});
			return convertView;
		}
	}
	
	private String htmlContent;
	private class UpdateAsyncTask extends AsyncTask<Void,Integer,String>
	{
		@Override
        protected void onPreExecute()
        {
        }

		@Override
		protected String doInBackground(Void[] v)
		{
			htmlContent = null;
			try{
				// HttpGet对象
				HttpGet httpRequest = new HttpGet(THANKS);
				// HttpClient对象 
				HttpClient httpClient = new DefaultHttpClient();
				// 获得HttpResponse对象
				HttpResponse httpResponse = httpClient.execute(httpRequest);
				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// 取得返回的数据
					thanks_list = EntityUtils.toString(httpResponse.getEntity());
				}
			} catch (Exception e) {
				showErrorSnackbar(e.toString());
			}
			return htmlContent;
		}

		@Override
        protected void onPostExecute(String result)
        {
        }
	}
	//备份恢复等相关
	private void showBKorRECDialog(){
		List<String>menu=new ArrayList<String>();
		menu.add("备份WIFI");
		menu.add("恢复WIFI");
		final MaterialDialog a=new MaterialDialog(con);
			a.setItems(menu, new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int pos, long p4)
				{
					if(pos==0){
						if(wifiinfos.size()!=0){
							backupWIFI();
						} else{
							snackbar("一个WIFI都没有呢");
						}
					} else {
						//recovery();
						snackbar("恢复功能暂未完善");
					}
					a.dismiss();
				}
			})
		.show();
	}
	private String jsonResult="";
	private void backupWIFI(){
		JSONObject objectAll = new JSONObject();
		JSONArray jsonarray = new JSONArray();//json数组
		try {
			for(int i=0;i<wifiinfos.size();i++){
				WIFIinfo info=wifiinfos.get(i);
				JSONObject object = new JSONObject();
				object.put(SSID, info.SSID);
				object.put(PSK, info.PSK);
				jsonarray.put(object);
				objectAll.put("WIFI", jsonarray);
			}
        } catch (Exception e){
		}
		jsonResult=objectAll.toString();
		//snackbar(jsonResult);
		final MaterialDialog a=new MaterialDialog(con);
		a.setDialogBGColorResource(BN_COLOR)
		.setMessage("已生成用于备份WIFI的文本，请复制然后自行备份此文本")
			.setPositiveButton("复制", new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					Clickboard(jsonResult);
					snackbar("已复制备份文本");
					a.dismiss();
				}
			})
			.setNegativeButton(CANCEL, new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					a.dismiss();
				}
			})
		.show();
	}
	
	private void recovery(){
		final MaterialDialog a=new MaterialDialog(con);
		a.setDialogBGColorResource(BN_COLOR)
		.setMessage("恢复WIFI需要您复制之前软件生成的 备份文本 内容，是否读取剪切板里的内容？")
			.setPositiveButton(OK, new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					recWIFI();
					a.dismiss();
				}
			})
			.setNegativeButton(CANCEL, new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					a.dismiss();
				}
			})
		.show();
	}
	List<WIFIinfo> dd=new ArrayList<WIFIinfo>();
	private void recWIFI(){
		final String bk_content=getClickboardContent();
		if(!bk_content.startsWith("{")||!bk_content.endsWith("}")){
			snackbar("看来发生了错误，您确定已复制或者格式正确？");
        } else{
			snackbar("正在恢复WIFI...");
			try {
				JSONObject jsonObject=new JSONObject(bk_content);  
				JSONArray jsonArray=jsonObject.getJSONArray("WIFI");  
				int size=jsonArray.length();
				for(int i=0;i<size;i++){
					WIFIinfo info=new WIFIinfo();
					JSONObject jsonobj = jsonArray.getJSONObject(i);
					String ssid=jsonobj.getString(SSID);
					String psk=jsonobj.getString(PSK);
					if(!psk.isEmpty()){
						info.SSID=ssid;
						info.PSK=psk;
						dd.add(info);
					}
				}
				for(int i=0;i<dd.size();i++){
					addWIFI(dd.get(i).SSID,dd.get(i).PSK);
				}
				loadWIFIList();
			} catch (JSONException e) {
				showErrorSnackbar(e.toString());
			}
		}
	}
	private void addWIFI(String ssid,String psk){
		if( (!ssid.isEmpty()&&ssid!=null) && (!psk.isEmpty()&&psk!=null&&psk.length()<=8) ){
			if(!localWifiUtils.IsConfiguration(ssid)){
				localWifiUtils.WifiOpen();
				int netId = localWifiUtils.AddWifiConfig(ssid, psk);
				if(netId != -1){
					localWifiUtils.getConfiguration();
					//snackbar("添加成功："+ssid+"，还剩 "+dd.size()+" 条WIFI");
				} else {
					//网络连接错误或者其他
					snackbar("添加错误："+ssid);
				}
			} else{
				//snackbar("早已保存了："+ssid+"，所以被跳过");
			}
		} else{
			//snackbar("抱歉，存在错误");
		}
	}
	
	private void Clickboard(String copy){
		ClipboardManager clickboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE); 
		clickboard.setText(copy);
	}
	
	private String getClickboardContent(){
		ClipboardManager cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
		ClipData cd2 = cm.getPrimaryClip();
		return cd2.getItemAt(0).getText().toString();
	}
	
	private void dialogShow(String title,String message,String yes,String no){
		final MaterialDialog dialog=new MaterialDialog(MainActivity.this);
		dialog.setTitle(title).setMessage(message);
		dialog.setDialogBGColorResource(BN_COLOR);
		if(!title.isEmpty()){
			dialog.setPositiveButton(yes, new OnClickListener(){
					@Override
					public void onClick(View p1)
					{
						dialog.dismiss();
					}
				});
		}
		if(!no.isEmpty()){
			dialog.setNegativeButton(no, new OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						dialog.dismiss();
					}
				});
		}
		dialog.show();
	}
	
	private void update_report(){
		sp = this.getSharedPreferences("WIFI", Context.MODE_PRIVATE);
		editor = sp.edit();
		int mUpdate=sp.getInt("Version", 0);
		if (mUpdate != AppUtils.getAppVersionCode(this)){
			//如果读取更新的值不是新版本那么弹出更新日志
			ShowUpdate();
			editor.putInt("Version",AppUtils.getAppVersionCode(this));
		}
		editor.commit();
	}
	
	private void ShowUpdate(){
		final MaterialDialog dialog=new MaterialDialog(MainActivity.this);
		dialog.setDialogBGColorResource(BN_COLOR);
		dialog.setTitle("更新日志").setMessage(R.string.update_report).setPositiveButton(OK, new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					sp=getSharedPreferences("WIFILITE",Context.MODE_PRIVATE);
					editor=sp.edit();
					editor.putInt("VERSION",AppUtils.getAppVersionCode(MainActivity.this));
					editor.commit();
					dialog.dismiss();
				}
			}).setCanceledOnTouchOutside(false).show();
	}
	
	private void share(){
		String title = getResources().getString(R.string.share_title);
		String message = getResources().getString(R.string.share_message);
		Intent intent=new Intent(Intent.ACTION_SEND);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_SUBJECT, title);
		intent.putExtra(Intent.EXTRA_TEXT, message);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(Intent.createChooser(intent, getTitle()));
	}
	
	private void webIntent(String url){
		Uri uri = Uri.parse(url);
		Intent intent=new Intent(Intent.ACTION_VIEW,uri);
		startActivity(intent);
	}
    
}
