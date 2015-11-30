package com.andretietz.rxservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import rx.Observable;
import rx.Subscriber;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.Subscriptions;

public class ServiceManager {

	private static final String TAG = ServiceManager.class.getSimpleName();
	private final Context context;
	private final BehaviorSubject<ICounterService> serviceSubject = BehaviorSubject.create();
	private ServiceConnection serviceConnection;
	private Intent serviceIntent;

	public ServiceManager(Context context) {
		this.context = context;
		this.serviceIntent = new Intent(context, CounterService.class);
	}

	/**
	 * @param stayAlive if {@code true}, the service will stay alive, even when the activity dies
	 */
	public Observable<Long> receiveCounterFromService(boolean stayAlive) {
		return startService(stayAlive)
				.flatMap(started -> connectService())
				.flatMap(this::counterCallback);
	}

	public Observable<Long> counterCallback(ICounterService service) {
		return Observable.create(new Observable.OnSubscribe<Long>() {
			@Override
			public void call(Subscriber<? super Long> subscriber) {
				subscriber.add(Subscriptions.create(() -> {
					try {
						service.unregisterCallback();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}));
				try {
					service.registerCallback(new ICounterServiceCallback.Stub() {
						@Override
						public void onCounterEvent(long count) throws RemoteException {
							subscriber.onNext(count);
						}
					});
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private Observable<ICounterService> connectService() {
		return Observable.create(subscriber -> {
			synchronized (ServiceManager.this) {
				if (serviceConnection == null) {
					subscriber.add(Subscriptions.create(() -> {
						Log.d(TAG, "disconnecting service...");
						context.unbindService(serviceConnection);
						serviceConnection = null;
					}));
					serviceConnection = getServiceConnection();
					Log.d(TAG, "connecting service...");
					context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
				}
			}
			subscriber.onNext(serviceConnection);
			subscriber.onCompleted();
		}).flatMap(connection -> serviceSubject);
	}

	private Observable<Boolean> startService(boolean stayAlive) {
		return Observable.create(subscriber -> {
			if (stayAlive && !CounterService.isRunning(context)) {
				Log.d(TAG, "Starting service to stay alive!");
				context.startService(serviceIntent);
			}
			subscriber.onNext(CounterService.isRunning(context));
			subscriber.onCompleted();
		});
	}

	private ServiceConnection getServiceConnection() {
		return new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "service connected!");
				serviceSubject.onNext(ICounterService.Stub.asInterface(service));
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "service disconnected!");
				serviceSubject.onCompleted();
			}
		};
	}

}
