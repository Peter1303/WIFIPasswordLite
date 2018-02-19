package com.wifi.password2.utils;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.util.List;

public class WifiUtils
 {
	private WifiManager localWifiManager;//提供Wifi管理的各种主要API，主要包含wifi的扫描、建立连接、配置信息等
	private List<WifiConfiguration> wifiConfigList;//WIFIConfiguration描述WIFI的链接信息，包括SSID、SSID隐藏、password等的设置
	private WifiInfo wifiConnectedInfo;//已经建立好网络链接的信息
	
	public WifiUtils( Context context){
		localWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	}
    //检查WIFI状态
	public int WifiCheckState(){
		return localWifiManager.getWifiState();
	}
	//开启WIFI
	public void WifiOpen(){
		if(!localWifiManager.isWifiEnabled()){
			localWifiManager.setWifiEnabled(true);
		}
	}
	//关闭WIFI
	public void WifiClose(){
		if(!localWifiManager.isWifiEnabled()){
			localWifiManager.setWifiEnabled(false);
		}
	}
	//得到Wifi配置好的信息
	public void getConfiguration(){
		wifiConfigList = localWifiManager.getConfiguredNetworks();//得到配置好的网络信息
		for(int i =0;i<wifiConfigList.size();i++){
			//LogUtils.i("getConfiguration",String.valueOf(wifiConfigList.get(i).networkId));
		}
	}
	//是否存在
	public boolean IsConfiguration(String SSID){
		wifiConfigList = localWifiManager.getConfiguredNetworks();//得到配置好的网络信息
		for(int i = 0; i < wifiConfigList.size(); i++){
			String LIST_SSID=wifiConfigList.get(i).SSID;
			if(LIST_SSID.startsWith("\"")){
				LIST_SSID=LIST_SSID.substring(1,LIST_SSID.length()-1);
			}
			//LogUtils.i(LIST_SSID);
			if((!LIST_SSID.isEmpty()&&LIST_SSID!=null)&&LIST_SSID.equals(SSID)){//地址相同
				return true;
			}
		}
		return false;
	}
	//添加指定WIFI的配置信息,原列表不存在此SSID
	public int AddWifiConfig(String ssid,String pwd){
		int wifiId = -1;
		WifiConfiguration wifiCong = new WifiConfiguration();
		wifiCong.SSID = "\""+ssid+"\"";
		wifiCong.preSharedKey = "\""+pwd+"\"";//WPA-PSK密码
		wifiCong.hiddenSSID = false;
		wifiCong.status = WifiConfiguration.Status.ENABLED;
		wifiId = localWifiManager.addNetwork(wifiCong);//将配置好的特定WIFI密码信息添加,添加完成后默认是不激活状态，成功返回ID，否则为-1
		if(wifiId != -1){
			return wifiId;
		}
		return wifiId;
	}
	//得到连接的ID
	public int getConnectedID(){
		return (wifiConnectedInfo == null)? 0:wifiConnectedInfo.getNetworkId();
	}
}


