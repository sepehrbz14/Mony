package com.tolou.mony.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun VerifyCodeScreen(
    viewModel: LoginViewModel,
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    val state = viewModel.state
    val inputShape = RoundedCornerShape(20.dp)

    LaunchedEffect(state) {
        if (state is LoginState.LoggedIn) {
            onVerified()
            viewModel.consumeLoggedIn()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter signup code", style = MaterialTheme.typography.titleLarge)

        val otpState = state as? LoginState.OtpSent
        otpState?.let {
            Spacer(Modifier.height(8.dp))
            Text("Attempts left: ${it.remainingAttempts}", style = MaterialTheme.typography.bodyMedium)
            Text("Expires at: ${it.expiresAt}", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("OTP Code") },
            supportingText = { Text("Verification is validated by the server challenge") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = inputShape,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.verifySignup(code) },
            enabled = state !is LoginState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state is LoginState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Verify & Create Account")
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }

        if (state is LoginState.Error) {
            Spacer(Modifier.height(12.dp))
            Text(state.message, color = MaterialTheme.colorScheme.error)
        }
    }
}
