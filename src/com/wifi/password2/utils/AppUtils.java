package com.wifi.password2.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class AppUtils {
	public static String LOAD_WIFI_LIST="/data/misc/wifi/*.conf";//加载密码

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
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader); 
			String line = null; 
			while ((line = bufferedReader.readLine()) != null) {
				conf.append(line);
			}
			bufferedReader.close();
			inputStreamReader.close();
			process.waitFor();
		} catch (Exception e) {
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
}
