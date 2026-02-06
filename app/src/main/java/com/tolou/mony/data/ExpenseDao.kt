package com.tolou.mony.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun getAllFlow(): Flow<List<Expense>>

    @Delete
    suspend fun delete(expense: Expense)
}
