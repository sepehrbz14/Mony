package com.tolou.mony.ui.screens.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

enum class TransactionType(val label: String) {
    Income("Income"),
    Expense("Expense")
}

@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onSubmit: (type: TransactionType, amount: Long, category: String, description: String) -> Unit
) {
    var selectedType by remember { mutableStateOf(TransactionType.Expense) }
    var amountInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food") }
    var descriptionInput by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val categories = when (selectedType) {
        TransactionType.Income -> listOf("Salary", "Bonus", "Gift", "Other")
        TransactionType.Expense -> listOf(
            "Food",
            "Transport",
            "Housing",
            "Entertainment",
            "Utilities",
            "Other"
        )
    }

    if (selectedCategory !in categories) {
        selectedCategory = categories.first()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Add a transaction",
            style = MaterialTheme.typography.headlineSmall
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { selectedType = TransactionType.Income },
                enabled = selectedType != TransactionType.Income
            ) {
                Text(TransactionType.Income.label)
            }
            Button(
                onClick = { selectedType = TransactionType.Expense },
                enabled = selectedType != TransactionType.Expense
            ) {
                Text(TransactionType.Expense.label)
            }
        }

        OutlinedTextField(
            value = amountInput,
            onValueChange = { amountInput = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Column {
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelMedium
            )
            Button(onClick = { categoryExpanded = true }) {
                Text(selectedCategory)
            }
            DropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = descriptionInput,
            onValueChange = { descriptionInput = it },
            label = { Text("Description (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onBack) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    val amount = amountInput.toLongOrNull()
                    if (amount != null) {
                        onSubmit(
                            selectedType,
                            amount,
                            selectedCategory,
                            descriptionInput.trim()
                        )
                    }
                }
            ) {
                Text("Add")
            }
        }
    }
}
