package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.ExtensionItem
import com.example.ui.theme.VsCodeActiveTabBg
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeEditorBg
import com.example.ui.theme.VsCodeLavender
import com.example.ui.theme.VsCodeSidebarBg
import com.example.ui.theme.VsCodeSurfaceDark
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary

@Composable
fun ExtensionsView(
  installedExtensions: List<ExtensionItem>,
  marketplaceExtensions: List<ExtensionItem>,
  onInstallExtension: (String) -> Unit,
  onUninstallExtension: (String) -> Unit,
  onToggleEnabled: (String) -> Unit,
  onOpenAddExtensionDialog: () -> Unit,
  onOpenImportExtensionDialog: () -> Unit = {},
  onApplySnippet: (String) -> Unit,
  onExecuteCommand: (String) -> Unit = {},
  onReloadExtension: (String) -> Unit = {}
) {
  var selectedTab by remember { mutableStateOf(0) } // 0: Installed, 1: Marketplace
  var searchQuery by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(VsCodeSidebarBg)
      .testTag("extensions_view")
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Extension,
          contentDescription = "Extensions",
          tint = VsCodeLavender,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "EXTENSIONS",
          color = VsCodeTextSecondary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
      }

      // Action Buttons: Import App + Add Extension
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // "Import App" Button
        Button(
          onClick = onOpenImportExtensionDialog,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .height(28.dp)
            .testTag("import_extension_header_button"),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = "Import Extension App",
            tint = Color(0xFF063022),
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Import App",
            color = Color(0xFF063022),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }

        // "+ Custom" Button
        OutlinedButton(
          onClick = onOpenAddExtensionDialog,
          border = BorderStroke(1.dp, VsCodeLavender),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .height(28.dp)
            .testTag("add_extension_header_button"),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 7.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Extension",
            tint = VsCodeLavender,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "Custom",
            color = VsCodeLavender,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    // Search bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search Extensions (API, Tools, Snippets)...", fontSize = 12.sp) },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Search",
          tint = VsCodeTextMuted,
          modifier = Modifier.size(16.dp)
        )
      },
      singleLine = true,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp)
        .testTag("extensions_search_input"),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = VsCodeLavender,
        unfocusedBorderColor = VsCodeBorder,
        focusedTextColor = VsCodeTextPrimary,
        unfocusedTextColor = VsCodeTextPrimary,
        focusedContainerColor = VsCodeSurfaceDark,
        unfocusedContainerColor = VsCodeSurfaceDark
      )
    )

    // Tabs: Installed vs Marketplace
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(6.dp))
          .background(if (selectedTab == 0) VsCodeActiveTabBg else Color.Transparent)
          .border(1.dp, if (selectedTab == 0) VsCodeLavender else VsCodeBorder, RoundedCornerShape(6.dp))
          .clickable { selectedTab = 0 }
          .padding(vertical = 6.dp)
          .testTag("tab_installed_extensions"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Installed (${installedExtensions.size})",
          color = if (selectedTab == 0) VsCodeLavender else VsCodeTextSecondary,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(6.dp))
          .background(if (selectedTab == 1) VsCodeActiveTabBg else Color.Transparent)
          .border(1.dp, if (selectedTab == 1) VsCodeLavender else VsCodeBorder, RoundedCornerShape(6.dp))
          .clickable { selectedTab = 1 }
          .padding(vertical = 6.dp)
          .testTag("tab_marketplace_extensions"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Marketplace (${marketplaceExtensions.size})",
          color = if (selectedTab == 1) VsCodeLavender else VsCodeTextSecondary,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    // Extension List
    val currentList = if (selectedTab == 0) installedExtensions else marketplaceExtensions
    val filteredList = currentList.filter {
      it.name.contains(searchQuery, ignoreCase = true) ||
        it.description.contains(searchQuery, ignoreCase = true) ||
        it.category.contains(searchQuery, ignoreCase = true)
    }

    if (filteredList.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = if (selectedTab == 0) "No installed extensions found." else "No marketplace extensions match.",
          color = VsCodeTextMuted,
          fontSize = 12.sp
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 10.dp, vertical = 4.dp)
      ) {
        items(filteredList, key = { it.id }) { ext ->
          ExtensionCard(
            item = ext,
            onInstall = { onInstallExtension(ext.id) },
            onUninstall = { onUninstallExtension(ext.id) },
            onToggle = { onToggleEnabled(ext.id) },
            onReload = { onReloadExtension(ext.id) },
            onExecuteCommand = onExecuteCommand,
            onApplySnippet = ext.codeSnippet?.let { snippet -> { onApplySnippet(snippet) } }
          )
        }
      }
    }
  }
}

