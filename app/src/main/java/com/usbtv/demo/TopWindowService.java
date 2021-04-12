package com.usbtv.demo;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.usbtv.demo.view.MyVideoView;

import butterknife.BindView;

public class TopWindowService extends Service
{
	public static final String OPERATION = "operation";
	public static final int OPERATION_SHOW = 100;
	public static final int OPERATION_HIDE = 101;

	private static final int HANDLE_CHECK_ACTIVITY = 200;

	private boolean isAdded = false;
	private static WindowManager wm;
	private static WindowManager.LayoutParams params;

	private List<String> homeList;
	private ActivityManager mActivityManager;
	MyVideoView videoView;
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		homeList = getHomes();
		createFloatView();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	private void createFloatView()
	{


		wm = (WindowManager) getApplicationContext().getSystemService(
				Context.WINDOW_SERVICE);

		WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

		layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
		layoutParams.type = WindowManager.LayoutParams.LAST_APPLICATION_WINDOW;
		layoutParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		layoutParams.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;



		layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;



		LayoutInflater inflater =LayoutInflater.from(getApplicationContext());//加载需要的XML布局文件

		RelativeLayout mInView = (RelativeLayout)inflater.inflate(R.layout.video, null, false);//......//添加到WindowManager里面
		videoView = mInView.findViewById(R.id.video_view);

		wm.addView(mInView, layoutParams);
		isAdded = true;
	}

	private List<String> getHomes()
	{
		List<String> names = new ArrayList<String>();
		PackageManager packageManager = this.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(
				intent, PackageManager.MATCH_DEFAULT_ONLY);
		for (ResolveInfo ri : resolveInfo)
		{
			names.add(ri.activityInfo.packageName);
		}
		return names;
	}


	public boolean isHome()
	{
		if (mActivityManager == null)
		{
			mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		}
		List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
		return true;
	}

}
