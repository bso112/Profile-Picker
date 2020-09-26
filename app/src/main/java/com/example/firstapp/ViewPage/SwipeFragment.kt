package com.example.firstapp.ViewPage

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.firstapp.Card.Card
import com.example.firstapp.Card.CardAdapter
import com.example.firstapp.R
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import kotlinx.android.synthetic.main.frag_swipe.*


class SwipeFragment : Fragment() {

    lateinit var  mCardAdapter : CardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_swipe, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //카드데이터 요청
        requestCardData();

        //카드데이터를 모두 받았으면 그걸 토대로 뷰 셋팅
        readyFragementView();

    }

    private fun readyFragementView()
    {

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

    private fun readySwipeView()
    {
        if (null == context) {
            Log.d("SwipeFragment", "Context is null")
            return
        }

        //craete cardAdapter
       mCardAdapter = CardAdapter(context!!, R.layout.swipe_item)
        //set the listener and the adapter
        swipeView.adapter = mCardAdapter
        swipeView.setFlingListener(object : SwipeFlingAdapterView.onFlingListener {

            override fun removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!")

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
                // Ask for more data here
                // 여기서 더 많은 데이터를 가져온다.

            }

            override fun onScroll(p0: Float) {
                // not implemented
            }
        })
    }


    private fun requestCardData() {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(context!!)

        //Card를 만들 데이터 받아오기
        //널 초기화를 해줘야하는가?
        var bitmaps: ArrayList<Bitmap> = ArrayList<Bitmap>()
        //게시글 작성자
        var email : String = ""
        //게시글 내용(글)
        var content: String = ""

        //게시글 이미지파일들 이름
        var imageNames: ArrayList<String> = ArrayList<String>()

        //랜덤한 유저의 게시글을 얻는다.
        //현재 로그인된 유저정보를 바탕으로
        val postInfoRequest = JsonObjectRequest(Request.Method.GET,
            getString(R.string.urlToServer) + "getPost/",
            null,
            {

                Log.d ("card info", it.toString()) //확인
                //게시글 정보를 받아와서 파싱
//                it?.let {
//                    email = it.getString("email")
//                    content = it.getString("content")
//                    //email 유저가 올린 이미지들을 받는다.
//                    val names = it.getJSONArray("imageNames")
//                    for (i in 1..names.length())
//                        imageNames.add(names.getString(i))
//
//                }
//
//                //파싱한 데이터를 토대로 이미지 리퀘스트
//                for (imgName in imageNames) {
//                    val imgRequest = ImageRequest(getString(R.string.urlToServer) + "/" + imgName,
//                        {
//                            //카드추가
//                            it?.let{   mCardAdapter.add(Card(email, content, it)) }
//
//                        }, 300, 800, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888,
//                        {
//                            Log.e("Request Image Failed", it.message ?: "err ocurr!")
//                        })
//
//                    queue.add(imgRequest)
//                }
                
            },
            {
                Log.e("Volley", "Fail to get UserInfo")
            })

        queue.add(postInfoRequest)
    }

}