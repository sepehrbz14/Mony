package com.tolou.mony.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.text.input.KeyboardType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    username: String,
    onSettingsClick: () -> Unit,
    onAddTransactionClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val greeting = if (username.isBlank()) "Hello" else "Hi, $username!"
    val totalIncome = viewModel.totalIncome(uiState.incomes)
    val totalSpending = viewModel.totalSpending(uiState.expenses)
    val currentBalance = totalIncome - totalSpending
    var monthlyBudget by rememberSaveable { mutableStateOf(2000L) }
    var showBudgetSheet by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf(monthlyBudget.toString()) }
    val budgetSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val budgetUsed = (totalSpending.coerceAtLeast(0L)).toFloat() / monthlyBudget.coerceAtLeast(1L)

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .padding(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "March, 2026",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8E8E93)
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFFD1D1D6),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Current Balance",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "$${"%,.2f".format(currentBalance / 1.0)}",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color(0xFF111111)
                )
            }

            MonthlyBudgetCard(
                spent = totalSpending,
                budget = monthlyBudget,
                progress = budgetUsed.coerceIn(0f, 1f),
                onClick = {
                    budgetInput = monthlyBudget.toString()
                    showBudgetSheet = true
                }
            )

            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleSmall
            )

            when {
                uiState.isLoading -> {
                    Text("Loading expenses…")
                }
                uiState.error != null -> {
                    Text(uiState.error ?: "Something went wrong.")
                }
                uiState.expenses.isEmpty() && uiState.incomes.isEmpty() -> {
                    Text("No transactions yet.")
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.incomes) { income ->
                            TransactionRow(
                                title = income.title,
                                date = "Today",
                                amount = income.amount,
                                isIncome = true
                            )
                        }
                        items(uiState.expenses) { expense ->
                            TransactionRow(
                                title = expense.title,
                                date = "Today",
                                amount = expense.amount,
                                isIncome = false
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddTransactionClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .size(64.dp),
            containerColor = Color.Black
        ) {
            Text(
                text = "+",
                color = Color.White,
                fontSize = 28.sp
            )
        }
    }

    if (showBudgetSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBudgetSheet = false },
            sheetState = budgetSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Set Monthly Budget",
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            val parsed = budgetInput.toLongOrNull()
                            if (parsed != null && parsed > 0) {
                                monthlyBudget = parsed
                                showBudgetSheet = false
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyBudgetCard(
    spent: Long,
    budget: Long,
    progress: Float,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B2D6D))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Monthly Budget",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = Color.White,
                trackColor = Color(0xFF6D7FA8)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${"%,.2f".format(spent / 1.0)} / $${"%,.0f".format(budget / 1.0)}",
                    color = Color.White
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(
    title: String,
    date: String,
    amount: Long,
    isIncome: Boolean
) {
    val amountColor = if (isIncome) Color(0xFF0B2D6D) else Color(0xFFFF3B30)
    val amountPrefix = if (isIncome) "+" else "-"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF2F2F7)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("•")
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8E8E93)
                    )
                }
            }
            Text(
                text = "$amountPrefix$${"%,.2f".format(amount / 1.0)}",
                color = amountColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
