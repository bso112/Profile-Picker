package com.example.firstapp.Activity.ViewPage

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.firstapp.Adapter.CardAdapter
import com.example.firstapp.Default.EXTRA_POSTID
import com.example.firstapp.Activity.PostActivity
import com.example.firstapp.Default.Card
import com.example.firstapp.Helper.UtiliyHelper
import com.example.firstapp.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.frag_swipe.*
import kotlinx.android.synthetic.main.swipe_ad.view.*
import java.util.*
import kotlin.collections.HashSet


class SwipeFragment : Fragment() {

    private var REQUEST_VOTE = 0
    private lateinit var mCardAdapter: CardAdapter


//    private lateinit var mSwipeLeftSetting: SwipeAnimationSetting
//    private lateinit var mSwipeRightSetting: SwipeAnimationSetting
    private lateinit var mSwipeLayoutManager: CardStackLayoutManager

    private var mOldCategory = HashSet<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_swipe, container, false)
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        readyFragementView()

        UtiliyHelper.getInstance().mUserInfo?.categorys?.let { mOldCategory = it }


    }


    override fun onStart() {
        super.onStart()

        //만약 카테고리가 바뀌었으면
        val currCategory = UtiliyHelper.getInstance().mUserInfo?.categorys
        currCategory?.let {
            if (mOldCategory != it) {
                //리프레쉬
                refresh()
                mOldCategory = it
            }
        }
    }

    private fun refresh() {
        mCardAdapter.refresh({ tv_swipe_empty.visibility = View.INVISIBLE }, { tv_swipe_empty.visibility = View.VISIBLE })
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun readyFragementView() {

        readySwipeView()

        val btnAnim = AnimationUtils.loadAnimation(context!!, R.anim.anim_flinch)
        //게시물보기 버튼 눌렀을때
        btn_like.setOnClickListener {btn->

            if (mCardAdapter.isEmpty())
                return@setOnClickListener

            mCardAdapter.getItemAt(0)?.let { card ->
                val intent = Intent(context, PostActivity::class.java).apply {
                    putExtra(EXTRA_POSTID, card.postId)
                    startActivityForResult(this, REQUEST_VOTE)
                }
            }

            btn.startAnimation(btnAnim)
        }



        // 수동으로 swipe하면 버그생김..
//        btn_prv.setOnClickListener {
//            if (mCardAdapter.isEmpty())
//                return@setOnClickListener
//
//            mSwipeLayoutManager.setSwipeAnimationSetting(mSwipeLeftSetting)
//            sv_swipeView.swipe()
//        }
//        btn_next.setOnClickListener {
//            if (mCardAdapter.isEmpty())
//                return@setOnClickListener
//            mSwipeLayoutManager.setSwipeAnimationSetting(mSwipeRightSetting)
//            sv_swipeView.swipe()
//
//        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK)
            return

        if (requestCode == REQUEST_VOTE)
            sv_swipeView.swipe()
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun readySwipeView() {
        if (null == context) {
            Log.d("SwipeFragment", "Context is null")
            return
        }


        val cardListener = object : CardStackListener {
            override fun onCardDisappeared(view: View?, position: Int) {

            }

            override fun onCardDragging(direction: Direction?, ratio: Float) {

            }

            override fun onCardSwiped(direction: Direction?) {
                mCardAdapter.removeCardAtFront()
            }

            override fun onCardCanceled() {

            }

            override fun onCardAppeared(view: View?, position: Int) {

            }

            override fun onCardRewound() {

            }

        }



//        mSwipeLeftSetting = SwipeAnimationSetting.Builder().setDirection(Direction.Left).build()
//        mSwipeRightSetting = SwipeAnimationSetting.Builder().setDirection(Direction.Right).build()


        mSwipeLayoutManager = CardStackLayoutManager(context, cardListener)
        mSwipeLayoutManager.setSwipeThreshold(0.1F)
        sv_swipeView.layoutManager = mSwipeLayoutManager
        mCardAdapter = CardAdapter(context!!, LinkedList())
        sv_swipeView.adapter = mCardAdapter



        mCardAdapter.requestAndAddCardData(
            resources.getInteger(R.integer.CardRequestAtOnce),
            { tv_swipe_empty.visibility = View.INVISIBLE },
            { tv_swipe_empty.visibility = View.VISIBLE })


    }


    override fun onDestroy() {
        mCardAdapter.onDestroy()
        super.onDestroy()
    }

}
