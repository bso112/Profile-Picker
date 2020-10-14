package com.example.firstapp.Helper

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.example.firstapp.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import kotlinx.android.synthetic.main.swipe_item.view.*

class AdHelper {

    private lateinit var mFrontAd: InterstitialAd

    companion object {

        private var INSTANCE: AdHelper? = null

        fun getInstance(): AdHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE = AdHelper()
                return INSTANCE as AdHelper
            }

    }









    fun initializeFronAd(context: Context) {

        mFrontAd = InterstitialAd(context)
        mFrontAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        mFrontAd.adListener = object : AdListener()
        {
            override fun onAdLoaded() {
                mFrontAd.show()
            }
        }
    }

    fun loadAd() {

                mFrontAd.loadAd(AdRequest.Builder().build())


    }




}