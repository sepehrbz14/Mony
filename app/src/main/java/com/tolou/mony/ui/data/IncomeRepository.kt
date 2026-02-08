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
        return api.listIncomes("Bearer $token")
    }

    suspend fun addIncome(title: String, amount: Long): IncomeResponse {
        val token = requireNotNull(authRepository.token()) { "Missing auth token." }
        return api.createIncome(
            token = "Bearer $token",
            request = IncomeRequest(title = title, amount = amount)
        )
    }
}
