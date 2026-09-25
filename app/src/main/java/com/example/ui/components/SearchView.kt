package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.CodeFile
import com.example.ui.theme.VsCodeActiveTabBg
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeSidebarBg
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary

@Composable
fun SearchView(
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  files: List<CodeFile>,
  onSelectFile: (String) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(VsCodeSidebarBg)
      .testTag("search_view")
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Search,
        contentDescription = "Search",
        tint = VsCodeCyan,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "SEARCH",
        color = VsCodeTextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
    }

    // Search Input
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchQueryChange,
      placeholder = { Text("Search text across all files...", fontSize = 12.sp) },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Search",
          tint = VsCodeTextMuted,
          modifier = Modifier.size(16.dp)
        )
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { onSearchQueryChange("") }, modifier = Modifier.size(20.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = VsCodeTextMuted)
          }
        }
      },
      singleLine = true,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp)
        .testTag("global_search_input"),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = VsCodeCyan,
        unfocusedBorderColor = VsCodeBorder,
        focusedTextColor = VsCodeTextPrimary,
        unfocusedTextColor = VsCodeTextPrimary,
        focusedContainerColor = Color(0xFF141624),
        unfocusedContainerColor = Color(0xFF141624)
      )
    )

    // Results list
    if (searchQuery.isBlank()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Text("Type a query to search in project files.", color = VsCodeTextMuted, fontSize = 12.sp)
      }
    } else {
      val matchingFiles = files.mapNotNull { file ->
        val lines = file.content.lines()
        val matches = lines.mapIndexedNotNull { index, line ->
          if (line.contains(searchQuery, ignoreCase = true)) {
            (index + 1) to line.trim()
          } else null
        }
        if (matches.isNotEmpty()) file to matches else null
      }

      if (matchingFiles.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Text("No results found for \"$searchQuery\"", color = VsCodeTextMuted, fontSize = 12.sp)
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(8.dp)
        ) {
          matchingFiles.forEach { (file, matches) ->
            item {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(4.dp))
                  .clickable { onSelectFile(file.id) }
                  .background(VsCodeActiveTabBg)
                  .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Description,
                  contentDescription = "File",
                  tint = VsCodeCyan,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = file.name,
                  color = VsCodeTextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "${matches.size} matches",
                  color = VsCodeCyan,
                  fontSize = 10.sp
                )
              }
            }

            items(matches) { (lineNum, lineText) ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onSelectFile(file.id) }
                  .padding(start = 24.dp, top = 2.dp, bottom = 2.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "$lineNum:",
                  color = VsCodeTextMuted,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier.width(28.dp)
                )
                Text(
                  text = lineText,
                  color = VsCodeTextSecondary,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  maxLines = 1
                )
              }
            }
          }
        }
      }
    }
  }
}
