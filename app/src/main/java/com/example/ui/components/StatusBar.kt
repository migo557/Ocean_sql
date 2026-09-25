package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.ContributedStatusBarItem
import com.example.ui.theme.VsCodeBlue
import com.example.ui.theme.VsCodeEmerald
import com.example.ui.theme.VsCodeLavender
import com.example.viewmodel.AutoSaveStatus
import com.example.viewmodel.StudioVersion

@Composable
fun StatusBar(
  activeLanguage: String,
  currentVersion: StudioVersion,
  onOpenRunner: () -> Unit,
  onOpenVersionPicker: () -> Unit,
  isRunnerActive: Boolean,
  contributedItems: List<ContributedStatusBarItem> = emptyList(),
  onExecuteCommand: (String) -> Unit = {},
  autoSaveStatus: AutoSaveStatus = AutoSaveStatus.SAVED,
  isAutoSaveEnabled: Boolean = true,
  lastSavedTime: String = "Just now",
  isLivePreviewOpen: Boolean = false,
  onToggleLivePreview: () -> Unit = {},
  onManualSave: () -> Unit = {},
  onToggleAutoSave: () -> Unit = {}
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(28.dp) // h-7
      .background(VsCodeBlue) // bg-[#007ACC]
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 12.dp)
      .testTag("status_bar"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Left group: Git branch, Problems, Live Server status, Extension items
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Text(
        text = "main*",
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = "0",
        color = Color.White.copy(alpha = 0.9f),
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace
      )

      // Extension contributed status items
      contributedItems.forEach { item ->
        Row(
          modifier = Modifier
            .clip(CircleShape)
            .clickable(enabled = item.command != null) {
              item.command?.let { onExecuteCommand(it) }
            }
            .background(Color(0x33000000))
            .padding(horizontal = 6.dp, vertical = 2.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = item.text,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      // "Go Live" indicator
      Row(
        modifier = Modifier
          .clip(CircleShape)
          .clickable { onOpenRunner() }
          .background(if (isRunnerActive) Color(0xFF0F766E) else Color(0x33000000))
          .padding(horizontal = 6.dp, vertical = 2.dp)
          .testTag("status_bar_go_live"),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Bolt,
          contentDescription = "Live Server",
          tint = if (isRunnerActive) VsCodeEmerald else Color.White,
          modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
          text = if (isRunnerActive) "Port: 3000" else "Go Live",
          color = Color.White,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold
        )
      }

      // Live HTML Preview indicator
      Row(
        modifier = Modifier
          .clip(CircleShape)
          .clickable { onToggleLivePreview() }
          .background(if (isLivePreviewOpen) Color(0xFF0F766E) else Color(0x33000000))
          .padding(horizontal = 6.dp, vertical = 2.dp)
          .testTag("status_bar_live_preview"),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Bolt,
          contentDescription = "Live Preview",
          tint = if (isLivePreviewOpen) VsCodeEmerald else Color.White.copy(alpha = 0.8f),
          modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
          text = if (isLivePreviewOpen) "Preview: ON" else "Preview: OFF",
          color = Color.White,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // Right group: UTF-8, Language, Just now (from Sophisticated Dark Design HTML)
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Text(
        text = "UTF-8",
        color = Color.White.copy(alpha = 0.9f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
      )
      Text(
        text = activeLanguage.uppercase(),
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
      )
      // Auto-save interactive status badge
      Row(
        modifier = Modifier
          .clip(CircleShape)
          .clickable { onManualSave() }
          .background(
            when (autoSaveStatus) {
              AutoSaveStatus.SAVING -> Color(0x33D0BCFF)
              AutoSaveStatus.UNSAVED -> Color(0x33F59E0B)
              AutoSaveStatus.SAVED -> Color(0x22000000)
            }
          )
          .padding(horizontal = 6.dp, vertical = 2.dp)
          .testTag("status_bar_auto_save"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Icon(
          imageVector = when (autoSaveStatus) {
            AutoSaveStatus.SAVING -> Icons.Default.Refresh
            AutoSaveStatus.UNSAVED -> Icons.Default.Circle
            AutoSaveStatus.SAVED -> Icons.Default.Check
          },
          contentDescription = "Auto-save: ${autoSaveStatus.label}",
          tint = when (autoSaveStatus) {
            AutoSaveStatus.SAVING -> VsCodeLavender
            AutoSaveStatus.UNSAVED -> Color(0xFFF59E0B)
            AutoSaveStatus.SAVED -> Color.White.copy(alpha = 0.85f)
          },
          modifier = Modifier.size(if (autoSaveStatus == AutoSaveStatus.UNSAVED) 8.dp else 11.dp)
        )
        Text(
          text = when (autoSaveStatus) {
            AutoSaveStatus.SAVING -> "Saving..."
            AutoSaveStatus.UNSAVED -> "Unsaved (Auto-saving)"
            AutoSaveStatus.SAVED -> if (isAutoSaveEnabled) "Saved ($lastSavedTime)" else "Saved"
          },
          color = Color.White.copy(alpha = 0.95f),
          fontSize = 10.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}
