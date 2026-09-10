package com.example.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SneakerWithStock
import com.example.ui.MainViewModel
import com.example.ui.components.SneakerVisual

@Composable
fun InventoryScreen(
    viewModel: MainViewModel,
    onRestockOrderClick: (sneaker: SneakerWithStock) -> Unit,
    modifier: Modifier = Modifier
) {
    val allSneakersWithStock by viewModel.allSneakersWithStock.collectAsState()
    val filteredInventory by viewModel.filteredInventory.collectAsState()
    val inventoryFilter by viewModel.inventoryFilter.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var sneakerToAdjust by remember { mutableStateOf<SneakerWithStock?>(null) }

    // Metrics
    val totalPairs = allSneakersWithStock.sumOf { it.totalStock }
    val totalValue = allSneakersWithStock.sumOf { item -> item.totalStock * item.sneaker.purchasePrice }
    val lowStockCount = allSneakersWithStock.count { it.hasLowStockAlert && !it.isOutOfStock }
    val outOfStockCount = allSneakersWithStock.count { it.isOutOfStock }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Gestión de Inventario",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Control de existencias por modelo y talla",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_new_sneaker_btn"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo Modelo", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }

            // Summary Metric Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Stock total
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Pares Totales",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$totalPairs pares",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Valoración de inventario
                Card(
                    modifier = Modifier.weight(1.2f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Valor Total Costo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$${String.format("%.2f", totalValue)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = Color(0xFF10B981)
                        )
                    }
                }

                // Alertas
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (lowStockCount > 0 || outOfStockCount > 0) Color(0xFFFF9800).copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Alertas Stock",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${lowStockCount + outOfStockCount} avisos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = if (lowStockCount > 0 || outOfStockCount > 0) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ElevatedFilterChip(
                    selected = inventoryFilter == "ALL",
                    onClick = { viewModel.setInventoryFilter("ALL") },
                    label = { Text("Todos (${allSneakersWithStock.size})") },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                ElevatedFilterChip(
                    selected = inventoryFilter == "LOW_STOCK",
                    onClick = { viewModel.setInventoryFilter("LOW_STOCK") },
                    label = { Text("Bajo Stock ($lowStockCount)") },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = Color(0xFFFF9800),
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                ElevatedFilterChip(
                    selected = inventoryFilter == "OUT_OF_STOCK",
                    onClick = { viewModel.setInventoryFilter("OUT_OF_STOCK") },
                    label = { Text("Sin Stock ($outOfStockCount)") },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = Color(0xFFF43F5E),
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Inventory List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("inventory_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredInventory, key = { it.sneaker.id }) { item ->
                    InventoryItemCard(
                        item = item,
                        onAdjustClick = { sneakerToAdjust = item },
                        onRestockClick = { onRestockOrderClick(item) }
                    )
                }
            }
        }
    }

    // Add Sneaker Dialog
    if (showAddDialog) {
        AddSneakerDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }

    // Adjust Stock Dialog
    sneakerToAdjust?.let { item ->
        StockAdjustDialog(
            sneakerWithStock = item,
            viewModel = viewModel,
            onDismiss = { sneakerToAdjust = null }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InventoryItemCard(
    item: SneakerWithStock,
    onAdjustClick: () -> Unit,
    onRestockClick: () -> Unit
) {
    val sneaker = item.sneaker
    val totalStock = item.totalStock

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .testTag("inventory_card_${sneaker.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini visual
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF18181B)),
                    contentAlignment = Alignment.Center
                ) {
                    SneakerVisual(
                        primaryColor = Color(sneaker.primaryColorHex),
                        accentColor = Color(sneaker.accentColorHex),
                        size = 60.dp,
                        showContainer = false
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = sneaker.brand.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SKU: ${sneaker.sku}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = sneaker.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = sneaker.colorway,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Total Stock Badge
                val stockColor = when {
                    totalStock == 0 -> Color(0xFFF43F5E)
                    totalStock <= 3 -> Color(0xFFFF9800)
                    else -> Color(0xFF10B981)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(stockColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalStock",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = stockColor
                        )
                        Text(
                            text = "pares",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = stockColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Breakdown by size
            Text(
                text = "Desglose por Talla:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item.stocks.sortedBy { it.sizeEu }.forEach { stock ->
                    val isZero = stock.quantity == 0
                    val isLow = stock.quantity in 1..stock.minStockAlert

                    val pillBg = when {
                        isZero -> Color(0xFFF43F5E).copy(alpha = 0.15f)
                        isLow -> Color(0xFFFF9800).copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    val pillTextColor = when {
                        isZero -> Color(0xFFF43F5E)
                        isLow -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(pillBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "T${stock.sizeEu.toInt()}: ${stock.quantity}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isZero || isLow) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            ),
                            color = pillTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions: Ajustar Stock & Reabastecer (Comprar a proveedor)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAdjustClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajustar Stock", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = onRestockClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reabastecer", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
