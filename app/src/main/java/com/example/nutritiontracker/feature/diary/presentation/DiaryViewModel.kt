package com.example.nutritiontracker.feature.diary.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritiontracker.core.model.MealType
import com.example.nutritiontracker.feature.diary.data.DiaryRepository
import com.example.nutritiontracker.feature.diary.data.FoodEntry
import com.example.nutritiontracker.feature.diary.data.FoodEntryInput
import com.example.nutritiontracker.feature.food.data.Food
import com.example.nutritiontracker.feature.food.data.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DiaryUiState(
    val date: String = "",
    val mealType: MealType = MealType.BREAKFAST,
    val foods: List<Food> = emptyList(),
    val entries: List<FoodEntry> = emptyList(),
    val selectedFoodId: Long? = null,
    val actualWeightGram: String = "",
    val foodSearchQuery: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

private data class MealContext(
    val date: String,
    val mealType: MealType,
)

private data class FoodEntryDraft(
    val selectedFoodId: Long? = null,
    val actualWeightGram: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val foodSearchQuery: String = "",
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    foodRepository: FoodRepository,
) : ViewModel() {
    private val mealContext = MutableStateFlow(
        MealContext(date = LocalDate.now().toString(), mealType = MealType.BREAKFAST),
    )
    private val draft = MutableStateFlow(FoodEntryDraft())
    private val entries = mealContext.flatMapLatest { context ->
        diaryRepository.getEntriesForDateAndMealStream(context.date, context.mealType)
    }

    val uiState: StateFlow<DiaryUiState> = combine(
        mealContext,
        foodRepository.getFoodsStream(),
        entries,
        draft,
    ) { context, foods, entries, draft ->
        DiaryUiState(
            date = context.date,
            mealType = context.mealType,
            foods = foods,
            entries = entries,
            selectedFoodId = draft.selectedFoodId,
            actualWeightGram = draft.actualWeightGram,
            foodSearchQuery = draft.foodSearchQuery,
            errorMessage = draft.errorMessage,
            successMessage = draft.successMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DiaryUiState())

    fun openMeal(date: String, mealType: MealType) {
        mealContext.value = MealContext(date = date, mealType = mealType)
        draft.value = FoodEntryDraft()
    }

    fun onFoodSelected(foodId: Long) {
        draft.value = draft.value.copy(selectedFoodId = foodId, errorMessage = null, successMessage = null)
    }

    fun onActualWeightChange(value: String) {
        draft.value = draft.value.copy(actualWeightGram = value, errorMessage = null, successMessage = null)
    }

    fun onFoodSearchQueryChange(value: String) {
        draft.value = draft.value.copy(foodSearchQuery = value, errorMessage = null, successMessage = null)
    }

    fun saveEntry(): Boolean {
        val currentDraft = draft.value
        val selectedFoodId = currentDraft.selectedFoodId
        if (selectedFoodId == null) {
            draft.value = currentDraft.copy(errorMessage = "请选择食物")
            return false
        }

        val actualWeight = currentDraft.actualWeightGram.toDoubleOrNull()
        if (actualWeight == null) {
            draft.value = currentDraft.copy(errorMessage = "实际重量必须填写数字")
            return false
        }
        if (actualWeight <= 0.0) {
            draft.value = currentDraft.copy(errorMessage = "实际重量必须大于 0")
            return false
        }

        val context = mealContext.value
        viewModelScope.launch {
            diaryRepository.addFoodEntry(
                FoodEntryInput(
                    date = context.date,
                    mealType = context.mealType,
                    foodId = selectedFoodId,
                    actualWeightGram = actualWeight,
                ),
            )
            draft.value = FoodEntryDraft()
        }
        return true
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            diaryRepository.deleteFoodEntry(id)
        }
    }

    fun copyYesterdayMeal() {
        val context = mealContext.value
        val fromDate = LocalDate.parse(context.date).minusDays(1).toString()
        viewModelScope.launch {
            val count = diaryRepository.copyMeal(
                fromDate = fromDate,
                fromMealType = context.mealType,
                toDate = context.date,
                toMealType = context.mealType,
            )
            draft.value = draft.value.copy(successMessage = "已复制 $count 条记录", errorMessage = null)
        }
    }

    fun copyDayFromPreviousDate(date: String) {
        val fromDate = LocalDate.parse(date).minusDays(1).toString()
        viewModelScope.launch {
            diaryRepository.copyDay(fromDate = fromDate, toDate = date)
        }
    }
}
