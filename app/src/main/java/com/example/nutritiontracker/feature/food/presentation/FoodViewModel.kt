package com.example.nutritiontracker.feature.food.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritiontracker.core.datastore.UserPreferencesRepository
import com.example.nutritiontracker.feature.food.data.Food
import com.example.nutritiontracker.feature.food.data.FoodInput
import com.example.nutritiontracker.feature.food.data.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FoodDraft(
    val name: String = "",
    val imagePath: String? = null,
    val baseWeightGram: String = "500",
    val proteinGram: String = "",
    val fatGram: String = "",
    val carbGram: String = "",
)

data class FoodUiState(
    val foods: List<Food> = emptyList(),
    val draft: FoodDraft = FoodDraft(),
    val errorMessage: String? = null,
)

@HiltViewModel
class FoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val defaultBaseWeightGram = MutableStateFlow("500")
    private val draft = MutableStateFlow(FoodDraft())
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<FoodUiState> = combine(
        foodRepository.getFoodsStream(),
        draft,
        errorMessage,
    ) { foods, draft, error ->
        FoodUiState(foods = foods, draft = draft, errorMessage = error)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, FoodUiState())

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesStream.collect { preferences ->
                val previousDefault = defaultBaseWeightGram.value
                val nextDefault = preferences.defaultFoodBaseWeightGram.formatInput()
                defaultBaseWeightGram.value = nextDefault
                val currentDraft = draft.value
                if (currentDraft.isUntouched(previousDefault)) {
                    draft.value = currentDraft.copy(baseWeightGram = nextDefault)
                }
            }
        }
    }

    fun onNameChange(value: String) {
        draft.value = draft.value.copy(name = value)
        errorMessage.value = null
    }

    fun onBaseWeightChange(value: String) {
        draft.value = draft.value.copy(baseWeightGram = value)
        errorMessage.value = null
    }

    fun onProteinChange(value: String) {
        draft.value = draft.value.copy(proteinGram = value)
        errorMessage.value = null
    }

    fun onFatChange(value: String) {
        draft.value = draft.value.copy(fatGram = value)
        errorMessage.value = null
    }

    fun onCarbChange(value: String) {
        draft.value = draft.value.copy(carbGram = value)
        errorMessage.value = null
    }

    fun onImagePathChange(value: String?) {
        draft.value = draft.value.copy(imagePath = value)
        errorMessage.value = null
    }

    fun onImageError(message: String) {
        errorMessage.value = message
    }

    fun saveDraft(): Boolean {
        val input = draft.value.toInputOrError()
        if (input.error != null) {
            errorMessage.value = input.error
            return false
        }

        viewModelScope.launch {
            foodRepository.saveFood(input.foodInput!!)
            draft.value = FoodDraft(baseWeightGram = defaultBaseWeightGram.value)
            errorMessage.value = null
        }
        return true
    }

    fun toggleFavorite(foodId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            foodRepository.toggleFavorite(foodId, isFavorite)
        }
    }

    private fun FoodDraft.toInputOrError(): FoodInputResult {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return FoodInputResult(error = "食物名称不能为空")

        val baseWeight = baseWeightGram.toDoubleOrNull() ?: return FoodInputResult(error = "基准重量必须填写数字")
        if (baseWeight <= 0.0) return FoodInputResult(error = "基准重量必须大于 0")

        val protein = proteinGram.toDoubleOrNull() ?: return FoodInputResult(error = "蛋白质必须填写数字")
        val fat = fatGram.toDoubleOrNull() ?: return FoodInputResult(error = "脂肪必须填写数字")
        val carb = carbGram.toDoubleOrNull() ?: return FoodInputResult(error = "碳水必须填写数字")
        if (protein < 0.0 || fat < 0.0 || carb < 0.0) return FoodInputResult(error = "三大营养素不能为负数")

        return FoodInputResult(
            foodInput = FoodInput(
                name = trimmedName,
                imagePath = imagePath,
                baseWeightGram = baseWeight,
                proteinGram = protein,
                fatGram = fat,
                carbGram = carb,
            ),
        )
    }
}

private data class FoodInputResult(
    val foodInput: FoodInput? = null,
    val error: String? = null,
)

private fun FoodDraft.isUntouched(expectedBaseWeightGram: String): Boolean =
    name.isBlank() &&
        imagePath == null &&
        baseWeightGram == expectedBaseWeightGram &&
        proteinGram.isBlank() &&
        fatGram.isBlank() &&
        carbGram.isBlank()

private fun Double.formatInput(): String =
    if (this % 1.0 == 0.0) toInt().toString() else String.format(Locale.US, "%.1f", this)
