package com.maxfour.music.ads.AdsProviders;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.maxfour.music.R;
import com.maxfour.music.ads.Events.AdEventListener;

public class AdmobAdManager {

    private static final String TAG = "AdmobAdManager";
    private static AdmobAdManager singleton;
    public static InterstitialAd interstitialAd;
    public static AppOpenAd appOpenAd;
    private static boolean isShowingAd = false;
    public static boolean isFailedAd = true;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;

    private ProgressDialog progressDialog;

    public AdmobAdManager(Context context) {
        setUpProgress(context);
    }


    public static AdmobAdManager getInstance(Context context) {
        if (singleton == null) {
            singleton = new AdmobAdManager(context);
        }

        return singleton;
    }

    private void setUpProgress(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Ad Showing...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    public void showProgress() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    public void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void fetchAd(Context context, String appOpenAdID) {
        if (appOpenAd != null) return;

        loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAppOpenAdLoaded(AppOpenAd ad) {
                appOpenAd = ad;
            }

            @Override
            public void onAppOpenAdFailedToLoad(LoadAdError loadAdError) {
                // Handle the error.
                isFailedAd=true;
                Log.e("LOG_TAG", "error in loading:"+loadAdError.toString());
            }

        };
        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(context, appOpenAdID, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
    }

    public void loadAppOpenAd(final Activity context, final String appOpenAdID) {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.

        if(!isShowingAd && appOpenAd!=null) {
            Log.e("LOG_TAG", "Will show ad.");

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            appOpenAd = null;
                            isShowingAd = false;
                            fetchAd(context,appOpenAdID);

                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            isFailedAd=true;
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                        }
                    };

            appOpenAd.show(context, fullScreenContentCallback);
        } else {
            Log.e("LOG_TAG", "Can not show ad.");
            fetchAd(context,appOpenAdID);
        }

    }


    public void loadInterstitialAd(Context context,String adId) {
        Log.e(TAG, "loadInterstitialAd: adId:" + adId);
        try {
            interstitialAd = new InterstitialAd(context);
            interstitialAd.setAdUnitId(adId);
            interstitialAd.loadAd(new AdRequest.Builder().build());
        } catch (Exception e) {
            Log.e(TAG, "loadInterstitialAd: " + e.toString());
            e.printStackTrace();
        }
    }

    public InterstitialAd getInterstitialAd() {
        if (interstitialAd == null) {
            return null;
        }
        return interstitialAd;
    }


    public void loadBanner(Context context,String adId, FrameLayout adContainerView, final AdEventListener adEventListener) {
        Log.e(TAG, "loadBanner: adId:" + adId);
        try {
            AdView adView = new AdView(context);
            adView.setAdSize(AdSize.SMART_BANNER);
            adView.setAdUnitId(adId);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    if (adEventListener != null) {
                        adEventListener.onAdLoaded();
                    }
                }

                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    if (adEventListener != null) {
                        adEventListener.onAdClosed();
                    }
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log.e(TAG, "onAdFailedToLoadBanner: " + loadAdError.getMessage());
                    if (adEventListener != null) {
                        adEventListener.onLoadError(loadAdError.getMessage());
                    }
                }
            });
            adView.loadAd(adRequest);
            adContainerView.addView(adView);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "LoadBanner: " + e.toString());
        }

    }


    public void loadAdaptiveBanner(Context context,String adId, RelativeLayout adContainerView, final AdEventListener adEventListener) {
        Log.e(TAG, "loadAdaptiveBanner: adId:" + adId);

        try {
            // Create an ad request. Check your logcat output for the hashed device ID to
            // get test ads on a physical device. e.g.
            // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
            AdView adView = new AdView(context);
            adView.setAdUnitId(adId);
            adContainerView.removeAllViews();
            adContainerView.addView(adView);

            final AdSize adSize = getAdSize(context, adContainerView);
            adView.setAdSize(adSize);

            AdRequest adRequest =
                    new AdRequest.Builder().build();
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    if (adEventListener != null) {
                        adEventListener.onAdLoaded();
                    }
                }

                @Override
                public void onAdClosed() {
                    super.onAdClosed();

                    if (adEventListener != null) {
                        adEventListener.onAdClosed();
                    }
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    if (adEventListener != null) {
                        adEventListener.onLoadError(loadAdError.getMessage());
                    }
                    Log.e(TAG, "onAdFailedAdaptiveBanner: " + loadAdError.toString());
                }
            });

            // Start loading the ad in the background.
            adView.loadAd(adRequest);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "LoadAdaptiveBanner: " + e.toString());
        }

    }

    public AdSize getAdSize(Context context, RelativeLayout adContainerView) {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);

        //  return AdSize.getCurrentOrientationBannerAdSizeWithWidth(context, adWidth);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }


    public void loadNativeAd(final Context context,final String nativeAdId, final FrameLayout frameLayout, final boolean isShowMedia, final AdEventListener adEventListener) {
        Log.e(TAG, "loadNativeAd: adId:" + nativeAdId);
        try {
            AdLoader.Builder builder = new AdLoader.Builder(context, nativeAdId);

            builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                @Override
                public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {

                    int resource = R.layout.ad_unified;
                    if (isShowMedia) {
                        resource = R.layout.ad_unified_big;
                    } else {
                        resource = R.layout.ad_unified;
                    }
                    LayoutInflater layoutInflater = LayoutInflater.from(context);
                    UnifiedNativeAdView adView = (UnifiedNativeAdView) layoutInflater.inflate(resource, null);

                    //UnifiedNativeAdView adView = (UnifiedNativeAdView) context.getLayoutInflater().inflate(R.layout.ad_unified, null);
                    populateUnifiedNativeAdView(unifiedNativeAd, adView);
                    if (frameLayout != null) {
                        frameLayout.removeAllViews();
                        frameLayout.addView(adView);
                    }

                    if (adEventListener != null) {
                        adEventListener.onAdLoaded();
                    }
                }
            }).withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    if (adEventListener != null) {
                        adEventListener.onLoadError(loadAdError.getMessage());
                    }
                    Log.e(TAG, "onAdFailedToLoadNative:" + loadAdError.getCode());
                }
            });


            VideoOptions videoOptions = new VideoOptions.Builder()
                    .setStartMuted(true)
                    .build();
            NativeAdOptions adOptions = new NativeAdOptions.Builder()
                    .setVideoOptions(videoOptions)
                    .build();
            builder.withNativeAdOptions(adOptions);
            AdLoader adLoader = builder.build();
            adLoader.loadAd(new AdRequest.Builder().build());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "LoadNativeAd: " + e.toString());
        }

    }

    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {

        adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }
        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);
        VideoController vc = nativeAd.getVideoController();
        if (vc.hasVideoContent()) {
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    super.onVideoEnd();
                }
            });
        }
    }

    /*private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {

        adView.setMediaView((MediaView) adView.findViewById(R.id.mediaView));
        adView.setHeadlineView(adView.findViewById(R.id.adTitle));
        adView.setBodyView(adView.findViewById(R.id.adDescription));
        adView.setCallToActionView(adView.findViewById(R.id.callToAction));
        adView.setIconView(adView.findViewById(R.id.adIcon));
        adView.setAdvertiserView(adView.findViewById(R.id.adAdvertiser));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        *//*if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }*//*

     *//*if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }*//*

     *//*if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }*//*

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);
        VideoController vc = nativeAd.getMediaContent().getVideoController();
        if (vc.hasVideoContent()) {
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    super.onVideoEnd();
                }
            });
        }
    }*/

}
