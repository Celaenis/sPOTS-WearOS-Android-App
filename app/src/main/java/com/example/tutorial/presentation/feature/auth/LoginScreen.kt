package com.example.tutorial.com.example.tutorial.presentation.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tutorial.R

@Composable
fun LoginScreen(
    vm: AuthViewModel
) {
    val ui by vm.ui.collectAsState()
    val focus = LocalFocusManager.current

    if (ui.isLoggedIn) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "sPOTS",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Spot the signs your heart sends.",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(104.dp))

        OutlinedTextField(
            value = ui.email,
            onValueChange = vm::updateEmail,
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = ui.password,
            onValueChange = vm::updatePassword,
            label = { Text("Password") },
            singleLine = true,
            visualTransformation =
            if (ui.showPassword) VisualTransformation.None
            else PasswordVisualTransformation(),
            trailingIcon = {
                val icon =
                    if (ui.showPassword) R.drawable.visibility_off_24px
                    else R.drawable.visibility_24px

                Image(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { vm.togglePasswordVisibility() }
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = if (ui.isRegister) ImeAction.Next else ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        if (ui.isRegister) {
            OutlinedTextField(
                value = ui.name,
                onValueChange = vm::updateName,
                label = { Text("Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = ui.age,
                onValueChange = vm::updateAge,
                label = { Text("Age") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            val sexOptions = listOf("Male", "Female", "Other")
            var sexExpanded by remember { mutableStateOf(false) }
            val selectedSex = ui.sex

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopStart)
            ) {
                OutlinedTextField(
                    value = selectedSex,
                    onValueChange = {},
                    label = { Text("Sex") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = if (sexExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropDown,
                            contentDescription = "Toggle sex menu",
                            Modifier.clickable { sexExpanded = !sexExpanded }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { sexExpanded = true }
                )
                DropdownMenu(
                    expanded = sexExpanded,
                    onDismissRequest = { sexExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sexOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                vm.updateSex(option)
                                sexExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }


        if (ui.loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    focus.clearFocus()
                    if (ui.isRegister) vm.register() else vm.login()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (ui.isRegister) "Sign Up" else "Login")
            }

            if (!ui.isRegister) {
                TextButton(
                    onClick = {
                        focus.clearFocus()
                        vm.resetPassword(ui.email)
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Forgot Password?")
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { vm.toggleAuthMode() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (ui.isRegister)
                        "Already have an account? Login"
                    else
                        "Create account"
                )
            }
        }

        ui.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

    }
}
