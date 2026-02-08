package com.tolou.mony.ui.screens.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape

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
    var selectedCategory by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val categories = when (selectedType) {
        TransactionType.Income -> listOf(
            "Salary",
            "Business",
            "Freelance",
            "Bonus",
            "Commission",
            "Rental",
            "Interest",
            "Gift",
            "Refund",
            "Investment",
            "Side Hustle",
            "Other"
        )
        TransactionType.Expense -> listOf(
            "Food",
            "Groceries",
            "Dining",
            "Transport",
            "Fuel",
            "Rent",
            "Mortgage",
            "Home Supplies",
            "Entertainment",
            "Utilities",
            "Healthcare",
            "Shopping",
            "Personal Care",
            "Education",
            "Travel",
            "Insurance",
            "Subscriptions",
            "Gifts",
            "Charity",
            "Taxes",
            "Fees",
            "Pets",
            "Childcare",
            "Other"
        )
    }

    if (selectedCategory !in categories) {
        selectedCategory = categories.first()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Add transaction",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text("Cancel")
            }
        }

        TransactionTypeToggle(
            selectedType = selectedType,
            onTypeSelected = { selectedType = it }
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Amount",
                style = MaterialTheme.typography.labelLarge
            )
            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                placeholder = { Text("$0.00") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color(0xFFDADADA),
                    focusedIndicatorColor = Color(0xFF0B2D6D)
                )
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge
            )
            CategoryDropdownField(
                selectedCategory = selectedCategory,
                categories = categories,
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it },
                onCategorySelected = { selectedCategory = it }
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Description (optional)",
                style = MaterialTheme.typography.labelLarge
            )
            OutlinedTextField(
                value = descriptionInput,
                onValueChange = { descriptionInput = it },
                placeholder = { Text("Grocery shopping") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color(0xFFDADADA),
                    focusedIndicatorColor = Color(0xFF0B2D6D)
                )
            )
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
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B2D6D)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Save",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TransactionTypeToggle(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    val backgroundColor = Color(0xFFF4F4F4)
    val selectedColor = Color(0xFF0B2D6D)
    val unselectedText = Color(0xFF4A4A4A)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(28.dp))
            .padding(4.dp)
            .height(48.dp)
    ) {
        TransactionType.values().forEach { type ->
            val isSelected = selectedType == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isSelected) selectedColor else Color.Transparent)
                    .clickable { onTypeSelected(type) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.label,
                    color = if (isSelected) Color.White else unselectedText,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CategoryDropdownField(
    selectedCategory: String,
    categories: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCategorySelected: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .menuAnchor(),
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color(0xFFDADADA),
                focusedIndicatorColor = Color(0xFF0B2D6D)
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}
