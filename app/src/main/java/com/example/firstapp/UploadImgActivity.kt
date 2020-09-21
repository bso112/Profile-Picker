package com.example.firstapp

import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.ACTION_IMAGE_CAPTURE
import android.provider.MediaStore.Images
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_upload_img.*
import kotlinx.android.synthetic.main.frag_profile.*
import java.io.File


class UploadImgActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PICK_FROM_ALBUM = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_img)
    }

    override fun onStart() {
        super.onStart()
        btn_cam.setOnClickListener { dispatchTakePictureIntent() }
        btn_gallery.setOnClickListener {dispathGalleryIntent()}
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var picture: Bitmap? = null
        //이미캡쳐 리퀘스트였고, 그 결과가 성공이면
        if (requestCode === REQUEST_IMAGE_CAPTURE && resultCode === AppCompatActivity.RESULT_OK) {
            //데이터에서 번들을 뽑아내고, 번들에서 비트맵을 뽑아내서 iv_profile1에 적용한다.
            val extras: Bundle? = data?.extras
           picture = extras?.get("data") as Bitmap?


        } else if (requestCode == REQUEST_PICK_FROM_ALBUM) {
            val photoUri: Uri? = data?.data
            //데이터베이스 쿼리를 받기 위한 객체. 쿼리 결과에 대한 랜덤액세스를 제공한다.
            var cursor: Cursor? = null

            try {
                //Uri 스키마를 content:/// 에서 file:/// 로  변경한다.
                val proj = arrayOf(Images.Media.DATA)
                //contentResolver를 통해 contentProvider에 쿼리한다. photoUri 에 있는 Images.Media.DATA에 해당하는 데이터를 가져와라.
                cursor = photoUri?.let { contentResolver.query(it, proj, null, null, null) }

                //쿼리결과에서 Images.Media.DATA를 가져올 수 있는 인덱스를 얻는다.
                val column_index: Int = cursor?.getColumnIndexOrThrow(Images.Media.DATA) ?: -1
                //일단 처음으로 옮겨야하는듯? 포인터같은 개념인가.
                cursor?.moveToFirst()

                //파일을 얻는다.
                val file = File(cursor?.getString(column_index))
                //그냥 빈 옵션을 준비
                val options = BitmapFactory.Options()
                //파일을 디코딩해서 비트맵으로 만든다.
                picture = BitmapFactory.decodeFile(file.absolutePath, options)
            } finally {
                cursor?.close()

            }
        }
        //만든 비트맵을 이미지버튼에 셋팅.. ib_profile1은 이 액티비티에 없는데 괜찮나,. 안터지지만 안됨.
        picture?.let {  ib_profile1.setImageBitmap(it) }

        //메인액티비티로 넘어가기
        val intent = Intent(this, MainActivity::class.java).apply {
            //putExtra(FIRSTAPP_USERNAME,account.displayName)
        }
        startActivity(intent)

    }

    private fun dispatchTakePictureIntent() {
        /*
        카메라 기능이 있는 앱을 찾아서 실행시킨다.
        만약, 그런 앱이 없는데  startActivity()를 호출하면 앱이 정지된다.
        따라서 resolveActivity로 그 결과가 null인지 아닌지에 따라서 startActivity를 수행한다.
         */
        Intent(ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            //packageManager가 null이 아니면 수행하라.
            packageManager?.let {
                //resolveActivity 의 결과가 null이 아니면 수행하라.
                takePictureIntent.resolveActivity(it)?.also {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    private fun dispathGalleryIntent() {
        //Activity Action: Pick an item from the data, returning what was selected.
        Intent(ACTION_PICK).let {
            it.setType(Images.Media.CONTENT_TYPE)
            startActivityForResult(it, REQUEST_PICK_FROM_ALBUM)
        }

    }


}