package com.example.rentify.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// ========== ESQUEMA DE COLORES RENTIFY ==========
// Basado en el diseño del documento: Verde oscuro profesional

private val DarkColorScheme = darkColorScheme(
    primary = GreenMedium,           // Verde medio para modo oscuro
    secondary = GrayMedium,          // Gris medio
    tertiary = GreenLight,           // Verde claro para acentos
    background = GrayDark,           // Fondo oscuro
    surface = GrayDark,              // Superficie oscura
    onPrimary = White,               // Texto sobre primary
    onSecondary = White,             // Texto sobre secondary
    onTertiary = White,              // Texto sobre tertiary
    onBackground = White,            // Texto sobre background
    onSurface = White                // Texto sobre surface
)

private val LightColorScheme = lightColorScheme(
    primary = GreenDark,             // Verde oscuro (principal)
    secondary = GreenMedium,         // Verde medio (secundario)
    tertiary = GreenLight,           // Verde claro (acentos)
    background = White,              // Fondo blanco limpio
    surface = White,                 // Superficie blanca
    surfaceVariant = GrayBg,         // Fondo gris muy claro
    onPrimary = White,               // Texto sobre primary
    onSecondary = White,             // Texto sobre secondary
    onTertiary = Black,              // Texto sobre tertiary
    onBackground = GrayDark,         // Texto principal
    onSurface = GrayDark,            // Texto sobre surface
    onSurfaceVariant = GrayMedium,   // Texto secundario

    // Colores especiales
    error = ErrorRed,                // Errores
    primaryContainer = GreenLight,   // Contenedores primary
    secondaryContainer = GrayLight,  // Contenedores secondary
    onPrimaryContainer = GreenDark,  // Texto en containers primary
    onSecondaryContainer = GrayDark  // Texto en containers secondary
)

@Composable
fun RentifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,  // Deshabilitado para mantener identidad de marca
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}