package com.tolou.mony.ui.screens.login


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onCodeSent: (String) -> Unit
) {
    var phone by remember { mutableStateOf("") }
    val state = viewModel.state

    LaunchedEffect(state) {
        if (state is LoginState.CodeSent) {
            onCodeSent(state.phone)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter your phone number", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.sendCode(phone) },
            enabled = state !is LoginState.SendingCode,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state is LoginState.SendingCode) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Send Code")
            }
        }

        if (state is LoginState.Error) {
            Spacer(Modifier.height(12.dp))
            Text(state.message, color = MaterialTheme.colorScheme.error)
        }
    }
}
