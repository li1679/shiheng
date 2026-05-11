package com.example.nutritiontracker.feature.today.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.core.model.MealType
import com.example.nutritiontracker.core.ui.ChineseDatePickerDialog
import com.example.nutritiontracker.core.ui.MacroChip
import com.example.nutritiontracker.core.ui.NutritionAnimatedDialog
import com.example.nutritiontracker.core.ui.NutritionCard
import com.example.nutritiontracker.core.ui.SectionTitle
import com.example.nutritiontracker.core.ui.formatNutritionNumber
import com.example.nutritiontracker.feature.goals.data.GoalTemplate
import com.example.nutritiontracker.feature.today.data.MealSummary
import com.example.nutritiontracker.feature.today.data.TodaySummary
import com.example.nutritiontracker.feature.water.data.WaterEntry
import com.example.nutritiontracker.feature.water.presentation.WaterUiState
import com.example.nutritiontracker.feature.weight.presentation.WeightUiState
import com.example.nutritiontracker.theme.MacroCarb
import com.example.nutritiontracker.theme.MacroFat
import com.example.nutritiontracker.theme.MacroProtein
import com.example.nutritiontracker.theme.WaterBlue
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max

enum class MacroGoalType(val label: String) {
    Protein("蛋白"),
    Fat("脂肪"),
    Carb("碳水"),
}

