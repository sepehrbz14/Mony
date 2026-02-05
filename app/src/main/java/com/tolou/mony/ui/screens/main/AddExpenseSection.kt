package com.tolou.mony.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddExpenseSection(
    viewModel: MainViewModel
) {
    var titleInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        OutlinedTextField(
            value = titleInput,
            onValueChange = { titleInput = it },
            label = { Text("Expense title") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amountInput,
            onValueChange = { amountInput = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val amount = amountInput.toIntOrNull()

                if (titleInput.isNotBlank() && amount != null) {
                    viewModel.addExpense(
                        title = titleInput,
                        amount = amount
                    )

                    // Reset fields after success
                    titleInput = ""
                    amountInput = ""
                }
            }
        ) {
            Text("Add Expense")
        }
    }
}
