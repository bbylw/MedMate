package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Modern Clinical Apple Health Palette (Zero Blue)
val IOSHealthCoral = Color(0xFFFF2D55)      // Primary Health Coral Pink/Red
val IOSHealthCoralGlow = Color(0xFFFF6482)  // Gradient highlight Coral
val IOSHealthCoralDark = Color(0xFFD70040)
val IOSHealthCoralSoft = Color(0xFFFFEBEF)
val OnIOSHealthCoralContainer = Color(0xFF4A0014)

val IOSMintGreen = Color(0xFF2AC769)        // Refined Modern Emerald Mint (Completion & Safe)
val IOSMintGlow = Color(0xFF5CE190)         // Gradient highlight Mint
val IOSMintContainer = Color(0xFFE7F9EE)
val OnIOSMintContainer = Color(0xFF0A3D18)

val IOSWarmOrange = Color(0xFFFF9500)       // Warm Amber Orange (Morning / Warnings)
val IOSOrangeContainer = Color(0xFFFFF4E3)

val IOSPlumPurple = Color(0xFF9F51E8)       // Modern Iris Plum (Bedtime / Pharmacology)
val IOSPurpleContainer = Color(0xFFF5ECFD)
val OnIOSPurpleContainer = Color(0xFF3B0A57)

val IOSCrimsonRed = Color(0xFFFF3B30)       // Crisp Crimson Red (Destructive / Toxic Alert)
val IOSCrimsonContainer = Color(0xFFFFECEB)

// Refined Porcelain Light System Surfaces
val IOSGroupedBackground = Color(0xFFF4F5F9) // Soft pearl-porcelain canvas
val IOSCardSurface = Color(0xFFFFFFFF)       // Pure elevated card surface
val IOSSegmentTrack = Color(0xFFE8E9F0)      // Refined segmented control track
val IOSSeparator = Color(0xFFD8D9E2)         // Subtle crisp hairline separator
val IOSLabelPrimary = Color(0xFF111217)      // Rich ink charcoal (softer than harsh #000)
val IOSLabelSecondary = Color(0xFF6E717C)    // High-legibility slate gray
val IOSLabelTertiary = Color(0xFFA8ABB6)

// Deep Obsidian Dark System Surfaces
val IOSDarkGroupedBackground = Color(0xFF0B0C10)
val IOSDarkCardSurface = Color(0xFF16171E)
val IOSDarkElevatedSurface = Color(0xFF23252F)
val IOSDarkSeparator = Color(0xFF2D2F3B)
val IOSDarkLabelPrimary = Color(0xFFF5F6FA)
val IOSDarkLabelSecondary = Color(0xFF9699A6)

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
