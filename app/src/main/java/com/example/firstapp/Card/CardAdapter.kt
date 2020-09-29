package com.example.firstapp.Card

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.firstapp.R
import kotlinx.android.synthetic.main.swipe_item.view.*

class CardAdapter(context: Context, resourceID: Int) :
    ArrayAdapter<Card>(context, resourceID) {

    private var requestCount = Int.MAX_VALUE
    //네트워크에서 카드데이터를 받아오는 중인가?
    fun isBusy() : Boolean{
        return requestCount <  context.resources.getInteger(R.integer.CardRequestAtOnce)
    }


    //getView는 view가 필요할때 즉, 화면에 view가 보여야할때 불린다.
    //view마다 불리기 때문에 여러번 불린다.
    //position 은 어떤 포지션이지?
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val card: Card? = getItem(position)


        /*
        Inflater는 XML을 실제로 메모리에 올리는 역할을 한다. inflate을 통해 메모리에 올리고,
        그렇게 생성한 객체를 반환한다.
         */
        val cardView: View =
            LayoutInflater.from(context).inflate(R.layout.swipe_item, parent, false)

        card?.let {
            if(it.bitmaps.isNotEmpty())
                cardView.swipImg?.setImageBitmap((it.bitmaps.first()))
        }
        //만든 카드뷰를 리턴한다.
        return cardView
    }

     public fun removeCardAtFront()
     {
         if(!super.isEmpty())
            super.remove(super.getItem(0))
     }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public fun addCardData(callback: ((card : Card) -> Unit)? = null) {

        requestCount = 0
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(context!!)

        var card: Card? = null
        //랜덤한 유저의 게시글을 얻는다.
        //현재 로그인된 유저정보를 바탕으로
        val postInfoRequest = JsonArrayRequest(
            Request.Method.GET,
            context.getString(R.string.urlToServer) + "getRandomPost/",
            null,
            Response.Listener {
                it?.let { jsonArr->
                    for (i in 0 until jsonArr.length()) {
                        val obj = jsonArr.getJSONObject(i);
                        val postId = obj.getInt("postId")
                        val fileName = obj.getString("file_name")
                        val filePath = obj.getString("path")

                        //만약 게시물목록이 비었거나 전에 추가된 포스트의 id와 이번에 추가할
                        //포스트의 id가 다르다면, 게시물목록에 항목추가
                        if (card?.postId != postId) {
                            val content = obj.getString("content")
                            val writer = obj.getString("writer")
                            val imageInfo = arrayListOf(Pair(fileName, filePath))
                            card = Card(postId, content, writer, ArrayList<Bitmap>(), imageInfo)
                        } else {
                            //아니면 전에 추가한 포스트에 이미지정보만 추가
                            card!!.imageInfo.add(Pair(fileName, filePath))
                        }
                    }

                }

                //게시물의 첫번째 사진만 가져온다.
                card?.let { _card ->
                    //파싱한 데이터를 토대로 이미지 리퀘스트. 받아왔으면 뷰에 셋팅
                        val url = context.getString(R.string.urlToServer) + "getImage/" +
                                _card.imageInfo.first().first;

                        val imgRequest = ImageRequest(url,
                                { bitmap ->
                                    _card.bitmaps.add(bitmap)
                                    //어레이어댑터 아이템으로 추가
                                    super.add(_card)
                                    requestCount++

                                    if (callback != null) {
                                        callback(_card)
                                    };
                                },
                                300,
                                800,
                                ImageView.ScaleType.CENTER_CROP,
                                Bitmap.Config.ARGB_8888,
                                { err ->
                                    Log.e("volley", err.message ?: "err ocurr!")
                                })
                        queue.add(imgRequest)

                }
                //Response.Listener End
            },
            Response.ErrorListener {
                Log.e("Volley", it.toString())
            })

        queue.add(postInfoRequest)
    }


}