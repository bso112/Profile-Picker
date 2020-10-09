package com.example.firstapp.Default

import android.graphics.Bitmap
import java.io.Serializable


data class Card(
    var postId: Int,
    var title : String,
    var content: String,
    var writer: String,
    var pictures : ArrayList<MyPicture>
);


class Post(var tumbnail: Bitmap?, var postInfo: PostInfo) {
    public fun getTumbnailPictureName(): String {
        var result = ""
        var maxLikes = -1
        for (picture in postInfo.myPictures) {
            if (maxLikes < picture.likes) {
                result = picture.file_name
                maxLikes = picture.likes
            }
        }
        return result
    }

    public fun getTumbnailPicture(): MyPicture? {
        var result : MyPicture? = null
        var maxLikes = -1
        for (picture in postInfo.myPictures) {
            if (maxLikes < picture.likes) {
                result = picture
                maxLikes = picture.likes
            }
        }
        return result
    }

}

data class MyPicture(var bitmap: Bitmap?, val file_name: String, val path: String, val likes: Int) :
    Serializable

//Picture를 포함하니 Picture도 Serializable이여야한다.
data class PostInfo(
    val title: String = "", val date: String = "", val viewCnt: Int = 0,
    val postId: Int = 0, var myPictures: ArrayList<MyPicture> = ArrayList()
) : Serializable {

    constructor(other: PostInfo) : this(
        other.title
        , other.date, other.viewCnt, other.postId, ArrayList<MyPicture>(other.myPictures)
    )
}
