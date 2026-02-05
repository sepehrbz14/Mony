package com.tolou.mony.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tolou.mony.data.Expense
import com.tolou.mony.data.ExpenseDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val expenseDao: ExpenseDao
) : ViewModel() {

    // Auto-updating list of expenses
    val expenses = expenseDao.getAllFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addExpense(title: String, amount: Long) {
        viewModelScope.launch {
            expenseDao.insert(
                Expense(
                    title = title,
                    amount = amount,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun totalSpending(): Long {
        return expenses.value.sumOf { it.amount }
    }
}
