package com.palette.mobile.android

import com.palette.mobile.android.ui.create.CreatePaletteViewModel
import com.palette.mobile.android.ui.create.CreateUiState
import com.palette.mobile.core.model.AppResult
import com.palette.mobile.palette.model.Palette
import com.palette.mobile.palette.usecase.CreatePaletteUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class CreatePaletteViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val createPaletteUseCase = mockk<CreatePaletteUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun submitWithShortNameFailsValidation() = runTest(testDispatcher) {
        val viewModel = CreatePaletteViewModel(createPaletteUseCase)
        viewModel.setName("A")

        viewModel.submit { }
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state is CreateUiState.Error)
        assertEquals("Name must be between 2 and 80 characters", (state as CreateUiState.Error).message)
    }

    @Test
    fun submitWithDuplicateColorsFailsValidation() = runTest(testDispatcher) {
        val viewModel = CreatePaletteViewModel(createPaletteUseCase)
        viewModel.setName("Valid Name")
        viewModel.updateColor(0, "#FFFFFF")
        viewModel.updateColor(1, "#FFFFFF")

        viewModel.submit { }
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state is CreateUiState.Error)
    }

    @Test
    fun submitSuccessChangesStateToSuccess() = runTest(testDispatcher) {
        val created = Palette(
            id = "new-p",
            name = "Nordic",
            status = "PUBLISHED",
            likeCount = 0,
            colors = listOf("#111111", "#222222", "#333333", "#444444"),
            tags = listOf("nordic"),
            createdBy = "u1",
            createdAt = "2026-09-17T08:00:00Z",
            publishedAt = "2026-09-17T08:00:00Z",
            likedByMe = false
        )
        coEvery { createPaletteUseCase(any(), any(), any(), any(), any()) } returns AppResult.Success(created)

        val viewModel = CreatePaletteViewModel(createPaletteUseCase)
        viewModel.setName("Nordic")
        viewModel.setDescription("A crisp winter palette")
        viewModel.updateColor(0, "#111111")
        viewModel.updateColor(1, "#222222")
        viewModel.updateColor(2, "#333333")
        viewModel.updateColor(3, "#444444")

        viewModel.submit { }
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state is CreateUiState.Success)
        assertEquals("new-p", (state as CreateUiState.Success).palette.id)
    }

    @Test
    fun submitWithTooLongDescriptionFailsValidation() = runTest(testDispatcher) {
        val viewModel = CreatePaletteViewModel(createPaletteUseCase)
        viewModel.setName("Valid Name")
        viewModel.setDescription("A".repeat(251))

        viewModel.submit { }
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state is CreateUiState.Error)
        assertEquals("Description must not exceed 250 characters", (state as CreateUiState.Error).message)
    }
}
