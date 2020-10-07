package com.example.firstapp.Activity.ViewPage


import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.firstapp.Activity.LoginActivity
import com.example.firstapp.Activity.StatisticActivity
import com.example.firstapp.R
import com.example.firstapp.Activity.UploadImgActivity
import com.example.firstapp.Adapter.MyPostAdapter
import com.example.firstapp.Default.EXTRA_POSTINFO
import com.example.firstapp.Default.MyPicture
import com.example.firstapp.Default.Post
import com.example.firstapp.Default.PostInfo
import kotlinx.android.synthetic.main.frag_profile.*
import java.io.Serializable



class ProfileFragment : Fragment() {

    private lateinit var mPostAdapter: MyPostAdapter
    private val mPosts = ArrayList<Post>()

    //뷰를 생성할때
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_profile, container, false)
    }

    //뷰가 생성되었을때
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //글쓰기 액티비티로
        ib_profile_create.setOnClickListener {
            val intent = Intent(context, UploadImgActivity::class.java)
            startActivity(intent)
        }

        mPostAdapter = MyPostAdapter(context!!, R.layout.mypost_item, mPosts)
        lv_myPosts.adapter = mPostAdapter

        //포스트 목록 클릭하면 통계 액티비티로
        lv_myPosts.setOnItemClickListener { parent, view, position, id ->
            Intent(context, StatisticActivity::class.java).apply {
                if (position < mPosts.size)
                    putExtra(EXTRA_POSTINFO, mPosts[position].postInfo)
                startActivity(this)
            }

        }

    }

    //내가 쓴 게시물 조회는 액티비티 보일때마다 매번해야됨.
    //게시물 상태가 수시로 달라질 수 있으니.(조회수, 좋아요 등..)
    override fun onStart() {
        super.onStart()
        getMyPosts()
    }

    private fun getTumbnail(tumbnailName: String, post: Post) {

        val queue = Volley.newRequestQueue(context)

        val url = getString(R.string.urlToServer) + "getImage/${tumbnailName}"

        val tumbnailRequest = ImageRequest(url, {
            it?.let { bitmap -> post.tumbnail = it }
            //썸네일을 적용해서 어댑터뷰를 다시그린다.
            mPostAdapter.notifyDataSetChanged()
            lv_myPosts.adapter = mPostAdapter

        }, 0, 0, ImageView.ScaleType.CENTER_CROP, null, {
            Log.d("volley", it.message.toString())
        })


        queue.add(tumbnailRequest)
    }




    private fun getMyPosts() {

        //갱신하기 전의 포스트들
        val oldPosts = ArrayList<Post>(mPosts)
        mPosts.clear()

        val queue = Volley.newRequestQueue(context)

        val url = getString(R.string.urlToServer) + "getMyPost/" + LoginActivity.mAccount?.email
        val postRequest = JsonArrayRequest(Request.Method.GET, url, null,
            {
                it.let {
                    for (i in 0 until it.length()) {
                        val obj = it.getJSONObject(i)
                        val title = obj.getString("title")
                        val date = obj.getString("date")
                        val postId = obj.getInt("postId")
                        val file_name = obj.getString("file_name")
                        val path = obj.getString("path")
                        val likes = obj.getInt("likes")
                        val viewCnt = obj.getInt("view")

                        //이전에 포스트를 추가한적이 있고, 만약 새로운 포스트를 추가해야하거나
                        //이번에 추가하는게 마지막 사진이면 가장 많은 좋아요를 받은 사진을 썸네일으로 결정
                        if ((mPosts.isNotEmpty() && mPosts.last().postInfo.postId != postId) || i == (it.length() - 1)) {

                            //방금 추가된 post와 같은 postId를 가진 post를 oldPosts에서 찾는다.
                            val oldPost = getPostById(postId, oldPosts)

                            //만약 oldPost가 비어있거나(처음 Post를 만들거나)
                            //oldPost에서 썼던 썸네일과 현재 추가된 Post의 썸네일이 달라야한다면 서버에 썸네일을 요청한다.
                            val oldTumbnailName = oldPost?.getTumbnailPictureName()
                            val newTumbnailName = mPosts.last().getTumbnailPictureName()
                            if (oldPosts.size <= 0 ||
                                ((oldPost != null) && (oldTumbnailName != newTumbnailName))
                            )
                                getTumbnail(newTumbnailName, mPosts.last())
                            else
                                mPosts.last().tumbnail = oldPost?.tumbnail
                        }

                        //포스트를 처음추가하거나, 새로운 포스트를 추가해야한다면 포스트 추가
                        if (mPosts.isEmpty() || mPosts.last().postInfo.postId != postId) {
                            mPosts.add(
                                Post(
                                    null, PostInfo(
                                        title, date, viewCnt, postId,
                                        arrayListOf(MyPicture(null, file_name, path, likes))
                                    )
                                )
                            )
                        }
                        //아니면 사진만 추가
                        else
                            mPosts.last().postInfo.myPictures.add(MyPicture(null, file_name, path, likes))


                    }
                    //응답처리후 데이터 적용 ( 아직 썸네일 안그림)
                    mPostAdapter.notifyDataSetChanged()
                }
            },
            {
                Log.d("Volley", it.message.toString())
            })

        queue.add(postRequest)

    }

    private fun getPostById(postId: Int, posts: ArrayList<Post>): Post? {
        for (post in posts) {
            if (post.postInfo.postId == postId)
                return post;
        }
        return null
    }


}