package com.example.rmas.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rmas.presentation.login.LoginUIEvent
import com.example.rmas.viewmodels.LoginViewModel

@Composable
fun LoginScreen(navController: NavController, loginViewModel: LoginViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val state = loginViewModel.loginUIState.collectAsState()
    val visible = rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 28.dp, end = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(Color(0xFF415F91), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Explore,
                    contentDescription = "",
                    tint = Color.White,
                    modifier = Modifier.size(100.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp)),
                label = { Text(text = "Korisničko ime") },
                keyboardOptions = KeyboardOptions.Default,
                value = state.value.username,
                onValueChange = {
                    loginViewModel.onEvent(
                        LoginUIEvent.UsernameChanged(it),
                        context,
                        navigateToHome = { })
                },
                isError = state.value.usernameError != null,
                supportingText = {
                    if (state.value.usernameError != null) {
                        Text(
                            text = state.value.usernameError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp)),
                label = { Text(text = "Šifra") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                value = state.value.password,
                onValueChange = {
                    loginViewModel.onEvent(
                        LoginUIEvent.PasswordChanged(it),
                        context,
                        navigateToHome = { })
                },
                isError = state.value.passwordError != null,
                trailingIcon = {
                    val iconImage = if (visible.value) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    }
                    IconButton(onClick = { visible.value = !visible.value }) {
                        Icon(imageVector = iconImage, contentDescription = null)
                    }
                },
                visualTransformation =
                if (visible.value)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                supportingText = {
                    if (state.value.passwordError != null) {
                        Text(
                            text = state.value.passwordError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    loginViewModel.onEvent(
                        LoginUIEvent.LoginButtonClicked,
                        context,
                        navigateToHome = { navController.navigate("HomeScreen") })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(48.dp),
                contentPadding = PaddingValues()
            ) {
                if (loginViewModel.loginInProgress.value) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.inversePrimary,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Prijavi se",
                        fontSize = 18.sp,
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(
                onClick = { navController.navigate("SingUpScreen") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Registruj se",
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

            }
        }
    }
}