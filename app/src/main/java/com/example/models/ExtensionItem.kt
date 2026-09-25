package com.example.models

data class ExtensionItem(
  val id: String,
  val name: String,
  val author: String,
  val version: String,
  val description: String,
  val iconEmoji: String,
  val category: String,
  val isInstalled: Boolean,
  val isEnabled: Boolean = true,
  val downloads: String = "1.4M",
  val rating: Float = 4.9f,
  val codeSnippet: String? = null,
  val isCustom: Boolean = false,
  val jsCode: String? = null,
  val contributedCommands: List<String> = emptyList(),
  val contributedButtons: List<String> = emptyList(),
  val isImported: Boolean = false,
  val sourceFileName: String? = null
)
