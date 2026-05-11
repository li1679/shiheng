package com.example.nutritiontracker.feature.food.presentation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutritiontracker.core.image.CameraImageTarget
import com.example.nutritiontracker.core.image.ImageStorage
import com.example.nutritiontracker.core.ui.NutritionAnimatedContent
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FoodFeatureScreen(
    modifier: Modifier = Modifier,
    viewModel: FoodViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val imageStorage = remember(context) { ImageStorage(context) }
    val coroutineScope = rememberCoroutineScope()
    var cameraTarget by remember { mutableStateOf<CameraImageTarget?>(null) }
    var editing by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    BackHandler(enabled = editing) {
        editing = false
    }
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val imagePath = withContext(Dispatchers.IO) {
                        imageStorage.copyImageToPrivateStorage(uri)
                    }
                    viewModel.onImagePathChange(imagePath)
                } catch (_: IOException) {
                    viewModel.onImageError("图片保存失败")
                } catch (_: SecurityException) {
                    viewModel.onImageError("没有权限读取这张图片")
                }
            }
        }
    }
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { saved ->
        val target = cameraTarget
        if (saved && target != null) {
            viewModel.onImagePathChange(target.filePath)
        } else {
            viewModel.onImageError("拍照没有保存成功")
        }
        cameraTarget = null
    }

    NutritionAnimatedContent(
        targetState = editing,
        modifier = modifier,
        label = "food-page",
        isForward = { from, to -> !from && to },
    ) { isEditing ->
        if (isEditing) {
            FoodEditScreen(
                uiState = uiState,
                onNameChange = viewModel::onNameChange,
                onBaseWeightChange = viewModel::onBaseWeightChange,
                onProteinChange = viewModel::onProteinChange,
                onFatChange = viewModel::onFatChange,
                onCarbChange = viewModel::onCarbChange,
                onPickImage = {
                    pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onTakePhoto = {
                    try {
                        val target = imageStorage.createCameraImageTarget()
                        cameraTarget = target
                        takePhotoLauncher.launch(target.uri)
                    } catch (_: IllegalArgumentException) {
                        viewModel.onImageError("无法创建拍照文件")
                    }
                },
                onSave = {
                    if (viewModel.saveDraft()) editing = false
                },
                modifier = Modifier,
            )
        } else {
            FoodListScreen(
                foods = uiState.foods,
                query = query,
                onQueryChange = { query = it },
                onAddFood = { editing = true },
                onToggleFavorite = viewModel::toggleFavorite,
                modifier = Modifier,
            )
        }
    }
}
