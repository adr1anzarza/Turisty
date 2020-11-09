package com.adrian.zarza.turisty.placeDetail

import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.adrian.zarza.turisty.database.Place

@BindingAdapter("setPlaceTitleText")
fun EditText.setPlaceTitleText(place: Place?) {
    place?.let {
        post {setText(place.titlePlace)}

    }
}

@BindingAdapter("setPlaceDescriptionText")
fun EditText.setPlaceDescriptionText(place: Place?) {
    place?.let {
        setText(place.descriptionPlace)
    }
}

@BindingAdapter("setAddressText")
fun TextView.setPlaceAddressText(place: Place?) {
    place?.let {
        text = place.addressPlace
    }
}