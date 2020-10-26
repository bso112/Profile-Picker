package com.manta.firstapp.Activity

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.manta.firstapp.Default.EXTRA_FILEPATH
import com.manta.firstapp.R
import kotlinx.android.synthetic.main.activity_picture.*
import java.io.File

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