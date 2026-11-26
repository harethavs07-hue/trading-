package com.example.ui.components

import com.example.model.CurrencyMode
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

fun formatCurrency(amount: Double, mode: CurrencyMode): String {
    val absVal = abs(amount).toLong()
    val sign = if (amount < 0) "-" else ""
    return when (mode) {
        CurrencyMode.INR -> {
            val nf = NumberFormat.getNumberInstance(Locale("en", "IN"))
            "${sign}₹${nf.format(absVal)}"
        }
        CurrencyMode.USD -> {
            val nf = NumberFormat.getNumberInstance(Locale.US)
            "${sign}$${nf.format(absVal)}"
        }
    }
}

fun formatSignedDelta(amount: Double, mode: CurrencyMode): String {
    val prefix = if (amount >= 0) "+" else "-"
    val absVal = abs(amount).toLong()
    return when (mode) {
        CurrencyMode.INR -> {
            val nf = NumberFormat.getNumberInstance(Locale("en", "IN"))
            "${prefix}₹${nf.format(absVal)}"
        }
        CurrencyMode.USD -> {
            val nf = NumberFormat.getNumberInstance(Locale.US)
            "${prefix}$${nf.format(absVal)}"
        }
    }
}

fun formatAssetPrice(price: Float, asset: String): String {
    return when {
        asset == "EUR/USD" -> String.format(Locale.US, "%.4f", price)
        price >= 1000f -> String.format(Locale.US, "%,.0f", price)
        else -> String.format(Locale.US, "%.2f", price)
    }
}
