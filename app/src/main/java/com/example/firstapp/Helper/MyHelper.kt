package com.example.firstapp.Helper

import android.app.AlertDialog
import android.content.Context


fun showSimpleAlert(context: Context?, title: String?, message: String, onClickOk: () -> Unit, onClickCancle: (() -> Unit)? = null) {
    val alertDialog: AlertDialog? = context?.let {context ->
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
