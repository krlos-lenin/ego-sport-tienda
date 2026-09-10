package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments ORDER BY recordedAt DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE orderId = :orderId LIMIT 1")
    fun getPaymentByOrderId(orderId: Long): Flow<Payment?>

    @Query("SELECT * FROM payments WHERE id = :id LIMIT 1")
    suspend fun getPaymentByIdSync(id: Long): Payment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)

    @Query("SELECT SUM(amount) FROM payments WHERE paymentType = 'INCOME' AND status = 'CONFIRMED'")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM payments WHERE paymentType = 'EXPENSE' AND status = 'CONFIRMED'")
    fun getTotalExpenses(): Flow<Double?>
}
