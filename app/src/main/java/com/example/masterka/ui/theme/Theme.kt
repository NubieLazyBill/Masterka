package com.example.masterka.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.masterka.data.AppTheme

// ===== Схемы для Мастерской =====
private val WorkshopLight = lightColorScheme(
    primary = Amber40,
    onPrimary = Color.White,
    primaryContainer = Amber80,
    onPrimaryContainer = Color(0xFF2E1F00),
    secondary = Leather40,
    onSecondary = Color.White,
    secondaryContainer = Leather80,
    onSecondaryContainer = Color(0xFF2B1A14),
    tertiary = Orange40,
    onTertiary = Color.White,
    tertiaryContainer = Orange80,
    onTertiaryContainer = Color(0xFF2D1600),
    background = Cream99,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = ErrorLight,
    onError = Color.White,
    outline = Color(0xFF837468),
    outlineVariant = Color(0xFFD5C3B4)
)

private val WorkshopDark = darkColorScheme(
    primary = Amber80,
    onPrimary = Color(0xFF2E1F00),
    primaryContainer = Amber30,
    onPrimaryContainer = Amber90,
    secondary = Leather80,
    onSecondary = Color(0xFF2B1A14),
    secondaryContainer = Leather30,
    onSecondaryContainer = Leather90,
    tertiary = Orange80,
    onTertiary = Color(0xFF2D1600),
    tertiaryContainer = Orange30,
    onTertiaryContainer = Orange90,
    background = DarkBackground,
    onBackground = OnSurfaceDark,
    surface = DarkSurface,
    onSurface = OnSurfaceDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = ErrorDark,
    onError = Color(0xFF690005),
    outline = Color(0xFF9C8D80),
    outlineVariant = Color(0xFF504539)
)

// ===== Схемы для Современной =====
private val ModernLight = lightColorScheme(
    primary = Indigo40,
    onPrimary = Color.White,
    primaryContainer = Indigo80,
    onPrimaryContainer = Color(0xFF0D1247),
    secondary = Teal40,
    onSecondary = Color.White,
    secondaryContainer = Teal80,
    onSecondaryContainer = Color(0xFF00251A),
    tertiary = Violet40,
    onTertiary = Color.White,
    tertiaryContainer = Violet80,
    onTertiaryContainer = Color(0xFF2A0039),
    background = ModernLightBg,
    onBackground = ModernLightOnSurface,
    surface = ModernLightSurface,
    onSurface = ModernLightOnSurface,
    surfaceVariant = ModernLightSurfaceVariant,
    onSurfaceVariant = ModernLightOnSurfaceVariant,
    error = ErrorLight,
    onError = Color.White,
    outline = Color(0xFF767680),
    outlineVariant = Color(0xFFC6C6D0)
)

private val ModernDark = darkColorScheme(
    primary = Indigo80,
    onPrimary = Color(0xFF0D1247),
    primaryContainer = Indigo30,
    onPrimaryContainer = Indigo90,
    secondary = Teal80,
    onSecondary = Color(0xFF00251A),
    secondaryContainer = Teal30,
    onSecondaryContainer = Teal90,
    tertiary = Violet80,
    onTertiary = Color(0xFF2A0039),
    tertiaryContainer = Violet30,
    onTertiaryContainer = Violet90,
    background = ModernDarkBg,
    onBackground = ModernDarkOnSurface,
    surface = ModernDarkSurface,
    onSurface = ModernDarkOnSurface,
    surfaceVariant = ModernDarkSurfaceVariant,
    onSurfaceVariant = ModernDarkOnSurfaceVariant,
    error = ErrorDark,
    onError = Color(0xFF690005),
    outline = Color(0xFF90909A),
    outlineVariant = Color(0xFF45464F)
)


// ===== Схемы для Индустриальной =====
private val IndustrialLight = lightColorScheme(
    primary = IndustrialPrimary40,
    onPrimary = Color.White,
    primaryContainer = IndustrialPrimary90,
    onPrimaryContainer = IndustrialPrimary20,
    secondary = IndustrialSecondary40,
    onSecondary = Color.White,
    secondaryContainer = IndustrialSecondary90,
    onSecondaryContainer = IndustrialSecondary20,
    tertiary = IndustrialTertiary40,
    onTertiary = Color.White,
    tertiaryContainer = IndustrialTertiary90,
    onTertiaryContainer = IndustrialTertiary20,
    background = IndustrialLightBg,
    onBackground = IndustrialLightOnSurface,
    surface = IndustrialLightSurface,
    onSurface = IndustrialLightOnSurface,
    surfaceVariant = IndustrialLightSurfaceVariant,
    onSurfaceVariant = IndustrialLightOnSurfaceVariant,
    error = ErrorLight,
    onError = Color.White,
    outline = Color(0xFF7A7A7A),
    outlineVariant = Color(0xFFAAAAAA)
)

private val IndustrialDark = darkColorScheme(
    primary = IndustrialPrimary80,
    onPrimary = IndustrialPrimary20,
    primaryContainer = IndustrialPrimary30,
    onPrimaryContainer = IndustrialPrimary90,
    secondary = IndustrialSecondary80,
    onSecondary = IndustrialSecondary20,
    secondaryContainer = IndustrialSecondary30,
    onSecondaryContainer = IndustrialSecondary90,
    tertiary = IndustrialTertiary80,
    onTertiary = IndustrialTertiary20,
    tertiaryContainer = IndustrialTertiary30,
    onTertiaryContainer = IndustrialTertiary90,
    background = IndustrialDarkBg,
    onBackground = IndustrialDarkOnSurface,
    surface = IndustrialDarkSurface,
    onSurface = IndustrialDarkOnSurface,
    surfaceVariant = IndustrialDarkSurfaceVariant,
    onSurfaceVariant = IndustrialDarkOnSurfaceVariant,
    error = ErrorDark,
    onError = Color(0xFF690005),
    outline = Color(0xFF808080),
    outlineVariant = Color(0xFF3A3A3A)
)

@Composable
fun MasterkaTheme(
    appTheme: AppTheme = AppTheme.WORKSHOP,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.WORKSHOP -> if (darkTheme) WorkshopDark else WorkshopLight
        AppTheme.MODERN -> if (darkTheme) ModernDark else ModernLight
        AppTheme.INDUSTRIAL -> if (darkTheme) IndustrialDark else IndustrialLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}