package com.example.nutritiontracker.feature.goals.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutritiontracker.core.ui.NutritionAnimatedContent

@Composable
fun GoalsFeatureScreen(
    modifier: Modifier = Modifier,
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var applying by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = applying) {
        applying = false
    }

    NutritionAnimatedContent(
        targetState = applying,
        modifier = modifier,
        label = "goals-page",
        isForward = { from, to -> !from && to },
    ) { isApplying ->
        if (isApplying) {
            ApplyGoalTemplateScreen(
                uiState = uiState,
                onTemplateSelected = viewModel::onTemplateSelected,
                onStartDateChange = viewModel::onApplyStartDateChange,
                onDayCountChange = viewModel::onApplyDayCountChange,
                onSkippedDatesChange = viewModel::onSkippedDatesChange,
                onApply = { viewModel.applyTemplate() },
                modifier = Modifier,
            )
        } else {
            GoalsScreen(
                uiState = uiState,
                onTemplateNameChange = viewModel::onTemplateNameChange,
                onProteinChange = viewModel::onProteinChange,
                onFatChange = viewModel::onFatChange,
                onCarbChange = viewModel::onCarbChange,
                onSaveTemplate = { viewModel.saveTemplate() },
                onDeleteTemplate = viewModel::deleteTemplate,
                onOpenApply = { applying = true },
                modifier = Modifier,
            )
        }
    }
}
