package com.example.nutritiontracker.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun ChineseDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    var visibleMonth by remember(initialDate) { mutableStateOf(YearMonth.from(initialDate)) }
    var selectedDate by remember(initialDate) { mutableStateOf(initialDate) }
    val firstDay = visibleMonth.atDay(1)
    val offset = firstDay.dayOfWeek.value % 7
    val monthDays = (1..visibleMonth.lengthOfMonth()).map { visibleMonth.atDay(it) }
    val cells = List(offset) { null } + monthDays

    NutritionAnimatedDialog(onDismissRequest = onDismiss) {
        Text("选择日期", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { visibleMonth = visibleMonth.minusMonths(1) }) {
                Icon(Icons.Rounded.ChevronLeft, contentDescription = "上个月")
            }
            Text(
                "${visibleMonth.year}年${visibleMonth.monthValue}月",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = { visibleMonth = visibleMonth.plusMonths(1) }) {
                Icon(Icons.Rounded.ChevronRight, contentDescription = "下个月")
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            cells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    week.forEach { date ->
                        ChineseDateCell(
                            date = date,
                            selected = date == selectedDate,
                            onClick = { if (date != null) selectedDate = date },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(7 - week.size) {
                        ChineseDateCell(date = null, selected = false, onClick = {}, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = {
                    selectedDate = LocalDate.now()
                    visibleMonth = YearMonth.now()
                },
            ) {
                Text("回到今天")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
                TextButton(
                    onClick = {
                        onDateSelected(selectedDate)
                        onDismiss()
                    },
                ) {
                    Text("确定")
                }
            }
        }
    }
}

@Composable
private fun ChineseDateCell(
    date: LocalDate?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when {
        date == null -> Color.Transparent
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .clickable(enabled = date != null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = date?.dayOfMonth?.toString().orEmpty(),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        )
    }
}
