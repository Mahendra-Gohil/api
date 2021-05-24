package com.maxfour.music.ads.Events;

public interface AdEventListener {

    void onAdLoaded();

    void onAdClosed();

    void onLoadError(String errorCode);
}
