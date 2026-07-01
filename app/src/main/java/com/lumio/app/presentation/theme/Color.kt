package com.lumio.app.presentation.theme

import androidx.compose.ui.graphics.Color

// ── Brand Colors (legacy — kept so existing screens still compile) ──
val LumioBlue = Color(0xFF2563EB)
val LumioBlueDark = Color(0xFF1D4ED8)
val LumioBlueLight = Color(0xFF60A5FA)
val LumioPurple = Color(0xFF7C3AED)
val LumioPurpleDark = Color(0xFF6D28D9)
val LumioTeal = Color(0xFF0D9488)
val LumioGreen = Color(0xFF059669)
val LumioOrange = Color(0xFFEA580C)
val LumioRed = Color(0xFFDC2626)
val LumioAmber = Color(0xFFD97706)

// ── Legacy light/dark/amoled neutrals (kept for compatibility) ──
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurface2 = Color(0xFFF1F5F9)
val LightSurface3 = Color(0xFFE2E8F0)
val LightOnSurface = Color(0xFF0F172A)
val LightOnSurface2 = Color(0xFF475569)
val LightOnSurface3 = Color(0xFF94A3B8)
val LightDivider = Color(0xFFE2E8F0)

val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkSurface2 = Color(0xFF334155)
val DarkSurface3 = Color(0xFF475569)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkOnSurface2 = Color(0xFFCBD5E1)
val DarkOnSurface3 = Color(0xFF94A3B8)
val DarkDivider = Color(0xFF334155)

val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF0A0A0A)
val AmoledSurface2 = Color(0xFF141414)

val GradientBlue = listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
val GradientGreen = listOf(Color(0xFF059669), Color(0xFF0D9488))
val GradientOrange = listOf(Color(0xFFEA580C), Color(0xFFD97706))
val GradientPurple = listOf(Color(0xFF7C3AED), Color(0xFFDB2777))
val GradientRed = listOf(Color(0xFFDC2626), Color(0xFFEA580C))

// ══════════════════════════════════════════════════════════════
//  CLEAR MORNING — the redesign palette (calm, luminous, minimal)
// ══════════════════════════════════════════════════════════════
val LumioAccent      = Color(0xFF3B7A57) // sage / forest — primary action
val LumioAccentSoft  = Color(0xFFE8F1EB) // light green tint — selected states
val LumioAccentInk   = Color(0xFF2C5D42) // deep green — text on soft green

val ClearCanvas      = Color(0xFFF4F5F3) // warm off-white — app background
val ClearSurface     = Color(0xFFFFFFFF) // cards lift with pure white
val ClearSurfaceSoft = Color(0xFFEDEFEC) // faint neutral fill
val ClearInk         = Color(0xFF1B1D1C) // warm near-black — primary text
val ClearMuted       = Color(0xFF7A7E7B) // secondary text
val ClearFaint       = Color(0xFFAFB2AF) // tertiary / disabled
val ClearLine        = Color(0xFFEAEAE6) // hairline dividers / borders
val ClearLineSoft    = Color(0xFFDEE0DC) // slightly stronger hairline

val LumioSlate       = Color(0xFF566B60) // muted green-grey — secondary
val LumioSlateSoft   = Color(0xFFE4EAE5)

val CalmError        = Color(0xFFC0483B)
val CalmErrorSoft    = Color(0xFFF7E4E1)
val CalmErrorInk     = Color(0xFF7A2318)
