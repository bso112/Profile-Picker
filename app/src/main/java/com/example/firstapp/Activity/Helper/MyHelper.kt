package com.example.firstapp.Activity.Helper

import android.app.Activity
import android.app.AlertDialog


fun makeSimpleAlert(activity: Activity?, title: String, message: String, onClickOk: () -> Unit, onClickCancle: (() -> Unit)? = null) {
    val alertDialog: AlertDialog? = activity?.let {
        val builder = AlertDialog.Builder(it)
        builder.apply {
            setPositiveButton("확인") { dialog, which ->
                onClickOk()
            }
            setNegativeButton("취소") { dialog, which ->
                if (onClickCancle != null) {
                    onClickCancle()
                }
            }

            setTitle(title)
            setMessage(message)
        }
        builder.create()
    }

    alertDialog?.show()
}
