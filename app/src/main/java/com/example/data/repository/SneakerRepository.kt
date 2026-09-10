package com.example.data.repository

import com.example.data.local.OrderDao
import com.example.data.local.PaymentDao
import com.example.data.local.SneakerDao
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.Payment
import com.example.data.model.Sneaker
import com.example.data.model.SneakerStock
import com.example.data.model.SneakerWithStock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.random.Random

class SneakerRepository(
    private val sneakerDao: SneakerDao,
    private val orderDao: OrderDao,
    private val paymentDao: PaymentDao
) {
    val sneakersWithStock: Flow<List<SneakerWithStock>> = sneakerDao.getAllSneakersWithStock()
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()
    val totalIncome: Flow<Double?> = paymentDao.getTotalIncome()
    val totalExpenses: Flow<Double?> = paymentDao.getTotalExpenses()

    fun getOrderItems(orderId: Long): Flow<List<OrderItem>> = orderDao.getItemsForOrder(orderId)

    suspend fun getOrderById(orderId: Long): Order? = withContext(Dispatchers.IO) {
        orderDao.getOrderByIdSync(orderId)
    }

    suspend fun getItemsForOrder(orderId: Long): List<OrderItem> = withContext(Dispatchers.IO) {
        orderDao.getItemsForOrderSync(orderId)
    }

    suspend fun createSaleOrder(
        customerName: String,
        customerPhone: String,
        sneakerId: Long,
        sizeEu: Double,
        quantity: Int,
        unitPrice: Double,
        paymentMethod: String,
        recordPaymentNow: Boolean,
        notes: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val sneakerWithStock = sneakerDao.getSneakerWithStockSync(sneakerId)
                ?: return@withContext Result.failure(Exception("Zapatilla no encontrada"))

            val currentStock = sneakerDao.getStockForSizeSync(sneakerId, sizeEu)
            val currentQty = currentStock?.quantity ?: 0
            if (currentQty < quantity) {
                return@withContext Result.failure(
                    Exception("Stock insuficiente en talla $sizeEu (disponibles: $currentQty pares)")
                )
            }

            // Deduct stock
            val newStock = currentQty - quantity
            sneakerDao.updateStockQuantity(sneakerId, sizeEu, newStock)

            val orderNumber = "VTA-${System.currentTimeMillis() % 100000}"
            val total = unitPrice * quantity
            val initialPaymentStatus = if (recordPaymentNow) "PAID" else "PENDING"

            val orderId = orderDao.insertOrder(
                Order(
                    orderNumber = orderNumber,
                    orderType = "SALE",
                    partyName = customerName.ifBlank { "Cliente General" },
                    partyContact = customerPhone,
                    totalAmount = total,
                    status = "COMPLETED",
                    paymentStatus = initialPaymentStatus,
                    paymentMethod = paymentMethod,
                    notes = notes,
                    createdAt = System.currentTimeMillis()
                )
            )

            orderDao.insertOrderItems(
                listOf(
                    OrderItem(
                        orderId = orderId,
                        sneakerId = sneakerId,
                        sneakerName = sneakerWithStock.sneaker.name,
                        brand = sneakerWithStock.sneaker.brand,
                        sizeEu = sizeEu,
                        quantity = quantity,
                        unitPrice = unitPrice,
                        subtotal = total
                    )
                )
            )

            // Automatically record payment receipt if requested
            if (recordPaymentNow) {
                val receiptNumber = "REC-${Random.nextInt(10000, 99999)}"
                val paymentId = paymentDao.insertPayment(
                    Payment(
                        orderId = orderId,
                        orderNumber = orderNumber,
                        paymentType = "INCOME",
                        amount = total,
                        paymentMethod = paymentMethod,
                        receiptNumber = receiptNumber,
                        partyName = customerName.ifBlank { "Cliente General" },
                        status = "CONFIRMED",
                        notes = "Pago registrado automáticamente al confirmar venta.",
                        recordedAt = System.currentTimeMillis()
                    )
                )
                // Link payment ID to order
                val existingOrder = orderDao.getOrderByIdSync(orderId)
                if (existingOrder != null) {
                    orderDao.updateOrder(existingOrder.copy(autoRecordedPaymentId = paymentId))
                }
            }

            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPurchaseOrder(
        supplierName: String,
        supplierContact: String,
        sneakerId: Long,
        sizeEu: Double,
        quantity: Int,
        unitCost: Double,
        paymentMethod: String,
        recordExpenseNow: Boolean,
        notes: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val sneakerWithStock = sneakerDao.getSneakerWithStockSync(sneakerId)
                ?: return@withContext Result.failure(Exception("Zapatilla no encontrada"))

            val existingStock = sneakerDao.getStockForSizeSync(sneakerId, sizeEu)
            if (existingStock != null) {
                val newStock = existingStock.quantity + quantity
                sneakerDao.updateStockQuantity(sneakerId, sizeEu, newStock)
            } else {
                sneakerDao.insertStock(
                    SneakerStock(sneakerId = sneakerId, sizeEu = sizeEu, quantity = quantity)
                )
            }

            val orderNumber = "CMP-${System.currentTimeMillis() % 100000}"
            val total = unitCost * quantity
            val initialPaymentStatus = if (recordExpenseNow) "PAID" else "PENDING"

            val orderId = orderDao.insertOrder(
                Order(
                    orderNumber = orderNumber,
                    orderType = "PURCHASE",
                    partyName = supplierName.ifBlank { "Proveedor Mayorista" },
                    partyContact = supplierContact,
                    totalAmount = total,
                    status = "COMPLETED",
                    paymentStatus = initialPaymentStatus,
                    paymentMethod = paymentMethod,
                    notes = notes,
                    createdAt = System.currentTimeMillis()
                )
            )

            orderDao.insertOrderItems(
                listOf(
                    OrderItem(
                        orderId = orderId,
                        sneakerId = sneakerId,
                        sneakerName = sneakerWithStock.sneaker.name,
                        brand = sneakerWithStock.sneaker.brand,
                        sizeEu = sizeEu,
                        quantity = quantity,
                        unitPrice = unitCost,
                        subtotal = total
                    )
                )
            )

            // Automatically record payment expense if paid now
            if (recordExpenseNow) {
                val receiptNumber = "EGR-${Random.nextInt(10000, 99999)}"
                val paymentId = paymentDao.insertPayment(
                    Payment(
                        orderId = orderId,
                        orderNumber = orderNumber,
                        paymentType = "EXPENSE",
                        amount = total,
                        paymentMethod = paymentMethod,
                        receiptNumber = receiptNumber,
                        partyName = supplierName.ifBlank { "Proveedor Mayorista" },
                        status = "CONFIRMED",
                        notes = "Pago a proveedor registrado automáticamente.",
                        recordedAt = System.currentTimeMillis()
                    )
                )
                val existingOrder = orderDao.getOrderByIdSync(orderId)
                if (existingOrder != null) {
                    orderDao.updateOrder(existingOrder.copy(autoRecordedPaymentId = paymentId))
                }
            }

            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markOrderAsPaid(orderId: Long, paymentMethod: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val order = orderDao.getOrderByIdSync(orderId)
                ?: return@withContext Result.failure(Exception("Pedido no encontrado"))

            if (order.paymentStatus == "PAID") {
                return@withContext Result.success(Unit)
            }

            val receiptPrefix = if (order.orderType == "SALE") "REC" else "EGR"
            val receiptNumber = "$receiptPrefix-${Random.nextInt(10000, 99999)}"
            val paymentType = if (order.orderType == "SALE") "INCOME" else "EXPENSE"

            val paymentId = paymentDao.insertPayment(
                Payment(
                    orderId = order.id,
                    orderNumber = order.orderNumber,
                    paymentType = paymentType,
                    amount = order.totalAmount,
                    paymentMethod = paymentMethod,
                    receiptNumber = receiptNumber,
                    partyName = order.partyName,
                    status = "CONFIRMED",
                    notes = "Pago registrado al marcar pedido como pagado.",
                    recordedAt = System.currentTimeMillis()
                )
            )

            orderDao.updateOrder(
                order.copy(
                    paymentStatus = "PAID",
                    paymentMethod = paymentMethod,
                    autoRecordedPaymentId = paymentId
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStock(sneakerId: Long, sizeEu: Double, newQuantity: Int) = withContext(Dispatchers.IO) {
        val existing = sneakerDao.getStockForSizeSync(sneakerId, sizeEu)
        if (existing != null) {
            sneakerDao.updateStockQuantity(sneakerId, sizeEu, newQuantity.coerceAtLeast(0))
        } else {
            sneakerDao.insertStock(
                SneakerStock(sneakerId = sneakerId, sizeEu = sizeEu, quantity = newQuantity.coerceAtLeast(0))
            )
        }
    }

    suspend fun addNewSneaker(
        sneaker: Sneaker,
        sizesAndStock: Map<Double, Int>
    ): Long = withContext(Dispatchers.IO) {
        val sneakerId = sneakerDao.insertSneaker(sneaker)
        val stockEntities = sizesAndStock.map { (size, qty) ->
            SneakerStock(sneakerId = sneakerId, sizeEu = size, quantity = qty)
        }
        sneakerDao.insertStocks(stockEntities)
        sneakerId
    }

    suspend fun recordManualPayment(
        type: String, // "INCOME" or "EXPENSE"
        amount: Double,
        method: String,
        partyName: String,
        notes: String
    ): Long = withContext(Dispatchers.IO) {
        val prefix = if (type == "INCOME") "MAN-ING" else "MAN-EGR"
        paymentDao.insertPayment(
            Payment(
                orderId = null,
                orderNumber = null,
                paymentType = type,
                amount = amount,
                paymentMethod = method,
                receiptNumber = "$prefix-${Random.nextInt(1000, 9999)}",
                partyName = partyName.ifBlank { "Transacción Directa" },
                status = "CONFIRMED",
                notes = notes,
                recordedAt = System.currentTimeMillis()
            )
        )
    }
}
