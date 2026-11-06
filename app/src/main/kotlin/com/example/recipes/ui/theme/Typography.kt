package com.example.recipes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.recipes.R

val InterFontFamily = FontFamily(
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

val DmSansFontFamily = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
    Font(R.font.dm_sans_semibold, FontWeight.SemiBold),
)

private val headingStyle = TextStyle(fontFamily = InterFontFamily, color = RecipesColors.textPrimary)
private val bodyStyle = TextStyle(fontFamily = DmSansFontFamily, color = RecipesColors.textPrimary)

/** Labels have no colour of their own, so buttons and chips can set it through their content colour. */
private val labelStyle = TextStyle(fontFamily = DmSansFontFamily)

val RecipesTypography = Typography(
    // Screen title: 30 sp bold, letter spacing -0.02 em.
    headlineLarge = headingStyle.copy(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em),
    headlineMedium = headingStyle.copy(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em),
    // Card titles: 15-17 sp semibold.
    titleLarge = headingStyle.copy(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = headingStyle.copy(fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = headingStyle.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = bodyStyle.copy(fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = bodyStyle.copy(fontSize = 15.sp, lineHeight = 21.sp),
    bodySmall = bodyStyle.copy(fontSize = 13.sp, lineHeight = 18.sp, color = RecipesColors.textMuted),
    labelLarge = labelStyle.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = labelStyle.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
    labelSmall = labelStyle.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
)
