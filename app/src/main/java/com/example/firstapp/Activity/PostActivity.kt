package com.example.firstapp.Activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.firstapp.Card.Card
import com.example.firstapp.EXTRA_POSTID
import com.example.firstapp.R
import com.example.firstapp.Adapter.PostImgAdapter
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.post_img_item.*

class PostActivity : AppCompatActivity() {

    private val mCard: Card =
        Card(-1, "", "", ArrayList<Bitmap>(), ArrayList<Pair<String, String>>())

    private var mIsBusy: Boolean = false
    private lateinit var mPostImgAdapter: PostImgAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onStart() {
        super.onStart()


        mPostImgAdapter = PostImgAdapter(
            this,
            R.layout.post_img_item, mCard.bitmaps
        )

        lv_post_picture.adapter = mPostImgAdapter
        lv_post_picture.choiceMode = AbsListView.CHOICE_MODE_SINGLE

        lv_post_picture.setOnItemClickListener { parent, view, position, id ->
            
            //일단 다 안보이게 클리어함
            for(i in 0 until parent.childCount)
                parent.getChildAt(i).findViewById<View>(R.id.iv_vote).visibility = View.INVISIBLE
            
            view.isSelected = lv_post_picture.isItemChecked(position)
            
            view.findViewById<View>(R.id.iv_vote).visibility =
                if (view.isSelected) View.VISIBLE else View.INVISIBLE

        }

        btn_vote.setOnClickListener {
            vote()
            Intent(this, MainActivity::class.java).apply {
                startActivity(this)
            }
        }

        getPostInfo()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    private fun vote() {
        if (mCard.imageInfo.size <= lv_post_picture.checkedItemPosition ||
            lv_post_picture.checkedItemPosition < 0)
            return;

        val url =
            getString(R.string.urlToServer) + "vote/" + mCard.postId + "/" + mCard.imageInfo[lv_post_picture.checkedItemPosition].first
        Log.d("volley", url)
        val likeRequest = StringRequest(Request.Method.GET, url,
            { Log.d("volley", it)},
            { it.message?.let { err -> Log.d("volley", err) } })


        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        queue.add(likeRequest)
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public fun getPostInfo() {

        mIsBusy = true

        val postId = intent.getIntExtra(EXTRA_POSTID, -1)
        if (postId < 0)
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
                        if (mCard?.postId < 0) {
                            val content = obj.getString("content")
                            val writer = obj.getString("writer")
                            val imageInfo = arrayListOf(Pair(fileName, filePath))
                            mCard.postId = postId
                            mCard.content = content
                            mCard.writer = writer
                            mCard.imageInfo = imageInfo
                        } else {
                            //아니면 전에 추가한 포스트에 이미지정보만 추가
                            mCard.imageInfo.add(Pair(fileName, filePath))
                        }
                    }

                }


                //파싱한 데이터를 토대로 게시물의 이미지들을 리퀘스트. 받아왔으면 뷰에 셋팅
                for (imgInfo in mCard.imageInfo) {
                    val url = getString(R.string.urlToServer) + "getImage/" +
                            imgInfo.first;

                    val imgRequest = ImageRequest(url,
                        { bitmap ->
                            mCard.bitmaps.add(bitmap)
                            mPostImgAdapter.notifyDataSetChanged()
                            if (mCard.bitmaps.size >= mCard.imageInfo.size)
                                mIsBusy = false
                        },
                        0,
                        0,
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