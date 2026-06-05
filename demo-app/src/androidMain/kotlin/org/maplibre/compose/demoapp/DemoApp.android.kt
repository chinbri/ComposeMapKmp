package org.maplibre.compose.demoapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.maplibre.android.offline.OfflineGeometryRegionDefinition
import org.maplibre.android.offline.OfflineManager
import org.maplibre.geojson.BoundingBox
import org.maplibre.geojson.Geometry
import org.maplibre.geojson.MultiPoint
import org.maplibre.geojson.Point
//
//@Composable
//actual fun downloadOfflineRegion(
//    region: BoundingBox,
//    zoomRange: IntRange,
//    onComplete: () -> Unit
//) {
//
//    val context = LocalContext.current
//    val offlineManager = OfflineManager.getInstance(context)
//
//    offlineManager.createOfflineRegion()
//}
//
//private fun BoundingBox.toPolygon(): Geometry {
//    return MultiPoint.fromLngLats(
//        listOf(
//            Point.fromLngLat(this.west(), this.south()),
//            Point.fromLngLat((this.east(), this.south()),
//            Point.fromLngLat((this.east(), this.north()),
//            Point.fromLngLat((this.west(), this.north()),
//            Point.fromLngLat((this.west(), this.south()),
//        )
//    )
//}
