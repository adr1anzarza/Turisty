package com.adrian.zarza.turisty.place

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.adrian.zarza.turisty.database.Place

@BindingAdapter("descriptionPlaceString")
fun TextView.setDescriptionPlaceString(item: Place?) {
    item?.let {
        text = item.descriptionPlace
    }
}

@BindingAdapter("titlePlaceString")
fun TextView.setTitlePlaceString(item: Place?) {
    item?.let {
        text = item.titlePlace
    }
}

@BindingAdapter("addressPlaceString")
fun TextView.setAddressPlaceString(item: Place?) {
    item?.let {
        text = item.addressPlace
    }
}