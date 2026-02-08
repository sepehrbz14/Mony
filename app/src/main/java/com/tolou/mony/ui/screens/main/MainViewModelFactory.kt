package com.tolou.mony.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tolou.mony.ui.data.ExpenseRepository
import com.tolou.mony.ui.data.IncomeRepository

class MainViewModelFactory(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(expenseRepository, incomeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
