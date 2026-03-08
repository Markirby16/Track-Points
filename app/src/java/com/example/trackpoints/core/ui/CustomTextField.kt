package com.example.trackpoints.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackpoints.R
import com.example.trackpoints.ui.theme.AppColors
import com.example.trackpoints.ui.theme.AppFonts

@Composable
fun CustomTextField(
    text: String = "",
    labelText: String,
    supportingText: String = "",
    placeHolderText: String = "",
    isError: Boolean? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(15.dp),
    outlineColor: Color = Color(0xFFA2A2A2),
    containerColor: Color = Color(0xFFDADADA).copy(0.8f),
    contentColor: Color = Color(0xFFA2A2A2),
    minLines: Int = 1,
    maxLines: Int = 1,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation? = null,
    onValueChange: (String) -> Unit = {},
    prefix: @Composable (() -> Unit)? = null,
    iconButton: @Composable (() -> Unit)? = null,
) {

    var isFocused by remember { mutableStateOf(false) }
    val targetFontSize = if (isFocused || text.isNotEmpty()) 15f else 18f
    val animatedFontSize by animateFloatAsState(
        targetValue = targetFontSize,
        animationSpec = tween(durationMillis = 100),
        label = "fontSizeAnimation"
    )

    OutlinedTextField(
        value = text,
        onValueChange = onValueChange,
        isError = isError ?: false,
        prefix = prefix,
        minLines = minLines,
        maxLines = maxLines,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            },
        keyboardOptions = keyboardOptions,
        textStyle = TextStyle(
            fontFamily = AppFonts.roboto,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
        ),
        readOnly = readOnly,
        label =
            if (labelText.isNotBlank()) {
                {
                    Text(
                        labelText,
                        style = TextStyle(
                            fontFamily = AppFonts.robotoCondensed,
                            fontSize = animatedFontSize.sp,
                            fontWeight = FontWeight.Medium,
                            color = contentColor.copy(alpha = 0.5f),
                        ),
                    )
                }
            } else null,
        placeholder = if (placeHolderText.isNotBlank()) {
            {
                Text(
                    placeHolderText,
                    style = TextStyle(
                        fontFamily = AppFonts.robotoCondensed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = contentColor.copy(alpha = 0.5f),
                    ),
                )
            }
        } else null,
        supportingText =
            if (isError != null && isError && supportingText.isNotBlank()) {
                {
                    Text(
                        supportingText,
                        textAlign = TextAlign.Start,
                        style = TextStyle(
                            fontFamily = AppFonts.robotoCondensed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.error
                        ),
                    )
                }
            } else null,
        shape = shape,
        singleLine = maxLines <= 1,
        visualTransformation = visualTransformation ?: VisualTransformation.None,
        trailingIcon = if (text.isNotBlank() && iconButton != null) {
            { iconButton.invoke() }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = contentColor,

            unfocusedBorderColor = if (isError == false) AppColors.success else outlineColor,
            focusedBorderColor = if (isError == false) AppColors.success else outlineColor,
            errorBorderColor = AppColors.error,
            errorSupportingTextColor = AppColors.error,

            errorTextColor = contentColor,
            focusedTextColor = contentColor,
            unfocusedTextColor = contentColor.copy(0.8f),

            errorContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            focusedContainerColor = containerColor,
        ),
    )
}

@Composable
fun PasswordField(
    text: String = "",
    labelText: String = "Create Password",
    supportingText: String = "",
    placeHolderText: String = "",
    isError: Boolean? = null,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(15.dp),
    outlineColor: Color = Color(0xFFA2A2A2),
    iconColor: Color = Color(0xFFA2A2A2),
    onValueChange: (String) -> Unit = {},
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    CustomTextField(
        text,
        labelText,
        supportingText,
        placeHolderText = placeHolderText,
        isError = isError,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Unspecified,
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Unspecified
        ),
        modifier = modifier,
        shape = shape,
        outlineColor = outlineColor,
        onValueChange = onValueChange,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        iconButton = {
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        modifier = Modifier.size(28.dp),
                        painter = painterResource(
                            if (isPasswordVisible) R.drawable.visibility else R.drawable.visibility_off,
                        ),
                        contentDescription = "Toggle Password Visibility",
                        tint = iconColor,
                    )
                }
            }
        },
    )
}

@Composable
fun ClearTextField(
    text: String = "",
    labelText: String = "",
    supportingText: String = "",
    placeHolderText: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean? = null,
    modifier: Modifier = Modifier,
    outlineColor: Color = Color(0xFFA2A2A2),
    containerColor: Color = Color(0xFFDADADA).copy(0.8f),
    contentColor: Color = Color(0xFFA2A2A2),
    minLines: Int = 1,
    maxLines: Int = 1,
    iconColor: Color = Color(0xFFA2A2A2),
    onValueChange: (String) -> Unit = {},
    onClick: () -> Unit = {},
) {

    CustomTextField(
        text,
        labelText,
        supportingText,
        placeHolderText,
        isError = isError,
        keyboardOptions = keyboardOptions,
        modifier = modifier,
        minLines = minLines,
        maxLines = maxLines,
        outlineColor = outlineColor,
        containerColor = containerColor,
        contentColor = contentColor,
        onValueChange = onValueChange,
        iconButton = {
            IconButton(onClick = onClick) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.clear),
                    contentDescription = "Clear Input Text",
                    tint = iconColor,
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCustomInputField() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF261A36)),
        ) {
            CustomTextField(labelText = "First Name")
            CustomTextField(labelText = "Last Name")
            CustomTextField(labelText = "Email")
            CustomTextField(labelText = "@Username")
            PasswordField { }
            PasswordField(labelText = "Confirm Password") {}
            PasswordField("Hello") { }
            ClearTextField("Hello") { }
        }
    }
}
