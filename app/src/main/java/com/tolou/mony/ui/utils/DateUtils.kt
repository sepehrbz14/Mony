package com.tolou.mony.ui.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toDayString(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(this))
}