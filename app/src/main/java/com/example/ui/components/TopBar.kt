package com.example.ui.components

import android.content.Context
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeButtonSecondary
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeLavender
import com.example.ui.theme.VsCodeLavenderDark
import com.example.ui.theme.VsCodeTabsBg
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary
import com.example.viewmodel.StudioVersion

@Composable
fun TopBar(
  currentVersion: StudioVersion,
  onOpenVersionPicker: () -> Unit,
  onCopyHtml: (Context, Boolean) -> Unit,
  onOpenRunner: () -> Unit,
  onOpenGlobalFileSearch: () -> Unit = {},
  activeFileName: String = "index.html",
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showCopyMenu by remember { mutableStateOf(false) }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(56.dp)
      .background(VsCodeTabsBg) // bg-[#1A1A1A]
      .border(1.dp, VsCodeBorder) // border-b border-[#333333]
      .padding(horizontal = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Left: Terminal icon in #D0BCFF, file name & breadcrumb
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF2D2D2D)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Terminal,
          contentDescription = null,
          tint = VsCodeLavender, // text-[#D0BCFF]
          modifier = Modifier.size(20.dp)
        )
      }

      Column {
        Text(
          text = activeFileName,
          color = VsCodeTextPrimary, // text-slate-200
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = "easy-code/src/app",
          color = VsCodeTextMuted, // text-slate-500
          fontSize = 10.sp
        )
      }

      // Edition selector badge
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFF242424))
          .border(1.dp, VsCodeBorder, RoundedCornerShape(6.dp))
          .clickable(onClick = onOpenVersionPicker)
          .padding(horizontal = 7.dp, vertical = 3.dp)
          .testTag("top_bar_edition_badge"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(VsCodeLavender)
        )
        Text(
          text = currentVersion.editionName,
          color = VsCodeLavender,
          fontSize = 10.sp,
          fontWeight = FontWeight.SemiBold
        )
        Icon(
          imageVector = Icons.Default.KeyboardArrowDown,
          contentDescription = null,
          tint = VsCodeTextMuted,
          modifier = Modifier.size(11.dp)
        )
      }
    }

    // Right: Quick Open Search, Run action, Copy HTML, and overflow actions
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Quick Open Global File Search Button
      Row(
        modifier = Modifier
          .height(34.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF242427))
          .border(1.dp, VsCodeBorder, RoundedCornerShape(8.dp))
          .clickable(onClick = onOpenGlobalFileSearch)
          .padding(horizontal = 9.dp, vertical = 6.dp)
          .testTag("top_bar_global_file_search_btn"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Quick Open Files",
          tint = VsCodeCyan,
          modifier = Modifier.size(15.dp)
        )
        Text(
          text = "Go to File...",
          color = VsCodeTextSecondary,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium
        )
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1E1E20))
            .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
          Text(
            text = "Ctrl+P",
            color = VsCodeLavender,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      // Run Web icon button
      IconButton(
        onClick = onOpenRunner,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .testTag("top_bar_run_web_btn")
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = "Run Web",
          tint = VsCodeTextSecondary,
          modifier = Modifier.size(20.dp)
        )
      }

      // COPY HTML BUTTON
      Box {
        Button(
          onClick = { showCopyMenu = true },
          colors = ButtonDefaults.buttonColors(
            containerColor = VsCodeLavender,
            contentColor = VsCodeLavenderDark
          ),
          shape = RoundedCornerShape(12.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("top_bar_copy_html_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Code,
            contentDescription = "Copy HTML",
            tint = VsCodeLavenderDark,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Copy HTML",
            color = VsCodeLavenderDark,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        DropdownMenu(
          expanded = showCopyMenu,
          onDismissRequest = { showCopyMenu = false },
          modifier = Modifier
            .background(VsCodeTabsBg)
            .border(1.dp, VsCodeBorder)
        ) {
          DropdownMenuItem(
            text = {
              Column {
                Text(
                  text = "Copy Standalone HTML (Bundled)",
                  color = VsCodeTextPrimary,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Includes inlined style.css and script.js ready for any browser",
                  color = VsCodeTextMuted,
                  fontSize = 10.sp
                )
              }
            },
            onClick = {
              showCopyMenu = false
              onCopyHtml(context, true)
            }
          )
          DropdownMenuItem(
            text = {
              Column {
                Text(
                  text = "Copy Raw $activeFileName",
                  color = VsCodeTextPrimary,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Copies only the source text of the active document",
                  color = VsCodeTextMuted,
                  fontSize = 10.sp
                )
              }
            },
            onClick = {
              showCopyMenu = false
              onCopyHtml(context, false)
            }
          )
        }
      }

      IconButton(
        onClick = onOpenVersionPicker,
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          imageVector = Icons.Default.MoreVert,
          contentDescription = "Options",
          tint = VsCodeTextSecondary,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}
