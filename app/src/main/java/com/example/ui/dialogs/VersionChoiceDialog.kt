package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeEditorBg
import com.example.ui.theme.VsCodeLavender
import com.example.ui.theme.VsCodeSidebarBg
import com.example.ui.theme.VsCodeSurfaceDark
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary
import com.example.viewmodel.StudioVersion

@Composable
fun VersionChoiceDialog(
  currentVersion: StudioVersion,
  onSelectVersion: (StudioVersion) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = VsCodeSidebarBg,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Palette,
          contentDescription = null,
          tint = VsCodeLavender,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Studio Code Editions & Themes",
          color = VsCodeTextPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        StudioVersion.values().forEach { ver ->
          val isSelected = ver == currentVersion
          val accentColor = Color(ver.accentColorHex)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) VsCodeEditorBg else VsCodeSurfaceDark)
              .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accentColor else VsCodeBorder,
                shape = RoundedCornerShape(8.dp)
              )
              .clickable { onSelectVersion(ver) }
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(12.dp)
                  .clip(CircleShape)
                  .background(accentColor)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = ver.editionName,
                    color = VsCodeTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = ver.versionLabel,
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
                Text(
                  text = ver.description,
                  color = VsCodeTextMuted,
                  fontSize = 10.sp,
                  lineHeight = 14.sp
                )
              }
            }

            if (isSelected) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = accentColor,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    },
    confirmButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.testTag("version_picker_close_btn")
      ) {
        Text("Close", color = VsCodeTextPrimary, fontSize = 12.sp)
      }
    }
  )
}
