package com.example.tutorial.com.example.tutorial.presentation.feature.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tutorial.R
import com.example.tutorial.com.example.tutorial.presentation.feature.profile.ProfileViewModel
import com.example.tutorial.com.example.tutorial.presentation.components.ScreenScaffold


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: ProfileViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()
    val pwd by vm.pwd.collectAsState()
    val ctx = LocalContext.current
    var newName by remember { mutableStateOf(ui.profile?.displayName ?: "") }
    var newAge by remember { mutableStateOf(ui.profile?.age?.toString() ?: "") }

    var showCurrent by remember { mutableStateOf(false) }
    var showFresh by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val currentError = remember(pwd.error) {
        if (pwd.error?.contains("Current") == true) pwd.error else null
    }
    val freshError = remember(pwd.error) {
        if (pwd.error?.contains("short") == true) pwd.error else null
    }
    val confirmError = remember(pwd.error) {
        if (pwd.error?.contains("match") == true) pwd.error else null
    }
    val successMsg = pwd.msg

    ScreenScaffold(title = "Settings") { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text("Change personal info", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Display name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = newAge,
                onValueChange = { newAge = it },
                label = { Text("Age") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { vm.save(newName.trim(), newAge.trim()) },
                enabled = !ui.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (ui.loading) {
                    CircularProgressIndicator(Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text("Save profile")
            }


            HorizontalDivider(Modifier.padding(vertical = 24.dp))

            Text("Change password", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = pwd.current,
                onValueChange = { vm.updatePwdField("current", it) },
                label = { Text("Current password") },
                singleLine = true,
                isError = currentError != null,
                visualTransformation = if (showCurrent) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (showCurrent) R.drawable.visibility_off_24px else R.drawable.visibility_24px
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showCurrent = !showCurrent }
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            currentError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            OutlinedTextField(
                value = pwd.fresh,
                onValueChange = { vm.updatePwdField("fresh", it) },
                label = { Text("New password") },
                singleLine = true,
                isError = freshError != null,
                visualTransformation = if (showFresh) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (showFresh) R.drawable.visibility_off_24px else R.drawable.visibility_24px
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showFresh = !showFresh }
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            freshError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            OutlinedTextField(
                value = pwd.confirm,
                onValueChange = { vm.updatePwdField("confirm", it) },
                label = { Text("Repeat new password") },
                singleLine = true,
                isError = confirmError != null,
                visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (showConfirm) R.drawable.visibility_off_24px else R.drawable.visibility_24px
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showConfirm = !showConfirm }
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
            confirmError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { vm.changePassword() },
                enabled = !pwd.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (pwd.loading) {
                    CircularProgressIndicator(Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text("Update password")
            }

            successMsg?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(32.dp))

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Open app notification settings")
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = false,
                    onCheckedChange = {
                        ctx.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", ctx.packageName, null)
                            )
                        )
                    }
                )
            }
        }
    }
}
