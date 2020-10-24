package com.example.firstapp.Helper

import android.app.AlertDialog
import android.content.Context
import com.example.firstapp.R

class GlobalHelper private constructor(val context: Context) {

    companion object {

        private var INSTANCE: GlobalHelper? = null

        fun getInstance(context: Context): GlobalHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE = GlobalHelper(context)
                return INSTANCE as GlobalHelper
            }
    }

    val mCategory = listOf(
        context.getString(R.string.category0), context.getString(R.string.category1), context.getString(R.string.category2),
        context.getString(R.string.category3), context.getString(R.string.category4), context.getString(R.string.category5),
        context.getString(R.string.category_end))

    val mSex = listOf(context.getString(R.string.male), context.getString(R.string.female))


}

fun showAlertWithJustOkButton(context: Context?, title: String?, message: String, okButtonText: String, onClickOk: () -> Unit) {
    val alertDialog: AlertDialog? = context?.let { context ->
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setPositiveButton(okButtonText) { dialog, which ->
                onClickOk()
            }

            title?.let { setTitle(it) }
            setMessage(message)
        }
        builder.create()
    }
    alertDialog?.show()
}

fun showSimpleAlert(context: Context?, title: String?, message: String, onClickOk: () -> Unit, onClickCancle: (() -> Unit)? = null) {
    val alertDialog: AlertDialog? = context?.let { context ->
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setPositiveButton("확인") { dialog, which ->
                onClickOk()
            }
            setNegativeButton("취소") { dialog, which ->
                if (onClickCancle != null) {
                    onClickCancle()
                }
            }

            title?.let { setTitle(it) }
            setMessage(message)
        }
        builder.create()
    }

    alertDialog?.show()
}
