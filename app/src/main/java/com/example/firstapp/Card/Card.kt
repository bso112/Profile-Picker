package com.example.firstapp.Card

import android.graphics.Bitmap

data class Card(
    var postId: Int,
    var content: String,
    var writer: String,
    var bitmaps: ArrayList<Bitmap>,
    var imageInfo: ArrayList<Pair<String, String>>
);
