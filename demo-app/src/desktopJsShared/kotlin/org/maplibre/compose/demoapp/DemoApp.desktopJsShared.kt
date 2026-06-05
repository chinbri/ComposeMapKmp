package org.maplibre.compose.demoapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun DownloadTiles(
  downloadRequestId: Int,
  onDownloadStarted: () -> Unit,
  onDownloadComplete: () -> Unit,
  onDownloadError: (String) -> Unit,
) {
  LaunchedEffect(downloadRequestId) {
    if (downloadRequestId > 0) {
      onDownloadError("La descarga offline no está implementada en Desktop/JS")
    }
  }
}