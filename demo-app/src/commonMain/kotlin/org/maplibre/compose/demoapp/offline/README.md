# Offline template (Android + iOS)

This folder provides a minimal `expect/actual` template to download an offline region from shared KMP code.

## Files

- `OfflineTemplate.kt` (common API)
- `OfflineTemplate.androidIosShared.kt` (Android + iOS implementation)
- `OfflineTemplate.desktopJsShared.kt` (unsupported fallback)

## Usage from shared Compose UI

```kotlin
val downloader = rememberOfflineRegionDownloader()

LaunchedEffect(Unit) {
  downloader.downloadRegion(
    OfflineRegionRequest(
      name = "My city",
      styleUri = "https://example.com/style.json",
      bounds = BoundingBox(west = -0.43, south = 39.41, east = -0.32, north = 39.50),
      minZoom = 10,
      maxZoom = 16,
    )
  )
}

when (val state = downloader.state) {
  OfflineDownloadState.Idle -> Unit
  is OfflineDownloadState.Downloading -> {
    // state.completedResources / state.requiredResources
  }
  OfflineDownloadState.Ready -> {
    // show map fully offline
  }
  is OfflineDownloadState.Failed -> {
    // show retry UI
  }
}
```

