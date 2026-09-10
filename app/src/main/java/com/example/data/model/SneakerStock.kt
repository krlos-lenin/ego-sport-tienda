package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sneaker_stocks",
    indices = [Index(value = ["sneakerId", "sizeEu"], unique = true)]
)
data class SneakerStock(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sneakerId: Long,
    val sizeEu: Double, // ej. 38.0, 39.0, 40.0, 41.0, 42.0, 43.0, 44.0, 45.0
    val quantity: Int,
    val minStockAlert: Int = 2
)
