package com.andretietz.rxservice;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.NotImplementedException;

public class CounterService extends Service {

	private IBinder binder = new ICounterService.Stub() {
		@Override
		public long getPID() throws RemoteException {
			return android.os.Process.myPid();
		}

		@Override
		public void registerCallback(ICounterServiceCallback callback) throws RemoteException {
			throw new NotImplementedException("Not implemented yet!");
		}
	};

	/**
	 * This is a pretty good solution for checking if a Service is running or not.
	 * I found this on stackoverflow:
	 * http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-in-android
	 * <p>
	 * I changed it so that it can be called as static
	 *
	 * @param context Context which you need in this method
	 * @return <code>true</code> if the service is running, <code>false</code> if not
	 */
	@SuppressWarnings("unused")
	public static boolean isRunning(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (CounterService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
}
