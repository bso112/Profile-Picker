package com.example.firstapp.Activity.Helper

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.firstapp.Default.EXTRA_POSTID
import com.example.firstapp.Default.MyPicture
import com.example.firstapp.Default.PostInfo
import com.example.firstapp.R


class VolleyHelper (val context : Context){

    var mRequestQueue : RequestQueue = Volley.newRequestQueue(context.applicationContext)


    companion object {

        private var INSTANCE : VolleyHelper? = null

        fun getInstance(context: Context) : VolleyHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE = VolleyHelper(context)
                return INSTANCE as VolleyHelper
            }
    }

    fun <T> addRequestQueue(req : Request<T>)
    {
        mRequestQueue.add(req)
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public fun getPostInfo(postId : Int , postInfo : PostInfo) {


        //게시물 하나의 정보를 얻는다.
        val postInfoRequest = JsonArrayRequest(
            Request.Method.GET, context.resources.getString(R.string.urlToServer) + "getPost/" + postId.toString(),
            null,
            Response.Listener {
                it?.let { jsonArr ->
                    for (i in 0 until jsonArr.length()) {
                        val obj = jsonArr.getJSONObject(i);
                        val postId = obj.getInt("postId")
                        val fileName = obj.getString("file_name")
                        val filePath = obj.getString("path")
                        val content = obj.getString("content")
                        val writer = obj.getString("writer")
                        postInfo.postId = postId
                        postInfo.content = content
                        postInfo.writer = writer
                        postInfo.myPictures.add(MyPicture(null, fileName, filePath, 0))
                    }

                }

                var cnt = 0
                //파싱한 데이터를 토대로 게시물의 이미지들을 리퀘스트. 받아왔으면 뷰에 셋팅
                for (picture in postInfo.myPictures) {
                    val url = context.resources.getString(R.string.urlToServer) + "getImage/" +
                            picture.file_name

                    val imgRequest = ImageRequest(url,
                        { bitmap ->
                            picture.bitmap = bitmap
                            if (++cnt >= postInfo.myPictures.size)
                                postInfo.mOnInitialized?.let {
                                    it()
                                }
                        },
                        0,
                        0,
                        ImageView.ScaleType.CENTER_CROP,
                        Bitmap.Config.ARGB_8888,
                        { err ->
                            Log.e("volley", err.message ?: "err ocurr!")
                        })

                    addRequestQueue(imgRequest)
                }

                //Response.Listener End
            },
            Response.ErrorListener {
                Log.e("Volley", it.toString())
            })

       addRequestQueue(postInfoRequest)
    }


}