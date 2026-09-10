package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.Payment
import com.example.data.model.Sneaker
import com.example.data.model.SneakerStock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Sneaker::class,
        SneakerStock::class,
        Order::class,
        OrderItem::class,
        Payment::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sneakerDao(): SneakerDao
    abstract fun orderDao(): OrderDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sneaker_shop_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val sneakerDao = database.sneakerDao()
            val orderDao = database.orderDao()
            val paymentDao = database.paymentDao()

            if (sneakerDao.getSneakersCount() > 0) return

            // 1. Sneakers
            val s1Id = sneakerDao.insertSneaker(
                Sneaker(
                    name = "Air Jordan 1 High OG",
                    brand = "Jordan",
                    model = "Retro High",
                    sku = "DZ5485-612",
                    colorway = "Chicago / Lost & Found",
                    category = "Basketball",
                    purchasePrice = 110.0,
                    salePrice = 180.0,
                    primaryColorHex = 0xFFC62828, // Crimson Red
                    accentColorHex = 0xFF0F172A,  // Black
                    description = "La silueta legendaria que revolucionó la cultura sneaker en 1985. Cuero premium y estética vintage insuperable."
                )
            )
            sneakerDao.insertStocks(
                listOf(
                    SneakerStock(sneakerId = s1Id, sizeEu = 40.0, quantity = 3),
                    SneakerStock(sneakerId = s1Id, sizeEu = 41.0, quantity = 4),
                    SneakerStock(sneakerId = s1Id, sizeEu = 42.0, quantity = 2),
                    SneakerStock(sneakerId = s1Id, sizeEu = 43.0, quantity = 5),
                    SneakerStock(sneakerId = s1Id, sizeEu = 44.0, quantity = 1, minStockAlert = 2)
                )
            )

            val s2Id = sneakerDao.insertSneaker(
                Sneaker(
                    name = "Nike Dunk Low Retro",
                    brand = "Nike",
                    model = "Dunk Low",
                    sku = "DD1391-100",
                    colorway = "Panda Black/White",
                    category = "Lifestyle",
                    purchasePrice = 65.0,
                    salePrice = 115.0,
                    primaryColorHex = 0xFF18181B, // Black
                    accentColorHex = 0xFFFFFFFF,  // White
                    description = "El clásico monocromático más popular y versátil del streetwear actual. Suela de goma y amortiguación ligera."
                )
            )
            sneakerDao.insertStocks(
                listOf(
                    SneakerStock(sneakerId = s2Id, sizeEu = 39.0, quantity = 2),
                    SneakerStock(sneakerId = s2Id, sizeEu = 40.0, quantity = 5),
                    SneakerStock(sneakerId = s2Id, sizeEu = 41.0, quantity = 6),
                    SneakerStock(sneakerId = s2Id, sizeEu = 42.0, quantity = 8),
                    SneakerStock(sneakerId = s2Id, sizeEu = 43.0, quantity = 4),
                    SneakerStock(sneakerId = s2Id, sizeEu = 44.0, quantity = 3)
                )
            )

            val s3Id = sneakerDao.insertSneaker(
                Sneaker(
                    name = "Adidas Samba OG",
                    brand = "Adidas",
                    model = "Samba Classic",
                    sku = "B75806",
                    colorway = "Cloud White / Core Black / Gum",
                    category = "Lifestyle",
                    purchasePrice = 55.0,
                    salePrice = 100.0,
                    primaryColorHex = 0xFFF1F5F9, // Soft White
                    accentColorHex = 0xFF0284C7,  // Blue/Black accent
                    description = "Nacidas en los campos de fútbol en los años 50, hoy son un referente atemporal en la moda urbana internacional."
                )
            )
            sneakerDao.insertStocks(
                listOf(
                    SneakerStock(sneakerId = s3Id, sizeEu = 38.0, quantity = 3),
                    SneakerStock(sneakerId = s3Id, sizeEu = 39.0, quantity = 4),
                    SneakerStock(sneakerId = s3Id, sizeEu = 40.0, quantity = 5),
                    SneakerStock(sneakerId = s3Id, sizeEu = 41.0, quantity = 2),
                    SneakerStock(sneakerId = s3Id, sizeEu = 42.0, quantity = 4)
                )
            )

            val s4Id = sneakerDao.insertSneaker(
                Sneaker(
                    name = "New Balance 550",
                    brand = "New Balance",
                    model = "BB550",
                    sku = "BB550WT1",
                    colorway = "White / Forest Green",
                    category = "Basketball",
                    purchasePrice = 70.0,
                    salePrice = 120.0,
                    primaryColorHex = 0xFF166534, // Dark Green
                    accentColorHex = 0xFFFFFFFF,  // White
                    description = "Homenaje al calzado de básquet profesional de 1989. Silueta aerodinámica y cuero resistente con acentos verdes."
                )
            )
            sneakerDao.insertStocks(
                listOf(
                    SneakerStock(sneakerId = s4Id, sizeEu = 41.0, quantity = 3),
                    SneakerStock(sneakerId = s4Id, sizeEu = 42.0, quantity = 4),
                    SneakerStock(sneakerId = s4Id, sizeEu = 43.0, quantity = 3),
                    SneakerStock(sneakerId = s4Id, sizeEu = 44.0, quantity = 1, minStockAlert = 2),
                    SneakerStock(sneakerId = s4Id, sizeEu = 45.0, quantity = 2)
                )
            )

            val s5Id = sneakerDao.insertSneaker(
                Sneaker(
                    name = "Asics Gel-Kayano 14",
                    brand = "Asics",
                    model = "Gel-Kayano 14",
                    sku = "1201A019-108",
                    colorway = "Cream / Metallic Silver / Blue",
                    category = "Running",
                    purchasePrice = 95.0,
                    salePrice = 160.0,
                    primaryColorHex = 0xFF64748B, // Slate Silver
                    accentColorHex = 0xFF0284C7,  // Sky Blue
                    description = "Estética runner de los años 2000 renovada con materiales técnicos y amortiguación GEL original en toda la entresuela."
                )
            )
            sneakerDao.insertStocks(
                listOf(
                    SneakerStock(sneakerId = s5Id, sizeEu = 40.0, quantity = 2),
                    SneakerStock(sneakerId = s5Id, sizeEu = 41.0, quantity = 3),
                    SneakerStock(sneakerId = s5Id, sizeEu = 42.0, quantity = 5),
                    SneakerStock(sneakerId = s5Id, sizeEu = 43.0, quantity = 2)
                )
            )

            val s6Id = sneakerDao.insertSneaker(
                Sneaker(
                    name = "Puma Suede Classic XXI",
                    brand = "Puma",
                    model = "Suede Classic",
                    sku = "374915-01",
                    colorway = "Puma Black / White",
                    category = "Skate",
                    purchasePrice = 45.0,
                    salePrice = 75.0,
                    primaryColorHex = 0xFF0F172A, // Black
                    accentColorHex = 0xFFEA580C,  // Orange
                    description = "Zapatilla legendaria en gamuza suave que ha marcado generaciones desde 1968 en el breakdance y el skate."
                )
            )
            sneakerDao.insertStocks(
                listOf(
                    SneakerStock(sneakerId = s6Id, sizeEu = 39.0, quantity = 4),
                    SneakerStock(sneakerId = s6Id, sizeEu = 40.0, quantity = 4),
                    SneakerStock(sneakerId = s6Id, sizeEu = 41.0, quantity = 3),
                    SneakerStock(sneakerId = s6Id, sizeEu = 42.0, quantity = 6)
                )
            )

            // 2. Initial Sample Orders with Auto-Recorded Payments
            val now = System.currentTimeMillis()
            val dayMs = 86400000L

            // Sale 1
            val saleOrder1Id = orderDao.insertOrder(
                Order(
                    orderNumber = "VTA-2026-001",
                    orderType = "SALE",
                    partyName = "Carlos Mendoza",
                    partyContact = "+34 612 345 678",
                    totalAmount = 115.0,
                    status = "COMPLETED",
                    paymentStatus = "PAID",
                    paymentMethod = "Tarjeta",
                    notes = "Entregado en tienda física.",
                    createdAt = now - dayMs * 2
                )
            )
            orderDao.insertOrderItems(
                listOf(
                    OrderItem(
                        orderId = saleOrder1Id,
                        sneakerId = s2Id,
                        sneakerName = "Nike Dunk Low Retro",
                        brand = "Nike",
                        sizeEu = 42.0,
                        quantity = 1,
                        unitPrice = 115.0,
                        subtotal = 115.0
                    )
                )
            )
            paymentDao.insertPayment(
                Payment(
                    orderId = saleOrder1Id,
                    orderNumber = "VTA-2026-001",
                    paymentType = "INCOME",
                    amount = 115.0,
                    paymentMethod = "Tarjeta",
                    receiptNumber = "REC-10491",
                    partyName = "Carlos Mendoza",
                    status = "CONFIRMED",
                    notes = "Cobro procesado vía TPV automático.",
                    recordedAt = now - dayMs * 2
                )
            )

            // Purchase 1 (Reabastecimiento de stock a proveedor)
            val purchaseOrder1Id = orderDao.insertOrder(
                Order(
                    orderNumber = "CMP-2026-001",
                    orderType = "PURCHASE",
                    partyName = "Distribuidor Oficial Nike Iberia",
                    partyContact = "pedidos@nike-dist.es",
                    totalAmount = 550.0,
                    status = "COMPLETED",
                    paymentStatus = "PAID",
                    paymentMethod = "Transferencia",
                    notes = "Reabastecimiento lote 5 pares Jordan 1",
                    createdAt = now - dayMs * 4
                )
            )
            orderDao.insertOrderItems(
                listOf(
                    OrderItem(
                        orderId = purchaseOrder1Id,
                        sneakerId = s1Id,
                        sneakerName = "Air Jordan 1 High OG",
                        brand = "Jordan",
                        sizeEu = 43.0,
                        quantity = 5,
                        unitPrice = 110.0,
                        subtotal = 550.0
                    )
                )
            )
            paymentDao.insertPayment(
                Payment(
                    orderId = purchaseOrder1Id,
                    orderNumber = "CMP-2026-001",
                    paymentType = "EXPENSE",
                    amount = 550.0,
                    paymentMethod = "Transferencia",
                    receiptNumber = "REC-10492",
                    partyName = "Distribuidor Oficial Nike Iberia",
                    status = "CONFIRMED",
                    notes = "Pago de factura proveedor.",
                    recordedAt = now - dayMs * 4
                )
            )

            // Sale 2
            val saleOrder2Id = orderDao.insertOrder(
                Order(
                    orderNumber = "VTA-2026-002",
                    orderType = "SALE",
                    partyName = "Lucía Fernández",
                    partyContact = "+34 689 123 456",
                    totalAmount = 100.0,
                    status = "COMPLETED",
                    paymentStatus = "PAID",
                    paymentMethod = "Bizum / Móvil",
                    notes = "Pedido online con recogida.",
                    createdAt = now - dayMs
                )
            )
            orderDao.insertOrderItems(
                listOf(
                    OrderItem(
                        orderId = saleOrder2Id,
                        sneakerId = s3Id,
                        sneakerName = "Adidas Samba OG",
                        brand = "Adidas",
                        sizeEu = 39.0,
                        quantity = 1,
                        unitPrice = 100.0,
                        subtotal = 100.0
                    )
                )
            )
            paymentDao.insertPayment(
                Payment(
                    orderId = saleOrder2Id,
                    orderNumber = "VTA-2026-002",
                    paymentType = "INCOME",
                    amount = 100.0,
                    paymentMethod = "Bizum / Móvil",
                    receiptNumber = "REC-10493",
                    partyName = "Lucía Fernández",
                    status = "CONFIRMED",
                    notes = "Pago registrado automáticamente por Bizum.",
                    recordedAt = now - dayMs
                )
            )
        }
    }
}
