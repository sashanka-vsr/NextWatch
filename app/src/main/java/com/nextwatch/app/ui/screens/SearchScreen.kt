package com.nextwatch.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nextwatch.app.network.NextWatchApiClient
import com.nextwatch.app.network.TmdbMovie
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onResultClick: (TmdbMovie) -> Unit,
    modifier: Modifier = Modifier,
) {
    val apiClient = remember { NextWatchApiClient() }
    val focusManager = LocalFocusManager.current
    var query by rememberSaveable { mutableStateOf("") }
    var results by remember { mutableStateOf<List<TmdbMovie>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            results = emptyList()
            searching = false
            return@LaunchedEffect
        }
        delay(350)
        searching = true
        results = runCatching { apiClient.searchMovies(trimmed) }.getOrDefault(emptyList())
        searching = false
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Search") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search movies and series") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus() },
                ),
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    searching && results.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    results.isEmpty() && query.trim().length >= 2 && !searching -> {
                        Text(
                            text = "No results",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.Top,
                        ) {
                            items(results, key = { "${it.mediaType}_${it.tmdbId}" }) { media ->
                                SearchResultRow(
                                    media = media,
                                    onClick = { onResultClick(media) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    media: TmdbMovie,
    onClick: () -> Unit,
) {
    Column {
        ListItem(
            modifier = Modifier.clickable(
                role = Role.Button,
                onClick = onClick,
            ),
            headlineContent = { Text(media.title) },
            supportingContent = { Text(media.year.orEmpty()) },
            leadingContent = {
                AsyncImage(
                    model = media.posterUrl,
                    contentDescription = media.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 48.dp, height = 72.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
            },
            trailingContent = { TypeBadge(text = media.mediaType) },
        )
        HorizontalDivider()
    }
}
