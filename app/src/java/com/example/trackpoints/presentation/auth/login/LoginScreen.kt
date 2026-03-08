package com.example.trackpoints.presentation.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.trackpoints.R
import com.example.trackpoints.core.ui.Alert
import com.example.trackpoints.core.ui.CustomTextField
import com.example.trackpoints.core.ui.LoadingOverlay
import com.example.trackpoints.core.ui.PasswordField
import com.example.trackpoints.presentation.auth.sign_up.SignUpIntent
import com.example.trackpoints.presentation.main.MainIntent
import com.example.trackpoints.ui.theme.AppColors
import com.example.trackpoints.ui.theme.AppFonts
import com.example.trackpoints.ui.theme.AppFonts.robotoCondensed

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateSignUp: () -> Unit,
    onNavigateForgotPassword: () -> Unit,
    onNavigateBack: () -> Unit,
    onLogin: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LoginEffect.NavigateToSignUp -> onNavigateSignUp()
                LoginEffect.NavigateToForgotPassword -> onNavigateForgotPassword()
                LoginEffect.NavigateBack -> onNavigateBack()
                LoginEffect.NavigateToNext -> onLogin()
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        LoginContent(
            state = state,
            onIntent = viewModel::handleIntent,
        )

        if (state.isLoading) {
            LoadingOverlay()
        }

        when {
            state.loginStatus.contains("rejected") -> {
                Alert(
                    onDismissRequest = {
                        viewModel.handleIntent(LoginIntent.LoginStatusChanged(""))
                    },
                    title = "Rejected Request for Approval",
                    message = state.loginStatus,
                    confirmButtonText = "Ok",
                    onConfirmClicked = {
                        viewModel.handleIntent(LoginIntent.LoginStatusChanged(""))
                    },
                )
            }
            state.loginStatus.isNotBlank() -> {
                Alert(
                    onDismissRequest = {
                        viewModel.handleIntent(LoginIntent.LoginStatusChanged(""))
                    },
                    title = "Request for Approval",
                    message = state.loginStatus,
                    confirmButtonText = "Ok",
                    onConfirmClicked = {
                        viewModel.handleIntent(LoginIntent.LoginStatusChanged(""))
                    },
                )
            }
        }
    }
}

@Composable
fun LoginContent(state: LoginState, onIntent: (LoginIntent) -> Unit) {

    var submittedOnce by remember { mutableStateOf(false) }
    val hasFilledOut = state.email.isNotBlank() && state.password.isNotBlank()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFEF3E2))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp)
                    .padding(paddingValues)
                    .padding(horizontal = 45.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.user_icon),
                    contentDescription = "User Icon",
                    modifier = Modifier
                        .size(110.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Log-in", style = TextStyle(
                        fontFamily = AppFonts.robotoCondensedItalic,
                        fontSize = 45.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.primary,
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                CustomTextField(
                    state.email,
                    labelText = "Email",
                    onValueChange = { onIntent(LoginIntent.EmailChanged(it)) },
                    isError = if (!submittedOnce) null else state.emailOrPasswordError.isNotBlank(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Unspecified,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Unspecified
                    ),
                )
                Spacer(Modifier.height(5.dp))
                PasswordField(
                    state.password,
                    labelText = "Password",
                    onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
                    isError = if (!submittedOnce) null else state.emailOrPasswordError.isNotBlank()
                )
                Spacer(modifier = Modifier.height(5.dp))
                if (state.emailOrPasswordError.isNotBlank()) {
                    Text(
                        state.emailOrPasswordError,
                        style = TextStyle(
                            fontFamily = robotoCondensed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.error,
                        ),
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .align(Alignment.Start)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Forgot Password",
                    style = TextStyle(
                        fontFamily = robotoCondensed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA2A2A2),
                    ),
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .align(Alignment.Start)
                        .clickable { onIntent(LoginIntent.ForgotPasswordClicked) }
                )
                Spacer(Modifier.height(15.dp))
                Button(
                    onClick = {
                        if (hasFilledOut) {
                            submittedOnce = true
                            onIntent(LoginIntent.IsLoadingChanged(true))
                            onIntent(LoginIntent.LoginClicked)
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.secondary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .dropShadow(
                            shadow = Shadow(
                                radius = 4.dp,
                                offset = DpOffset(x = 0.dp, y = 4.dp),
                                color = Color.Black.copy(0.25f)
                            ),
                            shape = RoundedCornerShape(30.dp)
                        )
                        .fillMaxWidth()
                        .height(45.dp)
                ) {
                    Text(
                        text = "Login",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "or",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFFA2A2A2),
                )
                Spacer(modifier = Modifier.height(5.dp))
                Button(
                    modifier = Modifier
                        .dropShadow(
                            shadow = Shadow(
                                radius = 4.dp,
                                offset = DpOffset(x = 0.dp, y = 4.dp),
                                color = Color.Black.copy(0.25f)
                            ),
                            shape = RoundedCornerShape(30.dp)
                        )
                        .fillMaxWidth()
                        .height(45.dp),
                    onClick = {
                        onIntent(LoginIntent.SignUpClicked)
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.secondary, contentColor = Color.White
                    ),
                ) {
                    Text(
                        text = "Sign-up", fontWeight = FontWeight.Bold, fontSize = 18.sp
                    )
                }
            }
        }
    }
}