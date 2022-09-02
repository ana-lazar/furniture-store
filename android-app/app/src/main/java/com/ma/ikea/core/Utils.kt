package com.ma.ikea

import android.app.Activity
import android.app.AlertDialog
import android.text.Editable
import android.util.Log
import okhttp3.ResponseBody

import retrofit2.Converter

import retrofit2.Retrofit




fun Any.logd(message: Any?="no message") {
    Log.d(this.javaClass.simpleName, message.toString())
}

fun showAlert(activity: Activity, title: String, message: String) {
    activity.let {
        val builder = AlertDialog.Builder(it)
        builder.apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("ok", null)
        }
        builder.create()
        builder.show()
    }
}

fun isIntegerValid(value: String): Boolean {
    return value.matches("^-?\\d+$".toRegex())
}

fun isStringValid(text: String): Boolean {
    return text.isNotBlank()
}
