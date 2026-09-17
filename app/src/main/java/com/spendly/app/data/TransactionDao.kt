package com.spendly.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(tx: Transaction): Long

    @Update
    suspend fun update(tx: Transaction)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Transaction?

    @Query(
        """
        SELECT * FROM transactions
        WHERE timestamp >= :monthStart AND timestamp < :monthEnd
        ORDER BY timestamp DESC
        """
    )
    fun observeForMonth(monthStart: Long, monthEnd: Long): Flow<List<Transaction>>
}
