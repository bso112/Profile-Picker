package com.example.firstapp.Default

import android.graphics.Bitmap
import java.io.Serializable

data class MyPicture(var bitmap: Bitmap?, val file_name: String, val path: String, val likes: Int) :
    Serializable

data class Card(
    var postId: Int,
    var content: String,
    var writer: String,
    var pictures : ArrayList<MyPicture>
);
