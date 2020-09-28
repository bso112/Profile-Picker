package com.example.firstapp.ViewPage

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.firstapp.Card.CardAdapter
import com.example.firstapp.R
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import kotlinx.android.synthetic.main.frag_swipe.*


class SwipeFragment : Fragment() {

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

        val btnAnim = AnimationUtils.loadAnimation(context!!, R.anim.anim_btn)
        //dislike 버튼 눌렀을때
        btn_dislike.setOnClickListener {
            swipeView.topCardListener.selectLeft()
            it.startAnimation(btnAnim)
        }
        //like 버튼 눌렀을때
        btn_like.setOnClickListener {
            swipeView.topCardListener.selectRight();
            it.startAnimation(btnAnim)
        }

        readySwipeView()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun readySwipeView() {
        if (null == context) {
            Log.d("SwipeFragment", "Context is null")
            return
        }

        //craete cardAdapter
        val cardAdapter = CardAdapter(context!!, R.layout.swipe_item)

        //set the listener and the adapter
        swipeView.adapter = cardAdapter
        swipeView.setFlingListener(object : SwipeFlingAdapterView.onFlingListener {

            override fun removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!")
                cardAdapter.removeCardAtFront()


                /*
                사진이 하나 remove됬을때 어댑터에게 그 사실을 알린다.
                그러면 CardAdapter의 부모클래스의 내부에 있는 옵저버들에게(이 옵저버들은 화면을 갱신하기 위해서 설정된 다른 오브젝트들일듯?)
                그 사실이 알려진다.
                그러면서 CardAdapter의 getView가 불리면서 4개의 뷰를 생성한다. (디버그해본 결과 한번에 최대 4개를 생성하는듯)
                 */
                cardAdapter.notifyDataSetChanged()
            }

            override fun onLeftCardExit(dataObject: Any) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
            }

            override fun onRightCardExit(dataObject: Any) {
            }

            override fun onAdapterAboutToEmpty(itemsInAdapter: Int) {
                // Ask for more data here
                // 여기서 더 많은 데이터를 가져온다.
                if(itemsInAdapter <= 2)
                {
                    for (i in 0..5)
                        cardAdapter.addCardData();
                }

            }

            override fun onScroll(p0: Float) {
                // not implemented
            }
        })
    }






}
