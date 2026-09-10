package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.SneakerWithStock
import com.example.ui.MainViewModel
import com.example.ui.catalog.CatalogScreen
import com.example.ui.inventory.InventoryScreen
import com.example.ui.orders.NewOrderDialog
import com.example.ui.orders.OrdersScreen
import com.example.ui.payments.PaymentsScreen
import com.example.ui.theme.MyApplicationTheme

enum class ScreenTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    CATALOG("Catálogo", Icons.Default.Storefront, Icons.Outlined.Storefront),
    ORDERS("Pedidos", Icons.Default.ReceiptLong, Icons.Outlined.ReceiptLong),
    INVENTORY("Inventario", Icons.Default.Inventory, Icons.Outlined.Inventory),
    PAYMENTS("Caja & Pagos", Icons.Default.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SneakerStoreApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SneakerStoreApp(viewModel: MainViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    var currentTab by remember { mutableIntStateOf(0) }

    // Dialog state for creating orders
    var showNewOrderDialog by remember { mutableStateOf(false) }
    var newOrderInitialSneaker by remember { mutableStateOf<SneakerWithStock?>(null) }
    var newOrderInitialSize by remember { mutableStateOf<Double?>(null) }
    var newOrderInitialType by remember { mutableStateOf("SALE") }

    val allSneakersWithStock by viewModel.allSneakersWithStock.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val lowStockCount = allSneakersWithStock.count { it.hasLowStockAlert }
    val pendingPaymentsCount = allOrders.count { it.paymentStatus != "PAID" }

    // Collect user notification messages
    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                ScreenTab.entries.forEachIndexed { index, tab ->
                    val isSelected = currentTab == index
                    val badgeCount = when (tab) {
                        ScreenTab.INVENTORY -> if (lowStockCount > 0) lowStockCount else null
                        ScreenTab.ORDERS -> if (pendingPaymentsCount > 0) pendingPaymentsCount else null
                        else -> null
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = index },
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}"),
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (badgeCount != null) {
                                        Badge(containerColor = if (tab == ScreenTab.INVENTORY) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) {
                                            Text("$badgeCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            }
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> CatalogScreen(
                    viewModel = viewModel,
                    onOpenNewOrder = { sneaker, size ->
                        newOrderInitialSneaker = sneaker
                        newOrderInitialSize = size
                        newOrderInitialType = "SALE"
                        showNewOrderDialog = true
                    }
                )
                1 -> OrdersScreen(
                    viewModel = viewModel,
                    onOpenNewOrder = {
                        newOrderInitialSneaker = null
                        newOrderInitialSize = null
                        newOrderInitialType = "SALE"
                        showNewOrderDialog = true
                    }
                )
                2 -> InventoryScreen(
                    viewModel = viewModel,
                    onRestockOrderClick = { sneaker ->
                        newOrderInitialSneaker = sneaker
                        newOrderInitialSize = null
                        newOrderInitialType = "PURCHASE"
                        showNewOrderDialog = true
                    }
                )
                3 -> PaymentsScreen(
                    viewModel = viewModel
                )
            }
        }
    }

    // New Order Dialog (Sale or Purchase)
    if (showNewOrderDialog) {
        NewOrderDialog(
            viewModel = viewModel,
            availableSneakers = allSneakersWithStock,
            initialSneaker = newOrderInitialSneaker,
            initialSize = newOrderInitialSize,
            initialType = newOrderInitialType,
            onDismiss = { showNewOrderDialog = false }
        )
    }
}
