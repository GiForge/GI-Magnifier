package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CapturedPhoto
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkSurfaceTranslucent
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ShutterRed
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.launch

@Composable
fun BottomActionBar(
    onCapturePhoto: () -> Unit,
    isFrozen: Boolean,
    onToggleFreeze: () -> Unit,
    lastPhoto: CapturedPhoto?,
    onOpenGallery: () -> Unit,
    isCapturing: Boolean,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val shutterScale = remember { Animatable(1.0f) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(DarkSurfaceTranslucent)
            .border(1.dp, GlassBorder, RoundedCornerShape(32.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Gallery Thumbnail or Icon
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.5.dp, GlassBorder, RoundedCornerShape(16.dp))
                .clickable { onOpenGallery() }
                .testTag("gallery_button"),
            contentAlignment = Alignment.Center
        ) {
            if (lastPhoto != null) {
                AsyncImage(
                    model = lastPhoto.uri,
                    contentDescription = "Galleria foto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Galleria foto",
                    tint = TextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Center: Big Shutter Button
        Box(
            modifier = Modifier
                .size(76.dp)
                .scale(shutterScale.value)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .border(3.dp, Color.White, CircleShape)
                .clickable(enabled = !isCapturing) {
                    coroutineScope.launch {
                        shutterScale.animateTo(0.88f, tween(80))
                        shutterScale.animateTo(1.0f, tween(120))
                    }
                    onCapturePhoto()
                }
                .testTag("shutter_button"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(if (isFrozen) AccentCyan else ShutterRed)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Scatta foto",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.Center)
                )
            }
        }

        // Right: Freeze Frame Button (Fermo Immagine)
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isFrozen) AccentCyan else Color.White.copy(alpha = 0.1f))
                .border(
                    1.5.dp,
                    if (isFrozen) AccentCyan else GlassBorder,
                    RoundedCornerShape(16.dp)
                )
                .clickable { onToggleFreeze() }
                .testTag("freeze_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFrozen) Icons.Default.PlayArrow else Icons.Default.AcUnit,
                contentDescription = if (isFrozen) "Riprendi anteprima" else "Blocca immagine",
                tint = if (isFrozen) Color.Black else TextPrimary,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
