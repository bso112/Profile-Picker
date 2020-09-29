package com.example.firstapp.Activity

import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.firstapp.Card.Card
import com.example.firstapp.EXTRA_POSTID
import com.example.firstapp.R
import com.example.firstapp.UploadImage.PostImgAdapter
import kotlinx.android.synthetic.main.activity_post.*

class PostActivity : AppCompatActivity() {

    private val card: Card =
        Card(-1, "", "", ArrayList<Bitmap>(), ArrayList<Pair<String, String>>())

    private var isBusy: Boolean = false
    private lateinit var postImgAdapter : PostImgAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onStart() {
        super.onStart()

        postImgAdapter = PostImgAdapter(this,
            R.layout.upload_img_item, card.bitmaps)
        lv_post_picture.adapter = postImgAdapter

        getPosInfo()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public fun getPosInfo() {

        isBusy = true

        val postId = intent.getIntExtra(EXTRA_POSTID, -1)
        if(postId < 0)
            return

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)

        //랜덤한 유저의 게시글을 얻는다.
        //현재 로그인된 유저정보를 바탕으로
        val postInfoRequest = JsonArrayRequest(
            Request.Method.GET, getString(R.string.urlToServer) + "getPost/" + postId.toString(),
            null,
            Response.Listener {
                it?.let { jsonArr ->
                    for (i in 0 until jsonArr.length()) {
                        val obj = jsonArr.getJSONObject(i);
                        val postId = obj.getInt("postId")
                        val fileName = obj.getString("file_name")
                        val filePath = obj.getString("path")

                        //만약 게시물목록이 비었거나 전에 추가된 포스트의 id와 이번에 추가할
                        //포스트의 id가 다르다면, 게시물목록에 항목추가
                        if (card?.postId < 0) {
                            val content = obj.getString("content")
                            val writer = obj.getString("writer")
                            val imageInfo = arrayListOf(Pair(fileName, filePath))
                            card.postId = postId
                            card.content = content
                            card.writer = writer
                            card.imageInfo = imageInfo
                        } else {
                            //아니면 전에 추가한 포스트에 이미지정보만 추가
                            card.imageInfo.add(Pair(fileName, filePath))
                        }
                    }

                }


                //파싱한 데이터를 토대로 게시물의 이미지들을 리퀘스트. 받아왔으면 뷰에 셋팅
                for (imgInfo in card.imageInfo) {
                    val url = getString(R.string.urlToServer) + "getImage/" +
                            imgInfo.first;

                    val imgRequest = ImageRequest(url,
                        { bitmap ->
                            card.bitmaps.add(bitmap)
                            postImgAdapter.notifyDataSetChanged()
                            if (card.bitmaps.size >= card.imageInfo.size)
                                isBusy = false
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