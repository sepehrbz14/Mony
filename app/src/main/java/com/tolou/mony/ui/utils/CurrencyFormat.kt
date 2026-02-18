package com.tolou.mony.ui.utils

import kotlin.math.abs

private const val RIAL_SYMBOL = "Ե"

fun formatRial(value: Long): String {
    val absolute = abs(value)
    val compact = when {
        absolute >= 1_000_000 -> "${absolute / 1_000_000} Million"
        absolute >= 100_000 -> "${absolute / 1_000} Thousand"
        else -> "%,d".format(absolute)
    }
    return "$RIAL_SYMBOL $compact"
}

fun formatSignedRial(value: Long): String {
    val sign = when {
        value > 0 -> "+"
        value < 0 -> "-"
        else -> ""
    }
    return sign + formatRial(value)
}
