package com.example.firstapp.Activity

import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AbsListView
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.firstapp.Default.EXTRA_POSTID
import com.example.firstapp.R
import com.example.firstapp.Adapter.PostImgAdapter
import com.example.firstapp.Default.Card
import com.example.firstapp.Default.MyPicture
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.post_img_item.view.*

class PostActivity : AppCompatActivity() {

    private var mCard: Card =
        Card(-1, "", "", ArrayList())
    private var mSelected = ArrayList<Boolean>()

    private var mIsBusy: Boolean = false
    private lateinit var mPostImgAdapter: PostImgAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)


        val btnAnim = AnimationUtils.loadAnimation(this, R.anim.anim_show_up)
        //처음에 안보이다가 하나 클릭하면 쓱 올라오게 하고싶음
        btn_vote.setOnClickListener {
            vote()
            finish()
        }


        mPostImgAdapter = PostImgAdapter(
            this,
            R.layout.post_img_item, mCard.pictures, mSelected
        )

        lv_post_picture.adapter = mPostImgAdapter
        lv_post_picture.choiceMode = AbsListView.CHOICE_MODE_SINGLE

        lv_post_picture.setOnItemClickListener { parent, view, position, id ->

            //parent는 어댑터뷰. 어댑터뷰의 자식수는 화면에 보이는 아이템 수이다.
            //view는 adapter의 getView에서 리턴한 뷰 중에서 선택된 뷰

            mSelected.fill(false)

            //view에 selected를 저장하면 안되고 이렇게 따로해야함.
            //view는 화면을 벗어나면 없어지니까.(정확히는 convertView가 됨)
            //만약 convertView의 selected에 의존하면 convertView가 가지고 있는건 converView가 되기 전의 상태이므로
            //재활용되서 화면에 표시될때 뜬금없이 체크된채로 나옴
            if (position < mSelected.size)
                mSelected[position] = true;

            //화면에 보이는 뷰(아이템)들의 iv_vote 안보이기
            for (i in 0 until parent.childCount)
                parent.getChildAt(i).iv_vote.visibility = View.INVISIBLE

            //현재 선택된 뷰(아이템)의 iv_vote 보이기
            view.iv_vote.visibility = if (lv_post_picture.isItemChecked(position)) View.VISIBLE else View.INVISIBLE


            //최초 클릭에서만 투표버튼의 애니메이션을 실핸한다.
            if (btn_vote.visibility != View.VISIBLE) {
                btn_vote.visibility = View.VISIBLE
                btn_vote.startAnimation(btnAnim)
            }


        }


    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onStart() {
        super.onStart()
        getPostInfo()
    }


    private fun vote() {
        if (mCard.pictures.size <= lv_post_picture.checkedItemPosition || lv_post_picture.checkedItemPosition < 0)
            return;

        val url = getString(R.string.urlToServer) + "vote/" + mCard.postId + "/" + mCard.pictures[lv_post_picture.checkedItemPosition].file_name
        Log.d("volley", url)
        val likeRequest = StringRequest(Request.Method.GET, url,
            { Log.d("volley", it) },
            { it.message?.let { err -> Log.d("volley", err) } })


        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        queue.add(likeRequest)
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public fun getPostInfo() {

        mCard.pictures.clear()

        mIsBusy = true

        val postId = intent.getIntExtra(EXTRA_POSTID, -1)
        if (postId < 0)
            return

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)

        //게시물 하나의 정보를 얻는다.
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
                        val content = obj.getString("content")
                        val writer = obj.getString("writer")
                        mCard.postId = postId
                        mCard.content = content
                        mCard.writer = writer
                        mCard.pictures.add(MyPicture(null, fileName, filePath, 0))
                    }

                    for (i in 0 until mCard.pictures.size)
                        mSelected.add(false)

                }


                //파싱한 데이터를 토대로 게시물의 이미지들을 리퀘스트. 받아왔으면 뷰에 셋팅
                for (picture in mCard.pictures) {
                    val url = getString(R.string.urlToServer) + "getImage/" +
                            picture.file_name

                    var cnt = 0
                    val imgRequest = ImageRequest(url,
                        { bitmap ->
                            picture.bitmap = bitmap
                            mPostImgAdapter.notifyDataSetChanged()
                            if (++cnt >= mCard.pictures.size)
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