@Composable
fun TodayScreen(
    uiState: TodayUiState,
    waterUiState: WaterUiState,
    weightUiState: WeightUiState,
    onMealClick: (MealType) -> Unit,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onSelectDate: (String) -> Unit,
    onSelectToday: () -> Unit,
    onQuickAddWater: (Int) -> Unit,
    onOpenWaterDetails: () -> Unit,
    onOpenGoalSettings: () -> Unit,
    onOpenMacroQuickEdit: (MacroGoalType) -> Unit,
    onOpenWeightDetails: () -> Unit,
    onOpenWeightRecord: () -> Unit,
    onCopyYesterday: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val summary = uiState.summary
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        TodayDateHeader(
            date = summary.date,
            onPreviousDate = onPreviousDate,
            onNextDate = onNextDate,
            onSelectDate = onSelectDate,
            onSelectToday = onSelectToday,
        )

        SummaryCard(
            summary = summary,
            waterUiState = waterUiState,
            onQuickAddWater = onQuickAddWater,
            onOpenWaterDetails = onOpenWaterDetails,
            onOpenGoalSettings = onOpenGoalSettings,
            onOpenMacroQuickEdit = onOpenMacroQuickEdit,
            onCopyYesterday = onCopyYesterday,
        )

        SectionTitle("餐次")
        summary.meals.forEach { meal ->
            MealSummaryRow(meal = meal, onClick = { onMealClick(meal.mealType) })
        }
        SectionTitle("体重")
        WeightSummaryCard(
            weightUiState = weightUiState,
            onOpenWeightDetails = onOpenWeightDetails,
            onOpenWeightRecord = onOpenWeightRecord,
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TodayDateHeader(
    date: String,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onSelectDate: (String) -> Unit,
    onSelectToday: () -> Unit,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val parsedDate = date.toLocalDateOrToday()
    val today = LocalDate.now()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousDate, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Rounded.ChevronLeft, contentDescription = "前一天")
        }
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .clickable { showDatePicker = true },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (parsedDate == today) "今天" else parsedDate.toReadableDate(), style = MaterialTheme.typography.headlineMedium)
                    Text(date, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Rounded.CalendarMonth, contentDescription = "选择日期")
            }
        }
        IconButton(onClick = onNextDate, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = "后一天")
        }
    }
    if (parsedDate != today) {
        FilledTonalButton(onClick = onSelectToday, modifier = Modifier.fillMaxWidth()) {
            Text("回到今天")
        }
    }
    if (showDatePicker) {
        ChineseDatePickerDialog(
            initialDate = parsedDate,
            onDateSelected = { selectedDate -> onSelectDate(selectedDate.toString()) },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun SummaryCard(
    summary: TodaySummary,
    waterUiState: WaterUiState,
    onQuickAddWater: (Int) -> Unit,
    onOpenWaterDetails: () -> Unit,
    onOpenGoalSettings: () -> Unit,
    onOpenMacroQuickEdit: (MacroGoalType) -> Unit,
    onCopyYesterday: () -> Unit,
) {
    NutritionCard(containerColor = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "打开目标设置" }
                .clickable(onClick = onOpenGoalSettings),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("已进食", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${summary.caloriesKcal.formatNutritionNumber()} kcal", style = MaterialTheme.typography.headlineSmall)
            }
            Text(goalCaloriesText(summary), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MacroStatusTile(
                label = "蛋白(g)",
                value = summary.totalMacros.proteinGram,
                goal = summary.dailyGoal?.proteinGoalGram,
                color = MacroProtein,
                onClick = { onOpenMacroQuickEdit(MacroGoalType.Protein) },
                contentDescription = "快速设置蛋白目标",
                modifier = Modifier.weight(1f),
            )
            MacroStatusTile(
                label = "碳水(g)",
                value = summary.totalMacros.carbGram,
                goal = summary.dailyGoal?.carbGoalGram,
                color = MacroCarb,
                onClick = { onOpenMacroQuickEdit(MacroGoalType.Carb) },
                contentDescription = "快速设置碳水目标",
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MacroStatusTile(
                label = "脂肪(g)",
                value = summary.totalMacros.fatGram,
                goal = summary.dailyGoal?.fatGoalGram,
                color = MacroFat,
                onClick = { onOpenMacroQuickEdit(MacroGoalType.Fat) },
                contentDescription = "快速设置脂肪目标",
                modifier = Modifier.weight(1f),
            )
            WaterStatusTile(
                totalMl = waterUiState.totalMl,
                goalMl = waterUiState.dailyWaterGoalMl,
                onClick = onOpenWaterDetails,
                modifier = Modifier.weight(1f),
            )
        }

        WaterQuickAddPanel(
            waterUiState = waterUiState,
            onQuickAddWater = onQuickAddWater,
        )
        FilledTonalButton(onClick = onCopyYesterday, modifier = Modifier.fillMaxWidth()) {
            Text("复制昨天饮食")
        }
        if (waterUiState.errorMessage != null) {
            Text(waterUiState.errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun MacroStatusTile(
    label: String,
    value: Double,
    goal: Double?,
    color: Color,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val progress = if (goal != null && goal > 0.0) (value / goal).coerceIn(0.0, 1.0).toFloat() else 0f
    val valueText = if (goal != null && goal > 0.0) {
        "${value.formatNutritionNumber()} / ${goal.formatNutritionNumber()}g"
    } else {
        "${value.formatNutritionNumber()}g"
    }
    val helperText = if (goal != null && goal > 0.0) {
        "还差 ${max(goal - value, 0.0).formatNutritionNumber()}g"
    } else {
        "未设目标"
    }

    StatusTile(
        label = label,
        valueText = valueText,
        helperText = helperText,
        progress = progress,
        color = color,
        onClick = onClick,
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

@Composable
private fun WaterStatusTile(
    totalMl: Int,
    goalMl: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = if (goalMl > 0) (totalMl.toFloat() / goalMl.toFloat()).coerceIn(0f, 1f) else 0f
    val helperText = if (goalMl > 0) {
        "${(progress * 100).toDouble().formatNutritionNumber()}%"
    } else {
        "未设目标"
    }
    StatusTile(
        label = "饮水(ml)",
        valueText = "$totalMl / ${goalMl}ml",
        helperText = helperText,
        progress = progress,
        color = WaterBlue,
        onClick = onClick,
        contentDescription = "打开饮水明细",
        modifier = modifier,
    )
}

@Composable
private fun StatusTile(
    label: String,
    valueText: String,
    helperText: String,
    progress: Float,
    color: Color,
    onClick: (() -> Unit)?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val interactiveModifier = if (onClick != null) {
        modifier
            .semantics {
                if (contentDescription != null) this.contentDescription = contentDescription
            }
            .clickable(onClick = onClick)
    } else {
        modifier
    }
    Surface(
        modifier = interactiveModifier,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.10f),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valueText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(helperText, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ProgressBar(progress = progress, color = color)
        }
    }
}

@Composable
private fun ProgressBar(progress: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.70f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(color),
        )
    }
}

@Composable
private fun WaterQuickAddPanel(
    waterUiState: WaterUiState,
    onQuickAddWater: (Int) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("饮水", style = MaterialTheme.typography.titleMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                waterUiState.quickAddMlValues.forEach { amount ->
                    FilledTonalButton(onClick = { onQuickAddWater(amount) }) {
                        Text("+${amount}ml")
                    }
                }
            }
        }
    }
}

@Composable
fun GoalSettingsScreen(
    uiState: TodayGoalQuickApplyUiState,
    onGoalTemplateSelected: (Long) -> Unit,
    onManualProteinGoalChange: (String) -> Unit,
    onManualFatGoalChange: (String) -> Unit,
    onManualCarbGoalChange: (String) -> Unit,
    onSaveManualGoal: () -> Unit,
    onApplyGoalToSelectedDate: () -> Unit,
    onApplyGoalForNextThirtyDays: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("目标设置", style = MaterialTheme.typography.headlineLarge)
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("设置目标", style = MaterialTheme.typography.titleMedium)
                }
                ManualGoalForm(
                    draft = uiState.manualDraft,
                    onManualProteinGoalChange = onManualProteinGoalChange,
                    onManualFatGoalChange = onManualFatGoalChange,
                    onManualCarbGoalChange = onManualCarbGoalChange,
                    onSaveManualGoal = onSaveManualGoal,
                )
                Text("选择目标模板", style = MaterialTheme.typography.titleMedium)
                if (uiState.templates.isEmpty()) {
                    Text("先到目标页创建模板", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val selectedTemplate = uiState.selectedTemplate ?: uiState.templates.first()
                    SelectedGoalTemplateCard(template = selectedTemplate)
                    TextButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.semantics {
                            contentDescription = if (expanded) "收起目标模板" else "展开目标模板"
                        },
                    ) {
                        Text(if (expanded) "收起模板列表" else "更换模板")
                    }
                    AnimatedVisibility(visible = expanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.templates
                                .filterNot { it.id == selectedTemplate.id }
                                .forEach { template ->
                                    GoalTemplateRow(
                                        template = template,
                                        onClick = {
                                            onGoalTemplateSelected(template.id)
                                            expanded = false
                                        },
                                    )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = onApplyGoalToSelectedDate, modifier = Modifier.weight(1f)) {
                            Text("应用到这一天")
                        }
                        FilledTonalButton(onClick = onApplyGoalForNextThirtyDays, modifier = Modifier.weight(1f)) {
                            Text("未来30天")
                        }
                    }
                }
                if (uiState.errorMessage != null) {
                    Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
                }
                if (uiState.successMessage != null) {
                    Text(uiState.successMessage, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun MacroQuickGoalDialog(
    macroGoalType: MacroGoalType,
    value: String,
    canDelete: Boolean = false,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    val accentColor = macroGoalType.accentColor()
    val currentValue = value.toDoubleOrNull() ?: 0.0

    NutritionAnimatedDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 420.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = accentColor.copy(alpha = 0.12f),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("${macroGoalType.label}目标", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "快速设定每日${macroGoalType.label}目标",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        MacroGoalValueEditor(
            label = "当前目标",
            value = value,
            unit = "g",
            color = accentColor,
            contentDescription = "输入${macroGoalType.label}目标",
            onValueChange = onValueChange,
        )

        Text("常用值", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            macroGoalType.quickPresetValues().forEach { preset ->
                MacroPresetChip(
                    label = "${preset}g",
                    selected = currentValue == preset.toDouble(),
                    color = accentColor,
                    onClick = { onValueChange(preset.toString()) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MacroStepButton(label = "-5g", color = accentColor, modifier = Modifier.weight(1f)) {
                onValueChange((currentValue - 5.0).coerceAtLeast(0.0).formatNutritionNumber())
            }
            MacroStepButton(label = "+5g", color = accentColor, modifier = Modifier.weight(1f)) {
                onValueChange((currentValue + 5.0).formatNutritionNumber())
            }
        }

        if (canDelete) {
            TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("删除当前目标", color = MaterialTheme.colorScheme.error)
            }
        }

        FilledTonalButton(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = accentColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            shape = RoundedCornerShape(22.dp),
        ) {
            Text("完成")
        }
    }
}

@Composable
private fun MacroGoalValueEditor(
    label: String,
    value: String,
    unit: String,
    color: Color,
    contentDescription: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { this.contentDescription = contentDescription },
        singleLine = true,
        textStyle = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        cursorBrush = SolidColor(color),
        decorationBox = { innerTextField ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (value.isBlank()) {
                                Text(
                                    text = "0",
                                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                )
                            }
                            innerTextField()
                        }
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun MacroPresetChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = if (selected) 0.18f else 0.07f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) color else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun MacroStepButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.08f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = color,
            )
        }
    }
}

private fun MacroGoalType.quickPresetValues(): List<Int> =
    when (this) {
        MacroGoalType.Protein -> listOf(80, 100, 120, 150)
        MacroGoalType.Fat -> listOf(40, 50, 60, 70)
        MacroGoalType.Carb -> listOf(150, 200, 250, 300)
    }

private fun MacroGoalType.accentColor(): Color =
    when (this) {
        MacroGoalType.Protein -> MacroProtein
        MacroGoalType.Fat -> MacroFat
        MacroGoalType.Carb -> MacroCarb
    }

@Composable
private fun ManualGoalForm(
    draft: ManualGoalDraft,
    onManualProteinGoalChange: (String) -> Unit,
    onManualFatGoalChange: (String) -> Unit,
    onManualCarbGoalChange: (String) -> Unit,
    onSaveManualGoal: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("手动设置目标", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = draft.proteinGoalGram,
                onValueChange = onManualProteinGoalChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "输入蛋白目标" },
                singleLine = true,
                label = { Text("蛋白目标（g）") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            OutlinedTextField(
                value = draft.fatGoalGram,
                onValueChange = onManualFatGoalChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "输入脂肪目标" },
                singleLine = true,
                label = { Text("脂肪目标（g）") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            OutlinedTextField(
                value = draft.carbGoalGram,
                onValueChange = onManualCarbGoalChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "输入碳水目标" },
                singleLine = true,
                label = { Text("碳水目标（g）") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            Button(onClick = onSaveManualGoal, modifier = Modifier.fillMaxWidth()) {
                Text("保存这一天目标")
            }
        }
    }
}

@Composable
private fun SelectedGoalTemplateCard(template: GoalTemplate) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(template.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            GoalMacroChips(template = template)
        }
    }
}

@Composable
private fun GoalTemplateRow(template: GoalTemplate, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(template.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            GoalMacroChips(template = template)
        }
    }
}

@Composable
private fun GoalMacroChips(template: GoalTemplate) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MacroChip("蛋白", template.proteinGoalGram, MacroProtein)
        MacroChip("脂肪", template.fatGoalGram, MacroFat)
        MacroChip("碳水", template.carbGoalGram, MacroCarb)
    }
}

