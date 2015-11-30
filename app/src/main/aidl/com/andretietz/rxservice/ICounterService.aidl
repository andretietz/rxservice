package com.andretietz.rxservice;

import com.andretietz.rxservice.ICounterServiceCallback;

interface ICounterService {
    long getPID();
    void registerCallback(ICounterServiceCallback callback);
    void unregisterCallback();
}
