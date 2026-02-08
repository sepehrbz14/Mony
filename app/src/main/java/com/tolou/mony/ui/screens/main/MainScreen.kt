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
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.TextButton
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
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
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
    var selectedTransaction by remember { mutableStateOf<TransactionDetails?>(null) }
    val transactionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                            val (category, description) = parseTransactionTitle(income.title)
                            TransactionRow(
                                title = category,
                                date = income.createdAt,
                                amount = income.amount,
                                isIncome = true,
                                icon = categoryIcon(category, isIncome = true),
                                onClick = {
                                    selectedTransaction = TransactionDetails(
                                        id = income.id,
                                        category = category,
                                        description = description,
                                        amount = income.amount,
                                        createdAt = income.createdAt,
                                        isIncome = true
                                    )
                                }
                            )
                        }
                        items(uiState.expenses) { expense ->
                            val (category, description) = parseTransactionTitle(expense.title)
                            TransactionRow(
                                title = category,
                                date = expense.createdAt,
                                amount = expense.amount,
                                isIncome = false,
                                icon = categoryIcon(category, isIncome = false),
                                onClick = {
                                    selectedTransaction = TransactionDetails(
                                        id = expense.id,
                                        category = category,
                                        description = description,
                                        amount = expense.amount,
                                        createdAt = expense.createdAt,
                                        isIncome = false
                                    )
                                }
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

    selectedTransaction?.let { transaction ->
        ModalBottomSheet(
            onDismissRequest = { selectedTransaction = null },
            sheetState = transactionSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = transaction.createdAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8E8E93)
                )
                if (transaction.description.isNotBlank()) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "${if (transaction.isIncome) "+" else "-"}$${"%,.2f".format(transaction.amount / 1.0)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (transaction.isIncome) Color(0xFF0B2D6D) else Color(0xFFFF3B30)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { selectedTransaction = null }) {
                        Text("Close")
                    }
                    TextButton(
                        onClick = {
                            viewModel.deleteTransaction(transaction.id, transaction.isIncome)
                            selectedTransaction = null
                        }
                    ) {
                        Text("Delete", color = Color(0xFFFF3B30))
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
    isIncome: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val amountColor = if (isIncome) Color(0xFF0B2D6D) else Color(0xFFFF3B30)
    val amountPrefix = if (isIncome) "+" else "-"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF111111),
                        modifier = Modifier.size(20.dp)
                    )
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

private data class TransactionDetails(
    val id: Int,
    val category: String,
    val description: String,
    val amount: Long,
    val createdAt: String,
    val isIncome: Boolean
)

private fun parseTransactionTitle(title: String): Pair<String, String> {
    val parts = title.split(": ", limit = 2)
    return if (parts.size == 2) {
        parts[0] to parts[1]
    } else {
        title to ""
    }
}

private fun categoryIcon(category: String, isIncome: Boolean): androidx.compose.ui.graphics.vector.ImageVector {
    val key = category.lowercase()
    return when {
        key.contains("grocery") -> Icons.Default.LocalGroceryStore
        key.contains("food") || key.contains("dining") -> Icons.Default.Restaurant
        key.contains("transport") || key.contains("fuel") -> Icons.Default.DirectionsCar
        key.contains("rent") || key.contains("mortgage") || key.contains("home") -> Icons.Default.Home
        key.contains("shopping") -> Icons.Default.ShoppingCart
        key.contains("entertainment") -> Icons.Default.Movie
        key.contains("health") -> Icons.Default.LocalHospital
        key.contains("education") -> Icons.Default.School
        key.contains("travel") -> Icons.Default.Flight
        key.contains("gift") -> Icons.Default.CardGiftcard
        key.contains("pet") -> Icons.Default.Pets
        key.contains("child") -> Icons.Default.ChildCare
        key.contains("salary") || key.contains("business") || key.contains("freelance") || key.contains("commission") -> Icons.Default.Work
        key.contains("investment") || key.contains("interest") || key.contains("refund") || key.contains("bonus") -> Icons.Default.Savings
        isIncome -> Icons.Default.AttachMoney
        else -> Icons.Default.LocalGasStation
    }
}
