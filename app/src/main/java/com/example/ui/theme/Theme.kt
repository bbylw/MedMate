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
    outlineVariant = Color(0xFFE5E5EA)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF375F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5C001E),
    onPrimaryContainer = Color(0xFFFFD9E0),
    secondary = Color(0xFFBF5AF2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A156B),
    onSecondaryContainer = Color(0xFFF6ECFC),
    tertiary = Color(0xFF30D158),
    onTertiary = Color(0xFF003912),
    tertiaryContainer = Color(0xFF0F5223),
    onTertiaryContainer = Color(0xFFC8F7D4),
    error = Color(0xFFFF453A),
    onError = Color.White,
    errorContainer = Color(0xFF7A0B0B),
    onErrorContainer = Color(0xFFFFDAD6),
    background = IOSDarkGroupedBackground,
    onBackground = IOSDarkLabelPrimary,
    surface = IOSDarkCardSurface,
    onSurface = IOSDarkLabelPrimary,
    surfaceVariant = IOSDarkElevatedSurface,
    onSurfaceVariant = IOSDarkLabelSecondary,
    outline = IOSDarkSeparator,
    outlineVariant = IOSDarkSeparator
)

val IOSShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp), // iOS standard insetGrouped corner radius
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
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
