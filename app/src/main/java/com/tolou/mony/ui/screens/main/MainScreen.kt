package com.tolou.mony.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

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
        TotalSpendingCard(total = viewModel.totalSpending(uiState.expenses))
        AddExpenseSection(
            viewModel = viewModel
        )

        if (uiState.isLoading) {
            Text("Loading expenses…")
        } else if (uiState.error != null) {
            Text(uiState.error ?: "Something went wrong.")
        } else if (uiState.expenses.isEmpty()) {
            Text("No expenses yet. Add your first one above.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.expenses) { expense ->
                    ExpenseItem(expense = expense)
                }
            }
        }
    }
}
