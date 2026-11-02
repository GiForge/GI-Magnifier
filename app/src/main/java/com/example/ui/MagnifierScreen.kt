package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BottomActionBar
import com.example.ui.components.CameraViewfinder
import com.example.ui.components.FilterBar
import com.example.ui.components.FreezeFrameView
import com.example.ui.components.PermissionRationale
import com.example.ui.components.PhotoGalleryDialog
import com.example.ui.components.TopControlsBar
import com.example.ui.components.ZoomControls
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkSurfaceTranslucent
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun MagnifierScreen(
    viewModel: MagnifierViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Check camera permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setCameraPermissionGranted(isGranted)
    }

    LaunchedEffect(Unit) {
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setCameraPermissionGranted(isGranted)
        if (!isGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Handle user snackbars
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    if (!uiState.hasCameraPermission) {
        PermissionRationale(
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )
        return
    }

    // View references
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // UI Panel Toggles
    var showFilters by remember { mutableStateOf(false) }
    var showExposure by remember { mutableStateOf(false) }

    // Shutter flash animation
    val flashAnimAlpha = remember { Animatable(0f) }

    val capturePhoto: () -> Unit = {
        coroutineScope.launch {
            flashAnimAlpha.snapTo(0.85f)
            flashAnimAlpha.animateTo(0f, tween(180))
        }

        if (uiState.isFrozen && uiState.frozenBitmap != null) {
            // Save currently frozen frame
            viewModel.onPhotoCaptured(uiState.frozenBitmap!!)
        } else {
            val ic = imageCaptureRef
            if (ic != null) {
                ic.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                        try {
                            val rotation = imageProxy.imageInfo.rotationDegrees
                            val buffer = imageProxy.planes[0].buffer
                            val bytes = ByteArray(buffer.remaining())
                            buffer.get(bytes)
                            val rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            val rotatedBitmap = if (rotation != 0) {
                                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                                Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
                            } else {
                                rawBitmap
                            }
                            viewModel.onPhotoCaptured(rotatedBitmap)
                        } catch (e: Exception) {
                            Log.e("MagnifierScreen", "Error processing photo", e)
                        } finally {
                            imageProxy.close()
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("MagnifierScreen", "Capture failed: ${exception.message}", exception)
                    }
                })
            } else {
                // Fallback to previewView bitmap if ImageCapture not bound
                val bmp = previewViewRef?.bitmap
                if (bmp != null) {
                    viewModel.onPhotoCaptured(bmp)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("magnifier_screen")
    ) {
        // Camera Viewfinder (Live stream)
        CameraViewfinder(
            zoomRatio = uiState.zoomRatio,
            isTorchOn = uiState.isTorchOn,
            exposureCompensation = uiState.exposureCompensation,
            filter = uiState.selectedFilter,
            onZoomBoundsAvailable = { min, max -> viewModel.setZoomBounds(min, max) },
            onZoomRatioChanged = { ratio -> viewModel.updateZoomRatio(ratio) },
            onHasFlashUnit = { hasFlash -> viewModel.setHasFlashUnit(hasFlash) },
            onExposureBoundsAvailable = { minExp, maxExp -> viewModel.setExposureBounds(minExp, maxExp) },
            onPreviewViewReady = { pv -> previewViewRef = pv },
            onCaptureReady = { ic -> imageCaptureRef = ic },
            modifier = Modifier.fillMaxSize()
        )

        // Freeze Frame View (when paused)
        if (uiState.isFrozen && uiState.frozenBitmap != null) {
            FreezeFrameView(
                bitmap = uiState.frozenBitmap!!,
                filter = uiState.selectedFilter,
                onUnfreeze = { viewModel.unfreezeFrame() },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Shutter white flash overlay
        if (flashAnimAlpha.value > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = flashAnimAlpha.value))
            )
        }

        // Top Overlay: Flashlight, Title, Filter and Brightness toggles
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TopControlsBar(
                isTorchOn = uiState.isTorchOn,
                hasFlashUnit = uiState.hasFlashUnit,
                onToggleTorch = { viewModel.toggleTorch() },
                showFilters = showFilters,
                onToggleFilters = { showFilters = !showFilters },
                showExposure = showExposure,
                onToggleExposure = { showExposure = !showExposure }
            )

            // Optional Filter Bar
            AnimatedVisibility(
                visible = showFilters,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                FilterBar(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = { viewModel.selectFilter(it) }
                )
            }

            // Optional Exposure / Brightness Slider
            AnimatedVisibility(
                visible = showExposure,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceTranslucent)
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrightnessLow,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Slider(
                            value = uiState.exposureCompensation.toFloat(),
                            onValueChange = { viewModel.updateExposure(it.toInt()) },
                            valueRange = uiState.minExposureCompensation.toFloat()..uiState.maxExposureCompensation.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = AccentCyan,
                                activeTrackColor = AccentCyan
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .testTag("exposure_slider")
                        )
                        Icon(
                            imageVector = Icons.Default.BrightnessMedium,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Bottom Overlay: Zoom Controls + Action Bar (Shutter, Gallery, Freeze)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Zoom slider and quick preset chips
            ZoomControls(
                currentZoom = uiState.zoomRatio,
                minZoom = uiState.minZoomRatio,
                maxZoom = uiState.maxZoomRatio,
                onZoomChanged = { viewModel.updateZoomRatio(it) }
            )

            // Bottom Action Bar: Gallery, Shutter, Freeze
            BottomActionBar(
                onCapturePhoto = capturePhoto,
                isFrozen = uiState.isFrozen,
                onToggleFreeze = {
                    if (uiState.isFrozen) {
                        viewModel.unfreezeFrame()
                    } else {
                        val frame = previewViewRef?.bitmap
                        if (frame != null) {
                            viewModel.freezeFrame(frame)
                        }
                    }
                },
                lastPhoto = uiState.lastCapturedPhoto,
                onOpenGallery = { viewModel.openGallery() },
                isCapturing = uiState.isCapturing
            )
        }

        // Fullscreen Gallery modal
        if (uiState.isGalleryOpen) {
            PhotoGalleryDialog(
                photos = uiState.savedPhotos,
                inspectedPhoto = uiState.inspectedPhoto,
                onInspectPhoto = { photo -> viewModel.inspectPhoto(photo) },
                onCloseInspectPhoto = { viewModel.closeInspectPhoto() },
                onDeletePhoto = { photo -> viewModel.deletePhoto(photo) },
                onSharePhoto = { photo -> viewModel.sharePhoto(photo) },
                onDismiss = { viewModel.closeGallery() }
            )
        }

        // Snackbar host for notifications
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 80.dp)
        )
    }
}
