package com.example.firstapp.Adapter

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.firstapp.Activity.LoginActivity
import com.example.firstapp.Default.Card
import com.example.firstapp.Default.MyPicture
import com.example.firstapp.R
import kotlinx.android.synthetic.main.swipe_item.view.*

class CardAdapter(context: Context, resourceID: Int) :
    ArrayAdapter<Card>(context, resourceID) {


    /**
     *     DB로부터 받아올 카드 데이터의 시작인덱스
     */
    private var mCardDataIndex: Int = 0


    /**
     * 네트워크에서 카드데이터를 받아오는 중인가?
     */
    var mIsBusy : Boolean = false
    private set;


    //getView는 view가 필요할때 즉, 화면에 view가 보여야할때 불린다.
    //view마다 불리기 때문에 여러번 불린다.
    //position 은 어떤 포지션이지?
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val card: Card? = getItem(position)

        var view: View
        if (null != convertView)
            view = convertView
        else
            view = LayoutInflater.from(context).inflate(R.layout.swipe_item, parent, false)

        if (card != null) {
            if (card.pictures.isNotEmpty())
                view.swipImg?.setImageBitmap(card.pictures.first().bitmap)

            view.tv_swipe_title.text = card.title
            //view.tv_swipe_userName.text = card.writer
            view.tv_swipe_content.text = card.content


        }

        //만든 카드뷰를 리턴한다.
        return view
    }

    public fun removeCardAtFront() {
        if (!super.isEmpty())
            super.remove(super.getItem(0))
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    /**
     * postCnt : 가져올 post 수
     */
    public fun addCardData(postCnt: Int, callback: ((card: Card) -> Unit)? = null) {

        mIsBusy = true

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(context!!)


        var cardList = ArrayList<Card>()

        var url = context.getString(R.string.urlToServer) + "getRandomPost/" + postCnt.toString() + "/" + mCardDataIndex.toString() +
                "/" + LoginActivity.mAccount?.email
        //랜덤한 유저의 게시글을 얻는다.
        //현재 로그인된 유저정보를 바탕으로
        val postInfoRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            Response.Listener {res->
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
                        } else {
                            //아니면 전에 추가한 포스트에 이미지정보만 추가 .. 할필요 없을듯?
                            cardList.last().pictures.add(MyPicture(null, fileName, filePath, 0))
                        }
                    }
                }

                //카드에 쓸 이미지 받아옴
                for (card in cardList) {
                    if (null == card)
                        return@Listener

                    val url = context.getString(R.string.urlToServer) + "getImage/" +
                            card.pictures.first().file_name;

                    val imgRequest = ImageRequest(url,
                        { bitmap ->
                            card.pictures.first().bitmap = bitmap
                            //완성한 카드를 어레디어댑터에 추가
                            super.add(card)

                            mCardDataIndex++

                            //마지막 루프면
                            if(card === cardList.last())
                            {
                                mIsBusy = false

                                //만약 받았은 포스트의 수가 요청한 것보다 적으면 마지막 데이터셋이라는 뜻
                                //그때는 mCardDataIndex를 0으로 돌린다.
                                if(cardList.count() < postCnt)
                                {
                                    mCardDataIndex = 0
                                    Toast.makeText(context, "새로운 게시물이 없습니다. 이전 게시물을 표시합니다.", Toast.LENGTH_SHORT)
                                }

                            }




                            if (callback != null) {
                                callback(card)
                            }
                        }, 300, 800, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888,
                        { err ->
                            Log.e("volley", err.message ?: "err ocurr!")
                        })

                    queue.add(imgRequest)

                }
            },
            Response.ErrorListener {
                Log.e("Volley", it.toString())
            })

        queue.add(postInfoRequest)
    }


}