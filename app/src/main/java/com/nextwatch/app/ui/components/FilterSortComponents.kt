package com.nextwatch.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextwatch.app.ui.screens.SortDirection
import com.nextwatch.app.ui.screens.WatchlistFilterState
import com.nextwatch.app.ui.screens.WatchlistSort
import com.nextwatch.app.ui.screens.WatchlistSortCriterion
import com.nextwatch.app.ui.theme.DarkBorder
import com.nextwatch.app.ui.theme.DarkSurface
import com.nextwatch.app.ui.theme.DarkSurfaceVariant
import com.nextwatch.app.ui.theme.NetflixRed
import com.nextwatch.app.ui.theme.PureBlack
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaSortBottomSheet(
    onDismissRequest: () -> Unit,
    currentSort: WatchlistSort,
    availableCriteria: List<WatchlistSortCriterion>,
    onSortChange: (WatchlistSort) -> Unit,
) {
    val sortSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sortSheetState,
        containerColor = DarkSurfaceVariant,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Sort by",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    val isAscending = currentSort.direction == SortDirection.Ascending

                    // Ascending
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isAscending) NetflixRed.copy(alpha = 0.18f)
                                else Color.Transparent
                            )
                            .clickable {
                                onSortChange(currentSort.copy(direction = SortDirection.Ascending))
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowUpward,
                            contentDescription = "Ascending",
                            tint = if (isAscending) NetflixRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp),
                        )
                    }

                    // Descending
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (!isAscending) NetflixRed.copy(alpha = 0.18f)
                                else Color.Transparent
                            )
                            .clickable {
                                onSortChange(currentSort.copy(direction = SortDirection.Descending))
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDownward,
                            contentDescription = "Descending",
                            tint = if (!isAscending) NetflixRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = DarkBorder)
            Spacer(modifier = Modifier.height(4.dp))

            availableCriteria.forEach { criterion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onSortChange(currentSort.copy(criterion = criterion))
                        }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = criterion == currentSort.criterion,
                        onClick = {
                            onSortChange(currentSort.copy(criterion = criterion))
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = NetflixRed,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = criterion.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (criterion == currentSort.criterion) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaFilterBottomSheet(
    onDismissRequest: () -> Unit,
    isMovies: Boolean,
    currentFilter: WatchlistFilterState,
    genrePool: List<String>,
    languagePool: List<String>,
    creatorPool: List<String>,
    onApplyFilter: (WatchlistFilterState) -> Unit,
) {
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var draftFilter by remember { mutableStateOf(currentFilter) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = filterSheetState,
        containerColor = DarkSurfaceVariant,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Filter",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (draftFilter.isActive) {
                    Text(
                        text = "Clear all",
                        style = MaterialTheme.typography.bodySmall,
                        color = NetflixRed,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { draftFilter = WatchlistFilterState.Empty }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = DarkBorder)

            // ── Genre ──────────────────────────────────────────────────
            if (genrePool.isNotEmpty()) {
                FilterSection(title = "Genre") {
                    ChipGroup(
                        options = genrePool,
                        selected = draftFilter.genres,
                        onToggle = { genre ->
                            draftFilter = draftFilter.copy(
                                genres = draftFilter.genres.toggle(genre),
                            )
                        },
                    )
                }
            }

            // ── Language ───────────────────────────────────────────────
            if (languagePool.isNotEmpty()) {
                FilterSection(title = "Language") {
                    ChipGroup(
                        options = languagePool,
                        selected = draftFilter.languages,
                        onToggle = { lang ->
                            draftFilter = draftFilter.copy(
                                languages = draftFilter.languages.toggle(lang),
                            )
                        },
                    )
                }
            }

            // ── Director / Creator ─────────────────────────────────────
            if (creatorPool.isNotEmpty()) {
                FilterSection(
                    title = if (isMovies) "Director" else "Creator",
                ) {
                    ChipGroup(
                        options = creatorPool,
                        selected = if (isMovies) draftFilter.directors else draftFilter.creators,
                        onToggle = { person ->
                            draftFilter = if (isMovies) {
                                draftFilter.copy(directors = draftFilter.directors.toggle(person))
                            } else {
                                draftFilter.copy(creators = draftFilter.creators.toggle(person))
                            }
                        },
                    )
                }
            }

            // ── IMDb Rating ────────────────────────────────────────────
            val ratingOptions = listOf(9.0, 8.0, 7.0, 6.0, 5.0)
            FilterSection(title = "Minimum IMDb rating") {
                ChipGroup(
                    options = ratingOptions.map { "≥ ${"%.0f".format(it)}" },
                    selected = draftFilter.ratingMin?.let { setOf("≥ ${"%.0f".format(it)}") } ?: emptySet(),
                    onToggle = { chip ->
                        val value = chip.removePrefix("≥ ").trim().toDoubleOrNull()
                        draftFilter = if (value != null && draftFilter.ratingMin == value) {
                            // Deselect
                            draftFilter.copy(ratingMin = null)
                        } else {
                            draftFilter.copy(ratingMin = value)
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Apply button ───────────────────────────────────────────
            Button(
                onClick = {
                    onApplyFilter(draftFilter)
                    coroutineScope.launch {
                        filterSheetState.hide()
                        onDismissRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NetflixRed,
                    contentColor = PureBlack,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = "Apply",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
fun FilteredEmptyState(
    onClearFilter: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "No titles match your filters",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClearFilter,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NetflixRed,
                    contentColor = PureBlack,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = "Clear Filters",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

fun Set<String>.toggle(item: String): Set<String> =
    if (item in this) this - item else this + item

@Composable
fun FilterSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp),
    )
    content()
    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(color = DarkBorder)
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChipGroup(
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { option ->
            val isSelected = option in selected
            FilterChip(
                selected = isSelected,
                onClick = { onToggle(option) },
                label = {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = DarkSurface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = NetflixRed.copy(alpha = 0.18f),
                    selectedLabelColor = NetflixRed,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = DarkBorder,
                    selectedBorderColor = NetflixRed.copy(alpha = 0.6f),
                ),
            )
        }
    }
}
