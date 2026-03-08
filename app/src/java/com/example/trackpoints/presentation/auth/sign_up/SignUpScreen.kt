package com.example.trackpoints.presentation.auth.sign_up

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.semantics.Role
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
import com.example.trackpoints.ui.theme.AppColors
import com.example.trackpoints.ui.theme.AppFonts
import com.example.trackpoints.ui.theme.AppFonts.robotoCondensed

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel,
    onNavigateBack: () -> Unit,
    onNavigateLogin: () -> Unit,
    onNavigateNext: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SignUpEffect.NavigateToHome -> {}
                SignUpEffect.NavigateBack -> onNavigateBack()
                SignUpEffect.NavigateToNext -> onNavigateNext()
                SignUpEffect.NavigateToLogin -> {
                    onNavigateLogin()
                    Log.d("SIGNUP", "HELLO ")
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        SignUpContent(
            state = state,
            onIntent = viewModel::handleIntent,
        )

        if (state.isLoading) {
            LoadingOverlay()
        }

        when {
            state.signUpError.isNotBlank() -> {
                Alert(
                    onDismissRequest = {
                        viewModel.handleIntent(SignUpIntent.SignUpErrorChanged(""))
                    },
                    title = "Error",
                    message = state.signUpError,
                    confirmButtonText = "Ok",
                    onConfirmClicked = {
                        viewModel.handleIntent(SignUpIntent.SignUpErrorChanged(""))
                    },
                )
            }
        }
    }
}


@Composable
fun SignUpContent(state: SignUpState, onIntent: (SignUpIntent) -> Unit) {

    var submittedOnce by remember { mutableStateOf(false) }
    val hasFilledOut =
        state.fullName.isNotBlank() && state.email.isNotBlank() && state.password.isNotBlank() && state.confirmPassword.isNotBlank()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFEF3E2))
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
                    .padding(top = 80.dp)
                    .padding(horizontal = 45.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.user_icon),
                    contentDescription = "User Icon",
                    modifier = Modifier.size(110.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Sign-up", style = TextStyle(
                        fontFamily = AppFonts.robotoCondensedItalic,
                        fontSize = 45.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.primary,
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                CustomTextField(
                    state.fullName,
                    labelText = "Full Name",
                    isError = if (!submittedOnce) null else state.fullNameError.isNotBlank(),
                    supportingText = state.fullNameError,
                    onValueChange = { onIntent(SignUpIntent.FullNameChanged(it)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Unspecified,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Unspecified
                    ),
                )
                CustomTextField(
                    state.email,
                    labelText = "Email",
                    onValueChange = { onIntent(SignUpIntent.EmailChanged(it)) },
                    isError = if (!submittedOnce) null else state.emailError.isNotBlank(),
                    supportingText = state.emailError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Unspecified,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Unspecified
                    ),
                )
                PasswordField(
                    state.password, labelText = "Password",
                    onValueChange = { onIntent(SignUpIntent.PasswordChanged(it)) },
                    isError = if (!submittedOnce) null else state.passwordError.isNotBlank(),
                    supportingText = state.passwordError,
                )
                PasswordField(
                    state.confirmPassword, labelText = "Confirm Password",
                    onValueChange = { onIntent(SignUpIntent.ConfirmPasswordChanged(it)) },
                    isError = if (!submittedOnce) null else state.confirmPasswordError.isNotBlank(),
                    supportingText = state.confirmPasswordError,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    listOf("Client", "Freelancer").forEach { text ->
                        Row(
                            modifier = Modifier
                                .selectable(
                                    selected = (text.lowercase() == state.role.lowercase()),
                                    onClick = { onIntent(SignUpIntent.RoleChanged(text)) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (text.lowercase() == state.role.lowercase()),
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFFFA812F),
                                    unselectedColor = Color(0xFFA2A2A2)
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = text,
                                color = if (text.lowercase() == state.role.lowercase()) Color(
                                    0xFFFA812F
                                )
                                else Color(0xFFA2A2A2),
                                fontWeight = FontWeight.Medium,
                                fontFamily = robotoCondensed,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
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
                        if (hasFilledOut) {
                            submittedOnce = true
                            onIntent(SignUpIntent.IsLoadingChanged(true))
                            onIntent(SignUpIntent.SignUpClicked)
                        }
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
                        Log.d("SIGNUP", "CLICKED")
                        onIntent(SignUpIntent.LoginClicked)
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.secondary, contentColor = Color.White
                    ),
                ) {
                    Text(
                        text = "Login", fontWeight = FontWeight.Bold, fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SignUpScreenPreview() {
    SignUpContent(state = SignUpState(), onIntent = {})
}