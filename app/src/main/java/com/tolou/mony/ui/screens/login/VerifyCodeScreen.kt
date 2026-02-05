package com.tolou.mony.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VerifyCodeScreen(
    phone: String,
    viewModel: LoginViewModel
) {
    var code by remember { mutableStateOf("") }
    val state = viewModel.state

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter verification code", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("OTP Code") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.verifyCode(phone, code) },
            enabled = state !is LoginState.Verifying,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state is LoginState.Verifying)
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else
                Text("Verify")
        }

        if (state is LoginState.Error) {
            Spacer(Modifier.height(12.dp))
            Text(state.message, color = MaterialTheme.colorScheme.error)
        }
    }
}