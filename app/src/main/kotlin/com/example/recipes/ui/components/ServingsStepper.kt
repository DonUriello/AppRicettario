package com.example.recipes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.recipes.R
import com.example.recipes.ui.theme.RecipesColors

/** "Meno / N / Più" stepper. */
@Composable
fun ServingsStepper(
    servings: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    valueDescription: String? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PillButton(
            text = stringResource(R.string.action_decrease),
            onClick = onDecrease,
            enabled = servings > 1,
            style = PillStyle.Tonal,
        )
        Text(
            text = servings.toString(),
            modifier = Modifier
                .widthIn(min = 32.dp)
                .semantics { if (valueDescription != null) contentDescription = valueDescription },
            style = MaterialTheme.typography.titleLarge,
            color = RecipesColors.textPrimary,
            textAlign = TextAlign.Center,
        )
        PillButton(
            text = stringResource(R.string.action_increase),
            onClick = onIncrease,
            style = PillStyle.Tonal,
        )
    }
}
