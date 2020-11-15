package com.manta.firstapp.Activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AbsListView
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.manta.firstapp.Adapter.PostImgAdapter
import com.manta.firstapp.Default.*
import com.manta.firstapp.Helper.MyFileHelper
import com.manta.firstapp.R
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.post_img_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


/**
 * by 변성욱
 * SwipeFragement에서 하트버튼 클릭시, 게시물을 보여주는 액티비티.
 * 게시물 정보를 네트워크를 통해 받아오고, 사용자의 투표를 처리한다.
 */
class PostActivity : AppCompatActivity() {

    //게시물 정보
    private var mCard: Card =
        Card(-1, "", "", "", "", ArrayList())

    //인덱스에 해당하는 이미지가 선택되어있는지 저장하는 변수
    private var mSelected = ArrayList<Boolean>()

    //네트워크요청중인가?
    private var mIsBusy: Boolean = false
    //게시물에 있는 이미지를 보여주는 어댑터
    private lateinit var mPostImgAdapter: PostImgAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)


        val btnAnim = AnimationUtils.loadAnimation(this, R.anim.anim_show_up)

        //하트버튼 (투표버튼)을 클릭하면 이 게시물의 정보를 intent에 담아서 SwipeFragement로 돌려준다.
        btn_vote.setOnClickListener {
            vote()
            for(picture in mCard.pictures) picture.bitmap = null;
            setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_POSTINFO, PostInfo(postId = mCard.postId, myPictures = mCard.pictures)))
            finish()
        }


        mPostImgAdapter = PostImgAdapter(
            this,
            R.layout.post_img_item, mCard.pictures, mSelected
        )

        lv_post_picture.adapter = mPostImgAdapter
        lv_post_picture.choiceMode = AbsListView.CHOICE_MODE_SINGLE

        //사진을 클릭하면 오른쪽 상단에 빨간 투표아이콘(iv_vote)를 표시한다.
        lv_post_picture.setOnItemClickListener { parent, view, position, id ->

            //false로 다 채움(초기화)
            mSelected.fill(false)

            //선택된 이미지 표시
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

        //사진을 길게 눌렀을때 PictureActivity로 인텐트를 보내 원본사진을 보여준다.
        lv_post_picture.setOnItemLongClickListener { parent, view, position, id ->

            CoroutineScope(IO).launch {
                //해당 아이템의 비트맵을 외부저장소의 개별디렉토리(캐시 디렉토리)에 저장한다.

                //비트맵을 얻는다.
                val bitmap = (view.iv_post_image.drawable as BitmapDrawable).bitmap


                val file = MyFileHelper.createImageTempFile(this@PostActivity)
                try {
                    //비트맵을 압축해서 파일에 쓴다.
                    file.outputStream().use {  bitmap.compress(Bitmap.CompressFormat.WEBP, 60, it) }
                    //공용공간에 쓰는 거였으면 이걸추가
                    //MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());

                    //메인콘텍스트에서 처리한다
                    withContext(Main){
                        Intent(this@PostActivity, PictureActivity::class.java).apply {
                            putExtra(EXTRA_FILEPATH, file.absolutePath)
                            startActivity(this)
                        }
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            
            true
        }

        getPostsInfo()

    }


    /**
     * by 변성욱
     * 투표하고 그 결과를 서버로 보낸다.
     */
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


    /**
     * by 변성욱
     * 현재 열람하려는 게시물을 정보를 서버에서 받아온다.
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public fun getPostsInfo() {

        mCard.pictures.clear()

        mIsBusy = true

        val extra_postId = intent.getLongExtra(EXTRA_POSTID, -1)
        if (extra_postId < 0)
            return

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)

        //게시물 하나의 정보를 얻는다.
        val postInfoRequest = JsonArrayRequest(
            Request.Method.GET, getString(R.string.urlToServer) + "getPost/" + extra_postId.toString(),
            null,
            {
                it?.let { jsonArr ->
                    for (i in 0 until jsonArr.length()) {
                        val obj = jsonArr.getJSONObject(i);
                        val postId = obj.getLong("postId")
                        val fileName = obj.getString("file_name")
                        val filePath = obj.getString("path")
                        val content = obj.getString("content")
                        val writer = obj.getString("writer")
                        val likes = obj.getInt("likes");
                        mCard.postId = postId
                        mCard.content = content
                        mCard.writer = writer
                        mCard.pictures.add(MyPicture(null, fileName, filePath, likes))
                    }

                    for (i in 0 until mCard.pictures.size)
                        mSelected.add(false)

                }


                var cnt = 0
                //파싱한 데이터를 토대로 게시물의 이미지들을 리퀘스트. 받아왔으면 뷰에 셋팅
                for (picture in mCard.pictures) {
                    val url = getString(R.string.urlToServer) + "getImage/" +
                            picture.file_name

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
            {
                Log.e("Volley", it.toString())
            })

        queue.add(postInfoRequest)
    }


}