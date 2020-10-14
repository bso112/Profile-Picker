package com.example.firstapp.Adapter

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.firstapp.Activity.LoginActivity
import com.example.firstapp.Default.Card
import com.example.firstapp.Default.MyPicture
import com.example.firstapp.Helper.AdHelper
import com.example.firstapp.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import kotlinx.android.synthetic.main.swipe_item.view.*

class CardAdapter(context: Context, resourceID: Int, onAdClick : (()->Unit)? = null) :
    ArrayAdapter<Card>(context, resourceID) {

    init {
        initializeCardAd(context, onAdClick)
    }

    private lateinit var mCardAd: AdLoader
    private var mCurrentNativeAd: UnifiedNativeAd? = null


    /**
     *     DB로부터 받아올 카드 데이터의 시작인덱스
     */
    private var mCardDataIndex: Int = 0


    /**
     * 네트워크에서 카드데이터를 받아오는 중인가?
     */
    var mIsBusy: Boolean = false
        private set;


    //getView는 view가 필요할때 즉, 화면에 view가 보여야할때 불린다.
    //view마다 불리기 때문에 여러번 불린다.
    //position 은 어떤 포지션이지?
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val card: Card? = getItem(position)
        var view: View

        if (null != convertView)
            view = convertView
        else
            view = LayoutInflater.from(context).inflate(R.layout.swipe_item, parent, false)

        if (card != null) {
            if (card.isAd)
            {
                view.rl_swipeCard.visibility = View.INVISIBLE
                view.uv_ad.visibility = View.VISIBLE
                setCardAdData(view.uv_ad)
            }
            else
            {
                view.rl_swipeCard.visibility = View.VISIBLE
                view.uv_ad.visibility = View.INVISIBLE
                setCardData(card!!, view)
            }
        }


        //만든 카드뷰를 리턴한다.
        return view
    }


    private fun setCardData(card: Card, view: View) {
        //썸네일을 설정. pictures에는 하나의 사진밖에 없음.
        if (card.pictures.isNotEmpty())
            view.swipImg?.setImageBitmap(card.pictures.first().bitmap)

        view.tv_swipe_title.text = card.title
        view.tv_swipe_userName.text = card.writer
        view.tv_swipe_content.text = card.content


    }

    fun removeCardAtFront() {
        if (!super.isEmpty())
            super.remove(super.getItem(0))
    }

    fun loadAd()
    {
        mCardAd.loadAd(AdRequest.Builder().build())
    }

    fun addAdData() {
        super.add(Card(isAd = true))
    }

    fun addCardData(postCnt: Int) {

        mIsBusy = true

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(context!!)


        var cardList = ArrayList<Card>()

        var url = context.getString(R.string.urlToServer) + "getRandomPost/" + postCnt.toString() + "/" + mCardDataIndex.toString() +
                "/" + LoginActivity.mAccount?.email
        //랜덤한 유저의 게시글을 얻는다.
        //현재 로그인된 유저정보를 바탕으로
        val postInfoRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            Response.Listener { res ->
                res?.let { jsonArr ->
                    for (i in 0 until jsonArr.length()) {
                        val obj = jsonArr.getJSONObject(i);
                        val postId = obj.getInt("postId")
                        val title = obj.getString("title")
                        val fileName = obj.getString("file_name")
                        val filePath = obj.getString("path")


                        //만약 게시물목록이 비었거나 전에 추가된 포스트의 id와 이번에 추가할
                        //포스트의 id가 다르다면, 게시물목록에 항목추가
                        if (cardList.isEmpty() || cardList.last().postId != postId) {
                            val content = obj.getString("content")
                            val writer = obj.getString("writer")
                            val picture = MyPicture(null, fileName, filePath, 0)
                            cardList.add(Card(postId, title, content, writer, arrayListOf(picture)))
                        }

                    }
                }


                //카드에 쓸 이미지는 그냥 첫번째 이미지
                //각 카드에 쓸 이미지 받아옴
                for (card in cardList) {
                    if (null == card)
                        return@Listener

                    val url = context.getString(R.string.urlToServer) + "getImage/" +
                            card.pictures.first().file_name;

                    val imgRequest = ImageRequest(url,
                        { bitmap ->
                            card.pictures.first().bitmap = bitmap
                            //완성한 카드를 어레디어댑터에 추가
                            super.add(card)

                            mCardDataIndex++

                            //마지막 루프면
                            if (card === cardList.last()) {
                                mIsBusy = false

                                //만약 받았은 포스트의 수가 요청한 것보다 적으면 마지막 데이터셋이라는 뜻
                                //그때는 mCardDataIndex를 0으로 돌린다.
                                if (cardList.count() < postCnt)
                                    mCardDataIndex = 0

                            }

                        }, 300, 800, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888,
                        { err ->
                            Log.e("volley", err.message ?: "err ocurr!")
                        })

                    queue.add(imgRequest)

                }
            },
            Response.ErrorListener {
                Log.e("Volley", it.toString())
            })

        queue.add(postInfoRequest)
    }


    private fun initializeCardAd(context: Context, onAdClick : (()->Unit)? = null)
    {

        //비디오는 일단 음소거
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()

        mCardAd = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
            .forUnifiedNativeAd { ad: UnifiedNativeAd ->

                mCurrentNativeAd?.destroy()
                mCurrentNativeAd = ad
//                //광고 로드가 완료되었을때
//
//                if(null == mCardAdView)
//                    mCardAdView = LayoutInflater.from(context).inflate(R.layout.swipe_item, null) as UnifiedNativeAdView
//
//                //adView에 광고데이터 붙임
//                populateUnifiedNativeAdView(ad, mCardAdView!!)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(errorCode: Int) {
                    // Handle the failure by logging, altering the UI, and so on.
                    Log.d("swipe", "광고 로드 실패")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    if (onAdClick != null) {
                        onAdClick()
                    }

                }

            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    // Methods in the NativeAdOptions.Builder class can be
                    // used here to specify individual options settings.
                    .setVideoOptions(videoOptions)
                    .build()
            )
            .build()
    }


    private fun setCardAdData(adView : UnifiedNativeAdView) {
        // You must call destroy on old ads when you are done with them,
        // otherwise you will have a memory leak.

        val nativeAd: UnifiedNativeAd = mCurrentNativeAd ?: return

        // Set the media view.
        adView.mediaView = adView.findViewById(R.id.mv_ad)

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.tv_ad_headline)
        adView.bodyView = adView.findViewById(R.id.tv_ad_body)
        adView.callToActionView = adView.findViewById(R.id.btn_ad_learnMore)
        adView.iconView = adView.findViewById(R.id.iv_ad_icon)
        adView.priceView = adView.findViewById(R.id.tv_ad_price)
        adView.starRatingView = adView.findViewById(R.id.rb_ad_stars)
        adView.storeView = adView.findViewById(R.id.tv_ad_store)
        adView.advertiserView = adView.findViewById(R.id.tv_ad_advertiser)

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        adView.mediaView.setMediaContent(nativeAd.mediaContent)

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView.visibility = View.INVISIBLE
        } else {
            adView.bodyView.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView.visibility = View.INVISIBLE
        } else {
            adView.callToActionView.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon.drawable
            )
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView.visibility = View.INVISIBLE
        } else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView.visibility = View.INVISIBLE
        } else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)


    }

    fun onDestroy() {
        //메모리릭 방지
        mCurrentNativeAd?.destroy()
    }




}