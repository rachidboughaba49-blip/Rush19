package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RushWayColorScheme = darkColorScheme(
  primary = RushPurpleAccent,
  onPrimary = RushTextWhite,
  primaryContainer = RushPurpleContainer,
  onPrimaryContainer = RushPurpleLight,
  secondary = RushCobalt,
  onSecondary = RushBlack,
  secondaryContainer = RushNavyElevated,
  onSecondaryContainer = RushTextWhite,
  tertiary = RushPurpleLight,
  onTertiary = RushBlack,
  background = RushDarkNavy,
  onBackground = RushTextWhite,
  surface = RushNavySurface,
  onSurface = RushTextWhite,
  surfaceVariant = RushCardBg,
  onSurfaceVariant = RushTextGray,
  outline = RushNavyBorder,
  error = RushDanger,
  onError = RushTextWhite
)

@Composable
fun TheRushWayTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = RushWayColorScheme,
    typography = Typography,
    content = content
  )
}
