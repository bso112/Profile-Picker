package com.example.firstapp.ViewPage

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.firstapp.R
import kotlinx.android.synthetic.main.frag_profile.*

class ProfileFragment : Fragment() {

    val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return  inflater.inflate(R.layout.frag_profile, container, false)
    }

    override fun onStart() {
        super.onStart()
        ib_profile1.setOnClickListener{ dispatchTakePictureIntent()}

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //이미캡쳐 리퀘스트였고, 그 결과가 성공이면
        if (requestCode === REQUEST_IMAGE_CAPTURE && resultCode === AppCompatActivity.RESULT_OK) {
            //데이터에서 번들을 뽑아내고, 번들에서 비트맵을 뽑아내서 iv_profile1에 적용한다.
            val extras: Bundle? = data?.extras
            val imageBitmap = extras?.get("data") as Bitmap?
            ib_profile1.setImageBitmap(imageBitmap)
        }
    }
    private fun dispatchTakePictureIntent() {
        /*
        카메라 기능이 있는 앱을 찾아서 실행시킨다.
        만약, 그런 앱이 없는데  startActivity()를 호출하면 앱이 정지된다.
        따라서 resolveActivity로 그 결과가 null인지 아닌지에 따라서 startActivity를 수행한다.
         */
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            //packageManager가 null이 아니면 수행하라.
            activity?.packageManager?.let {
                //resolveActivity 의 결과가 null이 아니면 수행하라.
                takePictureIntent.resolveActivity(it)?.also {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }
}