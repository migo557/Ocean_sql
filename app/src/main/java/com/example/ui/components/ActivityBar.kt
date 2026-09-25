package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeActivityBar
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeLavender
import com.example.ui.theme.VsCodeSurfaceDark
import com.example.ui.theme.VsCodeTextMuted
import com.example.viewmodel.ActivePanel

@Composable
fun ActivityBar(
  activePanel: ActivePanel,
  onSelectPanel: (ActivePanel) -> Unit,
  installedExtensionCount: Int,
  onOpenVersionPicker: () -> Unit,
  modifier: Modifier = Modifier,
  isVertical: Boolean = false,
  consoleErrorCount: Int = 0
) {
  if (isVertical) {
    Column(
      modifier = modifier
        .width(56.dp) // w-14
        .fillMaxHeight()
        .background(VsCodeActivityBar) // bg-[#141414]
        .border(width = 1.dp, color = VsCodeBorder) // border-r border-[#333333]
        .padding(vertical = 16.dp)
        .testTag("activity_bar_vertical"),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        ActivityBarItem(
          icon = Icons.Default.Folder,
          label = "Explorer",
          testTag = "activity_bar_explorer",
          isSelected = activePanel == ActivePanel.EXPLORER,
          onClick = { onSelectPanel(ActivePanel.EXPLORER) }
        )

        ActivityBarItem(
          icon = Icons.Default.Search,
          label = "Search",
          testTag = "activity_bar_search",
          isSelected = activePanel == ActivePanel.SEARCH,
          onClick = { onSelectPanel(ActivePanel.SEARCH) }
        )

        ActivityBarItem(
          icon = Icons.Default.Extension,
          label = "Extensions",
          testTag = "activity_bar_extensions",
          isSelected = activePanel == ActivePanel.EXTENSIONS,
          badgeCount = installedExtensionCount,
          onClick = { onSelectPanel(ActivePanel.EXTENSIONS) }
        )

        ActivityBarItem(
          icon = Icons.Default.PlayArrow,
          label = "Web Runner",
          testTag = "activity_bar_runner",
          isSelected = activePanel == ActivePanel.RUNNER,
          onClick = { onSelectPanel(ActivePanel.RUNNER) }
        )

        ActivityBarItem(
          icon = Icons.Default.Terminal,
          label = "Terminal",
          testTag = "activity_bar_terminal",
          isSelected = activePanel == ActivePanel.TERMINAL,
          onClick = { onSelectPanel(ActivePanel.TERMINAL) }
        )

        ActivityBarItem(
          icon = Icons.Default.Code,
          label = "Debug Console",
          testTag = "activity_bar_console",
          isSelected = activePanel == ActivePanel.CONSOLE,
          badgeCount = consoleErrorCount,
          onClick = { onSelectPanel(ActivePanel.CONSOLE) }
        )
      }

      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(12.dp))
          .clickable { onOpenVersionPicker() },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Version & Theme Settings",
          tint = VsCodeTextMuted,
          modifier = Modifier.size(22.dp)
        )
      }
    }
  } else {
    // Horizontal row for compact bottom bar
    Row(
      modifier = modifier
        .fillMaxWidth()
        .height(52.dp)
        .background(VsCodeActivityBar)
        .border(width = 1.dp, color = VsCodeBorder)
        .padding(horizontal = 8.dp)
        .testTag("activity_bar_horizontal"),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      ActivityBarItem(
        icon = Icons.Default.Folder,
        label = "Explorer",
        testTag = "activity_bar_explorer",
        isSelected = activePanel == ActivePanel.EXPLORER,
        onClick = { onSelectPanel(ActivePanel.EXPLORER) }
      )

      ActivityBarItem(
        icon = Icons.Default.Search,
        label = "Search",
        testTag = "activity_bar_search",
        isSelected = activePanel == ActivePanel.SEARCH,
        onClick = { onSelectPanel(ActivePanel.SEARCH) }
      )

      ActivityBarItem(
        icon = Icons.Default.Extension,
        label = "Extensions",
        testTag = "activity_bar_extensions",
        isSelected = activePanel == ActivePanel.EXTENSIONS,
        badgeCount = installedExtensionCount,
        onClick = { onSelectPanel(ActivePanel.EXTENSIONS) }
      )

      ActivityBarItem(
        icon = Icons.Default.PlayArrow,
        label = "Web Runner",
        testTag = "activity_bar_runner",
        isSelected = activePanel == ActivePanel.RUNNER,
        onClick = { onSelectPanel(ActivePanel.RUNNER) }
      )

      ActivityBarItem(
        icon = Icons.Default.Terminal,
        label = "Terminal",
        testTag = "activity_bar_terminal",
        isSelected = activePanel == ActivePanel.TERMINAL,
        onClick = { onSelectPanel(ActivePanel.TERMINAL) }
      )

      ActivityBarItem(
        icon = Icons.Default.Code,
        label = "Debug Console",
        testTag = "activity_bar_console",
        isSelected = activePanel == ActivePanel.CONSOLE,
        badgeCount = consoleErrorCount,
        onClick = { onSelectPanel(ActivePanel.CONSOLE) }
      )

      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(12.dp))
          .clickable { onOpenVersionPicker() },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Version Settings",
          tint = VsCodeTextMuted,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
fun ActivityBarItem(
  icon: ImageVector,
  label: String,
  testTag: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  badgeCount: Int = 0
) {
  Box(
    modifier = modifier
      .size(40.dp)
      .clip(RoundedCornerShape(12.dp)) // rounded-xl
      .background(if (isSelected) VsCodeSurfaceDark else Color.Transparent) // bg-[#2D2D2D] when selected
      .testTag(testTag)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    if (badgeCount > 0) {
      BadgedBox(
        badge = {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(VsCodeLavender) // bg-[#D0BCFF]
              .border(1.dp, VsCodeActivityBar, CircleShape)
          )
        }
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = if (isSelected) VsCodeLavender else VsCodeTextMuted, // text-[#D0BCFF] or text-slate-500
          modifier = Modifier.size(22.dp)
        )
      }
    } else {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isSelected) VsCodeLavender else VsCodeTextMuted, // text-[#D0BCFF] or text-slate-500
        modifier = Modifier.size(22.dp)
      )
    }
  }
}
