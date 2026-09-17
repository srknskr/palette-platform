package com.palette.mobile.android.ui.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.palette.mobile.android.ui.components.EmptyState
import com.palette.mobile.android.ui.components.ErrorState
import com.palette.mobile.android.ui.components.LoadingState
import com.palette.mobile.android.ui.components.PaletteCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    viewModel: CollectionViewModel,
    onPaletteClick: (String) -> Unit,
    onAuthRequired: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFavorites(isRefresh = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Collection",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Your saved and liked palettes",
                style = MaterialTheme.typography.bodyMedium.copy(color = androidx.compose.ui.graphics.Color.Gray)
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (val state = uiState) {
                is CollectionUiState.Loading -> LoadingState()
                is CollectionUiState.Empty -> EmptyState(
                    message = "You haven't saved any palettes yet. Discover and heart the palettes you love!",
                    actionLabel = null,
                    onAction = null
                )
                is CollectionUiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadFavorites(isRefresh = true) }
                )
                is CollectionUiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = { viewModel.loadFavorites(isRefresh = true) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.palettes, key = { it.id }) { palette ->
                                PaletteCard(
                                    palette = palette,
                                    onPaletteClick = onPaletteClick,
                                    onFavoriteClick = {
                                        viewModel.toggleFavorite(palette, onAuthRequired)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
