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
import com.manta.firstapp.Helper.UserInfoManager
import com.manta.firstapp.R
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.frag_swipe.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashSet

/**
 * by 변성욱
 * 게시물을 스와이프하는 프래그먼트.
 * CardStackView 라이브러리를 써서 구현했다.
 * 하트버튼을 클릭하면, PostActivity로 전환한다.
 */
class SwipeFragment : Fragment() {

    enum class SWIPE { LEFT, RIGHT, END }
    
    //투표 리퀘스트코드
    private var REQUEST_VOTE = 0
    //카드를 표시하는 어댑터
    private lateinit var mCardAdapter: CardAdapter

    //카드 스와이프 셋팅과 매니저
    private lateinit var mSwipeLeftSetting: SwipeAnimationSetting
    private lateinit var mSwipeRightSetting: SwipeAnimationSetting
    private lateinit var mSwipeLayoutManager: CardStackLayoutManager

    //카테고리 변경감지를 위한 변수
    private var mOldCategory = HashSet<Int>()
    //사용자 옵션 변경감지를 위한 변수
    private var mOldShowSelfPostOption = false;

    //스와이프의 쿨타임
    private val swipeCooldown: Long = 300 // 0.1초
    private var swipeTimeStamp: Long = 0

    //자동스와이프를 시작하기 위한 변수
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

        UserInfoManager.getInstance().mUserInfo?.let {
            mOldCategory = it.categorys
            mOldShowSelfPostOption = it.isShowSelfPost
        }



    }


    override fun onStart() {
        super.onStart()

        //사용자 설정 변경사항 적용
        UserInfoManager.getInstance().mUserInfo?.let {
            if(mOldCategory != it.categorys || mOldShowSelfPostOption != it.isShowSelfPost)
            {
                refresh()
                mOldCategory = it.categorys;
                mOldShowSelfPostOption = it.isShowSelfPost;
            }
        }
        
    }

    /**
     * by 변성욱
     * SettingActivity에서 관심사를 변경한경우, 변경된 관심사에 따라
     * 현재 가지고있는 게시물을 clear하고 다시 받아올 필요가 있다.
     */
    private fun refresh() {
        mCardAdapter.refresh({ tv_swipe_empty.visibility = View.INVISIBLE }, { tv_swipe_empty.visibility = View.VISIBLE })
    }

    //뷰를 설정한다.
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun readyFragementView() {

        readySwipeView()

        val btnAnim = AnimationUtils.loadAnimation(context!!, R.anim.anim_flinch)
        //게시물보기 버튼 눌렀을때 버튼이 눌리는 애니메이션을 실행하고 PostActivity로 전환한다.
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


        //이전 게시물보기 버튼을 눌렀을때 자동 스와이프 코루틴을 시작한다.
        btn_prv.setOnClickListener {

            if (mCardAdapter.isEmpty())
                return@setOnClickListener

            CoroutineScope(Default).launch {
                while (mIsSwipeButtonPressing)
                    swipe(SWIPE.LEFT)
            }

        }
        //다음 게시물보기 버튼을 눌렀을때 자동 스와이프 코루틴을 시작한다.
        btn_next.setOnClickListener {
            if (mCardAdapter.isEmpty())
                return@setOnClickListener

            CoroutineScope(Default).launch {
                while (mIsSwipeButtonPressing)
                    swipe(SWIPE.RIGHT)
            }
        }


        // mIsSwipeButtonPressing로 버튼 누르고있는 이벤트를 구현
        // 단순히, 버튼을 눌렀을때 mIsSwipeButtonPressing을 true 로 만들고, 뗐을때 false로 만든다.
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

    /**
     * by 변성욱
     * 인자로 주어진 스와이프 방향에 따라 게시물을 스와이프한다.
     * 스와이프에는 쿨타임을 적용했다.
     */
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


    /**
     * by 변성욱
     * PostActivity의 결과를 받는다.
     * "이전 게시물 결과보기" 버튼을 나타내고, 만약 눌렸으면 StatisticActivity로 전환하여
     * 이전 게시물의 투표결과를 보여준다.
     * 3초동안 눌리지않으면 버튼은 사라진다.
     */
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


        //카드를 스와이프하면 카드어댑터에서 카드를 하나 제거한다.
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
        //카드어댑터의 데이터는 카드어댑터 내부에서 서버로 요청하고 저장하기 때문에 빈 LinkedList만 넘겨준다.
        mCardAdapter = CardAdapter(context!!, LinkedList())
        sv_swipeView.adapter = mCardAdapter


        //보여줄 게시물의 정보를 CARD_REQUEST_AT_ONECE만큼 서버에 요청한다.
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