@Composable
private fun ExtensionCard(
  item: ExtensionItem,
  onInstall: () -> Unit,
  onUninstall: () -> Unit,
  onToggle: () -> Unit,
  onReload: () -> Unit,
  onExecuteCommand: (String) -> Unit,
  onApplySnippet: (() -> Unit)? = null
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
      .testTag("extension_card_${item.id}"),
    colors = CardDefaults.cardColors(containerColor = VsCodeEditorBg),
    border = BorderStroke(1.dp, VsCodeBorder),
    shape = RoundedCornerShape(10.dp)
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Icon emoji box
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(VsCodeSurfaceDark),
            contentAlignment = Alignment.Center
          ) {
            Text(item.iconEmoji, fontSize = 18.sp)
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = item.name,
                color = VsCodeTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
              if (item.isCustom) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(VsCodeLavender.copy(alpha = 0.2f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                  Text("CUSTOM", color = VsCodeLavender, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
              }
              if (item.isImported) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                  Text("IMPORTED", color = Color(0xFF10B981), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
              }
              if (item.jsCode != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFF7DF1E).copy(alpha = 0.2f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                  Text("JS API", color = Color(0xFFF7DF1E), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = item.author,
                color = VsCodeTextSecondary,
                fontSize = 11.sp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("•", color = VsCodeTextMuted, fontSize = 10.sp)
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "v${item.version}",
                color = VsCodeTextMuted,
                fontSize = 11.sp
              )
              if (item.sourceFileName != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Text("•", color = VsCodeTextMuted, fontSize = 10.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = item.sourceFileName,
                  color = VsCodeLavender,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }

        // Install or Actions (Toggle Switch, Reload, Uninstall)
        if (item.isInstalled) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
              checked = item.isEnabled,
              onCheckedChange = { onToggle() },
              colors = SwitchDefaults.colors(
                checkedThumbColor = VsCodeLavender,
                checkedTrackColor = VsCodeLavender.copy(alpha = 0.3f)
              ),
              modifier = Modifier.padding(end = 4.dp)
            )

            if (item.jsCode != null) {
              IconButton(
                onClick = onReload,
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Reload Extension",
                  tint = VsCodeTextMuted,
                  modifier = Modifier.size(15.dp)
                )
              }
            }

            IconButton(
              onClick = onUninstall,
              modifier = Modifier.size(24.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Uninstall",
                tint = VsCodeTextMuted,
                modifier = Modifier.size(15.dp)
              )
            }
          }
        } else {
          Button(
            onClick = onInstall,
            colors = ButtonDefaults.buttonColors(containerColor = VsCodeLavender),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.height(28.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
          ) {
            Text("Install", color = Color(0xFF1E1035), fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = item.description,
        color = VsCodeTextSecondary,
        fontSize = 11.sp,
        lineHeight = 15.sp
      )

      // Contributed commands list (if installed)
      if (item.isInstalled && item.contributedCommands.isNotEmpty()) {
        Spacer(modifier = Modifier.height(6.dp))
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(VsCodeSurfaceDark)
            .padding(6.dp)
        ) {
          Text(
            text = "Registered Commands (${item.contributedCommands.size}):",
            color = VsCodeLavender,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(3.dp))
          item.contributedCommands.forEach { cmd ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = cmd,
                color = VsCodeTextPrimary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
              )
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(VsCodeLavender.copy(alpha = 0.2f))
                  .clickable { onExecuteCommand(cmd) }
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Run",
                    tint = VsCodeLavender,
                    modifier = Modifier.size(10.dp)
                  )
                  Spacer(modifier = Modifier.width(2.dp))
                  Text("Run", color = VsCodeLavender, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Tags & Snippet injection
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(VsCodeSurfaceDark)
              .border(0.5.dp, VsCodeBorder, RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(item.category, color = VsCodeTextMuted, fontSize = 9.sp)
          }

          Spacer(modifier = Modifier.width(6.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = "Rating",
              tint = Color(0xFFFBBF24),
              modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(item.rating.toString(), color = VsCodeTextMuted, fontSize = 10.sp)
          }
        }

        if (onApplySnippet != null) {
          OutlinedButton(
            onClick = onApplySnippet,
            modifier = Modifier.height(24.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp),
            border = BorderStroke(0.5.dp, VsCodeLavender)
          ) {
            Text("Inject Snippet", color = VsCodeLavender, fontSize = 10.sp)
          }
        }
      }
    }
  }
}
