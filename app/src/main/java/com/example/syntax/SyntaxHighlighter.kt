package com.example.syntax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.SyntaxAttr
import com.example.ui.theme.SyntaxBoolean
import com.example.ui.theme.SyntaxComment
import com.example.ui.theme.SyntaxConstant
import com.example.ui.theme.SyntaxDoctype
import com.example.ui.theme.SyntaxFunction
import com.example.ui.theme.SyntaxKeyword
import com.example.ui.theme.SyntaxNumber
import com.example.ui.theme.SyntaxOperator
import com.example.ui.theme.SyntaxProperty
import com.example.ui.theme.SyntaxPunctuation
import com.example.ui.theme.SyntaxRegex
import com.example.ui.theme.SyntaxSelector
import com.example.ui.theme.SyntaxString
import com.example.ui.theme.SyntaxTag
import com.example.ui.theme.SyntaxVariable
import java.util.regex.Pattern

object SyntaxHighlighter {

  // --- JavaScript Keyword & Built-in Sets ---
  private val JS_KEYWORDS = setOf(
    "const", "let", "var", "function", "return", "if", "else", "for", "while",
    "do", "switch", "case", "break", "continue", "default", "try", "catch",
    "finally", "throw", "async", "await", "import", "export", "from", "as",
    "class", "extends", "new", "this", "super", "typeof", "instanceof", "in",
    "of", "yield", "static", "void", "delete", "debugger", "interface", "type",
    "enum", "implements", "get", "set", "constructor"
  )

  private val JS_BOOLEANS = setOf(
    "true", "false", "null", "undefined", "NaN", "Infinity"
  )

  private val JS_BUILTINS = setOf(
    "console", "document", "window", "Math", "JSON", "Array", "Object", "String",
    "Number", "Boolean", "Promise", "fetch", "setTimeout", "setInterval",
    "clearTimeout", "clearInterval", "addEventListener", "removeEventListener",
    "getElementById", "querySelector", "querySelectorAll", "createElement",
    "appendChild", "removeChild", "localStorage", "sessionStorage", "alert",
    "prompt", "confirm", "requestAnimationFrame", "Date", "RegExp", "Map",
    "Set", "Error", "Symbol", "Reflect", "Proxy", "Event", "CustomEvent",
    "HTMLElement", "Node", "navigator", "location", "history", "Intl"
  )

  // --- CSS Keyword Sets ---
  private val CSS_KEYWORDS = setOf(
    "none", "block", "flex", "grid", "inline", "inline-block", "inline-flex",
    "inline-grid", "auto", "inherit", "initial", "unset", "revert", "relative",
    "absolute", "fixed", "sticky", "static", "bold", "bolder", "lighter",
    "normal", "center", "pointer", "default", "transparent", "currentcolor",
    "solid", "dashed", "dotted", "double", "groove", "ridge", "inset", "outset",
    "hidden", "visible", "scroll", "cover", "contain", "fill", "scale-down",
    "nowrap", "wrap", "wrap-reverse", "column", "column-reverse", "row",
    "row-reverse", "flex-start", "flex-end", "space-between", "space-around",
    "space-evenly", "stretch", "baseline", "ease", "ease-in", "ease-out",
    "ease-in-out", "linear", "infinite", "both", "forwards", "backwards",
    "sans-serif", "serif", "monospace", "cursive", "fantasy", "border-box",
    "content-box", "padding-box", "uppercase", "lowercase", "capitalize",
    "underline", "line-through", "ellipsis", "clip", "break-word"
  )

