package com.tolou.mony.data.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class ExpenseRequest(
    val title: String,
    val amount: Long
)

data class ExpenseResponse(
    val id: Int,
    val userId: Int,
    val title: String,
    val amount: Long,
    val createdAt: String
)

interface ExpenseApi {

    @GET("expenses")
    suspend fun listExpenses(
        @Header("Authorization") token: String
    ): List<ExpenseResponse>

    @POST("expenses")
    suspend fun createExpense(
        @Header("Authorization") token: String,
        @Body request: ExpenseRequest
    ): ExpenseResponse

    @DELETE("expenses/{id}")
    suspend fun deleteExpense(
        @Header("Authorization") token: String,
        @retrofit2.http.Path("id") id: Int
    )
}
