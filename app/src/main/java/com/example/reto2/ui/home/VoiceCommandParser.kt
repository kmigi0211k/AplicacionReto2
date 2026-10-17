
package com.example.reto2.ui.home

import java.util.Locale

data class VoiceCommand(
    val operationType: String,
    val baseCurrency: String,
    val targetCurrency: String,
    val amount: Double
)

fun parseVoiceCommand(phrase: String): VoiceCommand? {
    val text = phrase
        .lowercase(Locale.ROOT)
        .replace("us$", " dólares ")
        .replace("$", " dólares ")
        .replace(",", ".")
        .trim()

    val operationType = when {
        Regex("""\b(comprar|compra|compro)\b""")
            .containsMatchIn(text) -> "BUY"

        Regex("""\b(vender|venta|vendo)\b""")
            .containsMatchIn(text) -> "SELL"

        else -> return null
    }

    val currencies = listOf(
        "dólares canadienses" to "CAD",
        "dolares canadienses" to "CAD",
        "dólar canadiense" to "CAD",
        "dolar canadiense" to "CAD",
        "dólares australianos" to "AUD",
        "dolares australianos" to "AUD",
        "dólar australiano" to "AUD",
        "dolar australiano" to "AUD",
        "francos suizos" to "CHF",
        "franco suizo" to "CHF",
        "pesos colombianos" to "COP",
        "peso colombiano" to "COP",
        "pesos mexicanos" to "MXN",
        "peso mexicano" to "MXN",
        "reales brasileños" to "BRL",
        "real brasileño" to "BRL",
        "dólares" to "USD",
        "dolares" to "USD",
        "dólar" to "USD",
        "dolar" to "USD",
        "euros" to "EUR",
        "euro" to "EUR",
        "libras" to "GBP",
        "libra" to "GBP",
        "yenes" to "JPY",
        "yen" to "JPY",
        "usd" to "USD",
        "eur" to "EUR",
        "gbp" to "GBP",
        "jpy" to "JPY",
        "cad" to "CAD",
        "chf" to "CHF",
        "aud" to "AUD",
        "mxn" to "MXN",
        "brl" to "BRL"
    )

    val currency = currencies
        .sortedByDescending { it.first.length }
        .firstOrNull { text.contains(it.first) }
        ?.second
        ?: return null

    val amountText = Regex("""\d+(?:\.\d+)?""")
        .find(text)
        ?.value
        ?: return null

    val amount = amountText.toDoubleOrNull() ?: return null

    if (amount <= 0) return null

    val targetCurrency = "COP"

    if (currency == targetCurrency) return null

    return VoiceCommand(
        operationType = operationType,
        baseCurrency = currency,
        targetCurrency = targetCurrency,
        amount = amount
    )
}
