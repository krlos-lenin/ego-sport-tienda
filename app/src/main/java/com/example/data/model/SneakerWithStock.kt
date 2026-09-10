package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class SneakerWithStock(
    @Embedded val sneaker: Sneaker,
    @Relation(
        parentColumn = "id",
        entityColumn = "sneakerId"
    )
    val stocks: List<SneakerStock>
) {
    val totalStock: Int
        get() = stocks.sumOf { it.quantity }

    val hasLowStockAlert: Boolean
        get() = stocks.any { it.quantity in 1..it.minStockAlert } || (totalStock in 1..3)

    val isOutOfStock: Boolean
        get() = totalStock == 0

    fun stockForSize(sizeEu: Double): Int {
        return stocks.firstOrNull { it.sizeEu == sizeEu }?.quantity ?: 0
    }
}
