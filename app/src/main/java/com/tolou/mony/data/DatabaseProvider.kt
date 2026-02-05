package com.tolou.mony.data

import android.content.Context
import androidx.room.Room
import com.tolou.mony.data.ExpenseDatabase

object DatabaseProvider {

    @Volatile
    private var INSTANCE: ExpenseDatabase? = null

    fun getDatabase(context: Context): ExpenseDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                ExpenseDatabase::class.java,
                "expense_db"
            ).build()

            INSTANCE = instance
            instance
        }
    }
}
