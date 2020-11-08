package com.adrian.zarza.turisty.placeDetail

import android.widget.EditText
import androidx.databinding.BindingAdapter
import com.adrian.zarza.turisty.database.Place

@BindingAdapter("setPlaceTitleText")
fun EditText.setPlaceTitleText(task: Place?) {
    task?.let {
        post {setText(task.titlePlace)}

    }
}

@BindingAdapter("setPlaceDescriptionText")
fun EditText.setPlaceDescriptionText(task: Place?) {
    task?.let {
        setText(task.descriptionPlace)
    }
}