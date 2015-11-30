package com.andretietz.rxservice;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.andretietz.rxservice.databinding.ActivityMainBinding;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

	private ActivityMainBinding binding;
	private ServiceManager manager;
	private Subscription subscription;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

		manager = new ServiceManager(this);

		binding.buttonConnect.setOnClickListener(view -> {
			subscription = manager.receiveCounterFromService(binding.checkboxLeaveAlive.isChecked())
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(pid -> {
						binding.textUpdatable.setText(String.format("PID: %d", pid));
					});
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(subscription != null)
			subscription.unsubscribe();
	}
}
