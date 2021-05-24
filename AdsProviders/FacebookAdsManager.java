// For Facebook Ads PlatForm
//implementation 'com.facebook.android:audience-network-sdk:6.4.0'

package com.maxfour.music.ads.AdsProviders;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.maxfour.music.R;
import com.maxfour.music.ads.Events.AdEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.facebook.ads.CacheFlag.ALL;


public class FacebookAdsManager {

    private static final String TAG = "FacebookAdsManager";
    private static FacebookAdsManager singleton;
    public static InterstitialAd interstitialAd;

    public static String interstitialAdID;
    public static boolean isAddFree = false;

    private ProgressDialog progressDialog;


    public FacebookAdsManager(Context context) {
        setUpProgress(context);
    }


    public static FacebookAdsManager getInstance(Context context) {
        if (singleton == null) {
            singleton = new FacebookAdsManager(context);
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


    public void loadInterstitialAd(final Context context, String interstitialAdID) {
        Log.e(TAG, "loadInterstitialAd: adId:" + interstitialAdID);
        try {

            interstitialAd = new InterstitialAd(context, interstitialAdID);
            interstitialAd.loadAd();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "loadInterstitialAd: " + e.toString());
        }

    }

    public InterstitialAd getInterstitialAd() {
        if (interstitialAd == null) {
            return null;
        }
        return interstitialAd;
    }

    public void loadInterstitialAd(Context context, String interstitialAdID, final AdEventListener adEventListener) {
        Log.e(TAG, "loadInterstitialAd: adId:" + interstitialAdID);
        try {
            interstitialAd = new InterstitialAd(context, interstitialAdID);
            interstitialAd.loadAd(interstitialAd.buildLoadAdConfig()
                    .withAdListener(new InterstitialAdListener() {
                        @Override
                        public void onInterstitialDisplayed(Ad ad) {

                        }

                        @Override
                        public void onInterstitialDismissed(Ad ad) {
                            if (adEventListener != null) {
                                adEventListener.onAdClosed();
                            }
                        }

                        @Override
                        public void onError(Ad ad, AdError adError) {
                            if (adEventListener != null) {
                                adEventListener.onLoadError(adError.getErrorMessage());
                            }
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            if (adEventListener != null) {
                                adEventListener.onAdLoaded();
                            }
                        }

                        @Override
                        public void onAdClicked(Ad ad) {

                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {

                        }
                    })
                    .withCacheFlags(ALL)
                    .build());
        } catch (Exception e) {
            Log.e("Exception----->", "FaceBookInterstialAdsShow: " + e.toString());
            e.printStackTrace();
        }


    }


    public void loadBannerAd(Context context, String bannerAdID, FrameLayout adContainer, final AdEventListener adEventListener) {
        Log.e(TAG, "loadBannerAd: adId:" + bannerAdID);
        try {
            AdView adView = new AdView(context, bannerAdID, AdSize.BANNER_HEIGHT_50);

            AdListener adListener = new AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    if (adEventListener != null) {
                        adEventListener.onLoadError(adError.getErrorMessage());
                    }
                    Log.e(TAG, "onErrorBanner: " + adError.toString());
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    if (adEventListener != null) {
                        adEventListener.onAdLoaded();
                    }

                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            };
            AdView.AdViewLoadConfig loadAdConfig = adView.buildLoadAdConfig()
                    .withAdListener(adListener)
                    .build();
            adView.loadAd(loadAdConfig);
            adContainer.addView(adView);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "LoadBannerAd: " + e.toString());
        }


    }

    public void showInterstitialAd() {
        try {
            interstitialAd.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadNativeAd(final Context context, String AdPlacementId, final FrameLayout frameLayout, final boolean isShowMedia, final AdEventListener adEventListener) {

        Log.e(TAG, "loadNativeAd: adId:" + AdPlacementId);
        try {
            final NativeAd nativeAd = new NativeAd(context, AdPlacementId);

            NativeAdBase.NativeLoadAdConfig nativeAdBase = nativeAd.buildLoadAdConfig()
                    .withAdListener(new NativeAdListener() {
                        @Override
                        public void onMediaDownloaded(Ad ad) {

                        }

                        @Override
                        public void onError(Ad ad, AdError adError) {
                            if (adEventListener != null) {
                                adEventListener.onLoadError(""+adError.getErrorCode()+""+adError.getErrorMessage());
                            }
                            Log.e(TAG, "onErrorNative: " + adError.getErrorMessage());
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            populateNativeAd(context, frameLayout, nativeAd, isShowMedia);
                            if (adEventListener != null) {
                                adEventListener.onAdLoaded();
                            }
                        }

                        @Override
                        public void onAdClicked(Ad ad) {

                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {

                        }
                    })
                    .build();
            nativeAd.loadAd(nativeAdBase);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "LoadNativeAd: " + e.toString());
        }


    }


    private void populateNativeAd(Context context, FrameLayout frameLayout, NativeAd nativeAd, boolean isShowMedia) {

        nativeAd.unregisterView();

        // Add the Ad view into the ad container.
        //nativeAdLayout = findViewById(R.id.native_ad_container);
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
        NativeAdLayout adView = (NativeAdLayout) inflater.inflate(R.layout.ad_facebook, frameLayout, false);

        if (frameLayout != null)
            frameLayout.removeAllViews();

        frameLayout.addView(adView);
        frameLayout.setVisibility(View.VISIBLE);


        // Add the AdOptionsView
        LinearLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(context, nativeAd, adView);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        MediaView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);


        if (isShowMedia) {
            nativeAdMedia.setVisibility(View.VISIBLE);
        } else {
            nativeAdMedia.setVisibility(View.GONE);
        }

        // Set the Text.
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
                adView,
                nativeAdMedia,
                nativeAdIcon,
                clickableViews);
    }
}
