package com.example.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SneakerWithStock
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderDialog(
    viewModel: MainViewModel,
    availableSneakers: List<SneakerWithStock>,
    initialSneaker: SneakerWithStock? = null,
    initialSize: Double? = null,
    initialType: String = "SALE", // "SALE" or "PURCHASE"
    onDismiss: () -> Unit
) {
    var orderType by remember { mutableStateOf(initialType) } // "SALE" vs "PURCHASE"

    var selectedSneaker by remember {
        mutableStateOf(initialSneaker ?: availableSneakers.firstOrNull())
    }

    val availableSizes = selectedSneaker?.stocks?.sortedBy { it.sizeEu } ?: emptyList()
    var selectedSize by remember(selectedSneaker) {
        val sizeToUse = initialSize ?: availableSizes.firstOrNull { it.quantity > 0 }?.sizeEu ?: availableSizes.firstOrNull()?.sizeEu ?: 41.0
        mutableDoubleStateOf(sizeToUse)
    }

    var quantity by remember { mutableIntStateOf(1) }
    var unitPrice by remember(selectedSneaker, orderType) {
        val defaultPrice = if (orderType == "SALE") {
            selectedSneaker?.sneaker?.salePrice ?: 100.0
        } else {
            selectedSneaker?.sneaker?.purchasePrice ?: 60.0
        }
        mutableDoubleStateOf(defaultPrice)
    }

    var partyName by remember { mutableStateOf("") }
    var partyContact by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Automatic Payment Recording!
    var autoRecordPayment by remember { mutableStateOf(true) }
    var paymentMethod by remember { mutableStateOf("Tarjeta") }

    val paymentMethods = listOf("Efectivo", "Tarjeta", "Transferencia", "Bizum / Móvil", "Código QR")
    var paymentDropdownExpanded by remember { mutableStateOf(false) }
    var sneakerDropdownExpanded by remember { mutableStateOf(false) }

    val currentStockForSize = selectedSneaker?.stockForSize(selectedSize) ?: 0
    val totalAmount = unitPrice * quantity
    val isStockInsufficient = orderType == "SALE" && quantity > currentStockForSize

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("new_order_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (orderType == "SALE") "Nuevo Pedido de Venta" else "Nueva Compra (Reabastecer)",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("close_order_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selector Order Type: Venta vs Compra
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (orderType == "SALE") MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable {
                                orderType = "SALE"
                                selectedSneaker?.let { unitPrice = it.sneaker.salePrice }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (orderType == "SALE") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Venta (Cliente)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (orderType == "SALE") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (orderType == "PURCHASE") MaterialTheme.colorScheme.secondary
                                else Color.Transparent
                            )
                            .clickable {
                                orderType = "PURCHASE"
                                selectedSneaker?.let { unitPrice = it.sneaker.purchasePrice }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (orderType == "PURCHASE") MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Compra (Proveedor)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (orderType == "PURCHASE") MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sneaker Dropdown Selector
                Text(
                    text = "Seleccionar Modelo de Zapatilla",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = sneakerDropdownExpanded,
                    onExpandedChange = { sneakerDropdownExpanded = !sneakerDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedSneaker?.let { "${it.sneaker.brand} - ${it.sneaker.name}" } ?: "Seleccionar...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sneakerDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("sneaker_select_dropdown"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = sneakerDropdownExpanded,
                        onDismissRequest = { sneakerDropdownExpanded = false }
                    ) {
                        availableSneakers.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "${item.sneaker.brand} - ${item.sneaker.name}",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${item.sneaker.colorway} • Stock total: ${item.totalStock} pares",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedSneaker = item
                                    sneakerDropdownExpanded = false
                                    unitPrice = if (orderType == "SALE") item.sneaker.salePrice else item.sneaker.purchasePrice
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Size Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Talla (EU)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (orderType == "SALE") "Stock disponible: $currentStockForSize pares" else "Existencias actuales: $currentStockForSize pares",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isStockInsufficient) Color(0xFFF43F5E) else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableSizes.forEach { stock ->
                        val isSelected = stock.sizeEu == selectedSize
                        val isOutOfStock = stock.quantity == 0 && orderType == "SALE"

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isOutOfStock -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .clickable { selectedSize = stock.sizeEu }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${stock.sizeEu.toInt()} (${stock.quantity})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quantity & Unit Price Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quantity counter
                    Column(modifier = Modifier.weight(1.1f)) {
                        Text(
                            text = "Cantidad",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Menos")
                            }
                            Text(
                                text = "$quantity",
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .testTag("order_quantity_text"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            IconButton(
                                onClick = { quantity++ },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Más")
                            }
                        }
                    }

                    // Unit Price field
                    Column(modifier = Modifier.weight(1.1f)) {
                        Text(
                            text = if (orderType == "SALE") "Precio Venta Unit." else "Costo Compra Unit.",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = if (unitPrice > 0) String.format("%.2f", unitPrice) else "",
                            onValueChange = {
                                unitPrice = it.toDoubleOrNull() ?: 0.0
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("unit_price_input")
                        )
                    }
                }

                if (isStockInsufficient) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "¡Alerta! Solo hay $currentStockForSize pares disponibles en talla ${selectedSize.toInt()}.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFF43F5E),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Party Name & Contact
                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text(if (orderType == "SALE") "Nombre del Cliente" else "Nombre del Proveedor") },
                    placeholder = { Text(if (orderType == "SALE") "ej. Juan Pérez" else "ej. Distribuidor Central Nike") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("party_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = partyContact,
                    onValueChange = { partyContact = it },
                    label = { Text("Teléfono / Email") },
                    placeholder = { Text("+34 600 000 000") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // AUTOMATIC PAYMENT SECTION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (autoRecordPayment) Color(0xFF10B981).copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Payment,
                                    contentDescription = null,
                                    tint = if (autoRecordPayment) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Registrar Pago Automáticamente",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (orderType == "SALE") "Ingreso registrado al instante en caja" else "Egreso registrado al instante en caja",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = autoRecordPayment,
                                onCheckedChange = { autoRecordPayment = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981)),
                                modifier = Modifier.testTag("auto_payment_switch")
                            )
                        }

                        if (autoRecordPayment) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Método de Pago Recibido:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                paymentMethods.forEach { method ->
                                    val isSelected = paymentMethod == method
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) Color(0xFF10B981)
                                                else MaterialTheme.colorScheme.surface
                                            )
                                            .clickable { paymentMethod = method }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = method,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Total Calculation Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total a ${if (orderType == "SALE") "Cobrar" else "Pagar"}:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$${String.format("%.2f", totalAmount)}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (orderType == "SALE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Confirm Button
                Button(
                    onClick = {
                        val sneaker = selectedSneaker ?: return@Button
                        if (orderType == "SALE") {
                            viewModel.createSaleOrder(
                                customerName = partyName,
                                customerPhone = partyContact,
                                sneakerId = sneaker.sneaker.id,
                                sizeEu = selectedSize,
                                quantity = quantity,
                                unitPrice = unitPrice,
                                paymentMethod = paymentMethod,
                                recordPaymentNow = autoRecordPayment,
                                notes = notes,
                                onSuccess = onDismiss
                            )
                        } else {
                            viewModel.createPurchaseOrder(
                                supplierName = partyName,
                                supplierContact = partyContact,
                                sneakerId = sneaker.sneaker.id,
                                sizeEu = selectedSize,
                                quantity = quantity,
                                unitCost = unitPrice,
                                paymentMethod = paymentMethod,
                                recordExpenseNow = autoRecordPayment,
                                notes = notes,
                                onSuccess = onDismiss
                            )
                        }
                    },
                    enabled = selectedSneaker != null && !isStockInsufficient && totalAmount > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_order_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (orderType == "SALE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (orderType == "SALE") "Completar Venta y Registrar" else "Completar Compra y Reabastecer",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
