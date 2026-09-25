package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeEditorBg
import com.example.ui.theme.VsCodeEmerald
import com.example.ui.theme.VsCodeSidebarBg
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary

@Composable
fun TerminalView(
  terminalLogs: List<String>,
  onRunCommand: (String) -> Unit,
  onClear: () -> Unit
) {
  var inputCmd by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  LaunchedEffect(terminalLogs.size) {
    if (terminalLogs.isNotEmpty()) {
      listState.animateScrollToItem(terminalLogs.size - 1)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(VsCodeEditorBg)
      .testTag("terminal_view")
  ) {
    // Terminal Tab header
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = Color(0xFF12141F),
      border = androidx.compose.foundation.BorderStroke(1.dp, VsCodeBorder)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Terminal,
            contentDescription = "Terminal",
            tint = VsCodeEmerald,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "TERMINAL: bash (node/v20)",
            color = VsCodeTextPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
          )
        }

        IconButton(
          onClick = onClear,
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ClearAll,
            contentDescription = "Clear",
            tint = VsCodeTextMuted,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }

    // Terminal Output Log
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
      items(terminalLogs) { log ->
        val isPrompt = log.startsWith("$ ")
        Text(
          text = log,
          color = if (isPrompt) VsCodeCyan else VsCodeTextSecondary,
          fontSize = 12.sp,
          fontFamily = FontFamily.Monospace,
          lineHeight = 18.sp
        )
      }
    }

    // Quick Command Shortcuts Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(34.dp)
        .background(Color(0xFF0F111A))
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      val quickCommands = listOf("help", "ls", "bundle", "run", "extensions", "version", "cat index.html", "clear")
      quickCommands.forEach { cmd ->
        Box(
          modifier = Modifier
            .padding(end = 6.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1E2130))
            .clickable { onRunCommand(cmd) }
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = cmd,
            color = VsCodeCyan,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    // Command Input Line
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFF12141F))
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "$ ",
        color = VsCodeEmerald,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      OutlinedTextField(
        value = inputCmd,
        onValueChange = { inputCmd = it },
        placeholder = { Text("type command (e.g. bundle, ls, help)...", fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = {
          if (inputCmd.isNotBlank()) {
            onRunCommand(inputCmd)
            inputCmd = ""
          }
        }),
        modifier = Modifier
          .weight(1f)
          .testTag("terminal_input_field"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = Color.Transparent,
          unfocusedBorderColor = Color.Transparent,
          focusedTextColor = VsCodeTextPrimary,
          unfocusedTextColor = VsCodeTextPrimary
        )
      )

      IconButton(
        onClick = {
          if (inputCmd.isNotBlank()) {
            onRunCommand(inputCmd)
            inputCmd = ""
          }
        },
        modifier = Modifier.size(32.dp).testTag("terminal_send_btn")
      ) {
        Icon(
          imageVector = Icons.Default.Send,
          contentDescription = "Run",
          tint = VsCodeCyan,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}
