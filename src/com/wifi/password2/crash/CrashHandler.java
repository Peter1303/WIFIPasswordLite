package com.wifi.password2.crash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import com.wifi.password2.activity.CrashActivity;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/** 
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告. 
 *  
 * @author user 
 *  
 */  
public class CrashHandler implements UncaughtExceptionHandler {  

    public static final String TAG = "CrashHandler";  

    //系统默认的UncaughtException处理类   
    private Thread.UncaughtExceptionHandler mDefaultHandler;  
    //CrashHandler实例  
    private static CrashHandler INSTANCE = new CrashHandler();  
    //程序的Context对象  
    private Context mContext;  
    //用来存储设备信息和异常信息  
    private Map<String, String> infos = new HashMap<String, String>();  

    //用于格式化日期,作为日志文件名的一部分  
    //private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");  

    /** 保证只有一个CrashHandler实例 */  
    private CrashHandler() {  
    }  

    /** 获取CrashHandler实例 ,单例模式 */  
    public static CrashHandler getInstance() {  
        return INSTANCE;  
    }

    /** 
     * 初始化 
     *  
     * @param context 
     */  
    public void init(Context context) {  
        mContext = context;
        //获取系统默认的UncaughtException处理器  
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();  
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }  

    /** 
     * 当UncaughtException发生时会转入该函数来处理 
     */  
    @Override  
    public void uncaughtException(Thread thread, Throwable throwable) {  
        if (!handleException(throwable) && mDefaultHandler != null) {  
            //如果用户没有处理则让系统默认的异常处理器来处理  
            mDefaultHandler.uncaughtException(thread, throwable);  
        } else {  
            try {  
                Thread.sleep(3000);  
            } catch (InterruptedException e) {
                Log.e(TAG, "错误: ", e);
            }  
            //退出程序  
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);  
        }  
    }  

    /** 
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 
     *  
     * @param ex 
     * @return true:如果处理了该异常信息;否则返回false. 
     */  
    private boolean handleException(Throwable throwable) {  
        if (throwable == null) {  
            return false;  
        }  
        //用来显示异常信息  
        new Thread() {  
            @Override  
            public void run() {  
                Looper.prepare();  
                //Toast.makeText(mContext, "程序崩溃了，正在处理中 ：(", Toast.LENGTH_LONG).show();
				Intent crash=new Intent(mContext,CrashActivity.class);
				crash.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(crash);
				Looper.loop();
            }  
        }.start();  
        //收集设备参数信息   
        collectDeviceInfo(mContext);  
        //保存日志文件   
        saveCrashInfo(throwable);  
        return true;  
    }  

    /** 
     * 收集设备参数信息 
     * @param ctx 
     */  
    public void collectDeviceInfo(Context ctx) {  
        try {  
            PackageManager pm = ctx.getPackageManager();  
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);  
            if (pi != null) {  
                String versionName = pi.versionName == null ? "null" : pi.versionName;  
                String versionCode = pi.versionCode + "";  
                infos.put("VersionName", versionName);  
                infos.put("VersionCode", versionCode);  
            }  
        } catch (NameNotFoundException e) {  
            Log.e(TAG, "程序在收集手机信息时发生错误", e);  
        }  
        Field[] fields = Build.class.getDeclaredFields();  
        for (Field field : fields) {  
            try {  
                field.setAccessible(true);  
                infos.put(field.getName(), field.get(null).toString());  
                Log.d(TAG, field.getName() + " : " + field.get(null));  
            } catch (Exception e) {  
                Log.e(TAG, "程序在收集错误信息是发生时错误", e);  
            }  
        }  
    }  

    /** 
     * 保存错误信息到文件中 
     *  
     * @param ex 
     * @return  返回文件名称,便于将文件传送到服务器 
     */  
    private String saveCrashInfo(Throwable ex) {  

        StringBuffer sb = new StringBuffer();  
        for (Map.Entry<String, String> entry : infos.entrySet()) {  
            String key = entry.getKey();  
            String value = entry.getValue();  
            sb.append(key + "=" + value + "\n");  
        }  

        Writer writer = new StringWriter();  
        PrintWriter printWriter = new PrintWriter(writer);  
        ex.printStackTrace(printWriter);  
        Throwable cause = ex.getCause();  
        while (cause != null) {  
            cause.printStackTrace(printWriter);  
            cause = cause.getCause();  
        }  
        printWriter.close();  
        String result = writer.toString();  
        sb.append(result);
		SharedPreferences sp=mContext.getSharedPreferences("CRASH",Context.MODE_PRIVATE);
		Editor editor;
		editor=sp.edit();
		editor.putString("CRASHLOG",result);
		editor.commit();
		return null;  
    }  
}  



