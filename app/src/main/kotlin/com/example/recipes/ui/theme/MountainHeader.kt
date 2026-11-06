package com.example.recipes.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale

private const val HEADER_WIDTH = 390f
private const val HEADER_HEIGHT = 280f

/** Builds the three layers of spec §7.5 in a 390 × 280 coordinate space. */
private fun backRidgePath() = Path().apply {
    moveTo(0f, 150f); lineTo(55f, 108f); lineTo(105f, 136f); lineTo(168f, 78f); lineTo(236f, 132f)
    lineTo(298f, 96f); lineTo(390f, 146f); lineTo(390f, 280f); lineTo(0f, 280f); close()
}

private fun frontRidgePath() = Path().apply {
    moveTo(0f, 196f); lineTo(74f, 158f); lineTo(142f, 186f); lineTo(214f, 148f); lineTo(292f, 182f)
    lineTo(390f, 160f); lineTo(390f, 280f); lineTo(0f, 280f); close()
}

private fun fogPath() = Path().apply {
    moveTo(0f, 236f)
    cubicTo(86f, 214f, 168f, 228f, 248f, 216f)
    cubicTo(318f, 206f, 360f, 218f, 390f, 222f)
    lineTo(390f, 280f); lineTo(0f, 280f); close()
}

/** Soft mountain landscape drawn at the top of each main screen, scaled to the screen width. */
@Composable
fun MountainHeader(modifier: Modifier = Modifier) {
    val backRidge = backRidgePath()
    val frontRidge = frontRidgePath()
    val fog = fogPath()
    Canvas(modifier = modifier.fillMaxWidth().aspectRatio(HEADER_WIDTH / HEADER_HEIGHT)) {
        val scaleFactor = size.width / HEADER_WIDTH
        scale(scale = scaleFactor, pivot = androidx.compose.ui.geometry.Offset.Zero) {
            drawPath(backRidge, RecipesColors.backRidge)
            drawPath(frontRidge, RecipesColors.frontRidge)
            drawPath(fog, RecipesColors.fog)
        }
    }
}
