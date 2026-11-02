package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.model.MagnifierFilter
import com.example.ui.theme.AccentAmber
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Composable
fun CameraViewfinder(
    zoomRatio: Float,
    isTorchOn: Boolean,
    exposureCompensation: Int,
    filter: MagnifierFilter,
    onZoomBoundsAvailable: (min: Float, max: Float) -> Unit,
    onZoomRatioChanged: (Float) -> Unit,
    onHasFlashUnit: (Boolean) -> Unit,
    onExposureBoundsAvailable: (min: Int, max: Int) -> Unit,
    onPreviewViewReady: (PreviewView) -> Unit,
    onCaptureReady: (ImageCapture) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var cameraRef by remember { mutableStateOf<Camera?>(null) }
    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }

    // Tap to focus state
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    val focusAnimScale = remember { Animatable(1.5f) }
    val focusAnimAlpha = remember { Animatable(0f) }

    // Synchronize torch
    LaunchedEffect(isTorchOn, cameraRef) {
        val cam = cameraRef ?: return@LaunchedEffect
        try {
            if (cam.cameraInfo.hasFlashUnit()) {
                cam.cameraControl.enableTorch(isTorchOn)
            }
        } catch (e: Exception) {
            Log.e("CameraViewfinder", "Error toggling torch", e)
        }
    }

    // Synchronize zoom
    LaunchedEffect(zoomRatio, cameraRef) {
        val cam = cameraRef ?: return@LaunchedEffect
        try {
            cam.cameraControl.setZoomRatio(zoomRatio)
        } catch (e: Exception) {
            Log.e("CameraViewfinder", "Error setting zoom", e)
        }
    }

    // Synchronize exposure
    LaunchedEffect(exposureCompensation, cameraRef) {
        val cam = cameraRef ?: return@LaunchedEffect
        try {
            cam.cameraControl.setExposureCompensationIndex(exposureCompensation)
        } catch (e: Exception) {
            Log.e("CameraViewfinder", "Error setting exposure compensation", e)
        }
    }

    // Pinch-to-zoom gesture
    val transformState = rememberTransformableState { zoomChange, _, _ ->
        if (zoomChange != 1.0f) {
            val newZoom = zoomRatio * zoomChange
            onZoomRatioChanged(newZoom)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("camera_viewfinder_container")
            .transformable(state = transformState)
            .pointerInput(previewViewRef, cameraRef) {
                detectTapGestures { offset ->
                    val pv = previewViewRef ?: return@detectTapGestures
                    val cam = cameraRef ?: return@detectTapGestures

                    // Focus & Metering
                    focusPoint = offset
                    coroutineScope.launch {
                        focusAnimScale.snapTo(1.6f)
                        focusAnimAlpha.snapTo(1f)
                        focusAnimScale.animateTo(1.0f, tween(250))
                    }

                    try {
                        val meteringPoint = pv.meteringPointFactory.createPoint(offset.x, offset.y)
                        val action = FocusMeteringAction.Builder(
                            meteringPoint,
                            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                        ).setAutoCancelDuration(3, TimeUnit.SECONDS).build()

                        cam.cameraControl.startFocusAndMetering(action)
                    } catch (e: Exception) {
                        Log.e("CameraViewfinder", "Focus metering error", e)
                    }

                    coroutineScope.launch {
                        kotlinx.coroutines.delay(1200)
                        focusAnimAlpha.animateTo(0f, tween(400))
                        focusPoint = null
                    }
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewViewRef = this
                    onPreviewViewReady(this)

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = surfaceProvider
                            }

                            val imageCapture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                                .build()
                            imageCaptureRef = imageCapture
                            onCaptureReady(imageCapture)

                            val cameraSelector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            } else {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            }

                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                            cameraRef = camera

                            // Flash unit detection
                            val hasFlash = camera.cameraInfo.hasFlashUnit()
                            onHasFlashUnit(hasFlash)

                            // Zoom range
                            camera.cameraInfo.zoomState.observe(lifecycleOwner) { zoomState ->
                                if (zoomState != null) {
                                    val minZ = zoomState.minZoomRatio
                                    val maxZ = zoomState.maxZoomRatio
                                    onZoomBoundsAvailable(minZ, maxZ)
                                }
                            }

                            // Exposure range
                            val exposureState = camera.cameraInfo.exposureState
                            if (exposureState.isExposureCompensationSupported) {
                                val range = exposureState.exposureCompensationRange
                                onExposureBoundsAvailable(range.lower, range.upper)
                            }
                        } catch (e: Exception) {
                            Log.e("CameraViewfinder", "Error binding camera", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Visual Reticle when tapped to focus
        focusPoint?.let { pt ->
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scale = focusAnimScale.value
                val alpha = focusAnimAlpha.value
                val reticleSize = 64.dp.toPx() * scale

                drawCircle(
                    color = AccentAmber.copy(alpha = alpha),
                    radius = reticleSize / 2f,
                    center = pt,
                    style = Stroke(width = 2.5.dp.toPx())
                )
                // Small corner indicators / crosshair ticks
                val tickLen = 8.dp.toPx()
                drawLine(
                    color = AccentAmber.copy(alpha = alpha),
                    start = Offset(pt.x - reticleSize / 2f, pt.y),
                    end = Offset(pt.x - reticleSize / 2f + tickLen, pt.y),
                    strokeWidth = 2.5.dp.toPx()
                )
                drawLine(
                    color = AccentAmber.copy(alpha = alpha),
                    start = Offset(pt.x + reticleSize / 2f - tickLen, pt.y),
                    end = Offset(pt.x + reticleSize / 2f, pt.y),
                    strokeWidth = 2.5.dp.toPx()
                )
                drawLine(
                    color = AccentAmber.copy(alpha = alpha),
                    start = Offset(pt.x, pt.y - reticleSize / 2f),
                    end = Offset(pt.x, pt.y - reticleSize / 2f + tickLen),
                    strokeWidth = 2.5.dp.toPx()
                )
                drawLine(
                    color = AccentAmber.copy(alpha = alpha),
                    start = Offset(pt.x, pt.y + reticleSize / 2f - tickLen),
                    end = Offset(pt.x, pt.y + reticleSize / 2f),
                    strokeWidth = 2.5.dp.toPx()
                )
            }
        }
    }
}
