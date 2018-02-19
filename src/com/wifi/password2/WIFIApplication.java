package com.wifi.password2;

import android.app.*;
import com.wifi.password2.crash.*;

public class WIFIApplication extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		CrashHandler crashHandler = CrashHandler.getInstance();  
        crashHandler.init(getApplicationContext());
		
	}
	
}
