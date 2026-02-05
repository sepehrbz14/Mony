package com.tolou.mony.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tolou.mony.data.ExpenseDatabase



@Composable
fun MainScreen(
    database: ExpenseDatabase,
    onSettingsClick: () -> Unit
) {
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(database.expenseDao())
    )
    AddExpenseSection(
        viewModel = viewModel
    )


    val expenses by viewModel.expenses.collectAsState()
    val total = viewModel.totalSpending()

}


