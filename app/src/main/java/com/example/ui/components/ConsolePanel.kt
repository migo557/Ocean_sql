package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeActiveTabBg
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeEditorBg
import com.example.ui.theme.VsCodeEmerald
import com.example.ui.theme.VsCodeLavender
import com.example.ui.theme.VsCodeRose
import com.example.ui.theme.VsCodeSidebarBg
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary
import com.example.viewmodel.ConsoleMessage

enum class ConsoleFilter(val label: String) {
  ALL("All"),
  ERRORS("Errors"),
  WARNINGS("Warnings"),
  INFO("Info"),
  LOGS("Logs")
}

@Composable
fun ConsolePanel(
  consoleLogs: List<ConsoleMessage>,
  onClearConsole: () -> Unit,
  onOpenRunner: () -> Unit,
  onEvaluateJs: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  var activeFilter by remember { mutableStateOf(ConsoleFilter.ALL) }
  var filterSearchQuery by remember { mutableStateOf("") }
  var jsEvalInput by remember { mutableStateOf("") }
  val listState = rememberLazyListState()
  val clipboardManager = LocalClipboardManager.current

  // Filter logs by level and text
  val filteredLogs = remember(consoleLogs, activeFilter, filterSearchQuery) {
    consoleLogs.filter { log ->
      val matchesLevel = when (activeFilter) {
        ConsoleFilter.ALL -> true
        ConsoleFilter.ERRORS -> log.level.equals("ERROR", ignoreCase = true)
        ConsoleFilter.WARNINGS -> log.level.equals("WARN", ignoreCase = true) || log.level.equals("WARNING", ignoreCase = true)
        ConsoleFilter.INFO -> log.level.equals("INFO", ignoreCase = true)
        ConsoleFilter.LOGS -> log.level.equals("LOG", ignoreCase = true)
      }
      val matchesQuery = if (filterSearchQuery.isBlank()) true else {
        log.text.contains(filterSearchQuery, ignoreCase = true) ||
          log.level.contains(filterSearchQuery, ignoreCase = true)
      }
      matchesLevel && matchesQuery
    }
  }

  // Error and warning counts
  val errorCount = remember(consoleLogs) {
    consoleLogs.count { it.level.equals("ERROR", ignoreCase = true) }
  }
  val warnCount = remember(consoleLogs) {
    consoleLogs.count { it.level.equals("WARN", ignoreCase = true) || it.level.equals("WARNING", ignoreCase = true) }
  }
  val infoCount = remember(consoleLogs) {
    consoleLogs.count { it.level.equals("INFO", ignoreCase = true) }
  }

  // Auto-scroll to bottom on new logs
  LaunchedEffect(filteredLogs.size) {
    if (filteredLogs.isNotEmpty()) {
      listState.animateScrollToItem(filteredLogs.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(VsCodeEditorBg)
      .testTag("integrated_console_panel")
  ) {
    // 1. Panel Header Bar (VS Code Style)
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = Color(0xFF131522),
      border = BorderStroke(1.dp, VsCodeBorder)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Terminal,
            contentDescription = "Console",
            tint = VsCodeLavender,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "DEBUG CONSOLE",
            color = VsCodeTextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            fontFamily = FontFamily.Monospace
          )

          // Error & Warning counters
          if (errorCount > 0) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x33F07178))
                .padding(horizontal = 5.dp, vertical = 1.dp)
                .testTag("console_error_badge")
            ) {
              Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = VsCodeRose,
                modifier = Modifier.size(11.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "$errorCount",
                color = VsCodeRose,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          if (warnCount > 0) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x33FFCB6B))
                .padding(horizontal = 5.dp, vertical = 1.dp)
                .testTag("console_warn_badge")
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFFCB6B),
                modifier = Modifier.size(11.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "$warnCount",
                color = Color(0xFFFFCB6B),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // Right side quick actions: Copy all, Go to Runner, Clear
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          // Open Live Runner
          IconButton(
            onClick = onOpenRunner,
            modifier = Modifier
              .size(26.dp)
              .testTag("console_open_runner_btn")
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Switch to Live Web Runner",
              tint = VsCodeEmerald,
              modifier = Modifier.size(16.dp)
            )
          }

          // Copy all logs
          IconButton(
            onClick = {
              val allText = consoleLogs.joinToString("\n") { "[${it.timestamp}] [${it.level}] ${it.text}" }
              clipboardManager.setText(AnnotatedString(allText))
            },
            enabled = consoleLogs.isNotEmpty(),
            modifier = Modifier
              .size(26.dp)
              .testTag("console_copy_logs_btn")
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy all logs",
              tint = if (consoleLogs.isNotEmpty()) VsCodeTextSecondary else VsCodeTextMuted,
              modifier = Modifier.size(14.dp)
            )
          }

          // Clear Console
          IconButton(
            onClick = onClearConsole,
            modifier = Modifier
              .size(26.dp)
              .testTag("console_clear_btn")
          ) {
            Icon(
              imageVector = Icons.Default.ClearAll,
              contentDescription = "Clear Console",
              tint = VsCodeTextSecondary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }

    // 2. Filter Tabs & Search Bar Toolbar
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = Color(0xFF10121D),
      border = BorderStroke(0.5.dp, VsCodeBorder)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        ConsoleFilter.entries.forEach { filter ->
          val isSelected = activeFilter == filter
          val count = when (filter) {
            ConsoleFilter.ALL -> consoleLogs.size
            ConsoleFilter.ERRORS -> errorCount
            ConsoleFilter.WARNINGS -> warnCount
            ConsoleFilter.INFO -> infoCount
            ConsoleFilter.LOGS -> consoleLogs.count { it.level.equals("LOG", ignoreCase = true) }
          }
          val activeColor = when (filter) {
            ConsoleFilter.ERRORS -> VsCodeRose
            ConsoleFilter.WARNINGS -> Color(0xFFFFCB6B)
            ConsoleFilter.INFO -> VsCodeCyan
            ConsoleFilter.LOGS -> VsCodeEmerald
            ConsoleFilter.ALL -> VsCodeLavender
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (isSelected) activeColor.copy(alpha = 0.2f) else Color.Transparent)
              .border(
                1.dp,
                if (isSelected) activeColor else Color.Transparent,
                RoundedCornerShape(6.dp)
              )
              .clickable { activeFilter = filter }
              .padding(horizontal = 8.dp, vertical = 3.dp)
              .testTag("console_filter_${filter.name.lowercase()}"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${filter.label} ($count)",
              color = if (isSelected) activeColor else VsCodeTextMuted,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Inline search query inside console
        Box(
          modifier = Modifier
            .width(150.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1A1D2B))
            .border(0.5.dp, VsCodeBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp),
          contentAlignment = Alignment.CenterStart
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = VsCodeTextMuted,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            OutlinedTextField(
              value = filterSearchQuery,
              onValueChange = { filterSearchQuery = it },
              placeholder = { Text("Filter...", fontSize = 10.sp, color = VsCodeTextMuted) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = VsCodeTextPrimary,
                unfocusedTextColor = VsCodeTextPrimary
              )
            )
          }
        }
      }
    }

    // 3. Console Logs Output List
    if (filteredLogs.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Terminal,
            contentDescription = null,
            tint = VsCodeTextMuted.copy(alpha = 0.5f),
            modifier = Modifier.size(40.dp)
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = if (consoleLogs.isEmpty()) {
              "No console output captured yet."
            } else {
              "No logs match filter '$filterSearchQuery' in ${activeFilter.label}."
            },
            color = VsCodeTextMuted,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Console messages from console.log(), warn(), error(), and runtime navigation will appear here in real time.",
            color = VsCodeTextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
          )
        }
      }
    } else {
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 8.dp, vertical = 4.dp)
          .testTag("console_logs_list")
      ) {
        items(filteredLogs, key = { it.id }) { log ->
          val isError = log.level.equals("ERROR", ignoreCase = true)
          val isWarn = log.level.equals("WARN", ignoreCase = true) || log.level.equals("WARNING", ignoreCase = true)
          val isInfo = log.level.equals("INFO", ignoreCase = true)

          val levelBadgeColor = when {
            isError -> VsCodeRose
            isWarn -> Color(0xFFFFCB6B)
            isInfo -> VsCodeCyan
            else -> VsCodeEmerald
          }

          val rowBgColor = when {
            isError -> Color(0x1AF07178)
            isWarn -> Color(0x12FFCB6B)
            else -> Color.Transparent
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(4.dp))
              .background(rowBgColor)
              .padding(horizontal = 6.dp, vertical = 3.dp)
              .testTag("console_log_item_${log.id}"),
            verticalAlignment = Alignment.Top
          ) {
            // Timestamp
            Text(
              text = log.timestamp,
              color = VsCodeTextMuted,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              modifier = Modifier.padding(top = 1.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Level Pill
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(levelBadgeColor.copy(alpha = 0.18f))
                .border(0.5.dp, levelBadgeColor.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
              Text(
                text = log.level.uppercase(),
                color = levelBadgeColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Log Text
            Text(
              text = log.text,
              color = if (isError) VsCodeRose else VsCodeTextPrimary,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              lineHeight = 16.sp,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // 4. Quick Execution / JS Evaluation Input Footer
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = Color(0xFF131522),
      border = BorderStroke(1.dp, VsCodeBorder)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = ">",
          color = VsCodeLavender,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(6.dp))
        OutlinedTextField(
          value = jsEvalInput,
          onValueChange = { jsEvalInput = it },
          placeholder = {
            Text(
              text = "Evaluate JavaScript expression or log test...",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = VsCodeTextMuted
            )
          },
          singleLine = true,
          modifier = Modifier
            .weight(1f)
            .testTag("console_eval_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = VsCodeTextPrimary,
            unfocusedTextColor = VsCodeTextPrimary
          )
        )

        IconButton(
          onClick = {
            if (jsEvalInput.isNotBlank()) {
              onEvaluateJs(jsEvalInput)
              jsEvalInput = ""
            }
          },
          modifier = Modifier
            .size(28.dp)
            .testTag("console_eval_send_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Send,
            contentDescription = "Evaluate",
            tint = VsCodeLavender,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }
  }
}
