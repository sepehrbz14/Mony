package com.tolou.mony.notifications

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

data class PendingTransaction(
    val id: String,
    val amount: Long,
    val type: ParsedTransactionType,
    val rawMessage: String,
    val createdAtMillis: Long
)

class PendingTransactionStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun addDetected(transaction: Transaction): PendingTransaction {
        val pending = PendingTransaction(
            id = UUID.randomUUID().toString(),
            amount = transaction.amount,
            type = transaction.type,
            rawMessage = transaction.rawMessage,
            createdAtMillis = System.currentTimeMillis()
        )
        val current = getAll().toMutableList()
        current.add(0, pending)
        saveAll(current)
        return pending
    }

    fun getAll(): List<PendingTransaction> {
        val raw = prefs.getString(KEY_ITEMS, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<PendingTransaction>>() {}.type
            gson.fromJson<List<PendingTransaction>>(raw, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    fun remove(id: String) {
        val updated = getAll().filterNot { it.id == id }
        saveAll(updated)
    }

    private fun saveAll(items: List<PendingTransaction>) {
        prefs.edit().putString(KEY_ITEMS, gson.toJson(items)).apply()
    }

    private companion object {
        const val PREFS_NAME = "pending_transactions"
        const val KEY_ITEMS = "items"
    }
}
