package com.example.models

data class RegisteredCommand(
  val id: String,
  val title: String = id,
  val extensionId: String,
  val description: String = ""
)

data class ContributedButton(
  val id: String,
  val label: String,
  val command: String,
  val icon: String = "Code",
  val tooltip: String = "",
  val extensionId: String
)

data class ContributedStatusBarItem(
  val id: String,
  val text: String,
  val command: String = "",
  val tooltip: String = "",
  val extensionId: String
)
