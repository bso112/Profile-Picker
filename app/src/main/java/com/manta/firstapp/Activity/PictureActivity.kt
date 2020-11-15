package com.manta.firstapp.Activity

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.manta.firstapp.Default.EXTRA_FILEPATH
import com.manta.firstapp.R
import kotlinx.android.synthetic.main.activity_picture.*
import java.io.File

/**
 * by 변성욱
 * 게시물의 사진을 길게 눌렀을때, 사진의 원본을 보여주는 액티비티.
 * 게시물에서 표시되는 사진은 어느정도 잘려서 보여지기 때문에 필요한 액티비티이다.
 * 게시물 액티비에서 임시파일로 저장된 비트맵이미지를 불러와 화면에 표시한다.
 */
class PictureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)

        val filePath= intent.getStringExtra(EXTRA_FILEPATH) ?: return
        val file = File(filePath)
        if(file.exists())
        {

            file.inputStream().use {
                val byteArray = it.readBytes()
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                iv_image_picture.setImageBitmap(bitmap)
            }
        }
        else
            Toast.makeText(this, "사진을 로드할 수 없습니다." , Toast.LENGTH_SHORT).show()

    }


}