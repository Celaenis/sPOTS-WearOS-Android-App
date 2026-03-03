package com.example.tutorial.com.example.tutorial.presentation.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.tutorial.com.example.tutorial.presentation.feature.auth.AuthViewModel
import com.example.tutorial.com.example.tutorial.presentation.components.ConfirmDialog

@Composable
fun MyPageScreen(
    nav: NavController,
    authVm: AuthViewModel,
    profileVm: ProfileViewModel = hiltViewModel()
) {
    val profile by profileVm.ui.collectAsState()
    val name = profile.profile?.displayName ?: ""
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("My Page", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Welcome, $name!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { nav.navigate("my_page/account_info") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Account info")
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { nav.navigate("my_page/settings") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Settings")
            }

            Spacer(Modifier.height(16.dp))

            var askLogout by remember { mutableStateOf(false) }
            var askDelete by remember { mutableStateOf(false) }

            Button(
                onClick = { askLogout = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { askDelete = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete account")
            }

            if (askLogout) {
                ConfirmDialog(
                    title = "Logout",
                    text = "Do you really want to sign out?",
                    onConfirm = {
                        askLogout = false
                        authVm.logout()
                        nav.navigate("login") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onDismiss = { askLogout = false }
                )
            }

            if (askDelete) {
                ConfirmDialog(
                    title = "Delete account",
                    text = "This will remove your data permanently. Continue?",
                    onConfirm = {
                        askDelete = false
                        nav.navigate("my_page/delete_account")
                    },
                    onDismiss = { askDelete = false }
                )
            }
        }
    }
}