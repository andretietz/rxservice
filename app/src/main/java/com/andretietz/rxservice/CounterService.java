package com.andretietz.rxservice;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

public class CounterService extends Service {

	private ICounterServiceCallback serviceCallback;

	private IBinder binder = new ICounterService.Stub() {
		@Override
		public long getPID() throws RemoteException {
			return android.os.Process.myPid();
		}

		@Override
		public void registerCallback(ICounterServiceCallback callback) throws RemoteException {
			serviceCallback = callback;
		}

		@Override
		public void unregisterCallback() throws RemoteException {
			serviceCallback = null;
		}
	};
	private Subscription counterSubscription;

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


	@Override
	public void onCreate() {
		super.onCreate();
		counterSubscription = Observable.interval(1, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.io())
				.subscribe(count -> {
					Log.d("Service", String.format("count: %d", count));
					if (serviceCallback != null) {
						try {
							serviceCallback.onCounterEvent(count);
						} catch (DeadObjectException e) {
							serviceCallback = null;
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY_COMPATIBILITY;
	}

	@Override
	public void onDestroy() {
		Log.e("Service", "onDestroy");
		if (null != counterSubscription) {
			counterSubscription.unsubscribe();
			counterSubscription = null;
		}
	}


}
