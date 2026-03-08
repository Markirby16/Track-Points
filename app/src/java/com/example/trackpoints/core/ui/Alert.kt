package com.example.trackpoints.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackpoints.ui.theme.AppFonts

@OptIn(ExperimentalMaterial3Api::class) // BasicAlertDialog is experimental
@Composable
fun Alert(
    onDismissRequest: () -> Unit,
    title: String,
    message: String = "",
    isContrast: Boolean = false,
    backgroundColor: Color = Color(0xFFFFD3AD),
    backgroundBrush: Brush? = null,
    confirmButtonText: String,
    onConfirmClicked: () -> Unit,
    cancelButtonText: String? = null,
    onCancelClicked: () -> Unit = {},
    content: (@Composable () -> Unit)? = null
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .then(
                    if (backgroundBrush != null) Modifier.background(backgroundBrush)
                    else Modifier.background(backgroundColor)
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        color = if (isContrast) Color.White else Color.Black,
                        fontFamily = AppFonts.robotoCondensed,
                        fontSize = 16.3.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    if (content != null) content()
                    if (message.isNotBlank()) Text(
                        text = message,
                        color = if (isContrast) Color.White else Color.Black,
                        fontFamily = AppFonts.roboto,
                        fontSize = 12.sp,
                        lineHeight = 17.3.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(color = Color(0xFF261A36).copy(alpha = 0.6f), thickness = 0.81.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    if (cancelButtonText != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onCancelClicked()
                                    onDismissRequest()
                                }
                                .padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = cancelButtonText,
                                color = if (isContrast) Color.White else Color.Black,
                                fontSize = 15.sp,
                                fontFamily = AppFonts.roboto,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.fillMaxHeight(),
                            color = Color(0xFF261A36).copy(alpha = 0.6f),
                            thickness = 0.81.dp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onConfirmClicked()
                                onDismissRequest()
                            }
                            .padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        val buttonColor =
                            if (cancelButtonText != null) Color(0xFFEE101A) else {
                                if (isContrast) Color.White else Color.Black
                            }

                        Text(
                            text = confirmButtonText,
                            color = buttonColor,
                            fontSize = 15.sp,
                            fontFamily = AppFonts.roboto,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AlertPreview() {
    Alert(
        onDismissRequest = {},
        title = "Change Name",
        message = "Are you sure you want to change your name?",
        confirmButtonText = "Confirm",
        onConfirmClicked = { /* Handle confirm */ },
        cancelButtonText = "Cancel",
        onCancelClicked = { /* Handle cancel */ })
//    Alert(
//        onDismissRequest = {},
//        title = "Event Created",
//        text = "Please wait for the moderator's approval",
//        confirmButtonText = "Ok",
//        onConfirmClicked = { /* Handle OK */ }
//    )
}
