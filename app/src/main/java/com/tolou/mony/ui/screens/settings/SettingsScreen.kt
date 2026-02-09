package com.tolou.mony.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.tolou.mony.ui.theme.AlertRed
import com.tolou.mony.ui.theme.PureBlack
import com.tolou.mony.ui.theme.PureWhite
import com.tolou.mony.ui.theme.RoyalBlue

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    saveError: String?,
    currentPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    onChangePassword: () -> Unit,
    isChangingPassword: Boolean,
    changePasswordError: String?,
    changePasswordSuccess: String?,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Username", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = PureBlack,
                    focusedIndicatorColor = RoyalBlue,
                    cursorColor = RoyalBlue
                )
            )
        }

        Button(
            onClick = onSave,
            enabled = username.isNotBlank() && !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PureBlack),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(text = if (isSaving) "Saving..." else "Save", color = PureWhite)
        }

        if (!saveError.isNullOrBlank()) {
            Text(saveError, color = AlertRed)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Change password", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = currentPassword,
                onValueChange = onCurrentPasswordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                label = { Text("Current password") },
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = PureBlack,
                    focusedIndicatorColor = RoyalBlue,
                    cursorColor = RoyalBlue
                )
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                label = { Text("New password") },
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = PureBlack,
                    focusedIndicatorColor = RoyalBlue,
                    cursorColor = RoyalBlue
                )
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                label = { Text("Confirm new password") },
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = PureBlack,
                    focusedIndicatorColor = RoyalBlue,
                    cursorColor = RoyalBlue
                )
            )
        }

        Button(
            onClick = onChangePassword,
            enabled = currentPassword.isNotBlank()
                && newPassword.isNotBlank()
                && newPassword == confirmPassword
                && !isChangingPassword,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PureBlack),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = if (isChangingPassword) "Updating..." else "Update password",
                color = PureWhite
            )
        }

        if (!changePasswordError.isNullOrBlank()) {
            Text(changePasswordError, color = AlertRed)
        }

        if (!changePasswordSuccess.isNullOrBlank()) {
            Text(changePasswordSuccess, color = RoyalBlue)
        }

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PureWhite),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(text = "Log out", color = PureBlack)
        }
    }
}
