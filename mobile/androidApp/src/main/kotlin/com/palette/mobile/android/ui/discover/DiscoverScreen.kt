package com.palette.mobile.android.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.palette.mobile.android.ui.components.EmptyState
import com.palette.mobile.android.ui.components.ErrorState
import com.palette.mobile.android.ui.components.LoadingState
import com.palette.mobile.android.ui.components.PaletteCard
import com.palette.mobile.palette.model.PaletteSort

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    onPaletteClick: (String) -> Unit,
    onAuthRequired: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val paletteCount by viewModel.paletteCount.collectAsState()
    var searchInput by remember { mutableStateOf("") }
    val gridState = rememberLazyGridState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadNextPage()
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchInput,
                onValueChange = {
                    searchInput = it
                    viewModel.setSearchQuery(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name, #hex, or tag") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                trailingIcon = {
                    if (searchInput.isNotBlank()) {
                        IconButton(onClick = {
                            searchInput = ""
                            viewModel.setSearchQuery("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            paletteCount?.let { count ->
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Published Palettes: $count",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter.sort == PaletteSort.NEWEST,
                    onClick = { viewModel.setSort(PaletteSort.NEWEST) },
                    label = { Text("New") }
                )
                FilterChip(
                    selected = filter.sort == PaletteSort.POPULAR,
                    onClick = { viewModel.setSort(PaletteSort.POPULAR) },
                    label = { Text("Popular") }
                )
                FilterChip(
                    selected = filter.sort == PaletteSort.RANDOM,
                    onClick = { viewModel.setSort(PaletteSort.RANDOM) },
                    label = { Text("Random") }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (val state = uiState) {
                is DiscoverUiState.Loading -> LoadingState()
                is DiscoverUiState.Empty -> EmptyState(
                    message = "No palettes found for your search or filter.",
                    actionLabel = "Clear filters",
                    onAction = {
                        searchInput = ""
                        viewModel.setSearchQuery("")
                        viewModel.setSort(PaletteSort.NEWEST)
                    }
                )
                is DiscoverUiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadPalettes(isRefresh = true) }
                )
                is DiscoverUiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            viewModel.loadPalettes(isRefresh = true)
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            state = gridState,
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
