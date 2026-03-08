package com.example.trackpoints.presentation.auth.forgot_password

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.trackpoints.R
import com.example.trackpoints.core.ui.LoadingOverlay
import com.example.trackpoints.ui.theme.AppColors
import com.example.trackpoints.ui.theme.AppFonts
import com.example.trackpoints.ui.theme.AppFonts.robotoCondensed
import kotlinx.coroutines.delay

@Composable
fun VerifyResetScreen(
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
        VerifyResetContent(
            state = state,
            onIntent = viewModel::handleIntent,
        )
        if (state.isLoading) {
            LoadingOverlay()
        }
    }
}

@Composable
fun VerifyResetContent(
    state: ForgotPasswordState,
    onIntent: (ForgotPasswordIntent) -> Unit
) {
    val otpValues =
        remember { mutableStateListOf<String>("", "", "", "", "", "") }
    val focusManager = LocalFocusManager.current

    var submitted by remember { mutableStateOf(false) }
    var hasFilledOut by remember { mutableStateOf(false) }

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
                    "Log-in", style = TextStyle(
                        fontFamily = AppFonts.robotoCondensedItalic,
                        fontSize = 45.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.primary,
                    )
                )
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = "Check your email",
                    fontFamily = AppFonts.roboto,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color(0xFF1E1E1E),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    buildAnnotatedString {
                        append("We sent a verification code to ")
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF545454),
                            )
                        ) {
                            append(state.email)
                        }
                        append(", enter the 6-digit code that is mentioned in the email")
                    },
                    fontFamily = AppFonts.roboto,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF989898),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OTPField(
                    otpValues = otpValues,
                    otpLength = 6,
                    isError = if (!submitted || !hasFilledOut) null else state.emailCodeError.isNotBlank(),
                    onOtpInputComplete = { hasFilledOut = true },
                    onUpdateOtpValuesByIndex = { index, value ->
                        otpValues[index] = value

                        val userOtp = otpValues.joinToString(separator = "")

                        if (userOtp.length < 6) {
                            submitted = false
                            hasFilledOut = false
                            onIntent(ForgotPasswordIntent.EmailCodeErrorChanged(""))
                        } else {
                            onIntent(
                                ForgotPasswordIntent.EmailCodeChanged(userOtp)
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(5.dp))
                if (state.emailCodeError.isNotBlank()) {
                    Text(
                        state.emailCodeError,
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
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (hasFilledOut && state.emailError.isBlank()) {
                            submitted = true
                            onIntent(ForgotPasswordIntent.IsLoadingChanged(true))
                            onIntent(ForgotPasswordIntent.VerifyOtp)
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
                        text = "Verify Code",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                ResendCodeButton(
                    60,
                    {
                        focusManager.clearFocus()
                        onIntent(ForgotPasswordIntent.ResendCode)
                    }, modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun OTPField(
    otpLength: Int,
    onUpdateOtpValuesByIndex: (Int, String) -> Unit,
    onOtpInputComplete: () -> Unit,
    modifier: Modifier = Modifier,
    otpValues: List<String> = List(otpLength) { "" },
    isError: Boolean? = null,
) {
    val focusRequesters = List(otpLength) { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        otpValues.forEachIndexed { index, value ->
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(3.dp)
                    .focusRequester(focusRequesters[index])
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.key == Key.Backspace) {
                            if (otpValues[index].isEmpty() && index > 0) {
                                onUpdateOtpValuesByIndex(index, "")
                                focusRequesters[index - 1].requestFocus()
                            } else {
                                onUpdateOtpValuesByIndex(index, "")
                            }
                            true
                        } else {
                            false
                        }
                    },
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Color(0xFF989898).copy(0.5f),

                    unfocusedBorderColor = Color(0xFF989898),
                    focusedBorderColor =
                        if (isError == false) AppColors.secondary else Color(0xFF989898),

                    errorBorderColor = AppColors.error,
                    errorSupportingTextColor = AppColors.error,

                    errorTextColor = Color(0xFF989898),
                    focusedTextColor = Color(0xFF989898),
                    unfocusedTextColor = Color(0xFF989898),

                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                ),
                value = value,
                onValueChange = { newValue ->
                    // To use OTP code copied from keyboard
                    if (newValue.length == otpLength) {
                        for (i in otpValues.indices) {
                            onUpdateOtpValuesByIndex(
                                i,
                                if (i < newValue.length && newValue[i].isDigit()) newValue[i].toString() else ""
                            )
                        }

                        keyboardController?.hide()
                        onOtpInputComplete()
                    } else if (newValue.length <= 1) {
                        onUpdateOtpValuesByIndex(index, newValue)
                        if (newValue.isNotEmpty()) {
                            if (index < otpLength - 1) {
                                focusRequesters[index + 1].requestFocus()
                            } else {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onOtpInputComplete()
                            }
                        }
                    } else {
                        if (index < otpLength - 1) focusRequesters[index + 1].requestFocus()
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (index == otpLength - 1) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (index < otpLength - 1) {
                            focusRequesters[index + 1].requestFocus()
                        }
                    },
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onOtpInputComplete()
                    }
                ),
                shape = RoundedCornerShape(15.dp),
                isError = isError ?: false,
                textStyle = TextStyle(
                    fontFamily = AppFonts.roboto,
                    fontWeight = FontWeight.Medium,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF989898),
                )
            )

            LaunchedEffect(value) {
                if (otpValues.all { it.isNotEmpty() }) {
                    focusManager.clearFocus()
                    onOtpInputComplete()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequesters.first().requestFocus()
    }
}

@Composable
fun ResendCodeButton(
    totalSeconds: Int, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    var secondsRemaining by remember { mutableStateOf(totalSeconds) }
    val isTimerRunning = secondsRemaining > 0

    LaunchedEffect(key1 = secondsRemaining) {
        if (isTimerRunning) {
            delay(1000L)
            secondsRemaining--
        }
    }

    TextButton(
        onClick = {
            onClick()
            secondsRemaining = totalSeconds
        }, enabled = !isTimerRunning, modifier = modifier, colors = ButtonColors(
            containerColor = Color.Unspecified,
            disabledContainerColor = Color.Unspecified,

            contentColor = Color(0xFF989898),
            disabledContentColor = Color(0xFF989898).copy(alpha = 0.5f)
        )
    ) {
        val buttonText = if (isTimerRunning) {
            "Resend Code ${secondsRemaining}s"
        } else {
            "Resend Code"
        }
        Text(
            text = buttonText, textAlign = TextAlign.End, style = TextStyle(
                fontFamily = AppFonts.roboto,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
            )
        )
    }
}
