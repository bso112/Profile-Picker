package com.example.firstapp

import android.Manifest
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore.ACTION_IMAGE_CAPTURE
import android.provider.MediaStore.Images
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_upload_img.*
import java.io.*

//업로드가 끝나면 액티비티도 끝낸다.
class UploadImgActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PICK_FROM_ALBUM = 2
    private val REQUEST_PERMISSIONS = 3
    private val pictureArray : ArrayList<Bitmap> = ArrayList<Bitmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_img)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()
        btn_post_addPicture.setOnClickListener { dispathGalleryIntent() }
        tv_post_save.setOnClickListener{ uploadPostToServer() }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != AppCompatActivity.RESULT_OK)
            return;

        var picture : Bitmap? = null
        //이미캡쳐 리퀘스트였고, 그 결과가 성공이면
        if (requestCode === REQUEST_IMAGE_CAPTURE) {
            //데이터에서 번들을 뽑아내고, 번들에서 비트맵을 뽑아내서 iv_profile1에 적용한다.
            val extras: Bundle? = data?.extras
            picture = extras?.get("data") as Bitmap

            //  mPicture = getBitmapFromDataTest(data)
        } else if (requestCode == REQUEST_PICK_FROM_ALBUM) {
            picture = getBitmapFromData(data)
        }
        
        picture?.let {
            pictureArray.add(picture)
            //업로드할 사진을 표시0
            iv_post_picture.setImageBitmap(picture)
        }

    }

    private fun uploadPostToServer() {

        //업로드 진행 UI 띄우고싶은데..
        val volleyMultipartRequest: VolleyMultipartRequest = object : VolleyMultipartRequest(
            Method.POST, getString(R.string.urlToServer) + "writePost/",
            Response.Listener { response ->
                val obj = String(response.data)
                Toast.makeText(applicationContext, obj, Toast.LENGTH_SHORT)
                    .show()

                finish()
            },
            Response.ErrorListener { error ->
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_LONG).show()
                Log.e("GotError", "" + error.message)
            }) //여기까지가 object가 구현하는 VolleyMultipartRequest
        //여기서부터는 익명클래스 정의부분
        {
            override fun getByteData(): Map<String, DataPart> {
                val params: HashMap<String, DataPart> = HashMap()
                
                //모든 사진을 바이트배열로 변환해서 바디에 쓴다.
                //key는 html form뷰의 name 항목. 즉, 파라미터가 되는듯
                for(picture in pictureArray)
                {
                    //이미지 이름은 고유해야함
                    val imagename = System.currentTimeMillis()
                    params.put("image", DataPart(imagename.toString(), getFileDataFromDrawable(picture)))
                }

                return params
            }

            override fun getParams(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()

                params.put("content", tv_post_content.text.toString())
                LoginActivity.mAccount?.email?.let {
                    params.put("email", it)
                }
                return params
            }


        }


        //adding the request to volley
        val requestQ = Volley.newRequestQueue(this)
        requestQ.add(volleyMultipartRequest)

    }

    fun getFileDataFromDrawable(bitmap: Bitmap): ByteArray? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var isDenied: Boolean = false;

        for (result in grantResults) {
            if (result == PackageManager.PERMISSION_DENIED)
                isDenied = true
        }

        //권한이 거부되었을때
        if (isDenied) {

        } else
            PickPictureFromGallay()

    }

    private fun PickPictureFromGallay() {
        //이미 퍼미션을 받았으면 바로 그냥 갤러리로
        Intent(ACTION_PICK).let {
            it.setType(Images.Media.CONTENT_TYPE)
            startActivityForResult(it, REQUEST_PICK_FROM_ALBUM)
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun dispathGalleryIntent() {
        //외부 스토리지 쓰기, 읽기 퍼미션이 있는지 먼저 확인한다.
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            //이미 퍼미션을 받았으면 바로 그냥 갤러리로
            PickPictureFromGallay()

        } else {
            //만약 교육용 UI를 표시해야한다면 (사용자가 퍼미션을 거부한적이 있으면)
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
            ) {
                //교육용 UI보여주기
                Toast.makeText(applicationContext, "권한이 필요합니다", Toast.LENGTH_SHORT).show()
            }

            //퍼미션 요청
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), REQUEST_PERMISSIONS
            )

        }

    }

    fun getByteArray(data: String): ByteArray? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        //DataOutputStream은 ByteArrayOutputStream에 데이터를 쓰기위한 함수를 제공하는 클래스인듯.
        val out = DataOutputStream(byteArrayOutputStream)
        try {
            out.write(data.toByteArray())
            byteArrayOutputStream.flush()
            byteArrayOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return byteArrayOutputStream.toByteArray()
    }

    private fun getBitmapFromDataTest(data: Intent?): Bitmap? {
        var bm: Bitmap? = null
        try {
            val inputStream = contentResolver.openInputStream(data!!.data!!)
            bm = BitmapFactory.decodeStream(inputStream)
            inputStream!!.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bm;
    }

    private fun getBitmapFromData(data: Intent?): Bitmap? {
        val photoUri: Uri? = data?.data
        //데이터베이스 쿼리를 받기 위한 객체. 쿼리 결과에 대한 랜덤액세스를 제공한다.
        var cursor: Cursor? = null


        //Uri 스키마를 content:/// 에서 file:/// 로  변경한다.
        val proj = arrayOf(Images.Media.DATA)
        //contentResolver를 통해 contentProvider에 쿼리한다. photoUri 에 있는 Images.Media.DATA에 해당하는 데이터를 가져와라.
        cursor = photoUri?.let { contentResolver.query(it, proj, null, null, null) }

        //cursor은 Closeable 을 구현하기 때문에 use를 쓸수있다.
        //익셉션이 발생하건 말건 마지막에 close(리소스반환)한다. 자바의 try-with-resource와 유사하다.
        cursor?.use {
            //받아온 데이터가 있으면
            if (it.moveToFirst()) {
                //쿼리결과에서 Images.Media.DATA를 가져올 수 있는 인덱스를 얻는다.
                val column_index: Int = it?.getColumnIndexOrThrow(Images.Media.DATA) ?: -1

                //파일을 얻는다.
                val file = File(it?.getString(column_index))
                //그냥 빈 옵션을 준비
                val options = BitmapFactory.Options()
                //파일을 디코딩해서 비트맵으로 만든다.
                return BitmapFactory.decodeFile(file.absolutePath, options)
            }
        }

        return null
    }


}