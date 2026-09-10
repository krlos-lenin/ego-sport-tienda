package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long? = null,
    val orderNumber: String? = null,
    val paymentType: String, // "INCOME" (Ingreso por Venta) o "EXPENSE" (Egreso por Compra)
    val amount: Double,
    val paymentMethod: String, // "Efectivo", "Tarjeta", "Transferencia", "Bizum / Móvil", "QR"
    val receiptNumber: String, // ej. "REC-94821"
    val partyName: String, // Cliente o Proveedor
    val status: String = "CONFIRMED", // "CONFIRMED", "PENDING"
    val notes: String = "",
    val recordedAt: Long = System.currentTimeMillis()
)
