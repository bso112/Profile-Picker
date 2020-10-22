package com.example.firstapp.Default

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

data class UserInfo(
    @SerializedName("email")
    val email : String = "",
    @SerializedName("nickname")
    val nickname : String = "",
    @SerializedName("sex")
    val sex : Int = 0,
    @SerializedName("age")
    val age : Int = 10,
    @SerializedName("categorys")
    val categorys : HashSet<Int> = hashSetOf()) : Serializable

data class Card(
    var postId: Long = 0,
    var title : String = "",
    var content: String = "",
    var writer: String = "",
    var nickname : String = "",
    var pictures : ArrayList<MyPicture> = ArrayList<MyPicture>(),
    var isAd : Boolean = false
) ;


class Post(var tumbnail: Bitmap?, var postInfo: PostInfo) {
    fun getTumbnailPictureName(): String {
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

    fun getLikeSum() : Int
    {
        var likeSum = 0
        for(picture in postInfo.myPictures)
            likeSum += picture.likes
        return likeSum
    }

    fun getTumbnailPicture(): MyPicture? {
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
    var title: String = "", val date: String = "", val viewCnt: Int = 0,
    var postId: Long = 0, var myPictures: ArrayList<MyPicture> = ArrayList(),
    var content: String = "", var writer: String = "", val warn : Int = 0
) : Serializable {

    var mOnInitialized : (() -> Unit)? = null

    constructor(other: PostInfo) : this(
        other.title
        , other.date, other.viewCnt, other.postId, ArrayList<MyPicture>(other.myPictures)
    )



}