  // Pre-compiled regular expressions for performance
  private val HTML_COMMENT_PATTERN = Pattern.compile("<!--[\\s\\S]*?-->")
  private val HTML_DOCTYPE_PATTERN = Pattern.compile("<!DOCTYPE\\s+([a-zA-Z0-9]+)>", Pattern.CASE_INSENSITIVE)
  private val HTML_STYLE_BLOCK_PATTERN = Pattern.compile("(?i)(<style\\b[^>]*>)([\\s\\S]*?)(</style>)")
  private val HTML_SCRIPT_BLOCK_PATTERN = Pattern.compile("(?i)(<script\\b[^>]*>)([\\s\\S]*?)(</script>)")
  private val HTML_TAG_PATTERN = Pattern.compile("</?([a-zA-Z0-9_-]+)")
  private val HTML_CLOSE_BRACKET_PATTERN = Pattern.compile("/?>")
  private val HTML_ATTR_PATTERN = Pattern.compile("([a-zA-Z0-9_:-]+)\\s*=")
  private val HTML_STRING_PATTERN = Pattern.compile("\"[^\"]*\"|'[^']*'")
  private val HTML_ENTITY_PATTERN = Pattern.compile("&[a-zA-Z0-9#]+;")

  private val CSS_COMMENT_PATTERN = Pattern.compile("/\\*[\\s\\S]*?\\*/")
  private val CSS_STRING_PATTERN = Pattern.compile("\"[^\"]*\"|'[^']*'")
  private val CSS_AT_RULE_PATTERN = Pattern.compile("@(?:media|keyframes|import|font-face|supports|layer|charset)\\b")
  private val CSS_SELECTOR_PATTERN = Pattern.compile("([.#]?[a-zA-Z0-9_:-]+)\\s*\\{")
  private val CSS_CLASS_SELECTOR_PATTERN = Pattern.compile("\\.[a-zA-Z0-9_-]+")
  private val CSS_ID_SELECTOR_PATTERN = Pattern.compile("#[a-zA-Z0-9_-]+")
  private val CSS_PSEUDO_PATTERN = Pattern.compile("::?[a-zA-Z0-9_-]+(?:\\([^\\)]*\\))?")
  private val CSS_PROPERTY_PATTERN = Pattern.compile("(?<![a-zA-Z0-9_-])(--[a-zA-Z0-9_-]+|[a-zA-Z0-9_-]+)\\s*:")
  private val CSS_FUNCTION_PATTERN = Pattern.compile("\\b(var|calc|rgb|rgba|hsl|hsla|linear-gradient|radial-gradient|url|clamp|min|max|rotate|scale|translate|blur)\\s*\\(")
  private val CSS_NUMBER_UNIT_PATTERN = Pattern.compile("\\b\\d+(\\.\\d+)?(px|rem|em|%|vh|vw|vmin|vmax|s|ms|deg|fr|ch|ex|pt)?\\b")
  private val CSS_HEX_COLOR_PATTERN = Pattern.compile("#(?:[0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})\\b")
  private val CSS_IMPORTANT_PATTERN = Pattern.compile("!important\\b")
  private val CSS_PUNCTUATION_PATTERN = Pattern.compile("[{}:;,]")

