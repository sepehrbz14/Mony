package com.tolou.mony.data.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class IncomeRequest(
    val title: String,
    val amount: Long,
    val createdAt: String? = null
)

data class IncomeResponse(
    val id: Int,
    val userId: Int,
    val title: String,
    val amount: Long,
    val createdAt: String
)

interface IncomeApi {
    @GET("incomes")
    suspend fun listIncomes(
        @Header("Authorization") token: String
    ): List<IncomeResponse>

    @POST("incomes")
    suspend fun createIncome(
        @Header("Authorization") token: String,
        @Body request: IncomeRequest
    ): IncomeResponse

    @DELETE("incomes/{id}")
    suspend fun deleteIncome(
        @Header("Authorization") token: String,
        @retrofit2.http.Path("id") id: Int
    )
}
