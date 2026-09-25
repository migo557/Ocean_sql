package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SophisticatedDarkColorScheme =
  darkColorScheme(
    primary = VsCodeLavender,
    onPrimary = VsCodeLavenderDark,
    primaryContainer = VsCodeSurfaceDark,
    onPrimaryContainer = VsCodeLavender,
    secondary = VsCodeCyan,
    onSecondary = VsCodeBg,
    secondaryContainer = VsCodeButtonSecondary,
    onSecondaryContainer = VsCodeTextPrimary,
    tertiary = VsCodeEmerald,
    onTertiary = VsCodeBg,
    background = VsCodeBg,
    onBackground = VsCodeTextPrimary,
    surface = VsCodeEditorBg,
    onSurface = VsCodeTextPrimary,
    surfaceVariant = VsCodeTabsBg,
    onSurfaceVariant = VsCodeTextSecondary,
    outline = VsCodeBorder,
    outlineVariant = VsCodeBorder,
    error = VsCodeRose,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = SophisticatedDarkColorScheme,
    typography = Typography,
    content = content,
  )
}
