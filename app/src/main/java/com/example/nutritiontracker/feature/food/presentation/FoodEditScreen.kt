package com.example.nutritiontracker.feature.food.presentation

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import java.io.File

@Composable
fun FoodEditScreen(
    uiState: FoodUiState,
    onNameChange: (String) -> Unit,
    onBaseWeightChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onCarbChange: (String) -> Unit,
    onPickImage: () -> Unit,
    onTakePhoto: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Column {
            Text("新建食物", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text("按一个基准重量填写营养素", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(20.dp))

        FoodImageSection(
            imagePath = uiState.draft.imagePath,
            onPickImage = onPickImage,
            onTakePhoto = onTakePhoto,
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = uiState.draft.name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("食物名称") },
        )

        Spacer(Modifier.height(12.dp))

        NumberField(
            value = uiState.draft.baseWeightGram,
            onValueChange = onBaseWeightChange,
            label = "基准重量（g）",
        )

        Spacer(Modifier.height(20.dp))
        Text("基准重量下的三大营养素", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        NumberField(value = uiState.draft.proteinGram, onValueChange = onProteinChange, label = "蛋白质（g）")
        Spacer(Modifier.height(12.dp))
        NumberField(value = uiState.draft.fatGram, onValueChange = onFatChange, label = "脂肪（g）")
        Spacer(Modifier.height(12.dp))
        NumberField(value = uiState.draft.carbGram, onValueChange = onCarbChange, label = "碳水（g）")

        if (uiState.errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text("保存食物")
        }
    }
}

@Composable
private fun NumberField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}

@Composable
private fun FoodImageSection(
    imagePath: String?,
    onPickImage: () -> Unit,
    onTakePhoto: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("食物图片", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (imagePath == null) {
                    Text("还没有图片", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    AsyncImage(
                        model = File(imagePath),
                        contentDescription = "食物图片预览",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = onPickImage) {
                Text("从相册选择")
            }
            FilledTonalButton(onClick = onTakePhoto) {
                Text("拍照")
            }
        }
    }
}
