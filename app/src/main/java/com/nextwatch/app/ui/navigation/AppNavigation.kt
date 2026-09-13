package com.nextwatch.app.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nextwatch.app.network.SearchResult
import com.nextwatch.app.network.TmdbMovie
import com.nextwatch.app.ui.screens.HubScreen
import com.nextwatch.app.ui.screens.MediaDetailScreen
import com.nextwatch.app.ui.screens.SearchScreen
import com.nextwatch.app.ui.screens.WatchHistoryScreen
import com.nextwatch.app.ui.screens.WatchlistScreen
import com.nextwatch.app.ui.viewmodel.NextWatchViewModel
import com.nextwatch.app.ui.viewmodel.ViewModelFactory
import com.nextwatch.app.ui.screens.SavedMediaDetailScreen

object AppRoutes {
    const val Hub = "hub"
    const val Watchlist = "watchlist"
    const val History = "history"
    const val Search = "search"
    const val SavedMediaDetail = "saved_media_detail/{mediaId}"

    fun savedMediaDetail(mediaId: Long): String ="saved_media_detail/$mediaId"
    const val MediaDetail =
        "media_detail/{tmdbId}/{mediaType}?title={title}&year={year}&posterUrl={posterUrl}"

    fun mediaDetail(
        tmdbId: Int,
        mediaType: String,
        title: String,
        year: String? = null,
        posterUrl: String? = null,
    ): String {
        val encodedTitle = Uri.encode(title)
        val encodedYear = Uri.encode(year.orEmpty())
        val encodedPoster = Uri.encode(posterUrl.orEmpty())
        val encodedMediaType = Uri.encode(mediaType)
        return "media_detail/$tmdbId/$encodedMediaType" +
            "?title=$encodedTitle&year=$encodedYear&posterUrl=$encodedPoster"
    }

    fun mediaDetail(media: TmdbMovie): String =
        mediaDetail(
            tmdbId = media.tmdbId,
            mediaType = media.mediaType,
            title = media.title,
            year = media.year,
            posterUrl = media.posterUrl,
        )
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val viewModel: NextWatchViewModel = viewModel(
        factory = ViewModelFactory(context),
    )

    NavHost(
        navController = navController,
        startDestination = AppRoutes.Hub,
        modifier = modifier,
    ) {
        composable(AppRoutes.Hub) {
            HubScreen(
                onWatchlistClick = { navController.navigate(AppRoutes.Watchlist) },
                onWatchHistoryClick = { navController.navigate(AppRoutes.History) },
            )
        }
        composable(AppRoutes.Watchlist) {
            WatchlistScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onAddClick = { navController.navigate(AppRoutes.Search) },
                onItemClick = { item ->
                    navController.navigate(
                        AppRoutes.savedMediaDetail(item.id)
                    )
                },
            )
        }
        composable(AppRoutes.Search) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onResultClick = { media ->
                    navController.navigate(AppRoutes.mediaDetail(media))
                },
            )
        }
        composable(
            route = AppRoutes.MediaDetail,
            arguments = listOf(
                navArgument("tmdbId") { type = NavType.IntType },
                navArgument("mediaType") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("year") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("posterUrl") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { entry ->
            val tmdbId = entry.arguments?.getInt("tmdbId") ?: return@composable
            val mediaType = entry.arguments?.getString("mediaType").orEmpty()
            val title = entry.arguments?.getString("title").orEmpty()
            val year = entry.arguments?.getString("year").orEmpty().ifBlank { null }
            val posterUrl = entry.arguments?.getString("posterUrl").orEmpty().ifBlank { null }
            MediaDetailScreen(
                media = TmdbMovie(
                    tmdbId = tmdbId,
                    title = title,
                    year = year,
                    posterUrl = posterUrl,
                    mediaType = mediaType,
                ),
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onSaved = {
                    navController.popBackStack(AppRoutes.Hub, inclusive = false)
                },
            )
        }
        composable(AppRoutes.History) {
            WatchHistoryScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onItemClick = { item ->
                    navController.navigate(
                        AppRoutes.savedMediaDetail(item.id)
                    )
                },
            )
        }

        composable(
            route = AppRoutes.SavedMediaDetail,
            arguments = listOf(
                navArgument("mediaId") {
                    type = NavType.LongType
                },
            ),
        ) { entry ->
            val mediaId = entry.arguments?.getLong("mediaId")
                ?: return@composable

            SavedMediaDetailScreen(
                mediaId = mediaId,
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
    }
}