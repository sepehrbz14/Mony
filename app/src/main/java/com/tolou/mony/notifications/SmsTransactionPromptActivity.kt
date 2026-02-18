package com.tolou.mony.notifications

import android.os.Bundle
import android.graphics.Color
import android.widget.Toast
import android.graphics.drawable.ColorDrawable
import androidx.lifecycle.lifecycleScope
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tolou.mony.data.SessionStorage
import com.tolou.mony.data.network.AuthApi
import com.tolou.mony.data.network.ExpenseApi
import com.tolou.mony.data.network.IncomeApi
import com.tolou.mony.data.network.RetrofitInstance
import com.tolou.mony.ui.data.AuthRepository
import com.tolou.mony.ui.data.ExpenseRepository
import com.tolou.mony.ui.data.IncomeRepository
import com.tolou.mony.ui.theme.MonyTheme
import com.tolou.mony.ui.utils.formatRial
import kotlinx.coroutines.launch

class SmsTransactionPromptActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setDimAmount(0.28f)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val amount = intent.getLongExtra(EXTRA_AMOUNT, 0L)
        val pendingId = intent.getStringExtra(EXTRA_PENDING_ID)
        val parsedType = intent.getStringExtra(EXTRA_TRANSACTION_TYPE)
            ?.let { runCatching { ParsedTransactionType.valueOf(it) }.getOrNull() }
            ?: ParsedTransactionType.UNKNOWN

        val categories = when (parsedType) {
            ParsedTransactionType.INCOME -> INCOME_CATEGORIES
            ParsedTransactionType.EXPENSE -> EXPENSE_CATEGORIES
            ParsedTransactionType.UNKNOWN -> EXPENSE_CATEGORIES
        }

        val sessionStorage = SessionStorage(applicationContext)
        val darkModeEnabled = sessionStorage.fetchDarkModeEnabled()
        val authRepository = AuthRepository(
            RetrofitInstance.retrofit.create(AuthApi::class.java),
            sessionStorage
        )
        val expenseRepository = ExpenseRepository(
            RetrofitInstance.retrofit.create(ExpenseApi::class.java),
            authRepository
        )
        val incomeRepository = IncomeRepository(
            RetrofitInstance.retrofit.create(IncomeApi::class.java),
            authRepository
        )

        setContent {
            MonyTheme(darkTheme = darkModeEnabled ?: androidx.compose.foundation.isSystemInDarkTheme()) {
                SmsTransactionPromptContent(
                    amount = amount,
                    categories = categories,
                    onCancel = { finish() },
                    onSave = { category, description ->
                        lifecycleScope.launch {
                            val title = if (description.isBlank()) {
                                category
                            } else {
                                "$category: $description"
                            }
                            val normalizedAmount = kotlin.math.abs(amount)

                            val result = runCatching {
                                when (parsedType) {
                                    ParsedTransactionType.INCOME -> {
                                        incomeRepository.addIncome(title, normalizedAmount)
                                    }

                                    ParsedTransactionType.EXPENSE -> {
                                        expenseRepository.addExpense(title, normalizedAmount)
                                    }

                                    ParsedTransactionType.UNKNOWN -> {
                                        error("Unsupported transaction type")
                                    }
                                }
                            }

                            if (result.isSuccess) {
                                pendingId?.let { PendingTransactionStore(applicationContext).remove(it) }
                                Toast.makeText(
                                    this@SmsTransactionPromptActivity,
                                    "Transaction saved",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                Toast.makeText(
                                    this@SmsTransactionPromptActivity,
                                    result.exceptionOrNull()?.localizedMessage
                                        ?: "Failed to save transaction",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_AMOUNT = "extra_amount"
        const val EXTRA_TRANSACTION_TYPE = "extra_transaction_type"
        const val EXTRA_PENDING_ID = "extra_pending_id"

        private val INCOME_CATEGORIES = listOf(
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

        private val EXPENSE_CATEGORIES = listOf(
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
}

@Composable
private fun SmsTransactionPromptContent(
    amount: Long,
    categories: List<String>,
    onCancel: () -> Unit,
    onSave: (category: String, description: String) -> Unit
) {
    var selectedCategory by rememberSaveable { mutableStateOf(categories.first()) }
    var description by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val inputBackground = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Detected transaction",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(inputBackground, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Amount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = formatRial(amount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                CategoryDropdown(
                    selectedCategory = selectedCategory,
                    categories = categories,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    onCategorySelected = { selectedCategory = it }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Description (optional)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Add a note", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = inputBackground,
                        unfocusedContainerColor = inputBackground,
                        disabledContainerColor = inputBackground,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Button(
                onClick = { onSave(selectedCategory, description.trim()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
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
                .height(52.dp)
                .menuAnchor(),
            readOnly = true,
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
