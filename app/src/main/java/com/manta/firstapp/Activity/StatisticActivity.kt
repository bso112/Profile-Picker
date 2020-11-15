package com.manta.firstapp.Activity

import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.Volley
import com.manta.firstapp.Adapter.StatisticPictureAdapter
import com.manta.firstapp.Default.EXTRA_POSTINFO
import com.manta.firstapp.Default.PostInfo
import com.manta.firstapp.R
import kotlinx.android.synthetic.main.activity_statistic.*

/**
 * by 변성욱
 * 게시물의 투표 통계를 보여주는 액티비티
 * intent로 게시물id를 받아서 서버에 게시물정보를 요청한뒤, 가공해서 표시한다.
 */
class  StatisticActivity : AppCompatActivity() {

    private var mPost: PostInfo = PostInfo()
    private lateinit var statisticPictureAdapter: StatisticPictureAdapter

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic)

        mPost = intent.getSerializableExtra(EXTRA_POSTINFO) as? PostInfo ?: return

        mPost.myPictures.reverse();

        //기본적으로 객체는 포인터로 표현되기 때문에 mPost.myPicture는 adpater에 셋팅한 다음에 참조가 바뀌면 안됨.
        statisticPictureAdapter = StatisticPictureAdapter(this, R.layout.statistic_picture_item, mPost.myPictures)
        gv_statistic_picture.adapter = statisticPictureAdapter

        getPostInfo()
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public fun getPostInfo() {

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)

        //파싱한 데이터를 토대로 게시물의 이미지들을 리퀘스트. 받아왔으면 뷰에 셋팅
        for (picture in mPost.myPictures) {
            val url = getString(R.string.urlToServer) + "getImage/" +
                    picture.file_name;


            val imgRequest = ImageRequest(url,
                { bitmap ->
                    picture.bitmap = bitmap
                    statisticPictureAdapter.notifyDataSetChanged()
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
    }
}