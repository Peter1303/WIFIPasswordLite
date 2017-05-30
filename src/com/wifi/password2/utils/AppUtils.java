package com.wifi.password2.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class AppUtils
{
	public static String LOAD_WIFI_LIST="/data/misc/wifi/*.conf";//加载密码
	
	public static File getAppStorageDir(Context context) {
		File file =context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
		if(!file.mkdirs()) {
		}
		return file;
	}
	
	Activity activity;
	public AppUtils(Activity activity){
		this.activity=activity;
	}
	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = activity.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}
	
	public static boolean isWifiConnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiNetworkInfo.isConnected()){
            return true;
        }
        return false ;
    }
	
	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	public static String Version(Context c){
		try
		{
			PackageManager manager = c.getPackageManager();
			PackageInfo info = manager.getPackageInfo(c.getPackageName(), 0);
			String mVersion = info.versionName;
			return "版本:" + mVersion;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static int getAppVersionCode(Context context) {
        int version = -1;
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
		}
        return version;
    }
	
	//使用shell命令读取
	public static String read(String command) throws Exception {
		Process process = null;
		DataOutputStream dataOutputStream = null;
		DataInputStream dataInputStream = null;
		StringBuffer conf = new StringBuffer(); 
		try { 
			process = Runtime.getRuntime().exec("su");
			dataOutputStream = new DataOutputStream(process.getOutputStream()); 
			dataInputStream = new DataInputStream(process.getInputStream());
			dataOutputStream.writeBytes(command); 
			dataOutputStream.writeBytes("exit\n");
			dataOutputStream.flush(); 
			InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream, "utf-8"); 
			BufferedReader bufferedReader = new BufferedReader( inputStreamReader); 
			String line = null; 
			while ((line = bufferedReader.readLine()) != null) {
				conf.append(line);
			}
			bufferedReader.close();
			inputStreamReader.close();
			process.waitFor();
		} catch (Exception e){
			throw e;
		} finally {
			try {
				if (dataOutputStream != null) {
					dataOutputStream.close();
				}
				if (dataInputStream != null) {
					dataInputStream.close();
				}
				process.destroy();
			} catch (Exception e) {
				throw e;
			}
		}
		return conf.toString();
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
                pos++;
            }
            return new String(buffer, 0, pos, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return string;
    }
	
}
