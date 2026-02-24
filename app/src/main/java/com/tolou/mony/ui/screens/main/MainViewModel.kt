package com.tolou.mony.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tolou.mony.data.network.ExpenseResponse
import com.tolou.mony.data.network.IncomeResponse
import com.tolou.mony.ui.data.ExpenseRepository
import com.tolou.mony.ui.data.IncomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.tolou.mony.ui.utils.toUserMessage

data class MainUiState(
    val isLoading: Boolean = false,
    val incomes: List<IncomeResponse> = emptyList(),
    val expenses: List<ExpenseResponse> = emptyList(),
    val error: String? = null
)

class MainViewModel(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    fun addIncome(title: String, amount: Long, createdAt: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                incomeRepository.addIncome(title, amount, createdAt)
                refresh()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.toUserMessage("Failed to add income.")
                    )
                }
            }
        }
    }

    fun addExpense(title: String, amount: Long, createdAt: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                expenseRepository.addExpense(title, amount, createdAt)
                refresh()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.toUserMessage("Failed to add expense.")
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val incomes = incomeRepository.listIncomes()
                val expenses = expenseRepository.listExpenses()
                _uiState.update {
                    it.copy(isLoading = false, incomes = incomes, expenses = expenses)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.toUserMessage("Failed to load expenses.")
                    )
                }
            }
        }
    }

    fun deleteTransaction(id: Int, isIncome: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                if (isIncome) {
                    incomeRepository.deleteIncome(id)
                } else {
                    expenseRepository.deleteExpense(id)
                }
                refresh()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.toUserMessage("Failed to delete transaction.")
                    )
                }
            }
        }
    }

    fun totalSpending(expenses: List<ExpenseResponse>): Long {
        return expenses.sumOf { it.amount }
    }

    fun totalIncome(incomes: List<IncomeResponse>): Long {
        return incomes.sumOf { it.amount }
    }
}
