package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sneakers")
data class Sneaker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val brand: String,
    val model: String,
    val sku: String,
    val colorway: String,
    val category: String, // Lifestyle, Basketball, Running, Skate
    val purchasePrice: Double, // Costo de compra al proveedor
    val salePrice: Double, // Precio de venta al cliente
    val primaryColorHex: Long, // Color primario para la ilustración
    val accentColorHex: Long,  // Color secundario/detalle
    val description: String,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
