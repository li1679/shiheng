package com.example.nutritiontracker.feature.trends.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutritiontracker.core.ui.ChineseDatePickerDialog
import com.example.nutritiontracker.core.ui.EmptyState
import com.example.nutritiontracker.core.ui.NutritionCard
import com.example.nutritiontracker.core.ui.PageHeader
import com.example.nutritiontracker.core.ui.SectionTitle
import com.example.nutritiontracker.core.ui.formatNutritionNumber
import com.example.nutritiontracker.feature.trends.data.NutritionTrendDay
import com.example.nutritiontracker.feature.trends.data.TrendsRange
import com.example.nutritiontracker.feature.weight.data.WeightLog
import com.example.nutritiontracker.theme.MacroCarb
import com.example.nutritiontracker.theme.MacroFat
import com.example.nutritiontracker.theme.MacroProtein
import com.example.nutritiontracker.theme.WaterBlue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max

@Composable
fun TrendsFeatureScreen(
    modifier: Modifier = Modifier,
    viewModel: TrendsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrendsScreen(
        uiState = uiState,
        onRangeSelected = viewModel::selectRange,
        onPreviousPeriod = viewModel::selectPreviousPeriod,
        onNextPeriod = viewModel::selectNextPeriod,
        onSelectAnchorDate = viewModel::selectAnchorDate,
        onSelectStartDate = viewModel::selectStartDate,
        onSelectEndDate = viewModel::selectEndDate,
        onSelectToday = viewModel::selectToday,
        modifier = modifier,
    )
}

