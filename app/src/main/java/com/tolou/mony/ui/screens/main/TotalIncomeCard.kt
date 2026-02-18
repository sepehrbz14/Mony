package com.tolou.mony.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tolou.mony.ui.utils.formatRial

@Composable
fun TotalIncomeCard(total: Long) {
    val formattedTotal = formatRial(total)
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Total Income",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = formattedTotal,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
