package com.example.models

data class CodeFile(
  val id: String,
  val name: String,
  val extension: String,
  val content: String,
  val isModified: Boolean = false,
  val isReadOnly: Boolean = false,
  val parentFolderId: String? = null
) {
  val language: String
    get() = when (extension.lowercase()) {
      "html", "htm" -> "html"
      "css" -> "css"
      "js", "javascript", "jsx", "ts", "tsx" -> "javascript"
      "json" -> "json"
      "md", "markdown" -> "markdown"
      "py", "python" -> "python"
      else -> "plaintext"
    }

  val iconColorHex: Long
    get() = when (extension.lowercase()) {
      "html" -> 0xFFE44D26
      "css" -> 0xFF264DE4
      "js" -> 0xFFF7DF1E
      "json" -> 0xFF5BBA50
      "md" -> 0xFF38BDF8
      "py" -> 0xFF3776AB
      else -> 0xFF94A3B8
    }
}