@Composable
fun TrendsScreen(
    uiState: TrendsUiState,
    modifier: Modifier = Modifier,
    onRangeSelected: (TrendsRange) -> Unit = {},
    onPreviousPeriod: () -> Unit = {},
    onNextPeriod: () -> Unit = {},
    onSelectAnchorDate: (String) -> Unit = {},
    onSelectStartDate: (String) -> Unit = {},
    onSelectEndDate: (String) -> Unit = {},
    onSelectToday: () -> Unit = {},
) {
    val summary = uiState.summary
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PageHeader(
            title = "饮食统计",
            subtitle = "${summary.startDate.toDateLabel()} - ${summary.endDate.toDateLabel()}",
        )

        TrendsDateNavigator(
            range = uiState.selectedRange,
            anchorDate = uiState.anchorDate,
            startDate = summary.startDate,
            endDate = summary.endDate,
            onPreviousPeriod = onPreviousPeriod,
            onNextPeriod = onNextPeriod,
            onSelectAnchorDate = onSelectAnchorDate,
            onSelectStartDate = onSelectStartDate,
            onSelectEndDate = onSelectEndDate,
            onSelectToday = onSelectToday,
        )

        RangeSelector(
            selectedRange = uiState.selectedRange,
            onRangeSelected = onRangeSelected,
        )

        NutritionCard(containerColor = MaterialTheme.colorScheme.surface) {
            Text("平均热量", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${summary.averageCaloriesKcal.formatNutritionNumber()} kcal/天", style = MaterialTheme.typography.headlineMedium)
            Text("总热量 ${summary.totalCaloriesKcal.formatNutritionNumber()} kcal", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile(
                label = "平均蛋白",
                value = "${summary.averageProteinGram.formatNutritionNumber()}g",
                color = MacroProtein,
                modifier = Modifier.weight(1f),
            )
            MetricTile(
                label = "平均碳水",
                value = "${summary.averageCarbGram.formatNutritionNumber()}g",
                color = MacroCarb,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile(
                label = "平均脂肪",
                value = "${summary.averageFatGram.formatNutritionNumber()}g",
                color = MacroFat,
                modifier = Modifier.weight(1f),
            )
            MetricTile(
                label = "总饮水",
                value = "${summary.totalWaterMl}ml",
                color = WaterBlue,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile(
                label = "记录天数",
                value = "${summary.loggedNutritionDayCount}/${summary.days.size}",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                helperText = "有饮食记录的天数",
            )
            MetricTile(
                label = "平均完成",
                value = "${summary.averageMacroCompletionPercent.formatNutritionNumber()}%",
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f),
                helperText = "按有目标日期计算",
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile(
                label = "目标天数",
                value = "${summary.goalDayCount}",
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
                helperText = "设置了每日目标的天数",
            )
            MetricTile(
                label = "体重变化",
                value = summary.weightChangeKg?.let { "${it.formatNutritionNumber()} kg" } ?: "未记录",
                color = WaterBlue,
                modifier = Modifier.weight(1f),
                helperText = "区间首尾记录差值",
            )
        }

        if (summary.range == TrendsRange.MONTH) {
            CalendarMonthSection(days = summary.days)
        }

        ChartSection(
            title = "每日总热量",
            days = summary.days,
            valueLabel = { "${it.caloriesKcal.formatNutritionNumber()} kcal" },
            value = { it.caloriesKcal },
            color = MaterialTheme.colorScheme.primary,
        )
        ChartSection(
            title = "每日蛋白",
            days = summary.days,
            valueLabel = { "${it.proteinGram.formatNutritionNumber()}g" },
            value = { it.proteinGram },
            color = MacroProtein,
        )
        ChartSection(
            title = "每日碳水",
            days = summary.days,
            valueLabel = { "${it.carbGram.formatNutritionNumber()}g" },
            value = { it.carbGram },
            color = MacroCarb,
        )
        ChartSection(
            title = "每日脂肪",
            days = summary.days,
            valueLabel = { "${it.fatGram.formatNutritionNumber()}g" },
            value = { it.fatGram },
            color = MacroFat,
        )
        ChartSection(
            title = "每日饮水",
            days = summary.days,
            valueLabel = { "${it.waterMl}ml" },
            value = { it.waterMl.toDouble() },
            color = WaterBlue,
        )

        WeightSection(weightLogs = summary.weightLogs)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TrendsDateNavigator(
    range: TrendsRange,
    anchorDate: LocalDate,
    startDate: LocalDate,
    endDate: LocalDate,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onSelectAnchorDate: (String) -> Unit,
    onSelectStartDate: (String) -> Unit,
    onSelectEndDate: (String) -> Unit,
    onSelectToday: () -> Unit,
) {
    var activeDatePicker by rememberSaveable { mutableStateOf<TrendsDatePickerTarget?>(null) }
    val today = LocalDate.now()
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.animateContentSize(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onPreviousPeriod, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.ChevronLeft, contentDescription = "上一段")
                }
                if (range == TrendsRange.CUSTOM) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RangeDateChip(
                            text = "开始 ${startDate.toDateLabel()}",
                            contentDescription = "选择开始日期",
                            onClick = { activeDatePicker = TrendsDatePickerTarget.Start },
                        )
                        RangeDateChip(
                            text = "结束 ${endDate.toDateLabel()}",
                            contentDescription = "选择结束日期",
                            onClick = { activeDatePicker = TrendsDatePickerTarget.End },
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { activeDatePicker = TrendsDatePickerTarget.Anchor },
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(range.anchorLabel(anchorDate), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("按${range.label}查看", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Rounded.CalendarMonth, contentDescription = "选择统计日期")
                        }
                    }
                }
                IconButton(onClick = onNextPeriod, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = "下一段")
                }
            }
            if (range != TrendsRange.CUSTOM && anchorDate != today) {
                FilledTonalButton(onClick = onSelectToday, modifier = Modifier.fillMaxWidth()) {
                    Text("回到今天")
                }
            }
        }
    }
    activeDatePicker?.let { target ->
        ChineseDatePickerDialog(
            initialDate = when (target) {
                TrendsDatePickerTarget.Anchor -> anchorDate
                TrendsDatePickerTarget.Start -> startDate
                TrendsDatePickerTarget.End -> endDate
            },
            onDateSelected = { selectedDate ->
                when (target) {
                    TrendsDatePickerTarget.Anchor -> onSelectAnchorDate(selectedDate.toString())
                    TrendsDatePickerTarget.Start -> onSelectStartDate(selectedDate.toString())
                    TrendsDatePickerTarget.End -> onSelectEndDate(selectedDate.toString())
                }
            },
            onDismiss = { activeDatePicker = null },
        )
    }
}

@Composable
private fun RangeDateChip(
    text: String,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
        }
    }
}

private enum class TrendsDatePickerTarget {
    Anchor,
    Start,
    End,
}

