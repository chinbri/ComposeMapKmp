package org.maplibre.compose.demoapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.maplibre.compose.demoapp.offline.OfflineDownloadState
import org.maplibre.compose.demoapp.offline.OfflineRegionRequest
import org.maplibre.compose.demoapp.offline.rememberOfflineRegionDownloader
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.BoundingBox

@Composable
actual fun DownloadTiles(
  downloadRequestId: Int,
  onDownloadStarted: () -> Unit,
  onDownloadComplete: () -> Unit,
  onDownloadError: (String) -> Unit,
) {
  val downloader = rememberOfflineRegionDownloader()
  val selectedStyle: DemoStyle = Protomaps.Light

  val boundingBox =
    BoundingBox(
      west = -0.4310,
      south = 39.4190,
      east = -0.3220,
      north = 39.5010,
    )

  LaunchedEffect(downloadRequestId) {
    if (downloadRequestId <= 0) return@LaunchedEffect

    val style = selectedStyle.base
    if (style !is BaseStyle.Uri) {
      onDownloadError("El estilo seleccionado no es una URI válida para offline")
      return@LaunchedEffect
    }

    onDownloadStarted()
    downloader.downloadRegion(
      OfflineRegionRequest(
        name = "Valencia pack",
        styleUri = style.uri,
        bounds = boundingBox,
        minZoom = 10,
        maxZoom = 16,
      )
    )

    when (val state = downloader.state) {
      OfflineDownloadState.Ready -> onDownloadComplete()
      is OfflineDownloadState.Failed -> onDownloadError(state.reason)
      else -> onDownloadError("La descarga no se completó")
    }
  }
}