  private val JS_SINGLE_COMMENT_PATTERN = Pattern.compile("//.*")
  private val JS_MULTI_COMMENT_PATTERN = Pattern.compile("/\\*[\\s\\S]*?\\*/")
  private val JS_JSDOC_TAG_PATTERN = Pattern.compile("@(?:param|returns|return|type|typedef|property|deprecated|template|author|see|example)\\b")
  private val JS_STRING_PATTERN = Pattern.compile("`[\\s\\S]*?`|\"(?:[^\"\\\\]|\\\\.)*\"|'(?:[^'\\\\]|\\\\.)*'")
  private val JS_TEMPLATE_INTERPOLATION_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}")
  private val JS_REGEX_PATTERN = Pattern.compile("/(?![/*\\s=])(?:[^/\\\\\r\n]|\\\\.)+/[gimsuy]*")
  private val JS_FUNCTION_CALL_PATTERN = Pattern.compile("\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*(?=\\()")
  private val JS_WORD_PATTERN = Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\b")
  private val JS_NUMBER_PATTERN = Pattern.compile("\\b(0x[0-9a-fA-F]+|0b[01]+|\\d+(\\.\\d+)?([eE][+-]?\\d+)?)\\b")
  private val JS_OPERATOR_PATTERN = Pattern.compile("===|!==|==|!=|<=|>=|=>|&&|\\|\\||\\?\\?|\\?\\.|\\+=|-=|\\*=|/=|\\+\\+|--|[+\\-*/%=!<>?:]")
  private val JS_PUNCTUATION_PATTERN = Pattern.compile("[(){},;]|\\[|\\]")

  fun highlight(
    code: String,
    language: String,
    findQuery: String = "",
    matchCase: Boolean = false,
    matchWholeWord: Boolean = false,
    useRegex: Boolean = false,
    currentMatchIndex: Int = -1
  ): AnnotatedString {
    val base = when (language.lowercase()) {
      "html", "htm" -> highlightHtml(code)
      "css" -> highlightCss(code)
      "js", "javascript", "jsx", "ts", "tsx" -> highlightJs(code)
      "json" -> highlightJson(code)
      "md", "markdown" -> highlightMarkdown(code)
      else -> buildAnnotatedString { append(code) }
    }

    if (findQuery.isEmpty()) {
      return base
    }

    // Overlay search match highlights
    val builder = AnnotatedString.Builder(base)
    try {
      val flags = if (matchCase) 0 else Pattern.CASE_INSENSITIVE
      val patternString = when {
        useRegex && matchWholeWord -> "\\b(?:$findQuery)\\b"
        useRegex -> findQuery
        matchWholeWord -> "\\b" + Pattern.quote(findQuery) + "\\b"
        else -> Pattern.quote(findQuery)
      }
      val pattern = Pattern.compile(patternString, flags)
      val matcher = pattern.matcher(code)
      var matchIdx = 0
      while (matcher.find()) {
        val start = matcher.start()
        val end = matcher.end()
        if (start < end && end <= code.length) {
          val isActive = matchIdx == currentMatchIndex
          builder.addStyle(
            SpanStyle(
              background = if (isActive) Color(0xFFF59E0B) else Color(0x66F59E0B),
              color = if (isActive) Color(0xFF0F172A) else Color(0xFFFEF08A),
              fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold
            ),
            start,
            end
          )
        }
        matchIdx++
      }
    } catch (_: Exception) {}

    return builder.toAnnotatedString()
  }

  // --- HTML Highlighting Engine with embedded CSS & JS support ---
  fun highlightHtml(code: String): AnnotatedString {
    val builder = AnnotatedString.Builder(code)
    if (code.isEmpty()) return builder.toAnnotatedString()

    // Protected ranges (embedded style/script blocks and comments)
    val protectedRanges = mutableListOf<IntRange>()

    // 1. Comments: <!-- ... -->
    val commentMatcher = HTML_COMMENT_PATTERN.matcher(code)
    while (commentMatcher.find()) {
      builder.addStyle(
        SpanStyle(color = SyntaxComment),
        commentMatcher.start(),
        commentMatcher.end()
      )
      protectedRanges.add(commentMatcher.start() until commentMatcher.end())
    }

    // 2. DOCTYPE declaration
    val doctypeMatcher = HTML_DOCTYPE_PATTERN.matcher(code)
    while (doctypeMatcher.find()) {
      builder.addStyle(
        SpanStyle(color = SyntaxDoctype, fontWeight = FontWeight.SemiBold),
        doctypeMatcher.start(),
        doctypeMatcher.end()
      )
      builder.addStyle(
        SpanStyle(color = SyntaxNumber, fontWeight = FontWeight.Bold),
        doctypeMatcher.start(1),
        doctypeMatcher.end(1)
      )
      protectedRanges.add(doctypeMatcher.start() until doctypeMatcher.end())
    }

    // 3. Embedded <style> blocks: recursively highlight CSS inside
    val styleMatcher = HTML_STYLE_BLOCK_PATTERN.matcher(code)
    while (styleMatcher.find()) {
      val openStart = styleMatcher.start(1)
      val openEnd = styleMatcher.end(1)
      val cssStart = styleMatcher.start(2)
      val cssEnd = styleMatcher.end(2)
      val closeStart = styleMatcher.start(3)
      val closeEnd = styleMatcher.end(3)

      // Highlight opening <style> and closing </style>
      highlightSingleTag(builder, code, openStart, openEnd)
      highlightSingleTag(builder, code, closeStart, closeEnd)

      // Highlight inner CSS content
      if (cssStart < cssEnd) {
        val cssText = styleMatcher.group(2)
        applySubLanguageHighlight(builder, cssText, cssStart) { subCode -> highlightCss(subCode) }
      }

      protectedRanges.add(openStart until closeEnd)
    }

    // 4. Embedded <script> blocks: recursively highlight JS inside
    val scriptMatcher = HTML_SCRIPT_BLOCK_PATTERN.matcher(code)
    while (scriptMatcher.find()) {
      val openStart = scriptMatcher.start(1)
      val openEnd = scriptMatcher.end(1)
      val jsStart = scriptMatcher.start(2)
      val jsEnd = scriptMatcher.end(2)
      val closeStart = scriptMatcher.start(3)
      val closeEnd = scriptMatcher.end(3)

      highlightSingleTag(builder, code, openStart, openEnd)
      highlightSingleTag(builder, code, closeStart, closeEnd)

      if (jsStart < jsEnd) {
        val jsText = scriptMatcher.group(2)
        applySubLanguageHighlight(builder, jsText, jsStart) { subCode -> highlightJs(subCode) }
      }

      protectedRanges.add(openStart until closeEnd)
    }

    // Helper to check if an index is inside any protected range
    fun isProtected(start: Int, end: Int): Boolean {
      return protectedRanges.any { range -> maxOf(start, range.first) < minOf(end, range.last + 1) }
    }

    // 5. General HTML Tags: </?[a-zA-Z0-9_-]+
    val tagMatcher = HTML_TAG_PATTERN.matcher(code)
    while (tagMatcher.find()) {
      val s = tagMatcher.start()
      val e = tagMatcher.end()
      if (!isProtected(s, e)) {
        // Brackets: < or </
        builder.addStyle(
          SpanStyle(color = SyntaxPunctuation),
          s,
          tagMatcher.start(1)
        )
        // Tag name
        builder.addStyle(
          SpanStyle(color = SyntaxTag, fontWeight = FontWeight.SemiBold),
          tagMatcher.start(1),
          e
        )
      }
    }

    // 6. Tag closing brackets: > or />
    val closeBracketMatcher = HTML_CLOSE_BRACKET_PATTERN.matcher(code)
    while (closeBracketMatcher.find()) {
      val s = closeBracketMatcher.start()
      val e = closeBracketMatcher.end()
      if (!isProtected(s, e)) {
        builder.addStyle(
          SpanStyle(color = SyntaxPunctuation),
          s,
          e
        )
      }
    }

    // 7. Attributes: [a-zA-Z0-9_:-]+(?=\s*=)
    val attrMatcher = HTML_ATTR_PATTERN.matcher(code)
    while (attrMatcher.find()) {
      val attrStart = attrMatcher.start(1)
      val attrEnd = attrMatcher.end(1)
      if (!isProtected(attrStart, attrEnd)) {
        builder.addStyle(
          SpanStyle(color = SyntaxAttr),
          attrStart,
          attrEnd
        )
        // Attribute equals sign '='
        builder.addStyle(
          SpanStyle(color = SyntaxPunctuation),
          attrEnd,
          attrMatcher.end()
        )
      }
    }

    // 8. Attribute Strings: "..." or '...'
    val stringMatcher = HTML_STRING_PATTERN.matcher(code)
    while (stringMatcher.find()) {
      val s = stringMatcher.start()
      val e = stringMatcher.end()
      if (!isProtected(s, e)) {
        builder.addStyle(
          SpanStyle(color = SyntaxString),
          s,
          e
        )
      }
    }

    // 9. HTML Entities: &amp;, &copy;, &lt;, etc.
    val entityMatcher = HTML_ENTITY_PATTERN.matcher(code)
    while (entityMatcher.find()) {
      val s = entityMatcher.start()
      val e = entityMatcher.end()
      if (!isProtected(s, e)) {
        builder.addStyle(
          SpanStyle(color = SyntaxNumber, fontWeight = FontWeight.Medium),
          s,
          e
        )
      }
    }

    return builder.toAnnotatedString()
  }

  // --- CSS Highlighting Engine ---
  fun highlightCss(code: String): AnnotatedString {
    val builder = AnnotatedString.Builder(code)
    if (code.isEmpty()) return builder.toAnnotatedString()

    val protectedRanges = mutableListOf<IntRange>()

    // 1. CSS Comments: /* ... */
    val commentMatcher = CSS_COMMENT_PATTERN.matcher(code)
    while (commentMatcher.find()) {
      val s = commentMatcher.start()
      val e = commentMatcher.end()
      builder.addStyle(SpanStyle(color = SyntaxComment), s, e)
      protectedRanges.add(s until e)
    }

    // 2. CSS Strings: "..." or '...'
    val stringMatcher = CSS_STRING_PATTERN.matcher(code)
    while (stringMatcher.find()) {
      val s = stringMatcher.start()
      val e = stringMatcher.end()
      builder.addStyle(SpanStyle(color = SyntaxString), s, e)
      protectedRanges.add(s until e)
    }

    fun isProtected(start: Int, end: Int): Boolean {
      return protectedRanges.any { range -> start < range.last && end > range.first }
    }

    // 3. At-rules: @media, @keyframes, @import, etc.
    val atMatcher = CSS_AT_RULE_PATTERN.matcher(code)
    while (atMatcher.find()) {
      val s = atMatcher.start()
      val e = atMatcher.end()
      if (!isProtected(s, e)) {
        builder.addStyle(SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold), s, e)
      }
    }

    // 4. Class selectors: .foo-bar
    val classMatcher = CSS_CLASS_SELECTOR_PATTERN.matcher(code)
    while (classMatcher.find()) {
      val s = classMatcher.start()
      val e = classMatcher.end()
      if (!isProtected(s, e)) {
        builder.addStyle(SpanStyle(color = SyntaxSelector, fontWeight = FontWeight.SemiBold), s, e)
      }
    }

    // 5. ID selectors: #app-root
    val idMatcher = CSS_ID_SELECTOR_PATTERN.matcher(code)
    while (idMatcher.find()) {
      val s = idMatcher.start()
      val e = idMatcher.end()
      // Exclude hex colors which are preceded by colon or within property values
      val before = code.substring(0, s).trimEnd()
      if (!before.endsWith(":") && !before.endsWith(",") && !isProtected(s, e)) {
        builder.addStyle(SpanStyle(color = SyntaxTag, fontWeight = FontWeight.Bold), s, e)
      }
    }

    // 6. Pseudo-classes & Pseudo-elements: :hover, ::after
    val pseudoMatcher = CSS_PSEUDO_PATTERN.matcher(code)
    while (pseudoMatcher.find()) {
      val s = pseudoMatcher.start()
      val e = pseudoMatcher.end()
      if (!isProtected(s, e)) {
        builder.addStyle(SpanStyle(color = SyntaxAttr), s, e)
      }
    }

    // 7. Property names: color:, background-color:, --my-var:
    val propMatcher = CSS_PROPERTY_PATTERN.matcher(code)
    while (propMatcher.find()) {
      val s = propMatcher.start(1)
      val e = propMatcher.end(1)
      if (!isProtected(s, e)) {
        val propName = propMatcher.group(1)
        if (propName.startsWith("--")) {
          builder.addStyle(SpanStyle(color = SyntaxVariable, fontWeight = FontWeight.Medium), s, e)
        } else {
          builder.addStyle(SpanStyle(color = SyntaxProperty, fontWeight = FontWeight.Medium), s, e)
        }
      }
    }

    // 8. Functions: var(...), rgb(...), calc(...)
    val funcMatcher = CSS_FUNCTION_PATTERN.matcher(code)
    while (funcMatcher.find()) {
      val s = funcMatcher.start(1)
      val e = funcMatcher.end(1)
      if (!isProtected(s, e)) {
        builder.addStyle(SpanStyle(color = SyntaxFunction, fontWeight = FontWeight.SemiBold), s, e)
      }
    }

    // 9. Hex colors: #ffffff, #007ACC
    val hexMatcher = CSS_HEX_COLOR_PATTERN.matcher(code)
    while (hexMatcher.find()) {
      val s = hexMatcher.start()
      val e = hexMatcher.end()
      if (!isProtected(s, e)) {
        builder.addStyle(SpanStyle(color = SyntaxNumber, fontWeight = FontWeight.Medium), s, e)
      }
    }

    // 10. Numbers and measurement units: 12px, 1.5rem, 100%
    val numMatcher = CSS_NUMBER_UNIT_PATTERN.matcher(code)
    while (numMatcher.find()) {
      val s = numMatcher.start()
      val e = numMatcher.end()
      if (!isProtected(s, e)) {
        builder.addStyle(SpanStyle(color = SyntaxNumber), s, e)
      }
    }

    // 11. CSS Keywords: block, flex, auto, relative, etc.
    val wordMatcher = Pattern.compile("\\b[a-zA-Z_-]+\\b").matcher(code)
    while (wordMatcher.find()) {
      val s = wordMatcher.start()
      val e = wordMatcher.end()
      val word = wordMatcher.group().lowercase()
      if (!isProtected(s, e) && CSS_KEYWORDS.contains(word)) {
        builder.addStyle(SpanStyle(color = SyntaxKeyword), s, e)
      }
    }

    // 12. !important declaration
    val impMatcher = CSS_IMPORTANT_PATTERN.matcher(code)
    while (impMatcher.find()) {
      val s = impMatcher.start()
      val e = impMatcher.end()
      if (!isProtected(s, e)) {
        builder.addStyle(SpanStyle(color = SyntaxDoctype, fontWeight = FontWeight.Bold), s, e)
      }
    }

    // 13. Punctuation: {, }, :, ;, ,
    val punctMatcher = CSS_PUNCTUATION_PATTERN.matcher(code)
    while (punctMatcher.find()) {
      val s = punctMatcher.start()
      val e = punctMatcher.end()
      if (!isProtected(s, e)) {
        builder.addStyle(SpanStyle(color = SyntaxPunctuation), s, e)
      }
    }

    return builder.toAnnotatedString()
  }

  // --- JavaScript Highlighting Engine ---
  fun highlightJs(code: String): AnnotatedString {
    val builder = AnnotatedString.Builder(code)
    if (code.isEmpty()) return builder.toAnnotatedString()

    val protectedRanges = mutableListOf<IntRange>()

    // 1. Comments: single-line // ... and multi-line /* ... */
    val singleComment = JS_SINGLE_COMMENT_PATTERN.matcher(code)
    while (singleComment.find()) {
      val s = singleComment.start()
      val e = singleComment.end()
      builder.addStyle(SpanStyle(color = SyntaxComment), s, e)
      protectedRanges.add(s until e)
    }

    val multiComment = JS_MULTI_COMMENT_PATTERN.matcher(code)
    while (multiComment.find()) {
      val s = multiComment.start()
      val e = multiComment.end()
      builder.addStyle(SpanStyle(color = SyntaxComment), s, e)
      protectedRanges.add(s until e)

      // Highlight JSDoc tags inside comments: @param, @returns, etc.
      val jsDoc = JS_JSDOC_TAG_PATTERN.matcher(code.substring(s, e))
      while (jsDoc.find()) {
        builder.addStyle(
          SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.SemiBold),
          s + jsDoc.start(),
          s + jsDoc.end()
        )
      }
    }

    // 2. Strings: '...', "...", and `...`
    val strMatcher = JS_STRING_PATTERN.matcher(code)
    while (strMatcher.find()) {
      val s = strMatcher.start()
      val e = strMatcher.end()
      val strContent = strMatcher.group()

      builder.addStyle(SpanStyle(color = SyntaxString), s, e)
      protectedRanges.add(s until e)

      // If template literal, highlight ${...} interpolation markers
      if (strContent.startsWith("`")) {
        val interpMatcher = JS_TEMPLATE_INTERPOLATION_PATTERN.matcher(strContent)
        while (interpMatcher.find()) {
          val exprStart = s + interpMatcher.start()
          val exprEnd = s + interpMatcher.end()
          // ${ and } in punctuation
          builder.addStyle(SpanStyle(color = SyntaxOperator, fontWeight = FontWeight.Bold), exprStart, exprStart + 2)
          builder.addStyle(SpanStyle(color = SyntaxOperator, fontWeight = FontWeight.Bold), exprEnd - 1, exprEnd)
        }
      }
    }

    // 3. Regular Expression Literals: /.../g
    val regexMatcher = JS_REGEX_PATTERN.matcher(code)
    while (regexMatcher.find()) {
      val s = regexMatcher.start()
      val e = regexMatcher.end()
      val isPrecededByIdentifier = if (s > 0) {
        val prevChar = code.substring(0, s).trimEnd().lastOrNull()
        prevChar != null && (prevChar.isLetterOrDigit() || prevChar == ')' || prevChar == ']' || prevChar == '}')
      } else false

      if (!isPrecededByIdentifier && protectedRanges.none { s < it.last && e > it.first }) {
        builder.addStyle(SpanStyle(color = SyntaxRegex), s, e)
        protectedRanges.add(s until e)
      }
    }

    fun isProtected(start: Int, end: Int): Boolean {
      return protectedRanges.any { range -> start < range.last && end > range.first }
    }

    // 4. Function invocations: myFunc(...) or console.log(...)
    val funcMatcher = JS_FUNCTION_CALL_PATTERN.matcher(code)
    while (funcMatcher.find()) {
      val s = funcMatcher.start(1)
      val e = funcMatcher.end(1)
      val name = funcMatcher.group(1)
      if (!isProtected(s, e) && !JS_KEYWORDS.contains(name)) {
        builder.addStyle(SpanStyle(color = SyntaxFunction, fontWeight = FontWeight.Medium), s, e)
      }
    }

    // 5. Words: Keywords, Booleans, Built-in globals, and Variables
    val wordMatcher = JS_WORD_PATTERN.matcher(code)
    while (wordMatcher.find()) {
      val s = wordMatcher.start()
      val e = wordMatcher.end()
      val word = wordMatcher.group()

      if (!isProtected(s, e)) {
        when {
          JS_KEYWORDS.contains(word) -> {
            builder.addStyle(
              SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.SemiBold),
              s,
              e
            )
          }
          JS_BOOLEANS.contains(word) -> {
            builder.addStyle(
              SpanStyle(color = SyntaxBoolean, fontWeight = FontWeight.SemiBold),
              s,
              e
            )
          }
          JS_BUILTINS.contains(word) -> {
            builder.addStyle(
              SpanStyle(color = SyntaxFunction, fontWeight = FontWeight.Bold),
              s,
              e
            )
          }
        }
      }
    }

    // 6. Numbers: Decimal, Hex (0x...), Binary (0b...), Exponential
    val numMatcher = JS_NUMBER_PATTERN.matcher(code)
    while (numMatcher.find()) {
      val s = numMatcher.start()
      val e = numMatcher.end()
      if (!isProtected(s, e)) {
        builder.addStyle(SpanStyle(color = SyntaxNumber), s, e)
      }
    }

    // 7. Operators: ===, !==, =>, &&, ||, +, -, etc.
    val opMatcher = JS_OPERATOR_PATTERN.matcher(code)
    while (opMatcher.find()) {
      val s = opMatcher.start()
      val e = opMatcher.end()
      if (!isProtected(s, e)) {
        builder.addStyle(SpanStyle(color = SyntaxOperator), s, e)
      }
    }

    // 8. Punctuation: (, ), {, }, [, ], ;, ,
    val punctMatcher = JS_PUNCTUATION_PATTERN.matcher(code)
    while (punctMatcher.find()) {
      val s = punctMatcher.start()
      val e = punctMatcher.end()
      if (!isProtected(s, e)) {
        builder.addStyle(SpanStyle(color = SyntaxPunctuation), s, e)
      }
    }

    return builder.toAnnotatedString()
  }

  // --- JSON Highlighting Engine ---
  private fun highlightJson(code: String): AnnotatedString {
    val builder = AnnotatedString.Builder(code)
    if (code.isEmpty()) return builder.toAnnotatedString()

    // Keys
    val keyMatcher = Pattern.compile("\"([^\"]+)\"\\s*:").matcher(code)
    while (keyMatcher.find()) {
      builder.addStyle(
        SpanStyle(color = SyntaxAttr, fontWeight = FontWeight.SemiBold),
        keyMatcher.start(1),
        keyMatcher.end(1)
      )
    }

    // String values
    val valStringMatcher = Pattern.compile(":\\s*\"([^\"]*)\"").matcher(code)
    while (valStringMatcher.find()) {
      builder.addStyle(
        SpanStyle(color = SyntaxString),
        valStringMatcher.start(1),
        valStringMatcher.end(1)
      )
    }

    // Numbers
    val numMatcher = Pattern.compile(":\\s*(-?\\d+(\\.\\d+)?)\\b").matcher(code)
    while (numMatcher.find()) {
      builder.addStyle(
        SpanStyle(color = SyntaxNumber),
        numMatcher.start(1),
        numMatcher.end(1)
      )
    }

    // Booleans & Null
    val boolMatcher = Pattern.compile(":\\s*(true|false|null)\\b").matcher(code)
    while (boolMatcher.find()) {
      builder.addStyle(
        SpanStyle(color = SyntaxBoolean, fontWeight = FontWeight.Bold),
        boolMatcher.start(1),
        boolMatcher.end(1)
      )
    }

    return builder.toAnnotatedString()
  }

  // --- Markdown Highlighting Engine ---
  private fun highlightMarkdown(code: String): AnnotatedString {
    val builder = AnnotatedString.Builder(code)
    if (code.isEmpty()) return builder.toAnnotatedString()

    val headerMatcher = Pattern.compile("^(#{1,6}\\s.*)$", Pattern.MULTILINE).matcher(code)
    while (headerMatcher.find()) {
      builder.addStyle(
        SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold),
        headerMatcher.start(),
        headerMatcher.end()
      )
    }

    val codeBlockMatcher = Pattern.compile("```[\\s\\S]*?```").matcher(code)
    while (codeBlockMatcher.find()) {
      builder.addStyle(
        SpanStyle(color = SyntaxString),
        codeBlockMatcher.start(),
        codeBlockMatcher.end()
      )
    }

    val linkMatcher = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)").matcher(code)
    while (linkMatcher.find()) {
      builder.addStyle(SpanStyle(color = SyntaxFunction, fontWeight = FontWeight.Medium), linkMatcher.start(1), linkMatcher.end(1))
      builder.addStyle(SpanStyle(color = SyntaxComment), linkMatcher.start(2), linkMatcher.end(2))
    }

    return builder.toAnnotatedString()
  }

  // --- Helper Methods ---
  private fun highlightSingleTag(builder: AnnotatedString.Builder, fullCode: String, start: Int, end: Int) {
    if (start >= end || end > fullCode.length) return
    val tagSnippet = fullCode.substring(start, end)
    val nameMatcher = HTML_TAG_PATTERN.matcher(tagSnippet)
    if (nameMatcher.find()) {
      builder.addStyle(
        SpanStyle(color = SyntaxPunctuation),
        start,
        start + nameMatcher.start(1)
      )
      builder.addStyle(
        SpanStyle(color = SyntaxTag, fontWeight = FontWeight.SemiBold),
        start + nameMatcher.start(1),
        start + nameMatcher.end(1)
      )
    }
    val closeBracket = HTML_CLOSE_BRACKET_PATTERN.matcher(tagSnippet)
    if (closeBracket.find()) {
      builder.addStyle(
        SpanStyle(color = SyntaxPunctuation),
        start + closeBracket.start(),
        start + closeBracket.end()
      )
    }
  }

  private fun applySubLanguageHighlight(
    parentBuilder: AnnotatedString.Builder,
    subCode: String,
    offset: Int,
    highlighter: (String) -> AnnotatedString
  ) {
    val subAnnotated = highlighter(subCode)
    for (span in subAnnotated.spanStyles) {
      val subStart = offset + span.start
      val subEnd = offset + span.end
      if (subStart < subEnd && subEnd <= parentBuilder.length) {
        parentBuilder.addStyle(span.item, subStart, subEnd)
      }
    }
  }
}
