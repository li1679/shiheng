package com.example.nutritiontracker.feature.today.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutritiontracker.core.model.MealType
import com.example.nutritiontracker.core.ui.NutritionAnimatedContent
import com.example.nutritiontracker.core.ui.nutritionEdgeSwipeBack
import com.example.nutritiontracker.feature.diary.presentation.AddFoodEntryScreen
import com.example.nutritiontracker.feature.diary.presentation.DiaryViewModel
import com.example.nutritiontracker.feature.diary.presentation.MealEntriesScreen
import com.example.nutritiontracker.feature.water.presentation.WaterViewModel
import com.example.nutritiontracker.feature.weight.presentation.WeightViewModel

@Composable
fun TodayFeatureScreen(
    modifier: Modifier = Modifier,
    todayViewModel: TodayViewModel = hiltViewModel(),
    diaryViewModel: DiaryViewModel = hiltViewModel(),
    waterViewModel: WaterViewModel = hiltViewModel(),
    weightViewModel: WeightViewModel = hiltViewModel(),
    goalQuickApplyViewModel: TodayGoalQuickApplyViewModel = hiltViewModel(),
) {
    val todayUiState by todayViewModel.uiState.collectAsStateWithLifecycle()
    val diaryUiState by diaryViewModel.uiState.collectAsStateWithLifecycle()
    val waterUiState by waterViewModel.uiState.collectAsStateWithLifecycle()
    val weightUiState by weightViewModel.uiState.collectAsStateWithLifecycle()
    val goalQuickApplyUiState by goalQuickApplyViewModel.uiState.collectAsStateWithLifecycle()
    var selectedMealType by rememberSaveable { mutableStateOf<MealType?>(null) }
    var addingEntry by rememberSaveable { mutableStateOf(false) }
    var showWaterDetails by rememberSaveable { mutableStateOf(false) }
    var showGoalSettings by rememberSaveable { mutableStateOf(false) }
    var showWeightDetails by rememberSaveable { mutableStateOf(false) }
    var showWeightRecord by rememberSaveable { mutableStateOf(false) }
    var editingMacroGoal by rememberSaveable { mutableStateOf<MacroGoalType?>(null) }
    var macroGoalInput by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(todayUiState.summary.date) {
        waterViewModel.selectDate(todayUiState.summary.date)
        weightViewModel.selectDate(todayUiState.summary.date)
    }

    val pageState = resolveTodayPageState(
        showGoalSettings = showGoalSettings,
        showWaterDetails = showWaterDetails,
        showWeightDetails = showWeightDetails,
        selectedMealType = selectedMealType,
        addingEntry = addingEntry,
    )

    fun goBack() {
        when {
            showWeightRecord -> showWeightRecord = false
            editingMacroGoal != null -> editingMacroGoal = null
            showGoalSettings -> showGoalSettings = false
            showWaterDetails -> showWaterDetails = false
            showWeightDetails -> showWeightDetails = false
            addingEntry -> addingEntry = false
            selectedMealType != null -> selectedMealType = null
        }
    }

    BackHandler(enabled = pageState != TodayPageState.Home || showWeightRecord || editingMacroGoal != null) {
        goBack()
    }

    NutritionAnimatedContent(
        targetState = pageState,
        modifier = modifier.nutritionEdgeSwipeBack(
            enabled = pageState != TodayPageState.Home,
            onBack = ::goBack,
        ),
        label = "today-page",
        isForward = { from, to -> to.order >= from.order },
    ) { state ->
        when (state) {
            TodayPageState.GoalSettings -> GoalSettingsScreen(
                uiState = goalQuickApplyUiState,
                onGoalTemplateSelected = goalQuickApplyViewModel::selectTemplate,
                onManualProteinGoalChange = goalQuickApplyViewModel::onManualProteinGoalChange,
                onManualFatGoalChange = goalQuickApplyViewModel::onManualFatGoalChange,
                onManualCarbGoalChange = goalQuickApplyViewModel::onManualCarbGoalChange,
                onSaveManualGoal = { goalQuickApplyViewModel.saveManualGoal(todayUiState.summary.date) },
                onApplyGoalToSelectedDate = { goalQuickApplyViewModel.applyToDate(todayUiState.summary.date) },
                onApplyGoalForNextThirtyDays = { goalQuickApplyViewModel.applyForNextThirtyDays(todayUiState.summary.date) },
                modifier = Modifier,
            )

            TodayPageState.WaterDetails -> WaterDetailsScreen(
                waterUiState = waterUiState,
                onQuickAddWater = waterViewModel::quickAdd,
                onDeleteWaterEntry = waterViewModel::deleteEntry,
                modifier = Modifier,
            )

            TodayPageState.WeightDetails -> WeightDetailsScreen(
                weightUiState = weightUiState,
                onOpenWeightRecord = { showWeightRecord = true },
                modifier = Modifier,
            )

            TodayPageState.Home -> TodayScreen(
                uiState = todayUiState,
                waterUiState = waterUiState,
                weightUiState = weightUiState,
                onMealClick = { selectedMeal ->
                    diaryViewModel.openMeal(todayUiState.summary.date, selectedMeal)
                    selectedMealType = selectedMeal
                    addingEntry = false
                },
                onPreviousDate = todayViewModel::selectPreviousDate,
                onNextDate = todayViewModel::selectNextDate,
                onSelectDate = todayViewModel::selectDate,
                onSelectToday = todayViewModel::selectToday,
                onQuickAddWater = waterViewModel::quickAdd,
                onOpenWaterDetails = { showWaterDetails = true },
                onOpenGoalSettings = { showGoalSettings = true },
                onOpenMacroQuickEdit = { type ->
                    editingMacroGoal = type
                    macroGoalInput = when (type) {
                        MacroGoalType.Protein -> todayUiState.summary.dailyGoal?.proteinGoalGram?.toString().orEmpty()
                        MacroGoalType.Fat -> todayUiState.summary.dailyGoal?.fatGoalGram?.toString().orEmpty()
                        MacroGoalType.Carb -> todayUiState.summary.dailyGoal?.carbGoalGram?.toString().orEmpty()
                    }
                },
                onOpenWeightDetails = { showWeightDetails = true },
                onOpenWeightRecord = { showWeightRecord = true },
                onCopyYesterday = { diaryViewModel.copyDayFromPreviousDate(todayUiState.summary.date) },
                modifier = Modifier,
            )

            is TodayPageState.MealEntries -> {
                val currentMealType = state.mealType
                LaunchedEffect(todayUiState.summary.date, currentMealType) {
                    diaryViewModel.openMeal(todayUiState.summary.date, currentMealType)
                }
                MealEntriesScreen(
                    uiState = diaryUiState,
                    onAddEntry = { addingEntry = true },
                    onCopyYesterdayMeal = diaryViewModel::copyYesterdayMeal,
                    onDeleteEntry = diaryViewModel::deleteEntry,
                    modifier = Modifier,
                )
            }

            is TodayPageState.AddEntry -> {
                val currentMealType = state.mealType
                LaunchedEffect(todayUiState.summary.date, currentMealType) {
                    diaryViewModel.openMeal(todayUiState.summary.date, currentMealType)
                }
                AddFoodEntryScreen(
                    uiState = diaryUiState,
                    onFoodSelected = diaryViewModel::onFoodSelected,
                    onActualWeightChange = diaryViewModel::onActualWeightChange,
                    onFoodSearchQueryChange = diaryViewModel::onFoodSearchQueryChange,
                    onSave = {
                        if (diaryViewModel.saveEntry()) addingEntry = false
                    },
                    modifier = Modifier,
                )
            }
        }
    }

    val macroType = editingMacroGoal
    if (macroType != null) {
        MacroQuickGoalDialog(
            macroGoalType = macroType,
            value = macroGoalInput,
            canDelete = when (macroType) {
                MacroGoalType.Protein -> (todayUiState.summary.dailyGoal?.proteinGoalGram ?: 0.0) > 0.0
                MacroGoalType.Fat -> (todayUiState.summary.dailyGoal?.fatGoalGram ?: 0.0) > 0.0
                MacroGoalType.Carb -> (todayUiState.summary.dailyGoal?.carbGoalGram ?: 0.0) > 0.0
            },
            onValueChange = { macroGoalInput = it },
            onSave = {
                if (
                    goalQuickApplyViewModel.saveMacroGoal(
                        date = todayUiState.summary.date,
                        type = macroType,
                        value = macroGoalInput,
                        currentProteinGoal = todayUiState.summary.dailyGoal?.proteinGoalGram,
                        currentFatGoal = todayUiState.summary.dailyGoal?.fatGoalGram,
                        currentCarbGoal = todayUiState.summary.dailyGoal?.carbGoalGram,
                    )
                ) {
                    editingMacroGoal = null
                }
            },
            onDelete = {
                if (
                    goalQuickApplyViewModel.clearMacroGoal(
                        date = todayUiState.summary.date,
                        type = macroType,
                        currentProteinGoal = todayUiState.summary.dailyGoal?.proteinGoalGram,
                        currentFatGoal = todayUiState.summary.dailyGoal?.fatGoalGram,
                        currentCarbGoal = todayUiState.summary.dailyGoal?.carbGoalGram,
                    )
                ) {
                    editingMacroGoal = null
                }
            },
            onDismiss = { editingMacroGoal = null },
        )
    }

    if (showWeightRecord) {
        WeightRecordDialog(
            weightUiState = weightUiState,
            onWeightChange = weightViewModel::onWeightChange,
            onWeightNoteChange = weightViewModel::onNoteChange,
            onSaveWeight = {
                if (weightViewModel.saveWeight()) showWeightRecord = false
            },
            onDismiss = { showWeightRecord = false },
        )
    }
}

internal sealed interface TodayPageState {
    val order: Int

    data object Home : TodayPageState {
        override val order = 0
    }

    data class MealEntries(val mealType: MealType) : TodayPageState {
        override val order = 1
    }

    data class AddEntry(val mealType: MealType) : TodayPageState {
        override val order = 2
    }

    data object WaterDetails : TodayPageState {
        override val order = 3
    }

    data object GoalSettings : TodayPageState {
        override val order = 4
    }

    data object WeightDetails : TodayPageState {
        override val order = 5
    }
}

internal fun resolveTodayPageState(
    showGoalSettings: Boolean,
    showWaterDetails: Boolean,
    showWeightDetails: Boolean,
    selectedMealType: MealType?,
    addingEntry: Boolean,
): TodayPageState = when {
    showGoalSettings -> TodayPageState.GoalSettings
    showWaterDetails -> TodayPageState.WaterDetails
    showWeightDetails -> TodayPageState.WeightDetails
    selectedMealType == null -> TodayPageState.Home
    addingEntry -> TodayPageState.AddEntry(selectedMealType)
    else -> TodayPageState.MealEntries(selectedMealType)
}
