package com.palette.mobile.android

import com.palette.mobile.android.ui.discover.DiscoverUiState
import com.palette.mobile.android.ui.discover.DiscoverViewModel
import com.palette.mobile.core.model.AppResult
import com.palette.mobile.core.model.PageMetadata
import com.palette.mobile.core.model.PagedList
import com.palette.mobile.favorite.usecase.ToggleFavoriteUseCase
import com.palette.mobile.palette.model.Palette
import com.palette.mobile.palette.model.PaletteFilter
import com.palette.mobile.palette.usecase.GetPalettesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
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
class DiscoverViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getPalettesUseCase = mockk<GetPalettesUseCase>()
    private val toggleFavoriteUseCase = mockk<ToggleFavoriteUseCase>()

    private val samplePalette = Palette(
        id = "p-1",
        name = "Cool Gray",
        status = "PUBLISHED",
        likeCount = 12,
        colors = listOf("#111111", "#222222", "#333333", "#444444"),
        tags = listOf("cool"),
        createdBy = "u-1",
        createdAt = "2026-09-17T08:00:00Z",
        publishedAt = "2026-09-17T08:00:00Z",
        likedByMe = false
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadPalettesSuccessUpdatesUiState() = runTest(testDispatcher) {
        val pagedList = PagedList(
            items = listOf(samplePalette),
            metadata = PageMetadata(0, 20, 1, 1, false, false)
        )
        coEvery { getPalettesUseCase(any(), any(), any()) } returns AppResult.Success(pagedList)

        val viewModel = DiscoverViewModel(getPalettesUseCase, toggleFavoriteUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state is DiscoverUiState.Success)
        assertEquals(1, (state as DiscoverUiState.Success).palettes.size)
    }

    @Test
    fun toggleFavoriteUpdatesLikeCountAndStatus() = runTest(testDispatcher) {
        val pagedList = PagedList(
            items = listOf(samplePalette),
            metadata = PageMetadata(0, 20, 1, 1, false, false)
        )
        coEvery { getPalettesUseCase(any(), any(), any()) } returns AppResult.Success(pagedList)
        coEvery { toggleFavoriteUseCase(samplePalette) } returns AppResult.Success(true)

        val viewModel = DiscoverViewModel(getPalettesUseCase, toggleFavoriteUseCase)
        advanceUntilIdle()

        var authTriggered = false
        viewModel.toggleFavorite(samplePalette) { authTriggered = true }
        advanceUntilIdle()

        val state = viewModel.uiState.first() as DiscoverUiState.Success
        assertEquals(true, state.palettes[0].likedByMe)
        assertEquals(13L, state.palettes[0].likeCount)
        assertEquals(false, authTriggered)
    }
}
