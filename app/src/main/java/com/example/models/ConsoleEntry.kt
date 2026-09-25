package com.example.models

enum class ConsoleLogLevel(val label: String, val badgeColorHex: Long) {
  ALL("All", 0xFF89DDFF),
  INFO("Info", 0xFF89DDFF),
  LOG("Log", 0xFFC3E88D),
  WARN("Warn", 0xFFFFCB6B),
  ERROR("Error", 0xFFF07178)
}

data class ConsoleEntry(
  val id: String,
  val level: String, // "LOG", "WARN", "ERROR", "INFO"
  val text: String,
  val source: String? = null,
  val lineNumber: Int? = null,
  val timestamp: String,
  val count: Int = 1
)
