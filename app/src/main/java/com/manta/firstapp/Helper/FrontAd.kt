package com.manta.firstapp.Helper

import android.content.Context
import com.google.android.gms.ads.*

//전면광고 (사용안함)
class FrontAd {

    private lateinit var mFrontAd: InterstitialAd

    companion object {

        private var INSTANCE: FrontAd? = null

        fun getInstance(): FrontAd =
            INSTANCE ?: synchronized(this) {
                INSTANCE = FrontAd()
                return INSTANCE as FrontAd
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