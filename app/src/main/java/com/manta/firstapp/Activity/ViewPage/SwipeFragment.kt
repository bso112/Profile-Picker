package com.manta.firstapp.Activity.ViewPage

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.manta.firstapp.Adapter.CardAdapter
import com.manta.firstapp.Default.EXTRA_POSTID
import com.manta.firstapp.Activity.PostActivity
import com.manta.firstapp.Activity.StatisticActivity
import com.manta.firstapp.Default.CARD_REQUEST_AT_ONECE
import com.manta.firstapp.Default.EXTRA_POSTINFO
import com.manta.firstapp.Helper.NetworkManager
import com.manta.firstapp.R
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.frag_swipe.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashSet


class SwipeFragment : Fragment() {

    enum class SWIPE { LEFT, RIGHT, END }

    private var REQUEST_VOTE = 0
    private lateinit var mCardAdapter: CardAdapter


    private lateinit var mSwipeLeftSetting: SwipeAnimationSetting
    private lateinit var mSwipeRightSetting: SwipeAnimationSetting
    private lateinit var mSwipeLayoutManager: CardStackLayoutManager

    private var mOldCategory = HashSet<Int>()
    private var mOldShowSelfPostOption = false;

    private val swipeCooldown: Long = 300 // 0.1초
    private var swipeTimeStamp: Long = 0

    private var mIsSwipeButtonPressing = false;

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

        NetworkManager.getInstance().mUserInfo?.let {
            mOldCategory = it.categorys
            mOldShowSelfPostOption = it.isShowSelfPost
        }



    }


    override fun onStart() {
        super.onStart()

        //사용자 설정 변경사항 적용
        NetworkManager.getInstance().mUserInfo?.let {
            if(mOldCategory != it.categorys || mOldShowSelfPostOption != it.isShowSelfPost)
            {
                refresh()
                mOldCategory = it.categorys;
                mOldShowSelfPostOption = it.isShowSelfPost;
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
        btn_like.setOnClickListener { btn ->

            if (mCardAdapter.isEmpty())
                return@setOnClickListener

            mCardAdapter.getItemAt(0)?.let { card ->
                if (!card.isAd) {
                    val intent = Intent(context, PostActivity::class.java).apply {
                        putExtra(EXTRA_POSTID, card.postId)
                        startActivityForResult(this, REQUEST_VOTE)
                    }
                }
            }

            btn.startAnimation(btnAnim)
        }


        // 수동으로 swipe하면 버그생김.. 해결중
        btn_prv.setOnClickListener {

            if (mCardAdapter.isEmpty())
                return@setOnClickListener

            CoroutineScope(Default).launch {
                while (mIsSwipeButtonPressing)
                    swipe(SWIPE.LEFT)
            }

        }
        btn_next.setOnClickListener {
            if (mCardAdapter.isEmpty())
                return@setOnClickListener

            CoroutineScope(Default).launch {
                while (mIsSwipeButtonPressing)
                    swipe(SWIPE.RIGHT)
            }
        }


        // mIsSwipeButtonPressing로 버튼 누르고있는 이벤트를 구현
        btn_prv.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mIsSwipeButtonPressing = true; v.performClick(); }
                MotionEvent.ACTION_UP ->
                    mIsSwipeButtonPressing = false;
            }
            //true를 하면 버튼의 디폴트 행동들을 막게된다.
            //모든 이벤트를 comsume하지 않았다고 알림.
            false;
        }

        btn_next.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mIsSwipeButtonPressing = true; v.performClick(); }
                MotionEvent.ACTION_UP ->
                    mIsSwipeButtonPressing = false;
            }
            //true를 하면 버튼의 디폴트 행동들을 막게된다.
            //모든 이벤트를 comsume하지 않았다고 알림.
            false;
        }


    }

    private fun swipe(direction: SWIPE) {
        var animSetting: SwipeAnimationSetting = mSwipeLeftSetting
        if (direction == SWIPE.RIGHT)
            animSetting = mSwipeRightSetting
        if (System.currentTimeMillis() > swipeTimeStamp + swipeCooldown) {
            mSwipeLayoutManager.setSwipeAnimationSetting(animSetting)
            sv_swipeView.swipe()
            swipeTimeStamp = System.currentTimeMillis();
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK)
            return

        if (requestCode == REQUEST_VOTE) {
            sv_swipeView.swipe()

            ///이전게시물 보기버튼 설정
            val btnAnim = AnimationUtils.loadAnimation(context!!, R.anim.anim_show_down)
            btn_show_statistic.setOnClickListener {
                Intent(context, StatisticActivity::class.java).apply {
                    putExtra(EXTRA_POSTINFO, data?.getSerializableExtra(EXTRA_POSTINFO))
                    startActivity(this)
                }
            }
            btn_show_statistic.visibility = View.VISIBLE;
            btn_show_statistic.startAnimation(btnAnim);

            Handler().postDelayed({
                val btnAnim = AnimationUtils.loadAnimation(context!!, R.anim.anim_disappear_up)
                btn_show_statistic.startAnimation(btnAnim)
                btnAnim.setAnimationListener(object : Animation.AnimationListener{
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        btn_show_statistic.visibility= View.INVISIBLE
                    }

                    override fun onAnimationStart(animation: Animation?) {
                    }
                })
            }, 3000);
            ///
        }
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



        mSwipeLeftSetting = SwipeAnimationSetting.Builder().setDirection(Direction.Left).build()
        mSwipeRightSetting = SwipeAnimationSetting.Builder().setDirection(Direction.Right).build()


        mSwipeLayoutManager = CardStackLayoutManager(context, cardListener)
        mSwipeLayoutManager.setSwipeThreshold(0.1F)
        sv_swipeView.layoutManager = mSwipeLayoutManager
        mCardAdapter = CardAdapter(context!!, LinkedList())
        sv_swipeView.adapter = mCardAdapter



        mCardAdapter.requestAndAddCardData(
            CARD_REQUEST_AT_ONECE,
            { tv_swipe_empty.visibility = View.INVISIBLE },
            { tv_swipe_empty.visibility = View.VISIBLE })


    }


    override fun onDestroy() {
        mCardAdapter.onDestroy()
        super.onDestroy()
    }

}
