package com.example.firstapp.Adapter

import android.content.Context
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
import com.example.firstapp.Activity.LoginActivity
import com.example.firstapp.Default.Card
import com.example.firstapp.Default.MyPicture
import com.example.firstapp.Helper.VolleyHelper
import com.example.firstapp.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import kotlinx.android.synthetic.main.swipe_item.view.*
import java.util.*
import kotlin.collections.ArrayList

class CardAdapter(var mContext: Context?, private val mDataset: LinkedList<Card>, onAdClick: (() -> Unit)? = null) :
    RecyclerView.Adapter<CardAdapter.ViewHolder>() {

    class ViewHolder(
        val layout: View, val rl_swipeCard: RelativeLayout, val uv_ad: UnifiedNativeAdView,
        val swipImg: ImageView, val tv_swipe_title: TextView, val tv_swipe_userName: TextView, val tv_swipe_content: TextView
    ) : RecyclerView.ViewHolder(layout)


    init {
        mContext?.let { initializeCardAd(it, onAdClick) }
    }

    private lateinit var mCardAd: AdLoader
    private var mCurrentNativeAd: UnifiedNativeAd? = null


    // DB로부터 받아올 카드 데이터의 시작인덱스
    private var mCardDataIndex: Int = 0

    //네트워크에서 카드데이터를 받아오는 중인가?
    var mIsBusy: Boolean = false
        private set;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout: View = LayoutInflater.from(parent.context).inflate(R.layout.swipe_item, parent, false)

        val viewHolder = ViewHolder(layout, layout.rl_swipeCard, layout.uv_ad, layout.swipImg, layout.tv_swipe_title, layout.tv_swipe_userName, layout.tv_swipe_content)
        val adView = viewHolder.uv_ad
        // Set the media view.
        adView.mediaView = adView.mv_ad
        // Set other ad assets.
        adView.headlineView = adView.tv_ad_headline
        adView.bodyView = adView.tv_ad_body
        adView.callToActionView = adView.btn_ad_learnMore
        adView.iconView = adView.iv_ad_icon
        adView.priceView = adView.tv_ad_price
        adView.starRatingView = adView.rb_ad_stars
        adView.storeView = adView.tv_ad_store
        adView.advertiserView = adView.tv_ad_advertiser
        return viewHolder
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (mDataset.size > position) {
            if (mDataset[position].isAd) {
                holder.rl_swipeCard.visibility = View.INVISIBLE
                holder.uv_ad.visibility = View.VISIBLE
                setCardAdData(holder.uv_ad)
            } else {
                holder.rl_swipeCard.visibility = View.VISIBLE
                holder.uv_ad.visibility = View.INVISIBLE
                setCardData(mDataset[position], holder)
            }
        }

    }

    override fun getItemCount(): Int {
        return mDataset.size
    }


    fun isEmpty(): Boolean {
        return mDataset.isEmpty()
    }

    private fun setCardData(card: Card, holder: ViewHolder) {


        //썸네일을 설정. pictures에는 하나의 사진밖에 없음.
        if (card.pictures.isNotEmpty())
            holder.swipImg.setImageBitmap(card.pictures.first().bitmap)

        holder.tv_swipe_title.text = card.title
        holder.tv_swipe_userName.text = card.writer
        holder.tv_swipe_content.text = card.content


    }

    fun removeCardAtFront() {
        if (mDataset.isNotEmpty()) {
            mDataset.removeFirst()
            notifyItemRemoved(0)
        }

        //http 요청중이 아니고, 카드 데이터가 비려고 하면 더 받아온다.
        if (!mIsBusy && mDataset.count() < 3)
            onItemAboutToEmpty()
    }

    private fun onItemAboutToEmpty() {
        requestAndAddCardDatas(R.integer.CardRequestAtOnce)
    }

    fun loadAd() {
        mCardAd.loadAd(AdRequest.Builder().build())
    }

    fun addAdData() {
        //맨앞에 집어넣으면 notifyItemInserted 하는 순간 맨위의 카드가 바뀌어버림.
        //현재 맨위 카드 다음장에 넣자.
        if (mDataset.size >= 1) {
            mDataset.add(1, Card(isAd = true))
            // 리사이클러뷰의 mState(리사이클러뷰의 상태. 다른 객체들이 참조한다) 갱신
            notifyItemInserted(1)
        }
    }

    fun getItemAt(index: Int): Card? {
        if (index >= mDataset.size)
            return null
        else {
            return mDataset[index]
        }
    }

    fun addCardData(card: Card) {
        mDataset.add(card)
        notifyItemInserted(mDataset.size - 1)
    }

    fun requestAndAddCardDatas(postCnt: Int) {

        mIsBusy = true


        var cardList = ArrayList<Card>()

        if (mContext == null)
            return

        var url = mContext!!.getString(R.string.urlToServer) + "getRandomPost/" + postCnt.toString() + "/" + mCardDataIndex.toString() +
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

                    val url = mContext!!.getString(R.string.urlToServer) + "getImage/" +
                            card.pictures.first().file_name;

                    val imgRequest = ImageRequest(url,
                        { bitmap ->
                            card.pictures.first().bitmap = bitmap
                            //완성한 카드를 어레디어댑터에 추가
                            addCardData(card)

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
                    VolleyHelper.getInstance(mContext!!).addRequestQueue(imgRequest)

                }
            },
            Response.ErrorListener {
                Log.e("Volley", it.toString())
            })
        VolleyHelper.getInstance(mContext!!).addRequestQueue(postInfoRequest)
    }


    private fun initializeCardAd(context: Context, onAdClick: (() -> Unit)? = null) {

        //비디오는 일단 음소거
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()

        mCardAd = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
            .forUnifiedNativeAd { ad: UnifiedNativeAd ->

                mCurrentNativeAd?.destroy()
                mCurrentNativeAd = ad
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


    private fun setCardAdData(adView: UnifiedNativeAdView) {
        // You must call destroy on old ads when you are done with them,
        // otherwise you will have a memory leak.

        val nativeAd: UnifiedNativeAd = mCurrentNativeAd ?: return

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
        mContext = null
        mCurrentNativeAd?.destroy()
    }


}