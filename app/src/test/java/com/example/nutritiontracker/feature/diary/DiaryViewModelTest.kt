package com.example.nutritiontracker.feature.diary

import com.example.nutritiontracker.core.model.MealType
import com.example.nutritiontracker.feature.diary.data.DiaryRepository
import com.example.nutritiontracker.feature.diary.data.FoodEntry
import com.example.nutritiontracker.feature.diary.data.FoodEntryInput
import com.example.nutritiontracker.feature.diary.presentation.DiaryViewModel
import com.example.nutritiontracker.feature.food.data.Food
import com.example.nutritiontracker.feature.food.data.FoodInput
import com.example.nutritiontracker.feature.food.data.FoodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveEntryUsesSelectedMealDateFoodAndWeight() = runTest(dispatcher) {
        val diaryRepository = FakeDiaryRepository()
        val foodRepository = FakeDiaryFoodRepository()
        foodRepository.setFoods(listOf(food(id = 7L, name = "米饭")))
        val viewModel = DiaryViewModel(
            diaryRepository = diaryRepository,
            foodRepository = foodRepository,
        )

        viewModel.openMeal(date = "2026-05-11", mealType = MealType.LUNCH)
        viewModel.onFoodSelected(7L)
        viewModel.onActualWeightChange("250")
        assertTrue(viewModel.saveEntry())
        advanceUntilIdle()

        assertEquals(
            FoodEntryInput(
                date = "2026-05-11",
                mealType = MealType.LUNCH,
                foodId = 7L,
                actualWeightGram = 250.0,
            ),
            diaryRepository.savedInputs.single(),
        )
        assertEquals("", viewModel.uiState.value.actualWeightGram)
    }

    @Test
    fun saveEntryRejectsMissingFoodSelection() = runTest(dispatcher) {
        val viewModel = DiaryViewModel(
            diaryRepository = FakeDiaryRepository(),
            foodRepository = FakeDiaryFoodRepository(),
        )

        viewModel.openMeal(date = "2026-05-11", mealType = MealType.LUNCH)
        viewModel.onActualWeightChange("250")
        assertFalse(viewModel.saveEntry())
        advanceUntilIdle()

        assertEquals("请选择食物", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun copyYesterdayMealUsesCurrentMealContext() = runTest(dispatcher) {
        val diaryRepository = FakeDiaryRepository()
        val viewModel = DiaryViewModel(
            diaryRepository = diaryRepository,
            foodRepository = FakeDiaryFoodRepository(),
        )

        viewModel.openMeal(date = "2026-05-11", mealType = MealType.LUNCH)
        viewModel.copyYesterdayMeal()
        advanceUntilIdle()

        assertEquals(CopyMealCall("2026-05-10", MealType.LUNCH, "2026-05-11", MealType.LUNCH), diaryRepository.copyMealCalls.single())
    }
}

private fun food(id: Long, name: String): Food = Food(
    id = id,
    name = name,
    imagePath = null,
    baseWeightGram = 500.0,
    proteinGram = 10.0,
    fatGram = 5.0,
    carbGram = 100.0,
)

private class FakeDiaryRepository : DiaryRepository {
    private val entries = MutableStateFlow<List<FoodEntry>>(emptyList())
    val savedInputs = mutableListOf<FoodEntryInput>()
    val copyMealCalls = mutableListOf<CopyMealCall>()
    val copyDayCalls = mutableListOf<Pair<String, String>>()

    override fun getEntriesForDateAndMealStream(date: String, mealType: MealType): Flow<List<FoodEntry>> = entries

    override suspend fun addFoodEntry(input: FoodEntryInput): Long {
        savedInputs += input
        return savedInputs.size.toLong()
    }

    override suspend fun deleteFoodEntry(id: Long) = Unit

    override suspend fun copyMeal(
        fromDate: String,
        fromMealType: MealType,
        toDate: String,
        toMealType: MealType,
    ): Int {
        copyMealCalls += CopyMealCall(fromDate, fromMealType, toDate, toMealType)
        return 1
    }

    override suspend fun copyDay(fromDate: String, toDate: String): Int {
        copyDayCalls += fromDate to toDate
        return 1
    }
}

private data class CopyMealCall(
    val fromDate: String,
    val fromMealType: MealType,
    val toDate: String,
    val toMealType: MealType,
)

private class FakeDiaryFoodRepository : FoodRepository {
    private val foods = MutableStateFlow<List<Food>>(emptyList())

    override fun getFoodsStream(): Flow<List<Food>> = foods

    override suspend fun saveFood(input: FoodInput) = Unit

    override suspend fun toggleFavorite(foodId: Long, isFavorite: Boolean) = Unit

    override suspend fun markFoodLogged(foodId: Long) = Unit

    fun setFoods(value: List<Food>) {
        foods.value = value
    }
}
