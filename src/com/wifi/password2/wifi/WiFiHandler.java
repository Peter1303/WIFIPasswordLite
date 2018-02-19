package com.wifi.password2.wifi;

import com.wifi.password2.utils.AppUtils;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern; 

public class WiFiHandler {
	public List<WiFiItem> Read(String confFile) throws Exception {
		List<WiFiItem> wifiInfos = new ArrayList<WiFiItem>(); 
		//String wifiConf=AppUtils.read("cat /data/misc/wifi/*.conf\n");
		String wifiConf = AppUtils.read("cat " + confFile + "\n");
		//寻找开头
		Pattern network = Pattern.compile("network=\\{([^\\}]+)\\}", Pattern.DOTALL); 
		Matcher networkMatcher = network.matcher(wifiConf);
		while (networkMatcher.find()) {
			//寻找账号
			String networkBlock = networkMatcher.group();
			Pattern ssid = Pattern.compile("ssid=(.+?)\t");
			Matcher ssidMatcher = ssid.matcher(networkBlock);
			if (ssidMatcher.find()) {
				WiFiItem info = new WiFiItem(); 
				String temp_name = ssidMatcher.group(1);
				//temp_name = temp_name.replace("\"", "");
				info.S = returnString(temp_name);
				//寻找密码
				Pattern psk = Pattern.compile("psk=\"(.+?)\""); 
				Matcher pskM = psk.matcher(networkBlock);
				//如果找到了密码
				if (pskM.find()) {
					info.P = pskM.group(1);
					wifiInfos.add(info);
				}
			}
		} 
		return wifiInfos; 
	}

	public static String returnString(String string) {
		String newString = "";
		if (string.charAt(0) == '"') {
			int len = string.length();
			newString = string.substring(1, len - 1);
		} else {
			newString = convertUTF8ToString(string);
		}
		return newString;
	}

	//将16进制的UTF-8编码转为对应的汉字，解决中文WiFi名乱码的问题
    public static String convertUTF8ToString(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        try {
            string = string.toUpperCase();

            int total = string.length() / 2;
            int pos = 0;

            byte[] buffer = new byte[total];
            for (int i = 0; i < total; i++) {
                int start = i * 2;
                buffer[i] = (byte) Integer.parseInt(string.substring(start, start + 2), 16);
                pos ++;
            }
            return new String(buffer, 0, pos, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
		return string;
    }
} 
