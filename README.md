# Installation

1. Open the [Releases](../../releases) page.
2. Download `NextWatch.apk` from the release assets.
3. Open the downloaded APK using your file manager and follow Android's installation instructions.

# OMDb API Key (IMDb Ratings)

NextWatch uses OMDb to retrieve IMDb ratings.

The default API key has a daily request limit. If IMDb ratings stop appearing because the limit has been reached, you can configure your own free OMDb API key:

1. Visit https://www.omdbapi.com/apikey.aspx.
2. Select the free account option and register.
3. Activate your key using the link sent to your email.
4. Copy the activated API key.
5. Open NextWatch → **Settings** → **OMDb API Key**, then paste and save your key.

Your personal key will be used for IMDb rating lookups.

OMDb's free API key is limited to 1,000 requests per day. Please check OMDb's current terms and usage limits.

NextWatch is a non-commercial project. OMDb data is subject to OMDb's terms of use.

# NEXTWATCH<span style="color:#E50914">

A minimalistic watchlist app for movies and series — built with Jetpack Compose, Room, and a pure AMOLED-black design.

## Overview

NextWatch lets you track what you want to watch and what you've already seen. Search for any movie or series, save it to your watchlist, and move it to your watch history when you're done. No accounts, no cloud sync, no bloat — everything stays on your device.

## Features

- **Search** — Find movies and series via TMDb's multi-search API
- **Watchlist** — Save titles with a single tap, organized into Movies and Series tabs
- **Watch History** — Track everything you've watched, with re-watch support
- **Currently Watching** — Pin series you're actively watching to the top of your list
- **IMDb Ratings** — Automatically fetched from OMDb and displayed on every title
- **Streaming Availability** — See where titles are streaming in your region (powered by JustWatch via TMDb)
- **Filter & Sort** — Filter by genre, language, director/creator, year, and IMDb rating; sort by title, year, rating, runtime, seasons, or episodes
- **Offline Posters** — Poster images are downloaded and cached locally
- **Backup & Restore** — Export your entire collection (including posters) as a ZIP file, and restore it on any device
- **Configurable Landing Page** — Choose whether the app opens to Home, Watchlist, or Watch History
- **Custom OMDb API Key** — Bring your own API key if the default key's daily quota is exhausted
- **Streaming Region** — Set your country to get accurate streaming provider data

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **UI** | Jetpack Compose (Material 3) |
| **Navigation** | Navigation Compose |
| **Database** | Room (SQLite) with KSP |
| **Networking** | Ktor (Android engine) |
| **Image Loading** | Coil |
| **Architecture** | Single-Activity, ViewModel + StateFlow |
| **Theme** | AMOLED-black dark theme, Netflix-inspired red accent |
| **Min SDK** | 24 (Android 7.0) |

## Architecture

```
com.nextwatch.app
├── data/                    # Room database, DAOs, entities, preferences
│   └── backup/              # ZIP-based backup & restore system
├── network/                 # TMDb & OMDb API client, data models
└── ui/
    ├── components/          # Reusable composables (media cards, filter/sort sheets)
    ├── navigation/          # NavHost and route definitions
    ├── screens/             # All app screens
    ├── theme/               # Color palette, typography, Material theme
    └── viewmodel/           # ViewModel and factory
```

## APIs Used

- **[TMDb](https://www.themoviedb.org/)** — Movie and series search, details, genres, streaming providers
- **[OMDb](https://www.omdbapi.com/)** — IMDb ratings and supplementary metadata
- **[JustWatch](https://www.justwatch.com/)** — Streaming availability data (via TMDb)

> This app uses the TMDb API but is not endorsed or certified by TMDb. Streaming data provided by JustWatch.

## Setup

1. Clone the repository
2. Add your API keys in `app/src/main/java/com/nextwatch/app/network/ApiKeys.kt`:
   ```kotlin
   object ApiKeys {
       const val TMDB = "your_tmdb_api_key"
       const val OMDB = "your_omdb_api_key"
   }
   ```
3. Build and run with Android Studio

## License

<!-- TODO: Add your license here -->
