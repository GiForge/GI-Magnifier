package com.example.model

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix

enum class MagnifierFilter(val displayName: String, val description: String) {
    NORMAL("Normale", "Colori naturali"),
    INVERTED("Negativo", "Testo bianco su sfondo scuro"),
    MONO_HIGH_CONTRAST("B/N Contrasto", "Scala di grigi potenziata"),
    YELLOW_ON_BLACK("Giallo su Nero", "Massima leggibilità per testi"),
    SEPIA("Seppia", "Tonalità calda riposante");

    fun toColorFilter(): ColorFilter? {
        return when (this) {
            NORMAL -> null
            INVERTED -> ColorFilter.colorMatrix(
                ColorMatrix(
                    floatArrayOf(
                        -1f,  0f,  0f, 0f, 255f,
                         0f, -1f,  0f, 0f, 255f,
                         0f,  0f, -1f, 0f, 255f,
                         0f,  0f,  0f, 1f,   0f
                    )
                )
            )
            MONO_HIGH_CONTRAST -> ColorFilter.colorMatrix(
                ColorMatrix(
                    floatArrayOf(
                        1.5f, 1.5f, 1.5f, 0f, -128f,
                        1.5f, 1.5f, 1.5f, 0f, -128f,
                        1.5f, 1.5f, 1.5f, 0f, -128f,
                        0f,   0f,   0f,   1f,    0f
                    )
                )
            )
            YELLOW_ON_BLACK -> ColorFilter.colorMatrix(
                ColorMatrix(
                    floatArrayOf(
                        -0.3f, -0.59f, -0.11f, 0f, 255f,
                        -0.3f, -0.59f, -0.11f, 0f, 255f,
                        0f,     0f,     0f,    0f,   0f,
                        0f,     0f,     0f,    1f,   0f
                    )
                )
            )
            SEPIA -> ColorFilter.colorMatrix(
                ColorMatrix(
                    floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 0f,
                        0.349f, 0.686f, 0.168f, 0f, 0f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0f,     0f,     0f,     1f, 0f
                    )
                )
            )
        }
    }
}
