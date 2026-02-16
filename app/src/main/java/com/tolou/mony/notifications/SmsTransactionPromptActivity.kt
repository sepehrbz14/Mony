package com.tolou.mony.notifications

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class SmsTransactionPromptActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val amount = intent.getLongExtra(EXTRA_AMOUNT, 0L)

        setContent {
            MaterialTheme {
                var category by rememberSaveable { mutableStateOf("") }
                var description by rememberSaveable { mutableStateOf("") }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Bank SMS detected",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Amount: $amount",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Category") },
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Description") },
                                maxLines = 4
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { finish() }) {
                                    Text("Cancel")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        Toast.makeText(
                                            this@SmsTransactionPromptActivity,
                                            "Transaction captured",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // TODO: Persist amount + category + description in repository.
                                        finish()
                                    },
                                    enabled = category.isNotBlank()
                                ) {
                                    Text("Save")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_AMOUNT = "extra_amount"
    }
}
