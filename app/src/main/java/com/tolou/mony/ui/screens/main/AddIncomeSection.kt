package com.tolou.mony.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddIncomeSection(
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
            label = { Text("Income source") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        )

        OutlinedTextField(
            value = amountInput,
            onValueChange = { amountInput = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(20.dp)
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val amount = amountInput.toLongOrNull()

                if (titleInput.isNotBlank() && amount != null) {
                    viewModel.addIncome(
                        title = titleInput,
                        amount = amount
                    )

                    titleInput = ""
                    amountInput = ""
                }
            }
        ) {
            Text("Add Income")
        }
    }
}
