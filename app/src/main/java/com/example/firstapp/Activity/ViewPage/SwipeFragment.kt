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
import com.example.firstapp.Default.Card
import com.example.firstapp.R
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import kotlinx.android.synthetic.main.frag_swipe.*


class SwipeFragment : Fragment() {

    var REQUEST_VOTE = 0
    lateinit var mCardAdapter: CardAdapter
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

        val btnAnim = AnimationUtils.loadAnimation(context!!, R.anim.anim_flinch)
        //게시물보기 버튼 눌렀을때
        btn_like.setOnClickListener {
            mCardAdapter.getItem(0)?.let {
                val intent = Intent(context, PostActivity::class.java).apply {
                    putExtra(EXTRA_POSTID, it.postId)
                }
                startActivityForResult(intent, REQUEST_VOTE)
            }
            it.startAnimation(btnAnim)
        }
        btn_prv.setOnClickListener {
            swipeView.topCardListener.selectLeft()
        }
        btn_next.setOnClickListener {
            swipeView.topCardListener.selectRight()
        }


        readySwipeView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode != RESULT_OK)
            return

        if(requestCode == REQUEST_VOTE)
            swipeView.topCardListener.selectLeft()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun readySwipeView() {
        if (null == context) {
            Log.d("SwipeFragment", "Context is null")
            return
        }

        //craete cardAdapter
        mCardAdapter = CardAdapter(
            context!!,
            R.layout.swipe_item
        )

        //set the listener and the adapter
        swipeView.adapter = mCardAdapter

        mCardAdapter.addCardData(resources.getInteger(R.integer.CardRequestAtOnce))

        swipeView.setFlingListener(object : SwipeFlingAdapterView.onFlingListener {

            override fun removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!")

                mCardAdapter.removeCardAtFront()


                /*
                사진이 하나 remove됬을때 어댑터에게 그 사실을 알린다.
                그러면 CardAdapter의 부모클래스의 내부에 있는 옵저버들에게(이 옵저버들은 화면을 갱신하기 위해서 설정된 다른 오브젝트들일듯?)
                그 사실이 알려진다.
                그러면서 CardAdapter의 getView가 불리면서 4개의 뷰를 생성한다. (디버그해본 결과 한번에 최대 4개를 생성하는듯)
                 */
                mCardAdapter.notifyDataSetChanged()
            }

            override fun onLeftCardExit(dataObject: Any) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
            }

            override fun onRightCardExit(dataObject: Any) {
            }

            override fun onAdapterAboutToEmpty(itemsInAdapter: Int) {
                // 여기서 더 많은 데이터를 가져온다.

                //아직 데이터를 받아오고 있는 중이면
                if (mCardAdapter.mIsBusy) {
//                    Toast.makeText(context, "카드 데이터를 받아오는 중입니다.", Toast.LENGTH_SHORT).show()
                    return
                }

                //남은 아이템수가 2이하일때
                if (itemsInAdapter <= 2) {
                    mCardAdapter.addCardData(resources.getInteger(R.integer.CardRequestAtOnce))
                }

            }

            override fun onScroll(p0: Float) {
                // not implemented
            }
        })
    }


}
