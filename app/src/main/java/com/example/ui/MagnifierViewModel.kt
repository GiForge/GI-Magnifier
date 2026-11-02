package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CapturedPhoto
import com.example.model.MagnifierFilter
import com.example.util.PhotoStorageHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MagnifierUiState(
    val hasCameraPermission: Boolean = false,
    val zoomRatio: Float = 1.0f,
    val minZoomRatio: Float = 1.0f,
    val maxZoomRatio: Float = 10.0f,
    val isTorchOn: Boolean = false,
    val hasFlashUnit: Boolean = true,
    val selectedFilter: MagnifierFilter = MagnifierFilter.NORMAL,
    val isFrozen: Boolean = false,
    val frozenBitmap: Bitmap? = null,
    val exposureCompensation: Int = 0,
    val minExposureCompensation: Int = -4,
    val maxExposureCompensation: Int = 4,
    val isCapturing: Boolean = false,
    val lastCapturedPhoto: CapturedPhoto? = null,
    val savedPhotos: List<CapturedPhoto> = emptyList(),
    val isGalleryOpen: Boolean = false,
    val inspectedPhoto: CapturedPhoto? = null,
    val userMessage: String? = null
)

class MagnifierViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MagnifierUiState())
    val uiState: StateFlow<MagnifierUiState> = _uiState.asStateFlow()

    init {
        loadSavedPhotos()
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
        if (granted) {
            loadSavedPhotos()
        }
    }

    fun setZoomBounds(minZoom: Float, maxZoom: Float) {
        val safeMin = maxOf(1.0f, minZoom)
        val safeMax = maxOf(safeMin * 2f, maxZoom)
        _uiState.update { current ->
            current.copy(
                minZoomRatio = safeMin,
                maxZoomRatio = safeMax,
                zoomRatio = current.zoomRatio.coerceIn(safeMin, safeMax)
            )
        }
    }

    fun updateZoomRatio(ratio: Float) {
        val state = _uiState.value
        val clamped = ratio.coerceIn(state.minZoomRatio, state.maxZoomRatio)
        _uiState.update { it.copy(zoomRatio = clamped) }
    }

    fun setPresetZoom(preset: Float) {
        val state = _uiState.value
        val clamped = preset.coerceIn(state.minZoomRatio, state.maxZoomRatio)
        _uiState.update { it.copy(zoomRatio = clamped) }
    }

    fun toggleTorch() {
        _uiState.update { it.copy(isTorchOn = !it.isTorchOn) }
    }

    fun setHasFlashUnit(hasFlash: Boolean) {
        _uiState.update { it.copy(hasFlashUnit = hasFlash) }
    }

    fun selectFilter(filter: MagnifierFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun freezeFrame(bitmap: Bitmap?) {
        if (bitmap == null) return
        _uiState.update {
            it.copy(
                isFrozen = true,
                frozenBitmap = bitmap
            )
        }
    }

    fun unfreezeFrame() {
        _uiState.update {
            it.copy(
                isFrozen = false,
                frozenBitmap = null
            )
        }
    }

    fun setExposureBounds(minExp: Int, maxExp: Int) {
        _uiState.update {
            it.copy(
                minExposureCompensation = minExp,
                maxExposureCompensation = maxExp
            )
        }
    }

    fun updateExposure(exposure: Int) {
        val state = _uiState.value
        val clamped = exposure.coerceIn(state.minExposureCompensation, state.maxExposureCompensation)
        _uiState.update { it.copy(exposureCompensation = clamped) }
    }

    fun onPhotoCaptured(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCapturing = true) }
            val zoom = _uiState.value.zoomRatio
            val savedPhoto = PhotoStorageHelper.saveBitmap(getApplication(), bitmap, zoom)
            if (savedPhoto != null) {
                _uiState.update { current ->
                    current.copy(
                        isCapturing = false,
                        lastCapturedPhoto = savedPhoto,
                        savedPhotos = listOf(savedPhoto) + current.savedPhotos.filter { it.id != savedPhoto.id },
                        userMessage = "Foto salvata con successo!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isCapturing = false,
                        userMessage = "Errore nel salvataggio della foto"
                    )
                }
            }
        }
    }

    fun loadSavedPhotos() {
        viewModelScope.launch {
            val photos = PhotoStorageHelper.loadSavedPhotos(getApplication())
            _uiState.update {
                it.copy(
                    savedPhotos = photos,
                    lastCapturedPhoto = photos.firstOrNull() ?: it.lastCapturedPhoto
                )
            }
        }
    }

    fun openGallery() {
        loadSavedPhotos()
        _uiState.update { it.copy(isGalleryOpen = true) }
    }

    fun closeGallery() {
        _uiState.update { it.copy(isGalleryOpen = false, inspectedPhoto = null) }
    }

    fun inspectPhoto(photo: CapturedPhoto) {
        _uiState.update { it.copy(inspectedPhoto = photo) }
    }

    fun closeInspectPhoto() {
        _uiState.update { it.copy(inspectedPhoto = null) }
    }

    fun deletePhoto(photo: CapturedPhoto) {
        viewModelScope.launch {
            val success = PhotoStorageHelper.deletePhoto(getApplication(), photo)
            if (success) {
                _uiState.update { current ->
                    val updated = current.savedPhotos.filter { it.id != photo.id }
                    val currentInspected = if (current.inspectedPhoto?.id == photo.id) null else current.inspectedPhoto
                    val newLast = updated.firstOrNull()
                    current.copy(
                        savedPhotos = updated,
                        inspectedPhoto = currentInspected,
                        lastCapturedPhoto = newLast,
                        userMessage = "Foto eliminata"
                    )
                }
            }
        }
    }

    fun sharePhoto(photo: CapturedPhoto) {
        PhotoStorageHelper.sharePhoto(getApplication(), photo)
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
