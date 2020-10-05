package com.example.firstapp.Activity.ViewPage


import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.firstapp.Activity.LoginActivity
import com.example.firstapp.Activity.StatisticActivity
import com.example.firstapp.R
import com.example.firstapp.Activity.UploadImgActivity
import com.example.firstapp.Adapter.MyPostAdapter
import com.example.firstapp.EXTRA_POSTINFO
import kotlinx.android.synthetic.main.frag_profile.*
import java.io.Serializable


data class MyPicture(var bitmap: Bitmap?, val file_name : String, val path : String, val likes : Int)
    :Serializable
//Picture를 포함하니 Picture도 Serializable이여야한다.
data class PostInfo(val title : String = "", val date : String = "", val viewCnt : Int = 0,
                    val postId : Int = 0, var  myPictures : ArrayList<MyPicture> = ArrayList<MyPicture>())
    : Serializable
{
    constructor(other : PostInfo) :this(other.title
    ,other.date, other.viewCnt, other.postId, ArrayList<MyPicture>(other.myPictures)
    )
}



class ProfileFragment : Fragment() {

    private lateinit var mPostAdapter : ArrayAdapter<PostInfo>
    private val mPostInfo = ArrayList<PostInfo>()

    //뷰를 생성할때
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return  inflater.inflate(R.layout.frag_profile, container, false)
    }

    //뷰가 생성되었을때
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //글쓰기 액티비티로
        ib_profile_create.setOnClickListener{
            val intent = Intent(context, UploadImgActivity::class.java)
            startActivity(intent)
        }

        mPostAdapter = MyPostAdapter(context!!, R.layout.mypost_item, mPostInfo)
        lv_myPosts.adapter = mPostAdapter

        //포스트 목록 클릭하면 통계 액티비티로
        lv_myPosts.setOnItemClickListener { parent, view, position, id ->
            Intent(context, StatisticActivity::class.java).apply {
                if(position < mPostInfo.size)
                    putExtra(EXTRA_POSTINFO, mPostInfo[position])
                startActivity(this)
            }

        }

        getMyPosts()

    }



    private fun getMyPosts()
    {
        val queue = Volley.newRequestQueue(context)

        val url = getString(R.string.urlToServer) + "getMyPost/" + LoginActivity.mAccount?.email
        val postRequest = JsonArrayRequest(Request.Method.GET, url, null,
            {
                it.let {
                    for(i in 0 until it.length())
                    {
                        val obj = it.getJSONObject(i)
                        val title = obj.getString("title")
                        val date = obj.getString("date")
                        val postId = obj.getInt("postId")
                        val file_name = obj.getString("file_name")
                        val path = obj.getString("path")
                        val likes = obj.getInt("likes")
                        if(mPostInfo.isEmpty() || mPostInfo.last().postId != postId)
                            mPostInfo.add(PostInfo(title, date, 0, postId,  arrayListOf(MyPicture(null, file_name, path, likes))))
                        else
                            mPostInfo.last().myPictures.add(MyPicture(null, file_name, path, likes))

                        mPostAdapter.notifyDataSetChanged()
                    }
                }
            },
            {
                Log.d("Volley", it.message.toString())
            })

        queue.add(postRequest)

    }



}