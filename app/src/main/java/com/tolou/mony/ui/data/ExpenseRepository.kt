package com.tolou.mony.ui.data

import com.tolou.mony.data.network.ExpenseApi
import com.tolou.mony.data.network.ExpenseRequest
import com.tolou.mony.data.network.ExpenseResponse

class ExpenseRepository(
    private val api: ExpenseApi,
    private val authRepository: AuthRepository
) {
    suspend fun listExpenses(): List<ExpenseResponse> {
        val token = requireNotNull(authRepository.token()) { "Missing auth token." }
        return api.listExpenses("Bearer $token").map {
            it.copy(
                title = TransactionCipher.decrypt(it.title),
                amount = TransactionCipher.decryptAmount(it.amount)
            )
        }
    }

    suspend fun addExpense(title: String, amount: Long): ExpenseResponse {
        val token = requireNotNull(authRepository.token()) { "Missing auth token." }
        return api.createExpense(
            token = "Bearer $token",
            request = ExpenseRequest(
                title = TransactionCipher.encrypt(title),
                amount = TransactionCipher.encryptAmount(amount)
            )
        ).copy(title = title, amount = amount)
    }

    suspend fun deleteExpense(id: Int) {
        val token = requireNotNull(authRepository.token()) { "Missing auth token." }
        api.deleteExpense(token = "Bearer $token", id = id)
    }
}
