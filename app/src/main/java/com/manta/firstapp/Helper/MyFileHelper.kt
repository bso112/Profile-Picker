package com.manta.firstapp.Helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object MyFileHelper {
    //외부저장소에 고유한 이름의 빈 임시파일을 생성한다. 저장된 파일은 jvm이 내려갈때 삭제된다.
    @Throws(IOException::class)
    fun createImageTempFile(context: Context): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        /*
        If your app works with media files that provide value to the user only within your app,
        it's best to store them in app-specific directories within external storage,
        as demonstrated in the following code snippet:
        아마 미디어파일은 내부저장소 캐시에 쓰기는 크기때문에 외부저장소에 저장하는듯.
         */

        // Environment.getExternalStoragePublicDirectory() 와는 달리
        // 외부저장소의 앱별 디렉터리(보통 캐시를 저장함)를 말한다. 앱을 지우면 같이 삭제된다.
        // context.externalCacheDir과 다른점은 DIRECTORY_PICTURES 을 통해서 Picture라는 이미지 디렉토리를 추가하는 점이다.
        // 이미지 같은 미디어콘텐츠 저장시에는 이렇게 해야한다.
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        //이렇게 만든 file은 mediaScanner에 자동으로 추가되지 않으므로 galleryAddPic 을 통해 수동으로 추가하라고한다...
        //는데 애초에 미디어스캐너는 외부저장소 공용디렉터리에 접근할때 쓰는거아님?

        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            //jvm이 내려갈때 임시파일을 지운다.
            deleteOnExit()
        }
    }



    //내부 DB에 사진저장
    fun galleryAddPic(context: Context, currentPhotoPath: String) {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            context.sendBroadcast(mediaScanIntent)
        }
    }

    /* Checks if external storage is available for read and write */
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /* Checks if external storage is available to at least read */
    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }
}


