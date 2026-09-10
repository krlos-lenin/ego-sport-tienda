package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.Payment
import com.example.data.model.Sneaker
import com.example.data.model.SneakerWithStock
import com.example.data.repository.SneakerRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SneakerRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = SneakerRepository(
            sneakerDao = database.sneakerDao(),
            orderDao = database.orderDao(),
            paymentDao = database.paymentDao()
        )
    }

    // --- State Streams ---
    val allSneakersWithStock: StateFlow<List<SneakerWithStock>> = repository.sneakersWithStock
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncome: StateFlow<Double?> = repository.totalIncome
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenses: StateFlow<Double?> = repository.totalExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- Catalog Filter State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedBrand = MutableStateFlow("Todas")
    val selectedBrand = _selectedBrand.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todas")
    val selectedCategory = _selectedCategory.asStateFlow()

    val filteredSneakers: StateFlow<List<SneakerWithStock>> = combine(
        allSneakersWithStock,
        _searchQuery,
        _selectedBrand,
        _selectedCategory
    ) { sneakers, query, brand, category ->
        sneakers.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.sneaker.name.contains(query, ignoreCase = true) ||
                item.sneaker.brand.contains(query, ignoreCase = true) ||
                item.sneaker.sku.contains(query, ignoreCase = true) ||
                item.sneaker.colorway.contains(query, ignoreCase = true)

            val matchesBrand = brand == "Todas" || item.sneaker.brand.equals(brand, ignoreCase = true)
            val matchesCat = category == "Todas" || item.sneaker.category.equals(category, ignoreCase = true)

            matchesQuery && matchesBrand && matchesCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Inventory Filter State ---
    private val _inventoryFilter = MutableStateFlow("ALL") // "ALL", "LOW_STOCK", "OUT_OF_STOCK"
    val inventoryFilter = _inventoryFilter.asStateFlow()

    val filteredInventory: StateFlow<List<SneakerWithStock>> = combine(
        allSneakersWithStock,
        _inventoryFilter
    ) { sneakers, filter ->
        when (filter) {
            "LOW_STOCK" -> sneakers.filter { it.hasLowStockAlert && !it.isOutOfStock }
            "OUT_OF_STOCK" -> sneakers.filter { it.isOutOfStock }
            else -> sneakers
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Orders Filter State ---
    private val _orderTypeFilter = MutableStateFlow("ALL") // "ALL", "SALE", "PURCHASE"
    val orderTypeFilter = _orderTypeFilter.asStateFlow()

    val filteredOrders: StateFlow<List<Order>> = combine(
        allOrders,
        _orderTypeFilter
    ) { orders, filter ->
        when (filter) {
            "SALE" -> orders.filter { it.orderType == "SALE" }
            "PURCHASE" -> orders.filter { it.orderType == "PURCHASE" }
            else -> orders
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Payments Filter State ---
    private val _paymentTypeFilter = MutableStateFlow("ALL") // "ALL", "INCOME", "EXPENSE"
    val paymentTypeFilter = _paymentTypeFilter.asStateFlow()

    val filteredPayments: StateFlow<List<Payment>> = combine(
        allPayments,
        _paymentTypeFilter
    ) { payments, filter ->
        when (filter) {
            "INCOME" -> payments.filter { it.paymentType == "INCOME" }
            "EXPENSE" -> payments.filter { it.paymentType == "EXPENSE" }
            else -> payments
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- UI Events / Notifications ---
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Dialog and Sheet states
    private val _selectedSneakerForDetail = MutableStateFlow<SneakerWithStock?>(null)
    val selectedSneakerForDetail = _selectedSneakerForDetail.asStateFlow()

    private val _selectedOrderForReceipt = MutableStateFlow<Order?>(null)
    val selectedOrderForReceipt = _selectedOrderForReceipt.asStateFlow()

    private val _selectedPaymentForReceipt = MutableStateFlow<Payment?>(null)
    val selectedPaymentForReceipt = _selectedPaymentForReceipt.asStateFlow()

    // --- Filter Setters ---
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSelectedBrand(brand: String) { _selectedBrand.value = brand }
    fun setSelectedCategory(cat: String) { _selectedCategory.value = cat }
    fun setInventoryFilter(filter: String) { _inventoryFilter.value = filter }
    fun setOrderTypeFilter(filter: String) { _orderTypeFilter.value = filter }
    fun setPaymentTypeFilter(filter: String) { _paymentTypeFilter.value = filter }

    fun selectSneakerForDetail(sneaker: SneakerWithStock?) {
        _selectedSneakerForDetail.value = sneaker
    }

    fun selectOrderForReceipt(order: Order?) {
        _selectedOrderForReceipt.value = order
    }

    fun selectPaymentForReceipt(payment: Payment?) {
        _selectedPaymentForReceipt.value = payment
    }

    // --- Operations ---
    fun createSaleOrder(
        customerName: String,
        customerPhone: String,
        sneakerId: Long,
        sizeEu: Double,
        quantity: Int,
        unitPrice: Double,
        paymentMethod: String,
        recordPaymentNow: Boolean,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.createSaleOrder(
                customerName = customerName,
                customerPhone = customerPhone,
                sneakerId = sneakerId,
                sizeEu = sizeEu,
                quantity = quantity,
                unitPrice = unitPrice,
                paymentMethod = paymentMethod,
                recordPaymentNow = recordPaymentNow,
                notes = notes
            )
            result.onSuccess {
                val msg = if (recordPaymentNow) {
                    "Pedido de venta completado y pago registrado automáticamente."
                } else {
                    "Pedido de venta registrado (pago pendiente)."
                }
                _userMessage.emit(msg)
                onSuccess()
            }.onFailure { error ->
                _userMessage.emit(error.message ?: "Error al registrar la venta")
            }
        }
    }

    fun createPurchaseOrder(
        supplierName: String,
        supplierContact: String,
        sneakerId: Long,
        sizeEu: Double,
        quantity: Int,
        unitCost: Double,
        paymentMethod: String,
        recordExpenseNow: Boolean,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.createPurchaseOrder(
                supplierName = supplierName,
                supplierContact = supplierContact,
                sneakerId = sneakerId,
                sizeEu = sizeEu,
                quantity = quantity,
                unitCost = unitCost,
                paymentMethod = paymentMethod,
                recordExpenseNow = recordExpenseNow,
                notes = notes
            )
            result.onSuccess {
                val msg = if (recordExpenseNow) {
                    "Compra a proveedor registrada y stock reabastecido (+egreso registrado)."
                } else {
                    "Compra a proveedor registrada e inventario actualizado."
                }
                _userMessage.emit(msg)
                onSuccess()
            }.onFailure { error ->
                _userMessage.emit(error.message ?: "Error al registrar la compra")
            }
        }
    }

    fun markOrderAsPaid(orderId: Long, paymentMethod: String) {
        viewModelScope.launch {
            val result = repository.markOrderAsPaid(orderId, paymentMethod)
            result.onSuccess {
                _userMessage.emit("Pago registrado automáticamente para el pedido.")
            }.onFailure {
                _userMessage.emit("Error al procesar pago: ${it.message}")
            }
        }
    }

    fun updateStock(sneakerId: Long, sizeEu: Double, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateStock(sneakerId, sizeEu, newQuantity)
            _userMessage.emit("Stock actualizado para talla $sizeEu")
        }
    }

    fun addNewSneaker(
        name: String,
        brand: String,
        model: String,
        sku: String,
        colorway: String,
        category: String,
        purchasePrice: Double,
        salePrice: Double,
        primaryColorHex: Long,
        accentColorHex: Long,
        description: String,
        sizesAndStock: Map<Double, Int>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val sneaker = Sneaker(
                name = name,
                brand = brand,
                model = model,
                sku = sku,
                colorway = colorway,
                category = category,
                purchasePrice = purchasePrice,
                salePrice = salePrice,
                primaryColorHex = primaryColorHex,
                accentColorHex = accentColorHex,
                description = description
            )
            repository.addNewSneaker(sneaker, sizesAndStock)
            _userMessage.emit("Modelo '$name' añadido al catálogo con su inventario.")
            onSuccess()
        }
    }

    fun recordManualPayment(
        type: String,
        amount: Double,
        method: String,
        partyName: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.recordManualPayment(type, amount, method, partyName, notes)
            val tipoStr = if (type == "INCOME") "Ingreso" else "Egreso"
            _userMessage.emit("$tipoStr registrado exitosamente en caja.")
            onSuccess()
        }
    }

    suspend fun getItemsForOrder(orderId: Long): List<OrderItem> {
        return repository.getItemsForOrder(orderId)
    }
}
