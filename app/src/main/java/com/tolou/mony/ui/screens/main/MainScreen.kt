package com.tolou.mony.ui.screens.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt
import com.tolou.mony.ui.theme.AlertRed
import com.tolou.mony.ui.theme.NeutralGray
import com.tolou.mony.ui.theme.PureWhite
import com.tolou.mony.ui.utils.formatRial
import com.tolou.mony.ui.utils.formatSignedRial


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    username: String,
    onSettingsClick: () -> Unit,
    onPendingClick: () -> Unit,
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
    val contentState = when {
        uiState.isLoading -> ContentState.Loading
        uiState.error != null -> ContentState.Error
        uiState.expenses.isEmpty() && uiState.incomes.isEmpty() -> ContentState.Empty
        else -> ContentState.Content
    }
    val sortedTransactions = remember(uiState.incomes, uiState.expenses) {
        val incomes = uiState.incomes.map { income ->
            val (category, description) = parseTransactionTitle(income.title)
            UiTransaction(
                id = income.id,
                category = category,
                description = description,
                amount = income.amount,
                createdAt = income.createdAt,
                isIncome = true,
                icon = categoryIcon(category, isIncome = true)
            )
        }
        val expenses = uiState.expenses.map { expense ->
            val (category, description) = parseTransactionTitle(expense.title)
            UiTransaction(
                id = expense.id,
                category = category,
                description = description,
                amount = expense.amount,
                createdAt = expense.createdAt,
                isIncome = false,
                icon = categoryIcon(category, isIncome = false)
            )
        }
        (incomes + expenses).sortedByDescending { transaction ->
            runCatching { Instant.parse(transaction.createdAt) }.getOrNull()
                ?: Instant.EPOCH
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .padding(bottom = 12.dp),
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
                }
                Row {
                    IconButton(onClick = onPendingClick) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Pending transactions",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Current Balance",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = formatRial(currentBalance),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface
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

            Crossfade(targetState = contentState, label = "transactionState") { state ->
                when (state) {
                    ContentState.Loading -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(6) {
                                TransactionSkeletonRow()
                            }
                        }
                    }
                    ContentState.Error -> {
                        Text(uiState.error ?: "Something went wrong.", color = AlertRed)
                    }
                    ContentState.Empty -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text("No transactions yet.")
                            }
                        }
                    }
                    ContentState.Content -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 92.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(sortedTransactions) { transaction ->
                                TransactionRow(
                                    title = transaction.category,
                                    date = transaction.createdAt,
                                    amount = transaction.amount,
                                    isIncome = transaction.isIncome,
                                    icon = transaction.icon,
                                    onClick = {
                                        selectedTransaction = TransactionDetails(
                                            id = transaction.id,
                                            category = transaction.category,
                                            description = transaction.description,
                                            amount = transaction.amount,
                                            createdAt = transaction.createdAt,
                                            isIncome = transaction.isIncome
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        }

        FloatingActionButton(
            onClick = onAddTransactionClick,
            modifier = Modifier
                 .align(Alignment.BottomEnd)
                 .padding(end = 16.dp, bottom = 16.dp)
                .size(64.dp),
            containerColor = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.background,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add transaction",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    if (showBudgetSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBudgetSheet = false },
            sheetState = budgetSheetState,
            containerColor = MaterialTheme.colorScheme.surface
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
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
            sheetState = transactionSheetState,
            containerColor = MaterialTheme.colorScheme.surface
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
                    text = formatDetailDateTime(transaction.createdAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeutralGray
                )
                if (transaction.description.isNotBlank()) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = formatSignedRial(if (transaction.isIncome) transaction.amount else -transaction.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (transaction.isIncome) incomeAmountColor() else AlertRed
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
                        Text("Delete", color = AlertRed)
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
    val percent = (progress * 100).roundToInt()
    val isDark = isSystemInDarkTheme()
    val shadowElevation = if (isDark) 7.dp else 8.dp
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(12.dp),
                ambientColor = if (isDark) Color.White.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.22f),
                spotColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.28f)
            ),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(88.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    strokeWidth = 8.dp
                )
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Monthly Budget",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Spent: ${formatRial(spent)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
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
    val amountColor = if (isIncome) incomeAmountColor() else AlertRed
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 3.5.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.16f),
                spotColor = if (isDark) Color.White.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.2f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
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
                        text = formatShortDate(date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralGray
                    )
                }
            }
            AnimatedContent(
                targetState = amount,
                label = "amountChange"
            ) { animatedAmount ->
                Text(
                    text = formatSignedRial(if (isIncome) animatedAmount else -animatedAmount),
                    color = amountColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TransactionSkeletonRow() {
    val shimmerAlpha by rememberInfiniteTransition(label = "skeleton")
        .animateFloat(
            initialValue = 0.4f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "skeletonAlpha"
        )
    val baseColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val highlightColor = MaterialTheme.colorScheme.onSurface.copy(alpha = shimmerAlpha)
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 3.5.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.16f),
                spotColor = if (isDark) Color.White.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        .background(highlightColor)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .width(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(baseColor)
                    )
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .width(90.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(baseColor)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .height(14.dp)
                    .width(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(baseColor)
            )
        }
    }
}

private enum class ContentState {
    Loading,
    Error,
    Empty,
    Content
}

private data class UiTransaction(
    val id: Int,
    val category: String,
    val description: String,
    val amount: Long,
    val createdAt: String,
    val isIncome: Boolean,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

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

private fun formatShortDate(isoDate: String): String {
    val instant = runCatching { Instant.parse(isoDate) }.getOrNull() ?: return isoDate
    val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val day = date.dayOfMonth
    return "$month $day${ordinalSuffix(day)}"
}

private fun formatDetailDateTime(isoDate: String): String {
    val instant = runCatching { Instant.parse(isoDate) }.getOrNull() ?: return isoDate
    val zonedDateTime = instant.atZone(ZoneId.systemDefault())
    val date = zonedDateTime.toLocalDate()
    val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val day = date.dayOfMonth
    val dateLabel = "$month $day${ordinalSuffix(day)}, ${date.year}"
    val timeLabel = zonedDateTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    return "$dateLabel \u2022 $timeLabel"
}

private fun ordinalSuffix(day: Int): String {
    if (day in 11..13) return "th"
    return when (day % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}


@Composable
private fun incomeAmountColor() = MaterialTheme.colorScheme.primary
