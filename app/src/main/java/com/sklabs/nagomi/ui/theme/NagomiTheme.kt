package com.sklabs.nagomi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class NagomiPalette(
    val primary: Color,
    val darkBackground: Color,
    val darkSurface: Color,
    val darkCard: Color,
    val lightBackground: Color,
    val lightSurface: Color,
)

private fun color(value: Long) = Color(value or 0xFF000000)

val NagomiPalettes = mapOf(
    "purple" to NagomiPalette(color(0x6D5DF6), color(0x020617), color(0x0F172A), color(0x111827), color(0xF6F4FF), color(0xFFFFFF)),
    "pinky" to NagomiPalette(color(0xD977A8), color(0x151016), color(0x251B27), color(0x251B27), color(0xFFF6FA), color(0xFFFFFF)),
    "ocean" to NagomiPalette(color(0x0E89C9), color(0x06141D), color(0x0C2533), color(0x0C2533), color(0xF2FAFE), color(0xFFFFFF)),
    "forest" to NagomiPalette(color(0x16A36A), color(0x071711), color(0x10271C), color(0x10271C), color(0xF2FAF5), color(0xFFFFFF)),
    "monochrome" to NagomiPalette(color(0x5E5E5E), color(0x0A0A0A), color(0x171717), color(0x1A1A1A), color(0xF8F8F8), color(0xFFFFFF)),
    "slate" to NagomiPalette(color(0x64748B), color(0x0F172A), color(0x1E293B), color(0x1E293B), color(0xF8FAFC), color(0xFFFFFF)),
    "amber" to NagomiPalette(color(0xD97706), color(0x140E0A), color(0x221810), color(0x221810), color(0xFFFBEB), color(0xFFFFFF)),
    "mint" to NagomiPalette(color(0x0D9488), color(0x0B0F12), color(0x121B22), color(0x121B22), color(0xF0F9F9), color(0xFFFFFF)),
)

@Composable
fun NagomiTheme(
    darkTheme: Boolean = true,
    paletteKey: String = "purple",
    content: @Composable () -> Unit,
) {
    val palette = NagomiPalettes[paletteKey] ?: NagomiPalettes.getValue("purple")
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.primary,
            background = palette.darkBackground,
            surface = palette.darkSurface,
            surfaceContainer = palette.darkCard,
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            background = palette.lightBackground,
            surface = palette.lightSurface,
        )
    }

    MaterialTheme(colorScheme = scheme, content = content)
}
