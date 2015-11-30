package com.andretietz.rxservice;

interface ICounterServiceCallback {
    oneway void onCounterEvent(long count);
}
