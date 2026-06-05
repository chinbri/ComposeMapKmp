package org.maplibre.compose.demoapp

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.demoapp.generated.Res
import org.maplibre.compose.demoapp.generated.keyboard_arrow_up_24px
import org.maplibre.compose.demoapp.util.Platform
import org.maplibre.compose.demoapp.util.PlatformFeature
import org.maplibre.compose.demoapp.util.getDefaultColorScheme
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import androidx.compose.ui.graphics.Color
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.layers.LineLayer
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.layout.padding

//
//@Composable
//expect fun downloadOfflineRegion(region: BoundingBox, zoomRange: IntRange, onComplete: () -> Unit)

@Composable
expect fun DownloadTiles(
    downloadRequestId: Int,
    onDownloadStarted: () -> Unit = {},
    onDownloadComplete: () -> Unit = {},
    onDownloadError: (String) -> Unit = {},
)

@Composable
fun DemoApp() {

    MyMap()
//  OriginalMap()
}

@Composable
private fun MyMap() {

    var showMap by remember { mutableStateOf(false) }
    val markerPositions = remember { mutableStateListOf<Position>() }
    val markerFeatures by remember {
        derivedStateOf {
            FeatureCollection(markerPositions.map { Feature(geometry = Point(it), properties = null) })
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val buildingBounds = BoundingBox(
        west = -0.37702,
        south = 39.46988,
        east = -0.37595,
        north = 39.47075,
    )
    var downloadRequestId by remember { mutableStateOf(0) }
    var isDownloading by remember { mutableStateOf(false) }
    var isDownloaded by remember { mutableStateOf(false) }
    var downloadError by remember { mutableStateOf<String?>(null) }

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(-0.3763, 39.4699),
            zoom = 11.0,
        )
    )

    DownloadTiles(
        downloadRequestId = downloadRequestId,
        onDownloadStarted = {
            isDownloading = true
            isDownloaded = false
            downloadError = null
        },
        onDownloadComplete = {
            isDownloading = false
            isDownloaded = true
        },
        onDownloadError = { error ->
            isDownloading = false
            isDownloaded = false
            downloadError = error
        },
    )

    if (showMap) {
        Box(modifier = Modifier.fillMaxSize()) {
            MaplibreMap(
                // Debe coincidir con el estilo descargado para que el SDK resuelva recursos offline.
                baseStyle = Protomaps.Light.base,
                cameraState = cameraState,
                onMapClick = { position, _ ->
                    markerPositions.add(position)
                    ClickResult.Consume
                },
            ) {
                val buildingSource =
                    rememberGeoJsonSource(GeoJsonData.Uri(Res.getUri("files/data/building.geojson")))
                val markerSource = rememberGeoJsonSource(data = GeoJsonData.Features(markerFeatures))

                FillLayer(
                    id = "building-fill",
                    source = buildingSource,
                    color = const(Color(0xFF6D4C41)),
                    opacity = const(0.55f),
                )
                LineLayer(
                    id = "building-outline",
                    source = buildingSource,
                    color = const(Color(0xFF3E2723)),
                    width = const(2.dp),
                )
                CircleLayer(
                    id = "tap-markers",
                    source = markerSource,
                    color = const(Color(0xFFE53935)),
                    radius = const(7.dp),
                    strokeColor = const(Color.White),
                    strokeWidth = const(2.dp),
                )
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        cameraState.animateTo(
                            boundingBox = buildingBounds,
                            padding = PaddingValues(48.dp),
                        )
                    }
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            ) {
                Text("Ir al edificio")
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val statusText = when {
                    isDownloading -> "Descargando datos offline..."
                    isDownloaded -> "Datos descargados. Ya puedes abrir el mapa sin wifi."
                    downloadError != null -> "Error de descarga: $downloadError"
                    else -> "Pulsa 'Descargar datos' para preparar el modo sin conexión."
                }
                Text(statusText)

                Button(
                    enabled = !isDownloading,
                    onClick = { downloadRequestId += 1 },
                ) {
                    Text(if (isDownloading) "Descargando..." else "Descargar datos")
                }

                Button(
                    enabled = isDownloaded && !isDownloading,
                    onClick = { showMap = true },
                ) {
                    Text("Abrir mapa")
                }
            }
        }
    }

}

@Composable
private fun OriginalMap() {
    val demoState = rememberDemoState()
    MaterialTheme(colorScheme = getDefaultColorScheme(isDark = demoState.selectedStyle.isDark)) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            if (PlatformFeature.InteropBlending !in Platform.supportedFeatures) {
                TwoColumnLayout(
                    menu = {
                        DemoSheetContent(
                            state = demoState,
                            modifier = Modifier.fillMaxHeight()
                        )
                    },
                    map = { DemoMap(demoState) },
                )
            } else {
                SheetLayout(
                    menu = {
                        DemoSheetContent(
                            state = demoState,
                            modifier = Modifier.fillMaxHeight()
                        )
                    },
                    map = { padding -> DemoMap(demoState, padding) },
                )
            }
        }
    }
}

@Composable
private fun TwoColumnLayout(
    menu: @Composable () -> Unit,
    map: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.width(300.dp).fillMaxHeight()) { menu() }
        Box(modifier = Modifier.weight(1f)) { map() }
    }
}

@Composable
private fun SheetLayout(
    menu: @Composable () -> Unit,
    map: @Composable (PaddingValues) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberBottomSheetScaffoldState()
    BottomSheetScaffold(
        sheetPeekHeight = 128.dp, // TODO dynamic peek based on selected demo
        scaffoldState = sheetState,
        sheetSwipeEnabled = true,
        sheetDragHandle = {
            ExpandCollapseButton(
                sheetState.bottomSheetState.targetValue == SheetValue.Expanded,
                onExpand = { sheetState.bottomSheetState.expand() },
                onCollapse = { sheetState.bottomSheetState.partialExpand() },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        sheetContent = {
            Box(
                // TODO this doesn't work well on landscape and small screens
                modifier =
                    Modifier.background(BottomSheetDefaults.ContainerColor)
                        .consumeWindowInsets(PaddingValues(top = 56.dp))
                        .requiredHeight(500.dp)
            ) {
                menu()
            }
        },
        modifier = modifier,
    ) { padding ->
        map(padding)
    }
}

@Composable
private fun ExpandCollapseButton(
    expanded: Boolean,
    onExpand: suspend () -> Unit,
    onCollapse: suspend () -> Unit,
    modifier: Modifier = Modifier,
) {
    val degrees by animateFloatAsState(targetValue = if (expanded) 180f else 0f)
    val coroutineScope = rememberCoroutineScope()
    IconButton(
        modifier = modifier,
        onClick = { coroutineScope.launch { if (expanded) onCollapse() else onExpand() } },
    ) {
        Icon(
            vectorResource(Res.drawable.keyboard_arrow_up_24px),
            contentDescription = if (expanded) "Collapse" else "Expand",
            modifier = Modifier.rotate(degrees),
        )
    }
}
