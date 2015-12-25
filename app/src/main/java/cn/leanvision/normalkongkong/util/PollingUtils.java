package cn.leanvision.normalkongkong.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class PollingUtils
{
	// 开启轮询广播
	public static void startPollingReceiver(Context context, int seconds, Class<?> cls, String action)
	{
		// 获取AlarmManager系统服务
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		// 包装需要执行广播的Intent
		Intent intent = null;
		if(null == cls)
			intent = new Intent();
		else
			intent = new Intent(context, cls);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// 触发广播的起始时间
		long triggerAtTime = SystemClock.elapsedRealtime();

		// 使用AlarmManger的setRepeating方法设置定期执行的时间间隔（seconds秒）和需要执行的广播
		manager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime, seconds * 1000, pendingIntent);
	}

	// 停止轮询广播
	public static void stopPollingReceiver(Context context, Class<?> cls, String action)
	{
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent  = null;
		if(null == cls)
			intent = new Intent();
		else
			intent = new Intent(context, cls);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// 取消正在执行的广播
		manager.cancel(pendingIntent);
	}
}
