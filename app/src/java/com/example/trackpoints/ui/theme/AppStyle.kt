package com.example.trackpoints.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.trackpoints.R

object AppColors {
    val primary = Color(0xFFDD0303)
    val secondary = Color(0xFFFA812F)
    val success = Color(0xFF22E222)
    val error = Color(0xFFED2121)
}

object AppFonts {
    val roboto = FontFamily(Font(R.font.roboto))
    val robotoItalic = FontFamily(Font(R.font.roboto_italic))
    val robotoCondensed = FontFamily(Font(R.font.roboto_condensed))
    val robotoCondensedItalic = FontFamily(Font(R.font.roboto_condensed_italic))
}
