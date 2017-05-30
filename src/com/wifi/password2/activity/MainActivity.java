package com.wifi.password2.activity;

import android.app.Activity;
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
import peter1303.material.MaterialDesignDialog.MaterialDialog;

public class MainActivity extends Activity implements OnClickListener
{
	//public static String UPDATE="https://raw.githubusercontent.com/Peter1303/WIFIPasswordLite/master/update/update.txt";
	//public static String UPDATE="https://coding.net/u/DevelopTeam/p/WIFIPD.io/git/raw/master/update.txt";
	public static String THANKS="https://coding.net/u/DevelopTeam/p/WIFIPD.io/git/raw/master/List.txt";
	public static String FEEDBACK="https://github.com/Peter1303/WIFIPasswordLite/issues";
	public static String UPDATELINK="https://www.pgyer.com/WIFIPasswordLite";
	public static String GITHUB="https://github.com/Peter1303/WIFIPasswordLite";
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
	//数值
	
	//复制String的传递
	private String WIFI_Account;//账号
	private String WIFI_Password;//密码
	private String WifiSsid_Psg;//账号加密码
	
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
		
		//检测权限
		if (!ROOTShellUtils.checkRootPermission()) {
			final MaterialDialog dialog=new MaterialDialog(MainActivity.this);
			dialog.setDialogBGColorResource(getResources().getColor(R.color.colorPrimary));
			dialog.setTitle("提示");
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
				dialog.setDialogBGColorResource(getResources().getColor(R.color.colorPrimary));
                dialog.setTitle("菜单");
                final List<String> list=new ArrayList<>();
                list.add("完整版下载");
				list.add("更新日志");
				list.add("软件反馈");
				list.add("分享软件");
				list.add("关于我们");
                dialog.setItems(list, new OnItemClickListener(){

                        @Override
                        public void onItemClick(AdapterView<?> p1, View p2, int i, long p4)
                        {
							if(i==0){
								Uri uri = Uri.parse("https://www.pgyer.com/WIFIPassword");
								Intent intent=new Intent(Intent.ACTION_VIEW,uri);
								startActivity(intent);
							}
							if(i==1){
								dialogShow("更新日志",getResources().getString(R.string.update_report),"确定","");
							}
							if(i==2){
								webIntent(FEEDBACK);
							}
							if(i==3){
								share();
							}
							if(i==4){
								String s=AppUtils.Version(MainActivity.this)+"\n\n";
								if(!thanks_list.isEmpty()){
									s+="感谢你们：\n"+thanks_list+"\n\n";
								}
								s+=getResources().getString(R.string.about);
								final MaterialDialog dialog=new MaterialDialog(MainActivity.this);
								dialog.setDialogBGColorResource(getResources().getColor(R.color.colorPrimary));
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
                    }).setPositiveButton("取消", new OnClickListener(){

						@Override
						public void onClick(View p1)
						{
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
	
	private class WifiAdapter extends BaseAdapter {
		public WifiAdapter() {
		}

		public final class WifiLayout { 
			public TextView wifi_ssid;  
			public TextView wifi_psg;
			public TextView key_mgmt;
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
				convertView = LayoutInflater.from(con).inflate(R.layout.wifi_list_item, null);
				wifi.wifi_ssid = (TextView)convertView.findViewById(R.id.WIFISSID);  
				wifi.wifi_psg = (TextView)convertView.findViewById(R.id.WIFIPSG);
				wifi.key_mgmt = (TextView)convertView.findViewById(R.id.WIFIMGMT);
				wifi.layout=(LinearLayout)convertView.findViewById(R.id.WIFILAYOUT);
				convertView.setTag(wifi);  
			} else{  
				wifi = (WifiLayout)convertView.getTag();  
			}
			
			//得到字符串
			//放到ListView
			String WifiSsid=wifiinfos.get(position).Ssid;
			String WIFIPSG=wifiinfos.get(position).Password;
			String Security=wifiinfos.get(position).Security;

			//绑定数据
			wifi.wifi_ssid.setText(WifiSsid);  
			wifi.wifi_psg.setText(WIFIPSG);
			wifi.key_mgmt.setText(Security);

			//复制事件
			wifi.layout.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						//得到字符串
						final String account = "账号：";
						final String password = "密码：";
						//得到位置
						final String WifiSsid=account + wifiinfos.get(position).Ssid;
						final String WifiPsg=password + wifiinfos.get(position).Password;
						//复制模式
						sp = getApplication().getSharedPreferences("WIFI", Context.MODE_PRIVATE);
						int mCopy=sp.getInt("Copy", 2);
						if (mCopy == 1) {
							WIFI_Account = wifiinfos.get(position).Ssid.toString();
							WIFI_Password = wifiinfos.get(position).Password.toString();
							WifiSsid_Psg = WIFI_Account + "\n" + WIFI_Password.toString();
						}
						if (mCopy == 2) {
							WIFI_Account = WifiSsid.toString();
							WIFI_Password = WifiPsg.toString();
							WifiSsid_Psg = WIFI_Account + "\n" + WIFI_Password.toString();
						}
						
						final MaterialDialog dialog=new MaterialDialog(MainActivity.this);
						dialog.setDialogBGColorResource(getResources().getColor(R.color.colorPrimary));
						dialog.setTitle("操作菜单");
						final List<String> list=new ArrayList<>();
						list.add("复制WIFI名称");
						if(!wifiinfos.get(position).Password.isEmpty()){
							list.add("复制WIFI密码");
							list.add("复制WIFI名称和密码");
						}
						dialog.setItems(list, new OnItemClickListener(){

								@Override
								public void onItemClick(AdapterView<?> p1, View p2, int i, long p4)
								{
									if(i==0){
										Clickboard(WIFI_Account);
										snackbar("已复制名称 :)");
									}
									if(i==1){
										if(wifiinfos.get(position).Password.isEmpty()){
											snackbar("该WIFI没有密码 :)");
										} else{
											Clickboard(WIFI_Password);
											snackbar("已复制密码 :)");
										}
									}
									if(i==2){
										if(wifiinfos.get(position).Password.isEmpty()){
											Clickboard(WIFI_Account);
											snackbar("由于没有密码，所以只复制了名称哦 :)");
										} else{
											Clickboard(WifiSsid_Psg);
											snackbar("已复制名称和密码 :)");
										}
									}
									dialog.dismiss();
								}
							}).setPositiveButton("取消", new OnClickListener(){

								@Override
								public void onClick(View p1)
								{
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
				snackbar("发生错误："+e.toString());
			}
			return htmlContent;
		}

		@Override
        protected void onPostExecute(String result)
        {
        }
	}
	
	private void Clickboard(String copy){
		ClipboardManager clickboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE); 
		clickboard.setText(copy);
	}
	
	private void dialogShow(String title,String message,String yes,String no){
		final MaterialDialog dialog=new MaterialDialog(MainActivity.this);
		dialog.setTitle(title).setMessage(message);
		dialog.setDialogBGColorResource(getResources().getColor(R.color.colorPrimary));
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
		dialog.setDialogBGColorResource(getResources().getColor(R.color.colorPrimary));
		dialog.setTitle("更新日志").setMessage(R.string.update_report).setPositiveButton("确定", new OnClickListener(){

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
