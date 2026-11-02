package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkSurfaceTranslucent
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TorchActive
import com.example.ui.theme.TorchGlow

@Composable
fun TopControlsBar(
    isTorchOn: Boolean,
    hasFlashUnit: Boolean,
    onToggleTorch: () -> Unit,
    showFilters: Boolean,
    onToggleFilters: () -> Unit,
    showExposure: Boolean,
    onToggleExposure: () -> Unit,
    modifier: Modifier = Modifier
) {
    val torchBgColor by animateColorAsState(
        targetValue = if (isTorchOn) TorchActive else Color.White.copy(alpha = 0.1f),
        label = "torch_bg"
    )
    val torchIconColor by animateColorAsState(
        targetValue = if (isTorchOn) Color.Black else TextPrimary,
        label = "torch_icon"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkSurfaceTranslucent)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // App brand info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(AccentCyan.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "GI-Magnifier",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Action buttons: Brightness, Filters, Torch
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Brightness / Exposure toggle
            IconButton(
                onClick = onToggleExposure,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (showExposure) AccentCyan else Color.White.copy(alpha = 0.1f))
                    .testTag("exposure_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Brightness6,
                    contentDescription = "Regola luminosità",
                    tint = if (showExposure) Color.Black else TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Filter toggle
            IconButton(
                onClick = onToggleFilters,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (showFilters) AccentCyan else Color.White.copy(alpha = 0.1f))
                    .testTag("filter_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ColorLens,
                    contentDescription = "Filtri di contrasto",
                    tint = if (showFilters) Color.Black else TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Torch Button (Flashlight)
            if (hasFlashUnit) {
                IconButton(
                    onClick = onToggleTorch,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(torchBgColor)
                        .then(
                            if (isTorchOn) Modifier.border(2.dp, TorchGlow, CircleShape)
                            else Modifier
                        )
                        .testTag("torch_button")
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = if (isTorchOn) "Spegni torcia" else "Accendi torcia",
                        tint = torchIconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
