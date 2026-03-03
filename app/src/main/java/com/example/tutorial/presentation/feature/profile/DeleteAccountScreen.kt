package com.example.tutorial.com.example.tutorial.presentation.feature.profile

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.tutorial.R
import com.example.tutorial.com.example.tutorial.presentation.feature.auth.AuthViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource

@Composable
fun DeleteAccountScreen(
    nav: NavController,
    vm: ProfileViewModel = hiltViewModel(),
    authVm: AuthViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
) {
    val ui by vm.ui.collectAsState()
    var pwd by remember { mutableStateOf("") }
    var showPwd by remember { mutableStateOf(false) }

    LaunchedEffect(ui.deleteDone) {
        if (ui.deleteDone) {
            authVm.logout()
            nav.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Delete account?", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = pwd,
                onValueChange = { pwd = it },
                label = { Text("Current password") },
                singleLine = true,
                visualTransformation =
                if (showPwd) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (showPwd) R.drawable.visibility_off_24px
                        else R.drawable.visibility_24px
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showPwd = !showPwd }
                    )
                }
            )

            Spacer(Modifier.height(24.dp))

            if (ui.loading) {
                CircularProgressIndicator()
            } else {
                Row {
                    OutlinedButton({ nav.popBackStack() }) { Text("Cancel") }
                    Spacer(Modifier.width(16.dp))
                    Button(
                        onClick = { vm.deleteAccount(pwd) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Delete") }
                }
                ui.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
