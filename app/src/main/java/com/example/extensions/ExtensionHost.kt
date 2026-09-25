package com.example.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.models.ContributedButton
import com.example.models.ContributedStatusBarItem
import com.example.models.RegisteredCommand

interface ExtensionHostListener {
  fun onCommandRegistered(command: RegisteredCommand)
  fun onButtonContributed(button: ContributedButton)
  fun onStatusBarItemContributed(item: ContributedStatusBarItem)
  fun onStatusBarItemUpdated(id: String, text: String)
  fun onShowNotification(type: String, message: String)
  fun onEditorContentChangeRequested(newContent: String)
  fun getCurrentEditorContent(): String
  fun getCurrentEditorFileName(): String
}

class ExtensionHost(
  private val context: Context,
  private val listener: ExtensionHostListener
) {
  private val mainHandler = Handler(Looper.getMainLooper())
  private var webView: WebView? = null
  private var isInitialized = false
  private val pendingScripts = mutableListOf<Pair<String, String>>()
  private var currentExtensionId: String = "core"

  init {
    mainHandler.post {
      initWebView()
    }
  }

  @SuppressLint("SetJavaScriptEnabled")
  private fun initWebView() {
    try {
      val wv = WebView(context).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webViewClient = object : WebViewClient() {
          override fun onPageFinished(view: WebView?, url: String?) {
            isInitialized = true
            pendingScripts.forEach { (extId, script) ->
              loadExtensionScriptInternal(extId, script)
            }
            pendingScripts.clear()
          }
        }
        addJavascriptInterface(ExtensionJsBridge(), "AndroidExtensionBridge")
      }
      webView = wv

      val bootstrapHtml = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8">
          <script>
            window._commandHandlers = {};
            window._activeExtensionId = 'core';
            
            window.vscode = {
              commands: {
                registerCommand: function(commandId, handler) {
                  window._commandHandlers[commandId] = handler;
                  if (window.AndroidExtensionBridge) {
                    window.AndroidExtensionBridge.onRegisterCommand(commandId, window._activeExtensionId);
                  }
                },
                executeCommand: function(commandId, ...args) {
                  if (window._commandHandlers && window._commandHandlers[commandId]) {
                    try {
                      return window._commandHandlers[commandId](...args);
                    } catch(e) {
                      if (window.AndroidExtensionBridge) {
                        window.AndroidExtensionBridge.showErrorMessage('Error in ' + commandId + ': ' + e.message);
                      }
                    }
                  } else {
                    if (window.AndroidExtensionBridge) {
                      window.AndroidExtensionBridge.showErrorMessage('Command not found: ' + commandId);
                    }
                  }
                }
              },
              window: {
                get activeTextEditor() {
                  return {
                    get document() {
                      return {
                        getText: function() {
                          return window.AndroidExtensionBridge ? window.AndroidExtensionBridge.getEditorContent() : '';
                        },
                        get fileName() {
                          return window.AndroidExtensionBridge ? window.AndroidExtensionBridge.getEditorFileName() : '';
                        }
                      };
                    },
                    edit: function(callback) {
                      if (!window.AndroidExtensionBridge) return;
                      var currentText = window.AndroidExtensionBridge.getEditorContent();
                      var editBuilder = {
                        replace: function(newContent) {
                          currentText = newContent;
                        },
                        insert: function(pos, text) {
                          currentText += text;
                        }
                      };
                      callback(editBuilder);
                      window.AndroidExtensionBridge.setEditorContent(currentText);
                    }
                  };
                },
                showInformationMessage: function(msg) {
                  if (window.AndroidExtensionBridge) {
                    window.AndroidExtensionBridge.showInformationMessage(msg);
                  }
                },
                showErrorMessage: function(msg) {
                  if (window.AndroidExtensionBridge) {
                    window.AndroidExtensionBridge.showErrorMessage(msg);
                  }
                },
                createStatusBarItem: function(options) {
                  var id = (options && options.id) ? options.id : 'status_' + Math.random().toString(36).substr(2, 9);
                  var text = (options && options.text) ? options.text : '';
                  var command = (options && options.command) ? options.command : '';
                  var tooltip = (options && options.tooltip) ? options.tooltip : '';
                  if (window.AndroidExtensionBridge) {
                    window.AndroidExtensionBridge.contributeStatusBarItem(
                      id, text, command, tooltip, window._activeExtensionId
                    );
                  }
                  return {
                    setText: function(newText) {
                      if (window.AndroidExtensionBridge) {
                        window.AndroidExtensionBridge.updateStatusBarItem(id, newText);
                      }
                    }
                  };
                }
              },
              ui: {
                addButton: function(options) {
                  if (window.AndroidExtensionBridge) {
                    window.AndroidExtensionBridge.contributeButton(
                      options.id || ('btn_' + Math.random().toString(36).substr(2, 9)),
                      options.label || options.text || 'Button',
                      options.command || '',
                      options.icon || 'Code',
                      options.tooltip || '',
                      window._activeExtensionId
                    );
                  }
                },
                showNotification: function(msg) {
                  if (window.AndroidExtensionBridge) {
                    window.AndroidExtensionBridge.showInformationMessage(msg);
                  }
                }
              }
            };
          </script>
        </head>
        <body></body>
        </html>
      """.trimIndent()

      wv.loadDataWithBaseURL("https://vscode.app", bootstrapHtml, "text/html", "UTF-8", null)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun loadExtension(extensionId: String, jsCode: String) {
    mainHandler.post {
      if (!isInitialized || webView == null) {
        pendingScripts.add(extensionId to jsCode)
      } else {
        loadExtensionScriptInternal(extensionId, jsCode)
      }
    }
  }

  private fun loadExtensionScriptInternal(extensionId: String, jsCode: String) {
    currentExtensionId = extensionId
    val escapedJs = """
      (function() {
        window._activeExtensionId = '$extensionId';
        try {
          $jsCode
        } catch(e) {
          if (window.AndroidExtensionBridge) {
            window.AndroidExtensionBridge.showErrorMessage('Extension error [' + '$extensionId' + ']: ' + e.message);
          }
        }
      })();
    """.trimIndent()
    webView?.evaluateJavascript(escapedJs, null)
  }

  fun executeCommand(commandId: String) {
    mainHandler.post {
      val callJs = "window.vscode.commands.executeCommand('$commandId');"
      webView?.evaluateJavascript(callJs, null)
    }
  }

  fun evaluateScript(script: String) {
    mainHandler.post {
      webView?.evaluateJavascript(script, null)
    }
  }

  fun reset() {
    mainHandler.post {
      isInitialized = false
      initWebView()
    }
  }

  inner class ExtensionJsBridge {
    @JavascriptInterface
    fun onRegisterCommand(commandId: String, extId: String) {
      mainHandler.post {
        val title = commandId.substringAfterLast(".").replaceFirstChar { it.uppercase() }
        listener.onCommandRegistered(
          RegisteredCommand(
            id = commandId,
            title = title,
            extensionId = extId,
            description = "Contributed by $extId"
          )
        )
      }
    }

    @JavascriptInterface
    fun getEditorContent(): String {
      return listener.getCurrentEditorContent()
    }

    @JavascriptInterface
    fun getEditorFileName(): String {
      return listener.getCurrentEditorFileName()
    }

    @JavascriptInterface
    fun setEditorContent(content: String) {
      mainHandler.post {
        listener.onEditorContentChangeRequested(content)
      }
    }

    @JavascriptInterface
    fun showInformationMessage(msg: String) {
      mainHandler.post {
        listener.onShowNotification("INFO", msg)
      }
    }

    @JavascriptInterface
    fun showErrorMessage(msg: String) {
      mainHandler.post {
        listener.onShowNotification("ERROR", msg)
      }
    }

    @JavascriptInterface
    fun contributeButton(id: String, label: String, command: String, icon: String, tooltip: String, extId: String) {
      mainHandler.post {
        listener.onButtonContributed(
          ContributedButton(
            id = id,
            label = label,
            command = command,
            icon = icon,
            tooltip = tooltip,
            extensionId = extId
          )
        )
      }
    }

    @JavascriptInterface
    fun contributeStatusBarItem(id: String, text: String, command: String, tooltip: String, extId: String) {
      mainHandler.post {
        listener.onStatusBarItemContributed(
          ContributedStatusBarItem(
            id = id,
            text = text,
            command = command,
            tooltip = tooltip,
            extensionId = extId
          )
        )
      }
    }

    @JavascriptInterface
    fun updateStatusBarItem(id: String, text: String) {
      mainHandler.post {
        listener.onStatusBarItemUpdated(id, text)
      }
    }
  }
}
