package com.example.firstapp.Activity.ViewPage

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.firstapp.Adapter.CardAdapter
import com.example.firstapp.Default.EXTRA_POSTID
import com.example.firstapp.Activity.PostActivity
import com.example.firstapp.R
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import kotlinx.android.synthetic.main.frag_swipe.*
import java.util.*


class SwipeFragment : Fragment() {

    var REQUEST_VOTE = 0
    lateinit var mCardAdapter: CardAdapter

    var mAdRequestCnt = 0
    var mAdShowCnt = 0

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
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun readyFragementView() {

        readySwipeView()

        val btnAnim = AnimationUtils.loadAnimation(context!!, R.anim.anim_flinch)
        //게시물보기 버튼 눌렀을때
        btn_like.setOnClickListener {

            if (mCardAdapter.isEmpty())
                return@setOnClickListener

            mCardAdapter.getItemAt(0)?.let {
                val intent = Intent(context, PostActivity::class.java).apply {
                    putExtra(EXTRA_POSTID, it.postId)
                }
                startActivityForResult(intent, REQUEST_VOTE)
            }
            it.startAnimation(btnAnim)
        }



        btn_prv.setOnClickListener {
            if (mCardAdapter.isEmpty())
                return@setOnClickListener
            sv_swipeView.swipe()
        }
        btn_next.setOnClickListener {
            if (mCardAdapter.isEmpty())
                return@setOnClickListener
            sv_swipeView.swipe()
        }


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

        //craete cardAdapter
        //mCardAdapter = CardAdapter(context!!, R.layout.swipe_item) { sv_swipeView.topCardListener.selectLeft()}

        val cardListener = object : CardStackListener {
            override fun onCardDisappeared(view: View?, position: Int) {

            }

            override fun onCardDragging(direction: Direction?, ratio: Float) {

            }

            override fun onCardSwiped(direction: Direction?) {
                mCardAdapter.removeCardAtFront()
                ++mAdShowCnt
                ++mAdRequestCnt
                if (mAdRequestCnt >= 5) {
                    mCardAdapter.loadAd()
                    mAdRequestCnt = 0

                    if (mAdShowCnt >= 10) {
                        mCardAdapter.addAdData()
                        mAdShowCnt = 0
                    }
                }
            }

            override fun onCardCanceled() {

            }

            override fun onCardAppeared(view: View?, position: Int) {

            }

            override fun onCardRewound() {

            }

        }



        sv_swipeView.layoutManager = CardStackLayoutManager(context, cardListener)
        mCardAdapter = CardAdapter(context!!, LinkedList()) { }
        sv_swipeView.adapter = mCardAdapter

        mCardAdapter.requestAndAddCardDatas(resources.getInteger(R.integer.CardRequestAtOnce))


    }

    override fun onDestroy() {
        mCardAdapter.onDestroy()
        super.onDestroy()
    }

}
