package com.manta.firstapp.Activity.ViewPage


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.manta.firstapp.Helper.VolleyHelper
import com.manta.firstapp.Helper.showSimpleAlert
import com.manta.firstapp.Activity.LoginActivity
import com.manta.firstapp.Activity.StatisticActivity
import com.manta.firstapp.Activity.UploadImgActivity
import com.manta.firstapp.Adapter.MyPostAdapter
import com.manta.firstapp.Default.*
import com.manta.firstapp.Helper.UserInfoManager
import com.manta.firstapp.R
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.frag_profile.*

/**
 * by 변성욱
 * 내가 올린 게시물을 볼 수 있는 프래그먼트.
 * 하단의 + 버튼을 누르면 글쓰기를 위해 UploadImgActivity로 전환한다.
 * 내 게시물 항목을 누르면 투표받은 통계를 보여주는 StatisticActivity로 전환한다.
 * 내 게시물 항목을 길게 누르면 수정, 삭제가 있는 메뉴를 보여준다.
 */
class ProfileFragment : Fragment() {

    private lateinit var mPostAdapter: MyPostAdapter
    private val mPosts = ArrayList<Post>()
    private var mSelectedPost: Post? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //하단의 + 버튼을 누르면 글쓰기 액티비티로
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


        //lv_myPosts의 아이템을 길게누르면 메뉴가 생성되며 onCreateContextMenu가 불림.
        //콘텍스트메뉴로서 lv_myPosts를 등록한다. 이래야 onCreateContextMenu와 onContextItemSelected가 불린다.
        registerForContextMenu(lv_myPosts)

        //배너광고요청
        val adRequest = AdRequest.Builder().build()
        av_profile_banner.loadAd(adRequest)

        //당겨서 새로고침
        swipeRefresh.setOnRefreshListener {
            refreshView();
            swipeRefresh.isRefreshing = false;
        }
    }



    //lv_myPosts의 아이템을 길게 누르면 메뉴를 생성한다.
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val info = menuInfo as? AdapterContextMenuInfo
        //선택된 포스트를 저장
        mSelectedPost = lv_myPosts.getItemAtPosition(info?.position ?: 0) as? Post

        //만약 선택된게 게시물을 표시하는 리스트뷰면, 메뉴를 띄운다.
        if(v.id == R.id.lv_myPosts)
            activity?.menuInflater?.let { it.inflate(R.menu.menu_post, menu) }

    }

    //메뉴 아이템이 선택되었을때 불린다.
    override fun onContextItemSelected(item: MenuItem): Boolean {

        when(item.itemId)
        {
            R.id.it_modify -> {
                mSelectedPost?.let { it1 -> modifyPost(it1) }
            }
            R.id.it_remove ->{
                showSimpleAlert(context, null, "정말로 삭제하시겠습니까?", {  mSelectedPost?.let { it1 -> deletePost(it1) }})
            }
        }
        return super.onContextItemSelected(item)

    }


    private fun deletePost(post: Post) {
        val url = getString(R.string.urlToServer) + "deletePost/${post.postInfo.postId}"
        val request = StringRequest(Request.Method.GET, url, {
            Toast.makeText(context, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
            refreshView()
        }, null)

        context?.let { VolleyHelper.getInstance(it).addRequestQueue(request) }
    }


    private fun modifyPost(post: Post) {
        Intent(context, UploadImgActivity::class.java).apply {
            putExtra(EXTRA_POSTID, post.postInfo.postId)
            startActivity(this)
        }
    }


    //내가 쓴 게시물 조회는 액티비티 보일때마다 매번해야됨.
    //게시물 상태가 수시로 달라질 수 있으니.(조회수, 좋아요 등..)
    override fun onStart() {
        super.onStart()
        refreshView()
    }


    private fun requestPostTumbnail(tumbnailName: String, post: Post) {


        val queue = Volley.newRequestQueue(context)

        val url = getString(R.string.urlToServer) + "getImage/${tumbnailName}"

        val tumbnailRequest = ImageRequest(url, {
            it?.let { bitmap ->
                post.tumbnail = it
                post.onTumbnailSet?.let { it() }
            }
            //썸네일을 적용해서 어댑터뷰를 다시그린다.
            mPostAdapter.notifyDataSetChanged()
            lv_myPosts.adapter = mPostAdapter

        }, 0, 0, ImageView.ScaleType.CENTER_CROP, null, {
            Log.d("volley", it.message.toString())
        })


        queue.add(tumbnailRequest)
    }

    private fun getPostTumbnail(oldPosts: ArrayList<Post>) {

        //매번 포스트에 대한 이미지리퀘스트를 날리는건 낭비가 심하니까
        //썸네일이 바뀌여야할때 이미지리퀘스트를 하고, 아니면 전에 썸네일을 그대로 쓴다.

        //이전 포스트의 postId와 일치하는 포스트를 oldposts에서 찾는다.
        val oldPost = getPostById(mPosts.last().postInfo.postId, oldPosts)

        //만약 oldPosts에 없으면 바로 리퀘스트
        if (oldPost == null) {
            //왜인지 모르겠지만 스와이프에서 나오는 썸네일이랑 순서가 반대임.
            val newTumbnailName = mPosts.last().postInfo.myPictures.last().file_name
            requestPostTumbnail(newTumbnailName, mPosts.last())
        }


        oldPost?.let {
            mPosts.last().tumbnail = it?.tumbnail
        }

//        //있으면 갱신해야되는지 확인후 갱신
//        oldPost?.let {
//
//            //찾은 포스트의 썸네일
//            val oldTumbnailName = it.getTumbnailPictureName()
//            //현재 post의 썸네일
//            val newTumbnailName = mPosts.last().getTumbnailPictureName()
//
//            //만약 두 썸네일이 다르다면(like가 갱신됬다면) 새로운 썸네일을 서버로 요청
//            if (oldTumbnailName != newTumbnailName)
//                requestPostTumbnail(newTumbnailName, mPosts.last())
//            //같다면 예전꺼 그대로 씀
//            else
//                mPosts.last().tumbnail = it?.tumbnail
//
//        }


    }


    private fun refreshView() {

        //사용자정보
        tv_profile_nickname.text = UserInfoManager.getInstance().mUserInfo?.nickname

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
                        val postId = obj.getLong("postId")
                        val file_name = obj.getString("file_name")
                        val path = obj.getString("path")
                        val likes = obj.getInt("likes")
                        val viewCnt = obj.getInt("view")

                        //만약 이번에 새로운 포스트를 추가한다면, 우선 이전 포스트 썸네일을 결정한다.
                        if (mPosts.isNotEmpty() && (mPosts.last().postInfo.postId != postId))
                            getPostTumbnail(oldPosts)


                        //포스트를 처음추가하거나, 새로운 포스트를 추가해야한다면 포스트 추가
                        if (mPosts.isEmpty() || mPosts.last().postInfo.postId != postId) {
                            mPosts.add(Post(null, PostInfo(title, date, viewCnt, postId, arrayListOf(MyPicture(null, file_name, path, likes)))))

                        }
                        //아니면 사진만 추가
                        else
                            mPosts.last().postInfo.myPictures.add(MyPicture(null, file_name, path, likes))

                        //마지막 포스트 처리
                        if (i == (it.length() - 1))
                            getPostTumbnail(oldPosts)


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

    private fun getPostById(postId: Long, posts: ArrayList<Post>): Post? {
        for (post in posts) {
            if (post.postInfo.postId == postId)
                return post;
        }
        return null
    }


}