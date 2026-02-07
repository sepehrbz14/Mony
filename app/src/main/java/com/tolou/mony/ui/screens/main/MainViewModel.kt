package com.tolou.mony.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tolou.mony.data.network.ExpenseResponse
import com.tolou.mony.ui.data.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoading: Boolean = false,
    val incomes: List<IncomeEntry> = emptyList(),
    val expenses: List<ExpenseResponse> = emptyList(),
    val error: String? = null
)

data class IncomeEntry(
    val title: String,
    val amount: Long
)

class MainViewModel(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    fun addIncome(title: String, amount: Long) {
        _uiState.update { currentState ->
            val updatedIncomes = currentState.incomes + IncomeEntry(title = title, amount = amount)
            currentState.copy(incomes = updatedIncomes)
        }
    }

    fun addExpense(title: String, amount: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                expenseRepository.addExpense(title, amount)
                refresh()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to add expense."
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val expenses = expenseRepository.listExpenses()
                _uiState.update { it.copy(isLoading = false, expenses = expenses) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to load expenses."
                    )
                }
            }
        }
    }

    fun totalSpending(expenses: List<ExpenseResponse>): Long {
        return expenses.sumOf { it.amount }
    }

    fun totalIncome(incomes: List<IncomeEntry>): Long {
        return incomes.sumOf { it.amount }
    }
}
