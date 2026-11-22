package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = IOSHealthCoral,
    onPrimary = Color.White,
    primaryContainer = IOSHealthCoralSoft,
    onPrimaryContainer = OnIOSHealthCoralContainer,
    secondary = IOSPlumPurple,
    onSecondary = Color.White,
    secondaryContainer = IOSPurpleContainer,
    onSecondaryContainer = OnIOSPurpleContainer,
    tertiary = IOSMintGreen,
    onTertiary = Color.White,
    tertiaryContainer = IOSMintContainer,
    onTertiaryContainer = OnIOSMintContainer,
    error = IOSCrimsonRed,
    onError = Color.White,
    errorContainer = IOSCrimsonContainer,
    onErrorContainer = Color(0xFF690005),
    background = IOSGroupedBackground,
    onBackground = IOSLabelPrimary,
    surface = IOSCardSurface,
    onSurface = IOSLabelPrimary,
    surfaceVariant = IOSSegmentTrack,
    onSurfaceVariant = IOSLabelSecondary,
    outline = IOSSeparator,
    outlineVariant = IOSOutlineHairline
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF4D6A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF540D1E),
    onPrimaryContainer = Color(0xFFFFD9E0),
    secondary = Color(0xFFA775F0),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3E1866),
    onSecondaryContainer = Color(0xFFF3EAFF),
    tertiary = Color(0xFF2CC881),
    onTertiary = Color(0xFF00381C),
    tertiaryContainer = Color(0xFF0A472B),
    onTertiaryContainer = Color(0xFFC9F8DF),
    error = Color(0xFFFF5252),
    onError = Color.White,
    errorContainer = Color(0xFF6E0E0E),
    onErrorContainer = Color(0xFFFFDAD6),
    background = IOSDarkGroupedBackground,
    onBackground = IOSDarkLabelPrimary,
    surface = IOSDarkCardSurface,
    onSurface = IOSDarkLabelPrimary,
    surfaceVariant = IOSDarkElevatedSurface,
    onSurfaceVariant = IOSDarkLabelSecondary,
    outline = IOSDarkSeparator,
    outlineVariant = IOSDarkOutlineHairline
)

val IOSShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun MedMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = IOSShapes,
        content = content
    )
}
