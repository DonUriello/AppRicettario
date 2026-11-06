package com.example.recipes.ui.recipelist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipes.R
import com.example.recipes.ui.components.CardTitle
import com.example.recipes.ui.components.GlassCard
import com.example.recipes.ui.components.PillButton
import com.example.recipes.ui.components.PillStyle
import com.example.recipes.ui.components.RecipesTextField
import com.example.recipes.ui.theme.RecipesColors

/** "Importa da link" (spec §8.1): paste a URL, get a draft to review. */
@Composable
fun LinkImportCard(
    onDraftCreated: (String) -> Unit,
    onCreateManually: (String) -> Unit,
    viewModel: LinkImportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LinkImportEvent.DraftCreated -> onDraftCreated(event.recipeId)
                is LinkImportEvent.CreateManually -> onCreateManually(event.sourceUrl)
            }
        }
    }

    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CardTitle(text = stringResource(R.string.import_link_title))
            Text(text = stringResource(R.string.import_link_hint), style = MaterialTheme.typography.bodySmall)
            RecipesTextField(
                value = uiState.url,
                onValueChange = viewModel::onUrlChange,
                label = stringResource(R.string.import_link_field),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { viewModel.import() }),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                PillButton(
                    text = stringResource(R.string.import_link_action),
                    onClick = viewModel::import,
                    enabled = uiState.canImport,
                    style = PillStyle.Primary,
                    iconResource = R.drawable.ic_link,
                )
                if (uiState.isImporting) {
                    Spacer(Modifier.width(12.dp))
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = RecipesColors.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(text = stringResource(R.string.import_link_loading), style = MaterialTheme.typography.bodySmall)
                }
            }
            uiState.problem?.let { problem ->
                val message = when (problem) {
                    LinkImportProblem.INVALID_URL -> R.string.import_error_invalid_url
                    LinkImportProblem.DOWNLOAD_FAILED -> R.string.import_error_download
                    LinkImportProblem.NO_RECIPE_FOUND -> R.string.import_error_no_recipe
                }
                Text(
                    text = stringResource(message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = RecipesColors.warningText,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
                if (problem != LinkImportProblem.INVALID_URL) {
                    PillButton(
                        text = stringResource(R.string.import_create_manually),
                        onClick = viewModel::createManually,
                        style = PillStyle.Tonal,
                    )
                }
            }
        }
    }
}
