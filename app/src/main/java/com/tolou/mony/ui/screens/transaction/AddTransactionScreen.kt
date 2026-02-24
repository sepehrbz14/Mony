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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.border
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class TransactionType(val label: String) {
    Income("Income"),
    Expense("Expense")
}

@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onSubmit: (type: TransactionType, amount: Long, category: String, description: String, createdAt: String?) -> Unit
) {
    var selectedType by remember { mutableStateOf(TransactionType.Expense) }
    var amountInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    val now = remember { LocalDateTime.now() }
    var dateInput by remember { mutableStateOf(now.toLocalDate().toString()) }
    var timeInput by remember { mutableStateOf(now.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))) }
    val inputShape = RoundedCornerShape(20.dp)

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
                prefix = { Text("Ե") },
                placeholder = { Text("0") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Start),
                shape = inputShape,
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
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
                placeholder = { Text("Add a note") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                shape = inputShape,
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "When",
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { dateInput = it },
                    modifier = Modifier
                        .weight(1.45f)
                        .height(64.dp),
                    singleLine = true,
                    label = null,
                    placeholder = { Text("yyyy-MM-dd") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = inputShape,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                OutlinedTextField(
                    value = timeInput,
                    onValueChange = { timeInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    singleLine = true,
                    label = null,
                    placeholder = { Text("HH:mm") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = inputShape,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Button(
            onClick = {
                val amount = amountInput.toLongOrNull()
                if (amount != null) {
                    val createdAt = runCatching {
                        val date = LocalDate.parse(dateInput)
                        val time = LocalTime.parse(timeInput)
                        LocalDateTime.of(date, time)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toString()
                    }.getOrNull()
                    onSubmit(
                        selectedType,
                        amount,
                        selectedCategory,
                        descriptionInput.trim(),
                        createdAt
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Save",
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
    val backgroundColor = MaterialTheme.colorScheme.surface
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedText = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(28.dp))
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(28.dp))
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
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else unselectedText,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdownField(
    selectedCategory: String,
    categories: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCategorySelected: (String) -> Unit
) {
    val inputShape = RoundedCornerShape(20.dp)
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
            shape = inputShape,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category, color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onCategorySelected(category)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}
