package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Sneaker
import com.example.data.model.SneakerStock
import com.example.data.model.SneakerWithStock
import kotlinx.coroutines.flow.Flow

@Dao
interface SneakerDao {

    @Transaction
    @Query("SELECT * FROM sneakers WHERE isArchived = 0 ORDER BY id DESC")
    fun getAllSneakersWithStock(): Flow<List<SneakerWithStock>>

    @Query("SELECT * FROM sneakers WHERE id = :id LIMIT 1")
    fun getSneakerById(id: Long): Flow<Sneaker?>

    @Transaction
    @Query("SELECT * FROM sneakers WHERE id = :id LIMIT 1")
    suspend fun getSneakerWithStockSync(id: Long): SneakerWithStock?

    @Query("SELECT * FROM sneaker_stocks WHERE sneakerId = :sneakerId")
    fun getStocksForSneaker(sneakerId: Long): Flow<List<SneakerStock>>

    @Query("SELECT * FROM sneaker_stocks WHERE sneakerId = :sneakerId AND sizeEu = :size LIMIT 1")
    suspend fun getStockForSizeSync(sneakerId: Long, size: Double): SneakerStock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSneaker(sneaker: Sneaker): Long

    @Update
    suspend fun updateSneaker(sneaker: Sneaker)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: SneakerStock): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<SneakerStock>)

    @Update
    suspend fun updateStock(stock: SneakerStock)

    @Query("UPDATE sneaker_stocks SET quantity = :newQuantity WHERE sneakerId = :sneakerId AND sizeEu = :sizeEu")
    suspend fun updateStockQuantity(sneakerId: Long, sizeEu: Double, newQuantity: Int)

    @Query("SELECT COUNT(*) FROM sneakers")
    suspend fun getSneakersCount(): Int
}
