package com.example.trackpoints.presentation.auth.forgot_password

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.trackpoints.R
import com.example.trackpoints.core.ui.LoadingOverlay
import com.example.trackpoints.core.ui.PasswordField
import com.example.trackpoints.ui.theme.AppColors
import com.example.trackpoints.ui.theme.AppFonts

@Composable
fun ResetPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ForgotPasswordEffect.NavigateToLogin -> {}
                ForgotPasswordEffect.NavigateBack -> onNavigateBack()
                ForgotPasswordEffect.NavigateToNext -> onNavigateNext()
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        ResetPasswordContent(
            state = state,
            onIntent = viewModel::handleIntent,
        )
        if (state.isLoading) {
            LoadingOverlay()
        }
    }
}

@Composable
fun ResetPasswordContent(
    state: ForgotPasswordState,
    onIntent: (ForgotPasswordIntent) -> Unit
) {

    var submitted by remember { mutableStateOf(false) }
    val hasFilledOut = state.newPassword.isNotBlank() && state.confirmNewPassword.isNotBlank()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFEF3E2))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(top = 80.dp)
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
                    "Reset Password", style = TextStyle(
                        fontFamily = AppFonts.robotoCondensedItalic,
                        fontSize = 45.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.primary,
                    )
                )
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = "Reset password",
                    fontFamily = AppFonts.roboto,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color(0xFF1E1E1E),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                PasswordField(
                    text = state.newPassword,
                    isError = if (!submitted) null else state.newPasswordError.isNotBlank() ||
                            state.confirmNewPasswordError.isNotBlank(),
                    onValueChange = { onIntent(ForgotPasswordIntent.NewPasswordChanged(it)) },
                    supportingText = state.newPasswordError,
                    labelText = "New Password"
                )
                Spacer(modifier = Modifier.height(5.dp))
                PasswordField(
                    text = state.confirmNewPassword,
                    isError = if (!submitted) null else state.confirmNewPasswordError.isNotBlank(),
                    supportingText = state.confirmNewPasswordError,
                    onValueChange = {
                        onIntent(ForgotPasswordIntent.ConfirmNewPasswordChanged(it))
                        submitted = false
                    },
                    labelText = "Confirm New Password"
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (hasFilledOut) {
                            submitted = true
                            onIntent(ForgotPasswordIntent.IsLoadingChanged(true))
                            onIntent(ForgotPasswordIntent.SubmitResetPasswordClicked)
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
                        text = "Reset Password",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}