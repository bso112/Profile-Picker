package com.example.firstapp.Adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat.getColor
import com.example.firstapp.R
import kotlinx.android.synthetic.main.post_img_item.view.*

class PostImgAdapter(context: Context, resourceID: Int, bitmaps: ArrayList<Bitmap>) :
    ArrayAdapter<Bitmap>(context, resourceID, bitmaps) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var bitmap = getItem(position)

        val cardView: View =
            LayoutInflater.from(context).inflate(R.layout.post_img_item, parent, false)


        cardView.ib_vote.setOnClickListener {
            if (it.getTag() == R.drawable.ic_baseline_radio_button_checked_24)
            {
                it.setBackgroundResource(R.drawable.ic_baseline_radio_button_unchecked_24)
                it.setTag(R.drawable.ic_baseline_radio_button_unchecked_24)
                cardView.iv_post_image.setColorFilter(getColor(it.context, R.color.Transparent))
            }
            else {
                it.setBackgroundResource(R.drawable.ic_baseline_radio_button_checked_24)
                it.setTag(R.drawable.ic_baseline_radio_button_checked_24)
                cardView.iv_post_image.setColorFilter(getColor(it.context, R.color.Black_alpha))

            }
        }
        bitmap?.let { cardView.iv_post_image.setImageBitmap(it) }
        return cardView
    }
}