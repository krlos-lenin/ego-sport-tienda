package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String, // ej. "VTA-2026-001" o "CMP-2026-001"
    val orderType: String, // "SALE" (Venta a cliente) o "PURCHASE" (Compra a proveedor)
    val partyName: String, // Nombre del cliente o proveedor
    val partyContact: String, // Teléfono o email
    val totalAmount: Double,
    val status: String, // "COMPLETED", "PENDING", "CANCELLED"
    val paymentStatus: String, // "PAID", "PENDING", "PARTIAL"
    val paymentMethod: String, // "Efectivo", "Tarjeta", "Transferencia", "Bizum / Móvil", "QR"
    val autoRecordedPaymentId: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "order_items")
data class OrderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val sneakerId: Long,
    val sneakerName: String,
    val brand: String,
    val sizeEu: Double,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)
