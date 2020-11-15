package com.manta.firstapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.manta.firstapp.Helper.showSimpleAlert
import com.manta.firstapp.Default.MyPicture
import com.manta.firstapp.R
import kotlinx.android.synthetic.main.upload_img_item.view.*
import java.util.*

/**
 * by 변성욱
 * UploadImageActivity에서 리사이클러뷰에 연결할 어뎁터
 */
class UploadImgAdapter(private val mPictures: ArrayList<MyPicture>, private var context : Context?) :
   RecyclerView.Adapter<UploadImgAdapter.ViewHolder>() {

    //뷰홀더 외부 데이터에 따라 값이 변하는 뷰들을 모두 갖고있는게 좋음.(캐싱)
    //안그러면 onCreateViewHolder마다 findViewById 해야되니까.
    class ViewHolder(val layout : View, val iv_picture : ImageView, val btn_cancel : ImageButton) : RecyclerView.ViewHolder(layout)

    //getview처럼 매번 inflate하는게 아니라, 화면에 보여질거 몇개 inflate 해두고 재사용.
    //새로 추가된 아이템이 보여야 할때 불린다.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout: View = LayoutInflater.from(parent.context).inflate(R.layout.upload_img_item, parent, false)

        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(layout, layout.iv_upload_picture, layout.btn_upload_cancel)
    }

    //스크롤시 화면을 벗어나있는 아이템이 화면에 보여야할때, 기존에 만들어둔 viewHolder를 가져와 데이터 바인딩후에 화면에 뿌려준다.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        holder.iv_picture.setImageBitmap(mPictures[position].bitmap)
        holder.btn_cancel.setOnClickListener {
            //경고
            showSimpleAlert(context, "경고","선택한 사진의 모든 정보가 삭제됩니다. 지우시겠습니까?",{
                mPictures.removeAt(position)
                notifyDataSetChanged()
            })
        }

    }

    override fun getItemCount(): Int {
        return mPictures.size
    }

    fun onDestroy()
    {
        //UploadImgActivity와 UploadImgAdapter는 상호참조라서 참조 끊어줘야함
        context = null
    }

}