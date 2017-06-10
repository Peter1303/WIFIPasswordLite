package com.wifi.password2.wifi;

import com.wifi.password2.utils.AppUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern; 
public class WifiManage 
{
	public List<WIFIinfo> Read(String confFile) throws Exception {
		List<WIFIinfo> wifiInfos=new ArrayList<WIFIinfo>(); 
		//String wifiConf=AppUtils.read("cat /data/misc/wifi/*.conf\n");
		String wifiConf=AppUtils.read("cat "+confFile+"\n");
		//寻找开头
		Pattern network = Pattern.compile("network=\\{([^\\}]+)\\}", Pattern.DOTALL); 
		Matcher networkMatcher = network.matcher(wifiConf);
		while (networkMatcher.find()) {
			//寻找账号
			String networkBlock = networkMatcher.group();
			Pattern ssid = Pattern.compile("ssid=(.+?)\t");
			Matcher ssidMatcher = ssid.matcher(networkBlock);
			if(ssidMatcher.find()){
				WIFIinfo wifiInfo=new WIFIinfo(); 
				String temp_name=ssidMatcher.group(1);
				if (temp_name.charAt(0) == '"') {
					int len = temp_name.length();
					temp_name = temp_name.substring(1, len - 1);
					wifiInfo.SSID=temp_name;
				} else{
					String convertString = AppUtils.convertUTF8ToString(temp_name);
					wifiInfo.SSID=convertString;
				}
				//寻找密码
				Pattern psk = Pattern.compile("psk=\"(.+?)\""); 
				Matcher pskMatcher = psk.matcher(networkBlock);
				//寻找加密方式
				//Pattern key_mgmt=Pattern.compile("key_mgmt=([^\"degpqwsmN}]+)");
				Pattern sec=Pattern.compile("key_mgmt=([^\t}]+)");
				Matcher sec_Matcher=sec.matcher(networkBlock);
				//---------------------------------
				//如果找到了密码
				if (pskMatcher.find()){
					wifiInfo.PSK=pskMatcher.group(1);
				}/* else{
				 wifiInfo.Password="无密码";
				 }*/
				//---------------------------------
				//如果找到了加密方式
				if(sec_Matcher.find()){
					wifiInfo.SEC=sec_Matcher.group(1);
				} else{
					wifiInfo.SEC="NONE";
				}
				wifiInfos.add(wifiInfo);
			}
		} 
		return wifiInfos; 
	}

} 
