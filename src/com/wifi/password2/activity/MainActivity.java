package com.wifi.password2.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.wifi.password2.wifi.WiFiHandler;
import com.wifi.password2.wifi.WiFiItem;
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
import android.app.AlertDialog;
import android.content.DialogInterface;

public class MainActivity extends Activity {
	//主布局
	private TextView mainEmpty;
	private ListView listView;
	//
	private WiFiHandler wifiHandler;
	private WifiAdapter adapter;
	//其他
	private Context con = this;
	private List<WiFiItem> list = new ArrayList<WiFiItem>();
	/*
	 本次更新:
	 -彻底 精简体积
	 -移除 冗余功能
	 -不再 显示开放网络
	 */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		init();
		setUpListView();
		load();
    }

	private void init() {
		mainEmpty = (TextView) findViewById(R.id.activity_main_TextView);
		listView = (ListView) findViewById(R.id.activity_main_ListView);
	}
	
	private void setUpListView() {
		adapter = new WifiAdapter();
		listView.setAdapter(adapter);
	}
	
	private void load() {
		wifiHandler = new WiFiHandler();
		try {
			list = wifiHandler.Read(AppUtils.LOAD_WIFI_LIST);
			if (list.size() == 0) {
				listView.setVisibility(View.GONE);
				mainEmpty.setVisibility(View.VISIBLE);
			} else {
				listView.setVisibility(View.VISIBLE);
				mainEmpty.setVisibility(View.GONE);
				setTitle(getResources().getString(R.string.app_name) + "(" + list.size() + "个)");
			}
		} catch (Exception e) {
		}
		adapter.notifyDataSetChanged();
	}
	
	private class WifiAdapter extends BaseAdapter {
		public WifiAdapter() {
		}

		public final class WifiLayout { 
			public TextView item_s;  
			public TextView item_p;
			public LinearLayout item_l;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			WifiLayout wifi=null;
			if (convertView == null) {
				wifi = new WifiLayout();
				//获得组件，实例化组件
				convertView = LayoutInflater.from(con).inflate(R.layout.wifi_item, null);
				wifi.item_s = (TextView) convertView.findViewById(R.id.item_s);  
				wifi.item_p = (TextView) convertView.findViewById(R.id.item_p);
				wifi.item_l = (LinearLayout) convertView.findViewById(R.id.item_l);
				convertView.setTag(wifi);  
			} else {  
				wifi = (WifiLayout)convertView.getTag();  
			}
			String s = list.get(position).S;
			String p = list.get(position).P;
			wifi.item_s.setText(s);  
			wifi.item_p.setText(p);
			//复制事件
			wifi.item_l.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View p1) {
						//得到位置
						String s = "名称：", p = "密码：";
						final String Wifi_SSID = s + list.get(position).S;
						final String WIFI_PSK = p + list.get(position).P;
						final String WIFI_ALL = Wifi_SSID + "\n" + WIFI_PSK;
						String[] menu = {"复制名称", "复制密码", "复制全部"};
						new AlertDialog.Builder(con)
							.setItems(menu, new DialogInterface.OnClickListener(){

								@Override
								public void onClick(DialogInterface p1, int pos) {
									if (pos == 0) {
										Clickboard(Wifi_SSID);
									}
									if (pos == 1) {
										Clickboard(WIFI_PSK);
									}
									if (pos == 2) {
										Clickboard(WIFI_ALL);
									}
								}
							})
						.show();
					}
				});
			return convertView;
		}
	}
	
	private void Clickboard(String copy) {
		ClipboardManager clickboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE); 
		clickboard.setText(copy);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_refresh:
				load();
				break;
		}
        return super.onOptionsItemSelected(item);
    }
}
