package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkSurfaceTranslucent
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun ZoomControls(
    currentZoom: Float,
    minZoom: Float,
    maxZoom: Float,
    onZoomChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(1.0f, 2.0f, 4.0f, 8.0f, 10.0f).filter { it in minZoom..maxZoom }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkSurfaceTranslucent)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row: - button, zoom slider, + button, and current zoom value badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { onZoomChanged((currentZoom - 0.5f).coerceIn(minZoom, maxZoom)) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .testTag("zoom_minus_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Riduci ingrandimento",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Slider(
                value = currentZoom,
                onValueChange = onZoomChanged,
                valueRange = minZoom..maxZoom,
                colors = SliderDefaults.colors(
                    thumbColor = AccentCyan,
                    activeTrackColor = AccentCyan,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .testTag("zoom_slider")
            )

            IconButton(
                onClick = { onZoomChanged((currentZoom + 0.5f).coerceIn(minZoom, maxZoom)) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .testTag("zoom_plus_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Aumenta ingrandimento",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Current magnification badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentCyan.copy(alpha = 0.2f))
                    .border(1.dp, AccentCyan, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("zoom_badge"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format(Locale.US, "%.1fx", currentZoom),
                    color = AccentCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Preset buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            presets.forEach { preset ->
                val isSelected = Math.abs(currentZoom - preset) < 0.2f
                val bg = if (isSelected) AccentCyan else Color.White.copy(alpha = 0.08f)
                val textColor = if (isSelected) Color.Black else TextSecondary

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(bg)
                        .clickable { onZoomChanged(preset) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("preset_${preset.toInt()}x"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${preset.toInt()}x",
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
