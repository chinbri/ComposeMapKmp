package org.maplibre.compose.demoapp.offline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberOfflineRegionDownloader(): OfflineRegionDownloader {
  return remember { UnsupportedOfflineRegionDownloader() }
}

private class UnsupportedOfflineRegionDownloader : OfflineRegionDownloader {
  override val state: OfflineDownloadState =
    OfflineDownloadState.Failed("Offline template is only implemented for Android and iOS")

  override suspend fun downloadRegion(request: OfflineRegionRequest) {
    // Intentionally left blank for unsupported targets.
  }
}

