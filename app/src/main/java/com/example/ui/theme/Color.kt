package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Brand Primary Colors (Indigo / Slate)
val PrimaryLight = Color(0xFF4F46E5)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFE0E7FF)
val OnPrimaryContainerLight = Color(0xFF1E1B4B)

val SecondaryLight = Color(0xFF0D9488)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFCCFBF1)
val OnSecondaryContainerLight = Color(0xFF115E59)

val TertiaryLight = Color(0xFFD97706)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFEF3C7)
val OnTertiaryContainerLight = Color(0xFF78350F)

val BackgroundLight = Color(0xFFF8FAFC)
val OnBackgroundLight = Color(0xFF0F172A)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF0F172A)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val OnSurfaceVariantLight = Color(0xFF475569)
val OutlineLight = Color(0xFFCBD5E1)

// Dark Palette - Deep Midnight Slate (Premium contrast)
val PrimaryDark = Color(0xFF818CF8)
val OnPrimaryDark = Color(0xFF1E1B4B)
val PrimaryContainerDark = Color(0xFF312E81)
val OnPrimaryContainerDark = Color(0xFFE0E7FF)

val SecondaryDark = Color(0xFF2DD4BF)
val OnSecondaryDark = Color(0xFF042F2E)
val SecondaryContainerDark = Color(0xFF115E59)
val OnSecondaryContainerDark = Color(0xFFCCFBF1)

val TertiaryDark = Color(0xFFFBBF24)
val OnTertiaryDark = Color(0xFF451A03)
val TertiaryContainerDark = Color(0xFF78350F)
val OnTertiaryContainerDark = Color(0xFFFEF3C7)

val BackgroundDark = Color(0xFF090D16)
val OnBackgroundDark = Color(0xFFF1F5F9)
val SurfaceDark = Color(0xFF111827)
val OnSurfaceDark = Color(0xFFF8FAFC)
val SurfaceVariantDark = Color(0xFF1E293B)
val OnSurfaceVariantDark = Color(0xFF94A3B8)
val OutlineDark = Color(0xFF334155)

// Note Accent Swatches (for color-coding notes)
data class NoteColorOption(
    val colorValue: Long,
    val name: String,
    val lightColor: Color,
    val darkColor: Color
)

val NoteColorOptions = listOf(
    NoteColorOption(0L, "Default", Color.Unspecified, Color.Unspecified),
    NoteColorOption(0xFFEF4444, "Coral", Color(0xFFFEE2E2), Color(0xFF450A0A)),
    NoteColorOption(0xFFF59E0B, "Amber", Color(0xFFFEF3C7), Color(0xFF451A03)),
    NoteColorOption(0xFF10B981, "Emerald", Color(0xFFD1FAE5), Color(0xFF064E3B)),
    NoteColorOption(0xFF06B6D4, "Cyan", Color(0xFFCFFAFE), Color(0xFF083344)),
    NoteColorOption(0xFF6366F1, "Indigo", Color(0xFFE0E7FF), Color(0xFF1E1B4B)),
    NoteColorOption(0xFF8B5CF6, "Violet", Color(0xFFEDE9FE), Color(0xFF2E1065)),
    NoteColorOption(0xFFEC4899, "Rose", Color(0xFFFCE7F3), Color(0xFF500724))
)
