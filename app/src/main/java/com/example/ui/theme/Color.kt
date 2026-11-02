package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// iOS Health (Apple Health) Non-Blue System Palette
val IOSHealthCoral = Color(0xFFFF2D55)      // Apple Health Primary Coral Pink/Red
val IOSHealthCoralDark = Color(0xFFD70040)
val IOSHealthCoralSoft = Color(0xFFFFEBEF)
val OnIOSHealthCoralContainer = Color(0xFF4A0014)

val IOSMintGreen = Color(0xFF34C759)        // iOS System Green (Completion & Safe)
val IOSMintContainer = Color(0xFFE8F9ED)
val OnIOSMintContainer = Color(0xFF0A3D18)

val IOSWarmOrange = Color(0xFFFF9500)       // iOS System Orange (Morning / Warnings)
val IOSOrangeContainer = Color(0xFFFFF3E0)

val IOSPlumPurple = Color(0xFFAF52DE)       // iOS System Purple (Bedtime / Pharmacology)
val IOSPurpleContainer = Color(0xFFF6ECFC)
val OnIOSPurpleContainer = Color(0xFF3B0A57)

val IOSCrimsonRed = Color(0xFFFF3B30)       // iOS System Red (Destructive / Toxic Alert)
val IOSCrimsonContainer = Color(0xFFFFEBEA)

// iOS Light System Surfaces
val IOSGroupedBackground = Color(0xFFF2F2F7) // UITableView.Style.insetGrouped background
val IOSCardSurface = Color(0xFFFFFFFF)       // Grouped cell background
val IOSSegmentTrack = Color(0xFFE5E5EA)      // UISegmentedControl track
val IOSSeparator = Color(0xFFC6C6C8)         // iOS Hairline separator
val IOSLabelPrimary = Color(0xFF000000)
val IOSLabelSecondary = Color(0xFF8E8E93)    // iOS System Gray
val IOSLabelTertiary = Color(0xFFC7C7CC)

// iOS Dark System Surfaces (OLED Black + Elevated Gray)
val IOSDarkGroupedBackground = Color(0xFF000000)
val IOSDarkCardSurface = Color(0xFF1C1C1E)
val IOSDarkElevatedSurface = Color(0xFF2C2C2E)
val IOSDarkSeparator = Color(0xFF38383A)
val IOSDarkLabelPrimary = Color(0xFFFFFFFF)
val IOSDarkLabelSecondary = Color(0xFF8E8E93)

// Semantic aliases used across screens (mapped to iOS Non-Blue colors)
val ClinicalBlue = IOSHealthCoral
val ClinicalBlueDark = IOSHealthCoralDark
val EmeraldSafe = IOSMintGreen
val AmberWarning = IOSWarmOrange
val CrimsonDanger = IOSCrimsonRed
val IndigoAccent = IOSPlumPurple

// iOS Medication Pill Swatches (Zero Blue)
val PillColorPalette = listOf(
    0xFFFF2D55L, // 苹果健康珊瑚红 (Apple Health Coral)
    0xFF34C759L, // 苹果薄荷绿 (iOS Mint Green)
    0xFFFF9500L, // 暖阳活力橙 (iOS System Orange)
    0xFFAF52DEL, // 薰衣草紫 (iOS System Purple)
    0xFFFF3B30L, // 朱砂红 (iOS System Red)
    0xFF5856D6L  // 暗夜紫罗兰 (iOS Violet)
)
