package com.tolou.mony.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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
    val expenses by viewModel.expenses.collectAsState()
    val total = viewModel.totalSpending()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = onSettingsClick) {
                Text("Settings")
            }
        }
        TotalSpendingCard(total = total)
        AddExpenseSection(
            viewModel = viewModel
        )

        if (expenses.isEmpty()) {
            Text("No expenses yet. Add your first one above.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(expenses) { expense ->
                    ExpenseItem(expense = expense)
                }
            }
        }
    }
}
