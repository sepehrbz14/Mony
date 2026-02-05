package com.tolou.mony.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tolou.mony.data.ExpenseDao

class MainViewModelFactory(
    private val expenseDao: ExpenseDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(expenseDao) as T
    }
}
