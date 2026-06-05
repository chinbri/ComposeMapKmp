package org.maplibre.compose.demoapp.offline

import androidx.compose.runtime.Composable
import org.maplibre.spatialk.geojson.BoundingBox

/** Request used by the shared layer to download an offline region. */
data class OfflineRegionRequest(
  val name: String,
  val styleUri: String,
  val bounds: BoundingBox,
  val minZoom: Int = 0,
  val maxZoom: Int? = null,
)

sealed interface OfflineDownloadState {
  data object Idle : OfflineDownloadState

  data class Downloading(
    val completedResources: Long,
    val requiredResources: Long,
  ) : OfflineDownloadState

  data object Ready : OfflineDownloadState

  data class Failed(val reason: String) : OfflineDownloadState
}

interface OfflineRegionDownloader {
  /** Read by Compose UI; implementations should back this with observable state. */
  val state: OfflineDownloadState

  /** Starts the download and completes when it reaches Ready or Failed. */
  suspend fun downloadRegion(request: OfflineRegionRequest)
}

@Composable
expect fun rememberOfflineRegionDownloader(): OfflineRegionDownloader

