package org.maplibre.compose.demoapp.offline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.maplibre.compose.offline.DownloadProgress
import org.maplibre.compose.offline.DownloadStatus
import org.maplibre.compose.offline.OfflineManagerException
import org.maplibre.compose.offline.OfflinePackDefinition
import org.maplibre.compose.offline.rememberOfflineManager

@Composable
actual fun rememberOfflineRegionDownloader(): OfflineRegionDownloader {
  val offlineManager = rememberOfflineManager()
  return remember(offlineManager) { NativeOfflineRegionDownloader(offlineManager) }
}

private class NativeOfflineRegionDownloader(
  private val offlineManager: org.maplibre.compose.offline.OfflineManager,
) : OfflineRegionDownloader {
  override var state: OfflineDownloadState by mutableStateOf(OfflineDownloadState.Idle)
    private set

  override suspend fun downloadRegion(request: OfflineRegionRequest) {
    state = OfflineDownloadState.Downloading(completedResources = 0, requiredResources = 0)

    try {
      val pack =
        offlineManager.create(
          definition =
            OfflinePackDefinition.TilePyramid(
              styleUrl = request.styleUri,
              bounds = request.bounds,
              minZoom = request.minZoom,
              maxZoom = request.maxZoom,
            ),
          metadata = request.name.encodeToByteArray(),
        )

      offlineManager.resume(pack)

      while (currentCoroutineContext().isActive) {
        when (val progress = pack.downloadProgress) {
          is DownloadProgress.Healthy -> {
            state =
              OfflineDownloadState.Downloading(
                completedResources = progress.completedResourceCount,
                requiredResources = progress.requiredResourceCount,
              )
            if (progress.status == DownloadStatus.Complete) {
              state = OfflineDownloadState.Ready
              return
            }
          }
          is DownloadProgress.Error -> {
            state = OfflineDownloadState.Failed(progress.message.ifBlank { progress.reason })
            return
          }
          is DownloadProgress.TileLimitExceeded -> {
            state = OfflineDownloadState.Failed("Tile limit exceeded: ${progress.limit}")
            return
          }
          DownloadProgress.Unknown -> {
            state = OfflineDownloadState.Downloading(completedResources = 0, requiredResources = 0)
          }
        }
        delay(350)
      }
    } catch (error: OfflineManagerException) {
      state = OfflineDownloadState.Failed(error.message ?: "Offline manager error")
    } catch (error: Throwable) {
      state = OfflineDownloadState.Failed(error.message ?: "Unknown download error")
    }
  }
}

