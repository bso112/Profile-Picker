package com.example.firstapp.Card

import android.graphics.Bitmap

data class Card(
    val postId: Int,
    val content: String,
    val writer: String,
    val bitmaps: ArrayList<Bitmap?>,
    val imageInfo: ArrayList<Pair<String, String>>
);
