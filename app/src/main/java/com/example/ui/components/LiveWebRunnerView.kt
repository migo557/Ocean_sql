package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeEditorBg
import com.example.ui.theme.VsCodeEmerald
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary
import com.example.viewmodel.ConsoleMessage as AppConsoleMessage
import com.example.viewmodel.PreviewDevice

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LiveWebRunnerView(
  bundledHtml: String,
  reloadTrigger: Int,
  previewDevice: PreviewDevice,
  onSelectPreviewDevice: (PreviewDevice) -> Unit,
  onReload: () -> Unit,
  onCopyHtml: (Context, Boolean) -> Unit,
  consoleLogs: List<AppConsoleMessage>,
  onAddConsoleLog: (String, String) -> Unit,
  onClearConsole: () -> Unit
) {
  val context = LocalContext.current
  var showConsoleDrawer by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(VsCodeEditorBg)
      .testTag("live_web_runner_view")
  ) {
    // 1. RUNNER BROWSER NAVIGATION BAR
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = Color(0xFF131520),
      border = BorderStroke(1.dp, VsCodeBorder)
    ) {
      Column(modifier = Modifier.padding(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Browser Address bar with SSL lock and URL
          Row(
            modifier = Modifier
              .weight(1f)
              .height(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF1E2232))
              .border(1.dp, VsCodeBorder, RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(VsCodeEmerald)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "http://localhost:3000/index.html",
              color = VsCodeCyan,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          // COPY HTML BUTTON
          Button(
            onClick = { onCopyHtml(context, true) },
            colors = ButtonDefaults.buttonColors(containerColor = VsCodeCyan),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .height(32.dp)
              .testTag("runner_copy_html_button"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy HTML",
              tint = Color(0xFF0F172A),
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Copy HTML",
              color = Color(0xFF0F172A),
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          // Reload Button
          IconButton(
            onClick = onReload,
            modifier = Modifier
              .size(32.dp)
              .testTag("runner_reload_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Reload Web View",
              tint = VsCodeTextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        // Viewport switcher row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            PreviewDevice.entries.forEach { device ->
              val isSelected = device == previewDevice
              Box(
                modifier = Modifier
                  .padding(end = 6.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isSelected) VsCodeCyan.copy(alpha = 0.2f) else Color.Transparent)
                  .border(
                    width = 1.dp,
                    color = if (isSelected) VsCodeCyan else VsCodeBorder,
                    shape = RoundedCornerShape(6.dp)
                  )
                  .clickable { onSelectPreviewDevice(device) }
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(
                  text = device.label,
                  color = if (isSelected) VsCodeCyan else VsCodeTextSecondary,
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              }
            }
          }

          // Console drawer toggle pill
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0xFF1E2232))
              .clickable { showConsoleDrawer = !showConsoleDrawer }
              .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Console (${consoleLogs.size})",
              color = if (consoleLogs.isNotEmpty()) VsCodeEmerald else VsCodeTextMuted,
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium
            )
            Icon(
              imageVector = if (showConsoleDrawer) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
              contentDescription = "Toggle Console",
              tint = VsCodeTextSecondary,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }
    }

    // 2. EMBEDDED WEBVIEW AREA
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .background(Color(0xFF0C0E14)),
      contentAlignment = Alignment.TopCenter
    ) {
      val targetModifier = if (previewDevice.widthDp != null) {
        Modifier
          .width(previewDevice.widthDp.dp)
          .fillMaxSize()
          .border(1.dp, VsCodeBorder)
      } else {
        Modifier.fillMaxSize()
      }

      AndroidView(
        modifier = targetModifier.testTag("webview_runner"),
        factory = { ctx ->
          WebView(ctx).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.allowFileAccess = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE

            webChromeClient = object : WebChromeClient() {
              override fun onConsoleMessage(cm: ConsoleMessage?): Boolean {
                if (cm != null) {
                  val levelStr = when (cm.messageLevel()) {
                    ConsoleMessage.MessageLevel.ERROR -> "ERROR"
                    ConsoleMessage.MessageLevel.WARNING -> "WARN"
                    else -> "LOG"
                  }
                  onAddConsoleLog(levelStr, "${cm.message()} (${cm.sourceId()}:${cm.lineNumber()})")
                }
                return super.onConsoleMessage(cm)
              }
            }

            webViewClient = object : WebViewClient() {
              override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onAddConsoleLog("INFO", "Navigating to $url")
              }
            }

            loadDataWithBaseURL(
              "http://localhost:3000/",
              bundledHtml,
              "text/html",
              "UTF-8",
              null
            )
          }
        },
        update = { webView ->
          // Reload when bundledHtml changes or reloadTrigger fires
          webView.loadDataWithBaseURL(
            "http://localhost:3000/",
            bundledHtml,
            "text/html",
            "UTF-8",
            null
          )
        }
      )
    }

    // 3. JAVASCRIPT CONSOLE DRAWER
    if (showConsoleDrawer) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .height(160.dp),
        color = Color(0xFF10121C),
        border = BorderStroke(1.dp, VsCodeBorder)
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF161824))
              .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "DEVELOPER CONSOLE",
              color = VsCodeTextSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            IconButton(
              onClick = onClearConsole,
              modifier = Modifier.size(22.dp)
            ) {
              Icon(
                imageVector = Icons.Default.ClearAll,
                contentDescription = "Clear Console",
                tint = VsCodeTextMuted,
                modifier = Modifier.size(15.dp)
              )
            }
          }

          if (consoleLogs.isEmpty()) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Console is empty. Click interactive buttons on the webpage to see output.",
                color = VsCodeTextMuted,
                fontSize = 11.sp
              )
            }
          } else {
            LazyColumn(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              items(consoleLogs, key = { it.id }) { log ->
                val logColor = when (log.level) {
                  "ERROR" -> Color(0xFFF87171)
                  "WARN" -> Color(0xFFFBBF24)
                  "INFO" -> VsCodeCyan
                  else -> VsCodeEmerald
                }

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                ) {
                  Text(
                    text = "[${log.timestamp}]",
                    color = VsCodeTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = log.level,
                    color = logColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = log.text,
                    color = VsCodeTextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
