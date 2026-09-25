package com.example.models

data class ProjectFolder(
  val id: String,
  val name: String,
  val parentFolderId: String? = null,
  val isExpanded: Boolean = true
)
