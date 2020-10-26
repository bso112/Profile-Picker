package com.example.firstapp.Adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.example.firstapp.Activity.LoginActivity
import com.example.firstapp.Default.CARD_REQUEST_AT_ONECE
import com.example.firstapp.Default.Card
import com.example.firstapp.Default.MyPicture
import com.example.firstapp.Helper.NetworkManager
import com.example.firstapp.Helper.VolleyHelper
import com.example.firstapp.Helper.showSimpleAlert
import com.example.firstapp.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import kotlinx.android.synthetic.main.swipe_ad.view.*
import kotlinx.android.synthetic.main.swipe_item.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * mDataset : UnifiedNativeAd or Card
 */
class CardAdapter(var mContext: Context?, private val mDataset: LinkedList<Card>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        mContext?.let { initializeCardAd(it) }
    }

    class CardViewHolder(
        val layout: View,
        val swipImg: ImageView, val tv_swipe_title: TextView, val tv_swipe_userName: TextView, val tv_swipe_content: TextView,
        val btn_swipe_report: View
    ) : RecyclerView.ViewHolder(layout)

    class AdViewHolder(val view: UnifiedNativeAdView) : RecyclerView.ViewHolder(view) {
        init {
            view.mediaView = view.mv_ad
            // Set other ad assets.
            view.headlineView = view.tv_ad_headline
            view.bodyView = view.tv_ad_body
            view.callToActionView = view.btn_ad_learnMore
            view.iconView = view.iv_ad_icon
            view.priceView = view.tv_ad_price
            view.starRatingView = view.rb_ad_stars
            view.storeView = view.tv_ad_store
            view.advertiserView = view.tv_ad_advertiser
        }
    }

    private val DATA_VIEW_TYPE = 0
    private val NATIVE_AD_VIEW_TYPE = 1


    //카드를 더 받아오는 기준점
    private val CARD_DATA_ABOUT_TO_EMPTY = 3

    //한번에 요청할 광고의 수
    private val REQUEST_AD_AT_ONECE = 5

    //광고를 더 받아오는 기준점
    private val AD_DATA_ABOUT_TO_EMPTY = 2

    //광고를 표시하는 간격
    private val SPACE_BETWEEN_REQUEST_AD = 20

    // DB로부터 받아올 카드 데이터의 시작인덱스
    private var mCardDataIndex: Int = 0

    //네트워크에서 카드데이터를 받아오는 중인가?
    var mIsBusy: Boolean = false
        private set;

    //광고요청 가산기
    private var mSwipeAcc = 0

    //광고로더
    private lateinit var mCardAd: AdLoader

    //광고데이터 큐
    private val mAdQueue: Queue<UnifiedNativeAd> = LinkedList<UnifiedNativeAd>()


    override fun getItemViewType(position: Int): Int {
        return if (mDataset[position].isAd) NATIVE_AD_VIEW_TYPE else DATA_VIEW_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            DATA_VIEW_TYPE -> {
                val layout: View = LayoutInflater.from(parent.context).inflate(R.layout.swipe_item, parent, false)
                CardViewHolder(
                    layout, layout.swipImg, layout.tv_swipe_title, layout.tv_swipe_userName, layout.tv_swipe_content,
                    layout.btn_swipe_report
                )
            }
            NATIVE_AD_VIEW_TYPE -> {
                AdViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.swipe_ad, parent, false) as UnifiedNativeAdView)
            }
            else -> {
                AdViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.swipe_ad, parent, false) as UnifiedNativeAdView)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            DATA_VIEW_TYPE -> {
                val cardViewHolder = holder as CardViewHolder
                setCardData(mDataset[position], cardViewHolder)
            }
            NATIVE_AD_VIEW_TYPE -> {
                val adViewHolder = holder as AdViewHolder
                mAdQueue.peek()?.let { setCardAdData(it, adViewHolder.view) }
            }
        }
    }

    override fun getItemCount(): Int {
        return mDataset.size
    }


    fun isEmpty(): Boolean {
        return mDataset.isEmpty()
    }

    private fun setCardData(card: Card, holder: CardViewHolder) {


        //썸네일을 설정. pictures에는 하나의 사진밖에 없음.
        if (card.pictures.isNotEmpty()) {
            if (card.pictures.first().bitmap == null)
                Toast.makeText(mContext, "비트맵이 널!", Toast.LENGTH_LONG).show()
            else
                holder.swipImg.setImageBitmap(card.pictures.first().bitmap)
        }

        holder.tv_swipe_title.text = card.title
        holder.tv_swipe_userName.text = card.nickname
        holder.tv_swipe_content.text = card.content

        //신고버튼
        holder.btn_swipe_report.isEnabled = true
        val enabledColor = mContext?.resources?.getColor(R.color.Black)
        holder.btn_swipe_report.backgroundTintList = enabledColor?.let { ColorStateList.valueOf(it) }
        holder.btn_swipe_report.setOnClickListener {
            showSimpleAlert(mContext, null, "신고하시겠습니까?", {
                report_post(card.writer, card.postId);
                holder.btn_swipe_report.isEnabled = false
                val disabledColor = mContext?.resources?.getColor(R.color.disable)
                holder.btn_swipe_report.backgroundTintList = disabledColor?.let { ColorStateList.valueOf(it) }

            })
        }


    }

    fun removeCardAtFront() {
        if (mDataset.isNotEmpty()) {
            //만약 광고면 파괴한다.
            if (mDataset.first().isAd)
                mAdQueue.poll()?.destroy()

            mDataset.removeFirst()
            notifyItemRemoved(0)
            mSwipeAcc++
        }


        if (mSwipeAcc >= SPACE_BETWEEN_REQUEST_AD) {
            ShowAd()
            mSwipeAcc = 0
        }

        if (mAdQueue.size <= AD_DATA_ABOUT_TO_EMPTY && !mCardAd.isLoading) {
            loadAds()
        }


        //http 요청중이 아니고, 카드 데이터가 비려고 하면 더 받아온다.
        if (!mIsBusy && mDataset.count() < CARD_DATA_ABOUT_TO_EMPTY)
            onItemAboutToEmpty()
    }

    fun refresh(onSuccess: (() -> Unit)? = null, onFailed: (() -> Unit)? = null) {
        mDataset.clear()
        notifyDataSetChanged()
        onItemAboutToEmpty(onSuccess, onFailed)
    }

    private fun onItemAboutToEmpty(onSuccess: (() -> Unit)? = null, onFailed: (() -> Unit)? = null) {
        requestAndAddCardData(CARD_REQUEST_AT_ONECE, onSuccess, onFailed)
    }

    private fun loadAds() {
        //광고요청하고 올때까지 꽤걸리니까 한번에 요청한다.
        mCardAd.loadAds(AdRequest.Builder().build(), REQUEST_AD_AT_ONECE)
    }


    fun getItemAt(index: Int): Card? {
        if (index >= mDataset.size)
            return null
        else {
            return mDataset[index]
        }
    }

    private fun addCardData(card: Card) {
        mDataset.add(card)
        notifyItemInserted(mDataset.size - 1)
    }


    private fun ShowAd() {
        if (mDataset.size >= 1) {
            mDataset.add(1, Card(isAd = true))
            notifyItemInserted(1)
        }

    }


    fun requestAndAddCardData(postSize: Int, onSuccess: (() -> Unit)? = null, onFailed: (() -> Unit)? = null) {

        mIsBusy = true


        var cardList = ArrayList<Card>()

        if (mContext == null)
            return


        var url = mContext!!.getString(R.string.urlToServer) + "getRandomPost/" + postSize.toString() + "/" + mCardDataIndex.toString() +
                "/" + LoginActivity.mAccount?.email + "/" + NetworkManager.getInstance().mUserInfo?.categorys.toString()
        //랜덤한 유저의 게시글을 얻는다.
        //현재 로그인된 유저정보를 바탕으로
        val postInfoRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            Response.Listener { res ->
                res?.let { jsonArr ->
                    if (jsonArr.length() <= 0) {
                        if (onFailed != null) {
                            onFailed()
                            return@Listener
                        }
                    }
                    if (onSuccess != null) {
                        onSuccess()
                    }

                    for (i in 0 until jsonArr.length()) {
                        val obj = jsonArr.getJSONObject(i);
                        val postId = obj.getLong("postId")
                        val title = obj.getString("title")
                        val fileName = obj.getString("file_name")
                        val filePath = obj.getString("path")


                        val content = obj.getString("content")
                        val writer = obj.getString("writer")
                        val nickname = obj.getString("nickname")
                        val picture = MyPicture(null, fileName, filePath, 0)


                        val getPictureUrl = mContext!!.getString(R.string.urlToServer) + "getImage/" +
                                picture.file_name;

                        val imgRequest = ImageRequest(getPictureUrl,
                            { bitmap ->
                                picture.bitmap = bitmap
                                val newCard = Card(postId, title, content, writer, nickname, arrayListOf(picture))
                                ///mDataset은 이전데이터도 함께 가지고있는반면
                                //cardList는 현재 requestAndAddCardData 요청에서 가져온 데이터만 가지고 있음.
                                cardList.add(newCard)

                                //응답을 받은대로 바로바로 보여주자.
                                addCardData(newCard)

                                //마지막 응답이면 (마지막루프라고 마지막응답은 아님)
                                if (cardList.size >= jsonArr.length()) {
                                    mIsBusy = false

                                    //만약에 20개보다 포스트가 적으면 반복해서 최대한 20개 채운다.
                                    if (cardList.count() < postSize) {

                                        //카드리스트를 mDataSet에 복사. 최대 20개까지 채움.
                                        for (i in 0 until ((postSize / cardList.size) - 1).coerceAtLeast(0))
                                            mDataset.addAll(cardList)

                                        mCardDataIndex = 0

                                        notifyDataSetChanged()

                                    }
                                }
                            }, 300, 800, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888,
                            { err ->
                                Log.e("volley", err.message ?: "err ocurr!")
                            })

                        VolleyHelper.getInstance(mContext!!).addRequestQueue(imgRequest)

                    }
                }

            },
            Response.ErrorListener {
                it.message?.let { it1 -> Log.d("volley", it1) }
            })
        VolleyHelper.getInstance(mContext!!).addRequestQueue(postInfoRequest)
    }


    private fun initializeCardAd(context: Context) {

        //비디오는 일단 음소거
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()

        mCardAd = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
            .forUnifiedNativeAd { ad: UnifiedNativeAd ->

                mAdQueue.add(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(errorCode: Int) {
                    // Handle the failure by logging, altering the UI, and so on.
                    Log.d("swipe", "광고 로드 실패")
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


    private fun setCardAdData(nativeAd: UnifiedNativeAd, adView: UnifiedNativeAdView) {
        // You must call destroy on old ads when you are done with them,
        // otherwise you will have a memory leak.


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

    private fun report_post(email: String, postId: Long) {
        val url = mContext?.getString(R.string.urlToServer) + "report_post/${email}/${postId}"
        val request = StringRequest(Request.Method.GET, url, {
            Toast.makeText(mContext, "신고가 완료되었습니다.", Toast.LENGTH_SHORT).show()
        }, null)

        mContext?.let { VolleyHelper.getInstance(it).addRequestQueue(request) }
    }


    fun onDestroy() {
        //메모리릭 방지
        mContext = null
        //불러온 광고 다 폐기
        mAdQueue.forEach { it.destroy() }
    }


}