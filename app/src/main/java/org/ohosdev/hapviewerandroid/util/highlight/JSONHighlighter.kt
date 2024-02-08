package org.ohosdev.hapviewerandroid.util.highlight

object JSONHighlighter {
    private const val COLOR_STRING = "#009688"
    private const val COLOR_KEYWORD = "#673AB7"
    private const val COLOR_NUMBER = "#2196F3"

    private val PATTERN_STRING = Regex("([^\\\\])(\"[^\"]*[^\\\\]\")")
    private val PATTERN_KEYWORD = Regex("\\b(true|false)\\b")
    private val PATTERN_NUMBER = Regex("([\\s{])(\\d+)([\\s},])")
    private val PATTERN_WRAP = Regex("\\n")
    private val PATTERN_SPACE = Regex(" ")

    fun highlight(
        jsonText: String,
        stringColor: String = COLOR_STRING,
        keywordColor: String = COLOR_KEYWORD,
        numberColor: String = COLOR_NUMBER
    ): String {
        var highlightedJson = jsonText
        highlightedJson = PATTERN_SPACE.replace(highlightedJson, "&nbsp;")
        highlightedJson =
            PATTERN_STRING.replace(highlightedJson) { "${it.groupValues[1]}<span style=\"color: $stringColor;\">${it.groupValues[2]}</span>" }
        highlightedJson =
            PATTERN_KEYWORD.replace(highlightedJson) { "<span style=\"color: $keywordColor;\">${it.value}</span>" }
        highlightedJson =
            PATTERN_NUMBER.replace(highlightedJson) { "${it.groupValues[1]}<span style=\"color: $numberColor;\">${it.groupValues[2]}</span>${it.groupValues[3]}" }
        highlightedJson = PATTERN_WRAP.replace(highlightedJson, "<br />")
        return highlightedJson
    }
}