@Composable
fun WaterDetailsScreen(
    waterUiState: WaterUiState,
    onQuickAddWater: (Int) -> Unit,
    onDeleteWaterEntry: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("饮水明细", style = MaterialTheme.typography.headlineLarge)
            Text(waterUiState.date, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        NutritionCard(containerColor = MaterialTheme.colorScheme.surface) {
            Text("今日总量", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${waterUiState.totalMl} / ${waterUiState.dailyWaterGoalMl}ml", style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                waterUiState.quickAddMlValues.forEach { amount ->
                    FilledTonalButton(onClick = { onQuickAddWater(amount) }) {
                        Text("+${amount}ml")
                    }
                }
            }
        }

        SectionTitle("记录")
        if (waterUiState.waterLog.entries.isEmpty()) {
            Text("还没有饮水记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                waterUiState.waterLog.entries.forEach { entry ->
                    WaterRecordRow(entry = entry, onDelete = { onDeleteWaterEntry(entry.id) })
                }
            }
        }
    }
}

@Composable
private fun WaterRecordRow(
    entry: WaterEntry,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("${entry.amountMl}ml", style = MaterialTheme.typography.titleMedium)
            Text(formatRecordTime(entry.recordedAt), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = "删除饮水记录 ${entry.amountMl}ml",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun goalCaloriesText(summary: TodaySummary): String {
    val goal = summary.dailyGoal ?: return "未设目标"
    val calories = goal.proteinGoalGram * 4.0 + goal.carbGoalGram * 4.0 + goal.fatGoalGram * 9.0
    return "目标 ${calories.formatNutritionNumber()} kcal"
}

private fun formatRecordTime(recordedAt: Long): String =
    Instant.ofEpochMilli(recordedAt)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm"))

private fun String.toLocalDateOrToday(): LocalDate = runCatching { LocalDate.parse(this) }.getOrDefault(LocalDate.now())

private fun LocalDate.toReadableDate(): String = "${monthValue}月${dayOfMonth}日"

@Composable
private fun WeightSummaryCard(
    weightUiState: WeightUiState,
    onOpenWeightDetails: () -> Unit,
    onOpenWeightRecord: () -> Unit,
) {
    NutritionCard(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .semantics { contentDescription = "打开体重详情" }
            .clickable(onClick = onOpenWeightDetails),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("体重", style = MaterialTheme.typography.titleLarge)
                Text(
                    weightUiState.weightLog?.note ?: "记录体重和备注",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                weightUiState.weightLog?.let { "${it.weightKg.formatNutritionNumber()} kg" } ?: "未记录",
                style = MaterialTheme.typography.titleLarge,
            )
        }
        FilledTonalButton(onClick = onOpenWeightRecord, modifier = Modifier.fillMaxWidth()) {
            Text("记录体重")
        }
    }
}

@Composable
fun WeightRecordDialog(
    weightUiState: WeightUiState,
    onWeightChange: (String) -> Unit,
    onWeightNoteChange: (String) -> Unit,
    onSaveWeight: () -> Unit,
    onDismiss: () -> Unit,
) {
    NutritionAnimatedDialog(onDismissRequest = onDismiss) {
        Text("记录体重", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = weightUiState.draft.weightKg,
            onValueChange = onWeightChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("体重（kg）") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        OutlinedTextField(
            value = weightUiState.draft.note,
            onValueChange = onWeightNoteChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("体重备注") },
        )
        if (weightUiState.errorMessage != null) {
            Text(weightUiState.errorMessage, color = MaterialTheme.colorScheme.error)
        }
        if (weightUiState.successMessage != null) {
            Text(weightUiState.successMessage, color = MaterialTheme.colorScheme.primary)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
            TextButton(onClick = onSaveWeight) {
                Text("保存")
            }
        }
    }
}

@Composable
fun WeightDetailsScreen(
    weightUiState: WeightUiState,
    onOpenWeightRecord: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("体重详情", style = MaterialTheme.typography.headlineLarge)
        NutritionCard(containerColor = MaterialTheme.colorScheme.surface) {
            Text(weightUiState.date, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                weightUiState.weightLog?.let { "${it.weightKg.formatNutritionNumber()} kg" } ?: "未记录",
                style = MaterialTheme.typography.headlineMedium,
            )
            if (weightUiState.weightLog?.note != null) {
                Text(weightUiState.weightLog.note, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onOpenWeightRecord, modifier = Modifier.fillMaxWidth()) {
                Text("记录体重")
            }
        }
    }
}

@Composable
private fun MealSummaryRow(meal: MealSummary, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(meal.mealType.label, style = MaterialTheme.typography.titleMedium)
                Text("${meal.entries.size} 条", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "蛋白 ${meal.totalMacros.proteinGram.formatNutritionNumber()}g，脂肪 ${meal.totalMacros.fatGram.formatNutritionNumber()}g，碳水 ${meal.totalMacros.carbGram.formatNutritionNumber()}g",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("${meal.caloriesKcal.formatNutritionNumber()} kcal", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