@Composable
private fun CalendarMonthSection(days: List<NutritionTrendDay>) {
    val firstDay = days.minByOrNull { it.date } ?: return
    val monthLabel = "${firstDay.date.year}年${firstDay.date.monthValue}月"
    val offset = firstDay.date.sundayFirstOffset()
    val cells = List(offset) { null } + days.sortedBy { it.date }

    NutritionCard(containerColor = MaterialTheme.colorScheme.surface) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("月历概览")
            Text(monthLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                week.forEach { day ->
                    CalendarDayCell(day = day, modifier = Modifier.weight(1f))
                }
                repeat(7 - week.size) {
                    CalendarDayCell(day = null, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: NutritionTrendDay?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(82.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (day?.hasNutrition == true || (day?.waterMl ?: 0) > 0) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.60f)
        },
    ) {
        if (day != null) {
            Column(Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(day.date.dayOfMonth.toString(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                if (day.caloriesKcal > 0.0) {
                    Text("${day.caloriesKcal.formatNutritionNumber()}kcal", style = MaterialTheme.typography.labelMedium)
                }
                if (day.waterMl > 0) {
                    Text("水 ${day.waterMl}ml", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun RangeSelector(
    selectedRange: TrendsRange,
    onRangeSelected: (TrendsRange) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TrendsRange.entries.forEach { range ->
            if (range == selectedRange) {
                Button(onClick = { onRangeSelected(range) }, modifier = Modifier.weight(1f)) {
                    Text(range.label)
                }
            } else {
                FilledTonalButton(onClick = { onRangeSelected(range) }, modifier = Modifier.weight(1f)) {
                    Text(range.label)
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    helperText: String = "按有饮食记录的天数计算",
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = color.copy(alpha = 0.10f),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("$label $value", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(helperText, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ChartSection(
    title: String,
    days: List<NutritionTrendDay>,
    valueLabel: (NutritionTrendDay) -> String,
    value: (NutritionTrendDay) -> Double,
    color: Color,
) {
    val displayDays = days.filter { value(it) > 0.0 }.take(14)
    val maxValue = max(displayDays.maxOfOrNull(value) ?: 0.0, 1.0)

    NutritionCard(containerColor = MaterialTheme.colorScheme.surface) {
        SectionTitle(title)
        if (displayDays.isEmpty()) {
            EmptyState(
                title = "还没有数据",
                body = "记录饮食、饮水或体重后，这里会显示趋势。",
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 170.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                displayDays.forEach { day ->
                    TrendBar(
                        day = day,
                        valueText = valueLabel(day),
                        heightRatio = (value(day) / maxValue).toFloat(),
                        color = color,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendBar(
    day: NutritionTrendDay,
    valueText: String,
    heightRatio: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Text(valueText, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(24.dp)
                .height((110.dp * heightRatio.coerceIn(0.08f, 1f)))
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color),
        )
        Spacer(Modifier.height(6.dp))
        Text(day.date.format(DateLabelFormatter), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun WeightSection(weightLogs: List<WeightLog>) {
    SectionTitle("体重记录")
    if (weightLogs.isEmpty()) {
        EmptyState(
            title = "还没有体重记录",
            body = "到今天页保存一次体重后，这里会显示变化。",
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            weightLogs.forEach { log ->
                WeightLogRow(log = log)
            }
        }
    }
}

@Composable
private fun WeightLogRow(log: WeightLog) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(log.date, fontWeight = FontWeight.Medium)
                Text("${log.weightKg.formatNutritionNumber()} kg", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            if (log.note != null) {
                Text(log.note, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private val DateLabelFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d")

private fun LocalDate.toDateLabel(): String = format(DateLabelFormatter)

private fun TrendsRange.anchorLabel(date: LocalDate): String =
    when (this) {
        TrendsRange.WEEK -> "包含 ${date.toDateLabel()} 的一周"
        TrendsRange.MONTH -> "统计 ${date.year}年${date.monthValue}月"
        TrendsRange.YEAR -> "统计 ${date.year}年"
        TrendsRange.CUSTOM -> "自定义区间"
    }

private fun LocalDate.sundayFirstOffset(): Int =
    dayOfWeek.value % 7
