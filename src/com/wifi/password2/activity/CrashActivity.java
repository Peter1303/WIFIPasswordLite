package com.wifi.password2.activity;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import com.wifi.password2.R;
import peter1303.material.MaterialDesignDialog.MaterialDialog;


public class CrashActivity extends Activity
{
	SharedPreferences spCrash;
	Editor edit;
	
	Button detailed;
	
	String crash=null;
	
	boolean feedbacked=false;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			//透明状态栏
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			//透明导航栏
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
		
		detailed=(Button)findViewById(R.id.crash_detailed);
		detailed.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					final MaterialDialog dialog=new MaterialDialog(CrashActivity.this);
					dialog.setDialogBGColorResource(getResources().getColor(R.color.colorCrash));
					dialog.setTitle("崩溃日志")
					.setMessage(crash)
						.setPositiveButton("确定", new OnClickListener(){

							@Override
							public void onClick(View p1)
							{
								dialog.dismiss();
							}
						})
						.setNegativeButton("邮件发送", new OnClickListener(){

							@Override
							public void onClick(View p1)
							{
								Intent data=new Intent(Intent.ACTION_SENDTO); 
								data.setData(Uri.parse("mailto:peter13034@outlook.com")); 
								data.putExtra(Intent.EXTRA_SUBJECT,"WIFIPDLITE FEEDBACK"); 
								data.putExtra(Intent.EXTRA_TEXT,crash); 
								startActivity(data);
								feedbacked=true;
								dialog.dismiss();
							}
						})
						.setNeutralButton("复制错误日志", new OnClickListener(){

							@Override
							public void onClick(View p1)
							{
								ClipboardManager clickboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE); 
								clickboard.setText(crash);
								dialog.dismiss();
							}
						})
					.setCanceledOnTouchOutside(false).show();
				}
			});
		CatLog();
    }
	
	public void CatLog(){
		spCrash=getSharedPreferences("CRASH",Context.MODE_PRIVATE);
		crash=spCrash.getString("CRASHLOG",null);
		if(crash.length()==0||crash==null){
			crash="无法读取日志或无日志 :（";
		}
	}
	
	@Override
	public void onBackPressed()
	{
		if(feedbacked){
			System.exit(0);
		} else{
			final MaterialDialog dialog=new MaterialDialog(CrashActivity.this);
			dialog.setDialogBGColorResource(getResources().getColor(R.color.colorCrash));
			dialog.setMessage("您还没有反馈是如何崩溃的,如果我们收到将会以最快的速度处理的(PS:顺便还可以调戏下程序猿「手动滑稽」)，您确定还要继续关闭吗？")
				.setPositiveButton("确定", new OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						System.exit(0);
					}
				})
				.setNegativeButton("取消", new OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						dialog.dismiss();
					}
				})
				.setCanceledOnTouchOutside(false).show();
		}
	}


}

