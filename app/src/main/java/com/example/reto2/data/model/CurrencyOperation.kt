
package com.example.reto2.data.model

data class CurrencyOperation(
    val id: String = "",
    val userId: String = "",
    val type: String = "",
    val baseCurrency: String = "USD",
    val targetCurrency: String = "COP",
    val amount: Double = 0.0,
    val referenceRate: Double = 0.0,
    val appliedRate: Double = 0.0,
    val total: Double = 0.0,
    val status: String = "PENDING_AUDIT",
    val createdAt: Long = 0L,
    val approvedAt: Long = 0L
)
