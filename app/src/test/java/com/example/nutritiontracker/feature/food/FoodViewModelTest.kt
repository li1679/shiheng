package com.example.nutritiontracker.feature.food

import com.example.nutritiontracker.core.datastore.UserPreferences
import com.example.nutritiontracker.core.datastore.UserPreferencesRepository
import com.example.nutritiontracker.feature.food.data.Food
import com.example.nutritiontracker.feature.food.data.FoodInput
import com.example.nutritiontracker.feature.food.data.FoodRepository
import com.example.nutritiontracker.feature.food.presentation.FoodViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FoodViewModelTest {
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
    fun uiStateShowsFoodsFromRepository() = runTest(dispatcher) {
        val repository = FakeFoodRepository()
        repository.setFoods(
            listOf(
                Food(
                    id = 1L,
                    name = "鸡胸肉",
                    imagePath = null,
                    baseWeightGram = 500.0,
                    proteinGram = 110.0,
                    fatGram = 9.0,
                    carbGram = 0.0,
                ),
            ),
        )

        val viewModel = FoodViewModel(repository, FakeUserPreferencesRepository())
        advanceUntilIdle()

        assertEquals("鸡胸肉", viewModel.uiState.value.foods.single().name)
    }

    @Test
    fun saveDraftStoresValidFoodAndClearsDraft() = runTest(dispatcher) {
        val repository = FakeFoodRepository()
        val viewModel = FoodViewModel(repository, FakeUserPreferencesRepository())

        viewModel.onNameChange("米饭")
        viewModel.onBaseWeightChange("500")
        viewModel.onProteinChange("12")
        viewModel.onFatChange("2")
        viewModel.onCarbChange("130")
        viewModel.saveDraft()
        advanceUntilIdle()

        assertEquals("米饭", repository.savedInputs.single().name)
        assertEquals(130.0, repository.savedInputs.single().carbGram, 0.001)
        assertEquals("", viewModel.uiState.value.draft.name)
    }

    @Test
    fun saveDraftRejectsInvalidBaseWeight() = runTest(dispatcher) {
        val repository = FakeFoodRepository()
        val viewModel = FoodViewModel(repository, FakeUserPreferencesRepository())

        viewModel.onNameChange("无效食物")
        viewModel.onBaseWeightChange("0")
        viewModel.onProteinChange("1")
        viewModel.onFatChange("1")
        viewModel.onCarbChange("1")
        viewModel.saveDraft()
        advanceUntilIdle()

        assertTrue(repository.savedInputs.isEmpty())
        assertEquals("基准重量必须大于 0", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun saveDraftStoresImagePath() = runTest(dispatcher) {
        val repository = FakeFoodRepository()
        val viewModel = FoodViewModel(repository, FakeUserPreferencesRepository())

        viewModel.onNameChange("鸡蛋")
        viewModel.onImagePathChange("C:/app/food-images/egg.jpg")
        viewModel.onBaseWeightChange("500")
        viewModel.onProteinChange("60")
        viewModel.onFatChange("40")
        viewModel.onCarbChange("5")
        viewModel.saveDraft()
        advanceUntilIdle()

        assertEquals("C:/app/food-images/egg.jpg", repository.savedInputs.single().imagePath)
    }

    @Test
    fun draftUsesDefaultBaseWeightFromPreferences() = runTest(dispatcher) {
        val viewModel = FoodViewModel(
            foodRepository = FakeFoodRepository(),
            userPreferencesRepository = FakeUserPreferencesRepository(
                UserPreferences(defaultFoodBaseWeightGram = 400.0),
            ),
        )
        advanceUntilIdle()

        assertEquals("400", viewModel.uiState.value.draft.baseWeightGram)
    }

    @Test
    fun toggleFavoriteUpdatesFoodFavoriteState() = runTest(dispatcher) {
        val repository = FakeFoodRepository()
        repository.setFoods(
            listOf(
                Food(
                    id = 3L,
                    name = "鸡蛋",
                    imagePath = null,
                    baseWeightGram = 500.0,
                    proteinGram = 60.0,
                    fatGram = 40.0,
                    carbGram = 5.0,
                ),
            ),
        )
        val viewModel = FoodViewModel(repository, FakeUserPreferencesRepository())
        advanceUntilIdle()

        viewModel.toggleFavorite(3L, true)
        advanceUntilIdle()

        assertEquals(true, repository.favoriteCalls.single().second)
    }
}

private class FakeFoodRepository : FoodRepository {
    private val foods = MutableStateFlow<List<Food>>(emptyList())
    val savedInputs = mutableListOf<FoodInput>()
    val favoriteCalls = mutableListOf<Pair<Long, Boolean>>()

    override fun getFoodsStream(): Flow<List<Food>> = foods

    override suspend fun saveFood(input: FoodInput) {
        savedInputs += input
        foods.value += Food(
            id = savedInputs.size.toLong(),
            name = input.name,
            imagePath = input.imagePath,
            baseWeightGram = input.baseWeightGram,
            proteinGram = input.proteinGram,
            fatGram = input.fatGram,
            carbGram = input.carbGram,
        )
    }

    override suspend fun toggleFavorite(foodId: Long, isFavorite: Boolean) {
        favoriteCalls += foodId to isFavorite
    }

    override suspend fun markFoodLogged(foodId: Long) = Unit

    fun setFoods(value: List<Food>) {
        foods.value = value
    }
}

private class FakeUserPreferencesRepository(
    initial: UserPreferences = UserPreferences(),
) : UserPreferencesRepository {
    override val userPreferencesStream: Flow<UserPreferences> = MutableStateFlow(initial)

    override suspend fun updatePreferences(preferences: UserPreferences) = Unit
}
