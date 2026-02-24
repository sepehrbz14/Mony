package com.tolou.mony.ui.utils

import kotlin.math.abs

private const val RIAL_SYMBOL = "Ե"

fun formatRial(value: Long): String {
    val absolute = abs(value)
    val compact = compactScale(absolute) ?: "%,d".format(absolute)
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

private fun compactScale(absolute: Long): String? {
    if (absolute == 0L) return null

    val billion = compactWithOptionalSingleDecimal(absolute, 1_000_000_000L, "Billion")
    if (billion != null) return billion

    val million = compactWithOptionalSingleDecimal(absolute, 1_000_000L, "Million")
    if (million != null) return million

    if (absolute % 1_000L == 0L) {
        val thousands = absolute / 1_000L
        if (thousands in 1..999) return "$thousands Thousand"
    }

    return null
}

private fun compactWithOptionalSingleDecimal(absolute: Long, unit: Long, label: String): String? {
    if (absolute % unit == 0L) {
        val whole = absolute / unit
        if (whole in 1..99) return "$whole $label"
        return null
    }

    val tenthUnit = unit / 10L
    if (absolute % tenthUnit == 0L) {
        val tenthScaled = absolute / tenthUnit
        if (tenthScaled in 10..99) {
            val whole = tenthScaled / 10
            val decimal = tenthScaled % 10
            return "$whole.$decimal $label"
        }
    }
    return null
}
