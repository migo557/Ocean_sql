package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.HorizontalSplit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerticalSplit
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
import com.example.ui.theme.VsCodeLavender
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary
import com.example.viewmodel.PreviewDevice
import com.example.viewmodel.PreviewSplitOrientation

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LiveHtmlPreviewPane(
  bundledHtml: String,
  isUpdating: Boolean = false,
  previewDevice: PreviewDevice = PreviewDevice.RESPONSIVE,
  splitOrientation: PreviewSplitOrientation = PreviewSplitOrientation.HORIZONTAL,
  onSelectPreviewDevice: (PreviewDevice) -> Unit = {},
  onToggleOrientation: () -> Unit = {},
  onReload: () -> Unit = {},
  onOpenFullRunner: () -> Unit = {},
  onCopyHtml: (Context, Boolean) -> Unit = { _, _ -> },
  onClosePreview: () -> Unit = {},
  onAddConsoleLog: (String, String) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var localReloadTrigger by remember { mutableStateOf(0) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(VsCodeEditorBg)
      .testTag("live_html_preview_pane")
  ) {
    // 1. TOP CONTROL BAR
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = Color(0xFF141724),
      border = BorderStroke(1.dp, VsCodeBorder)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Title & Real-time Live Sync Indicator
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(end = 8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "Live HTML Preview",
            tint = VsCodeEmerald,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "LIVE PREVIEW",
            color = VsCodeTextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )

          Spacer(modifier = Modifier.width(8.dp))

          // Live status badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(if (isUpdating) Color(0x33F59E0B) else Color(0x3310B981))
              .border(
                0.5.dp,
                if (isUpdating) Color(0xFFF59E0B) else VsCodeEmerald,
                RoundedCornerShape(10.dp)
              )
              .padding(horizontal = 6.dp, vertical = 2.dp)
              .testTag("live_preview_sync_badge"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(if (isUpdating) Color(0xFFF59E0B) else VsCodeEmerald)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isUpdating) "Syncing..." else "Live Sync",
                color = if (isUpdating) Color(0xFFFBBF24) else VsCodeEmerald,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // Viewport Devices & Actions row
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
          // Device switcher
          PreviewDevice.entries.forEach { device ->
            val isSelected = device == previewDevice
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) VsCodeCyan.copy(alpha = 0.2f) else Color.Transparent)
                .border(
                  width = 1.dp,
                  color = if (isSelected) VsCodeCyan else VsCodeBorder,
                  shape = RoundedCornerShape(6.dp)
                )
                .clickable { onSelectPreviewDevice(device) }
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = when (device) {
                  PreviewDevice.RESPONSIVE -> "Full"
                  PreviewDevice.MOBILE -> "375px"
                  PreviewDevice.TABLET -> "640px"
                },
                color = if (isSelected) VsCodeCyan else VsCodeTextSecondary,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            }
          }

          // Split orientation toggle
          IconButton(
            onClick = onToggleOrientation,
            modifier = Modifier.size(26.dp)
          ) {
            Icon(
              imageVector = if (splitOrientation == PreviewSplitOrientation.HORIZONTAL) {
                Icons.Default.VerticalSplit
              } else {
                Icons.Default.HorizontalSplit
              },
              contentDescription = "Toggle Split Orientation (${splitOrientation.label})",
              tint = VsCodeLavender,
              modifier = Modifier.size(15.dp)
            )
          }

          // Force reload
          IconButton(
            onClick = {
              localReloadTrigger++
              onReload()
            },
            modifier = Modifier.size(26.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Reload Preview",
              tint = VsCodeTextSecondary,
              modifier = Modifier.size(15.dp)
            )
          }

          // Open Full Runner
          IconButton(
            onClick = onOpenFullRunner,
            modifier = Modifier.size(26.dp)
          ) {
            Icon(
              imageVector = Icons.Default.OpenInNew,
              contentDescription = "Open Full Web Runner",
              tint = VsCodeCyan,
              modifier = Modifier.size(15.dp)
            )
          }

          // Copy Standalone HTML
          IconButton(
            onClick = { onCopyHtml(context, true) },
            modifier = Modifier.size(26.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy Standalone HTML",
              tint = VsCodeLavender,
              modifier = Modifier.size(14.dp)
            )
          }

          // Close Preview
          IconButton(
            onClick = onClosePreview,
            modifier = Modifier.size(26.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close Live Preview",
              tint = VsCodeTextMuted,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }

    // 2. LIVE WEBVIEW RENDER WORKSPACE
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .background(Color(0xFF090A0F)),
      contentAlignment = Alignment.TopCenter
    ) {
      val targetModifier = if (previewDevice.widthDp != null) {
        Modifier
          .width(previewDevice.widthDp.dp)
          .fillMaxHeight()
          .padding(8.dp)
          .clip(RoundedCornerShape(8.dp))
          .border(1.dp, VsCodeBorder, RoundedCornerShape(8.dp))
      } else {
        Modifier.fillMaxSize()
      }

      AndroidView(
        modifier = targetModifier.testTag("live_preview_webview"),
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
              }
            }

            tag = bundledHtml
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
          // Automatically update when user types in editor (bundledHtml changes)
          if (webView.tag != bundledHtml || localReloadTrigger != 0) {
            webView.tag = bundledHtml
            webView.loadDataWithBaseURL(
              "http://localhost:3000/",
              bundledHtml,
              "text/html",
              "UTF-8",
              null
            )
          }
        }
      )
    }

    // 3. SUBTLE BOTTOM STATUS BAR
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(20.dp)
        .background(Color(0xFF10121C))
        .border(1.dp, VsCodeBorder)
        .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "⚡ Real-time rendered HTML (${bundledHtml.length} chars)",
        color = VsCodeTextMuted,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = "Viewport: ${previewDevice.label}",
        color = VsCodeTextMuted,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
