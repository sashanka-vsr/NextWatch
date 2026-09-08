package com.nextwatch.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nextwatch.app.ui.theme.NextWatchTheme

@Composable
fun HubScreen(
    modifier: Modifier = Modifier,
    onWatchlistClick: () -> Unit = {},
    onWatchHistoryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            HubHeader(onSettingsClick = onSettingsClick)
            Spacer(modifier = Modifier.height(28.dp))
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                HubBlock(
                    title = "Watchlist",
                    subtitle = "Titles you plan to watch",
                    modifier = Modifier.weight(1f),
                    onClick = onWatchlistClick,
                )
                HubBlock(
                    title = "Watch History",
                    subtitle = "What you've already watched",
                    modifier = Modifier.weight(1f),
                    onClick = onWatchHistoryClick,
                )
            }
        }
    }
}

@Composable
private fun HubHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "NextWatch",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun HubBlock(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HubScreenPreview() {
    NextWatchTheme(darkTheme = true, dynamicColor = false) {
        HubScreen()
    }
}
