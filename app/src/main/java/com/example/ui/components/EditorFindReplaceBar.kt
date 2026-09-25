package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeEditorBg
import com.example.ui.theme.VsCodeLavender
import com.example.ui.theme.VsCodeSurfaceDark
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary

@Composable
fun EditorFindReplaceBar(
  findQuery: String,
  replaceQuery: String,
  matchCase: Boolean,
  matchWholeWord: Boolean,
  useRegex: Boolean,
  currentMatchIndex: Int,
  totalMatches: Int,
  onFindQueryChange: (String) -> Unit,
  onReplaceQueryChange: (String) -> Unit,
  onToggleMatchCase: () -> Unit,
  onToggleMatchWholeWord: () -> Unit,
  onToggleUseRegex: () -> Unit,
  onFindNext: () -> Unit,
  onFindPrevious: () -> Unit,
  onReplaceCurrent: () -> Unit,
  onReplaceAll: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isReplaceExpanded by remember { mutableStateOf(true) }

  Box(
    modifier = modifier
      .shadow(8.dp, RoundedCornerShape(8.dp))
      .clip(RoundedCornerShape(8.dp))
      .background(VsCodeEditorBg)
      .border(1.dp, VsCodeBorder, RoundedCornerShape(8.dp))
      .padding(6.dp)
      .testTag("editor_find_replace_toolbar")
  ) {
    Column(modifier = Modifier.width(340.dp)) {
      // Row 1: Find row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Toggle Replace expand/collapse arrow
        IconButton(
          onClick = { isReplaceExpanded = !isReplaceExpanded },
          modifier = Modifier
            .size(24.dp)
            .testTag("toggle_replace_row_btn")
        ) {
          Icon(
            imageVector = if (isReplaceExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
            contentDescription = if (isReplaceExpanded) "Collapse Replace" else "Expand Replace",
            tint = VsCodeTextSecondary,
            modifier = Modifier.size(16.dp)
          )
        }

        Spacer(modifier = Modifier.width(2.dp))

        // Find Input Box with inner toggles
        Row(
          modifier = Modifier
            .weight(1f)
            .height(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(VsCodeSurfaceDark)
            .border(0.5.dp, if (findQuery.isNotEmpty()) VsCodeLavender else VsCodeBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          BasicTextField(
            value = findQuery,
            onValueChange = onFindQueryChange,
            singleLine = true,
            textStyle = TextStyle(
              color = VsCodeTextPrimary,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace
            ),
            cursorBrush = SolidColor(VsCodeLavender),
            modifier = Modifier
              .weight(1f)
              .testTag("find_input_field"),
            decorationBox = { innerTextField ->
              if (findQuery.isEmpty()) {
                Text(
                  text = "Find...",
                  color = VsCodeTextMuted,
                  fontSize = 12.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
              innerTextField()
            }
          )

          // Toggle: Match Case (Aa)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(3.dp))
              .background(if (matchCase) VsCodeLavender.copy(alpha = 0.3f) else Color.Transparent)
              .border(
                0.5.dp,
                if (matchCase) VsCodeLavender else Color.Transparent,
                RoundedCornerShape(3.dp)
              )
              .clickable(onClick = onToggleMatchCase)
              .padding(horizontal = 3.dp, vertical = 1.dp)
              .testTag("toggle_match_case_btn")
          ) {
            Text(
              text = "Aa",
              color = if (matchCase) VsCodeLavender else VsCodeTextMuted,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.width(3.dp))

          // Toggle: Match Whole Word (\b)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(3.dp))
              .background(if (matchWholeWord) VsCodeLavender.copy(alpha = 0.3f) else Color.Transparent)
              .border(
                0.5.dp,
                if (matchWholeWord) VsCodeLavender else Color.Transparent,
                RoundedCornerShape(3.dp)
              )
              .clickable(onClick = onToggleMatchWholeWord)
              .padding(horizontal = 3.dp, vertical = 1.dp)
              .testTag("toggle_match_whole_word_btn")
          ) {
            Text(
              text = "\\b",
              color = if (matchWholeWord) VsCodeLavender else VsCodeTextMuted,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.width(3.dp))

          // Toggle: Use Regex (.*)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(3.dp))
              .background(if (useRegex) VsCodeLavender.copy(alpha = 0.3f) else Color.Transparent)
              .border(
                0.5.dp,
                if (useRegex) VsCodeLavender else Color.Transparent,
                RoundedCornerShape(3.dp)
              )
              .clickable(onClick = onToggleUseRegex)
              .padding(horizontal = 3.dp, vertical = 1.dp)
              .testTag("toggle_use_regex_btn")
          ) {
            Text(
              text = ".*",
              color = if (useRegex) VsCodeLavender else VsCodeTextMuted,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Match counter badge
        val counterText = when {
          findQuery.isEmpty() -> ""
          totalMatches == 0 -> "No results"
          else -> "${currentMatchIndex + 1} of $totalMatches"
        }
        if (counterText.isNotEmpty()) {
          Text(
            text = counterText,
            color = if (totalMatches > 0) VsCodeLavender else Color(0xFFEF4444),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 3.dp)
          )
        }

        // Previous Match (Up)
        IconButton(
          onClick = onFindPrevious,
          enabled = totalMatches > 0,
          modifier = Modifier
            .size(24.dp)
            .testTag("find_previous_btn")
        ) {
          Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = "Previous match",
            tint = if (totalMatches > 0) VsCodeTextPrimary else VsCodeTextMuted,
            modifier = Modifier.size(16.dp)
          )
        }

        // Next Match (Down)
        IconButton(
          onClick = onFindNext,
          enabled = totalMatches > 0,
          modifier = Modifier
            .size(24.dp)
            .testTag("find_next_btn")
        ) {
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Next match",
            tint = if (totalMatches > 0) VsCodeTextPrimary else VsCodeTextMuted,
            modifier = Modifier.size(16.dp)
          )
        }

        // Close button
        IconButton(
          onClick = onClose,
          modifier = Modifier
            .size(24.dp)
            .testTag("close_find_replace_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close search",
            tint = VsCodeTextSecondary,
            modifier = Modifier.size(15.dp)
          )
        }
      }

      // Row 2: Replace row (Animated)
      AnimatedVisibility(
        visible = isReplaceExpanded,
        enter = expandVertically(),
        exit = shrinkVertically()
      ) {
        Column {
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Spacer(modifier = Modifier.width(26.dp))

            // Replace Input Box
            Row(
              modifier = Modifier
                .weight(1f)
                .height(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(VsCodeSurfaceDark)
                .border(0.5.dp, VsCodeBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              BasicTextField(
                value = replaceQuery,
                onValueChange = onReplaceQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                  color = VsCodeTextPrimary,
                  fontSize = 12.sp,
                  fontFamily = FontFamily.Monospace
                ),
                cursorBrush = SolidColor(VsCodeLavender),
                modifier = Modifier
                  .weight(1f)
                  .testTag("replace_input_field"),
                decorationBox = { innerTextField ->
                  if (replaceQuery.isEmpty()) {
                    Text(
                      text = "Replace...",
                      color = VsCodeTextMuted,
                      fontSize = 12.sp,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                  innerTextField()
                }
              )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Replace Current Button
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (totalMatches > 0) VsCodeSurfaceDark else Color.Transparent)
                .border(0.5.dp, if (totalMatches > 0) VsCodeBorder else Color.Transparent, RoundedCornerShape(4.dp))
                .clickable(enabled = totalMatches > 0, onClick = onReplaceCurrent)
                .padding(horizontal = 7.dp, vertical = 4.dp)
                .testTag("replace_current_btn")
            ) {
              Text(
                text = "Replace",
                color = if (totalMatches > 0) VsCodeLavender else VsCodeTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Replace All Button
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (totalMatches > 0) VsCodeLavender.copy(alpha = 0.2f) else Color.Transparent)
                .border(
                  0.5.dp,
                  if (totalMatches > 0) VsCodeLavender.copy(alpha = 0.6f) else Color.Transparent,
                  RoundedCornerShape(4.dp)
                )
                .clickable(enabled = totalMatches > 0, onClick = onReplaceAll)
                .padding(horizontal = 7.dp, vertical = 4.dp)
                .testTag("replace_all_btn")
            ) {
              Text(
                text = "All",
                color = if (totalMatches > 0) VsCodeLavender else VsCodeTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}
