package com.tolou.mony.ui.data

import com.tolou.mony.data.network.IncomeApi
import com.tolou.mony.data.network.IncomeRequest
import com.tolou.mony.data.network.IncomeResponse

class IncomeRepository(
    private val api: IncomeApi,
    private val authRepository: AuthRepository
) {
    suspend fun listIncomes(): List<IncomeResponse> {
        val token = requireNotNull(authRepository.token()) { "Missing auth token." }
        return api.listIncomes("Bearer $token").map {
            it.copy(
                title = TransactionCipher.decrypt(it.title),
                amount = TransactionCipher.decryptAmount(it.amount)
            )
        }
    }

    suspend fun addIncome(title: String, amount: Long): IncomeResponse {
        val token = requireNotNull(authRepository.token()) { "Missing auth token." }
        return api.createIncome(
            token = "Bearer $token",
            request = IncomeRequest(
                title = TransactionCipher.encrypt(title),
                amount = TransactionCipher.encryptAmount(amount)
            )
        ).copy(title = title, amount = amount)
    }

    suspend fun deleteIncome(id: Int) {
        val token = requireNotNull(authRepository.token()) { "Missing auth token." }
        api.deleteIncome(token = "Bearer $token", id = id)
    }
}
