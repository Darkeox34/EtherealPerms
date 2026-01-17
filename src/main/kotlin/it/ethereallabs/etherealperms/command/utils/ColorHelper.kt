package it.ethereallabs.etherealperms.command.utils

import com.hypixel.hytale.server.core.Message

object ColorHelper {
    private val legacyToHex: Map<Char, String> = mapOf(
        '0' to "#000000",
        '1' to "#0000AA",
        '2' to "#00AA00",
        '3' to "#00AAAA",
        '4' to "#AA0000",
        '5' to "#AA00AA",
        '6' to "#FFAA00",
        '7' to "#AAAAAA",
        '8' to "#555555",
        '9' to "#5555FF",
        'a' to "#55FF55",
        'b' to "#55FFFF",
        'c' to "#FF5555",
        'd' to "#FF55FF",
        'e' to "#FFFF55",
        'f' to "#FFFFFF"
    )

    val wellKnownToHex: Map<String, String> = mapOf(
        "RED" to "#FF5555",
        "DARK_RED" to "#AA0000",
        "GOLD" to "#FFAA00",
        "YELLOW" to "#FFFF55",
        "GREEN" to "#55FF55",
        "DARK_GREEN" to "#00AA00",
        "AQUA" to "#55FFFF",
        "DARK_AQUA" to "#00AAAA",
        "BLUE" to "#5555FF",
        "DARK_BLUE" to "#0000AA",
        "LIGHT_PURPLE" to "#FF55FF",
        "DARK_PURPLE" to "#AA00AA",
        "WHITE" to "#FFFFFF",
        "GRAY" to "#AAAAAA",
        "DARK_GRAY" to "#555555",
        "BLACK" to "#000000",
        "PINK" to "#FF69B4",
        "ORANGE" to "#FFA500",
        "LIME" to "#00FF00",
        "CYAN" to "#00FFFF"
    )

    data class TextSegment(val text: String, val colorHex: String?, val bold: Boolean = false, val italic: Boolean = false)

    fun translateMessageColors(input: String): Message {
        val segments = mutableListOf<TextSegment>()

        val pattern = Regex("<([^>]+)>|&([0-9a-fA-Fklmnor])")

        var lastIdx = 0
        var currentColor: String? = null
        var currentBold = false
        var currentItalic = false

        pattern.findAll(input).forEach { match ->
            val tagStart = match.range.first

            if (tagStart > lastIdx) {
                segments.add(TextSegment(input.substring(lastIdx, tagStart), currentColor, currentBold, currentItalic))
            }

            val fullMatch = match.value
            if (fullMatch.startsWith("&")) {
                val code = fullMatch[1].lowercaseChar()
                when (code) {
                    in '0'..'9', in 'a'..'f' -> {
                        currentColor = legacyToHex[code]
                    }
                    'l' -> currentBold = true
                    'o' -> currentItalic = true
                    'r' -> {
                        currentColor = null
                        currentBold = false
                        currentItalic = false
                    }
                }
            } else {
                val tagContent = match.groupValues[1].uppercase()
                when {
                    tagContent == "RESET" -> {
                        currentColor = null
                        currentBold = false
                        currentItalic = false
                    }
                    tagContent.startsWith("#") -> currentColor = tagContent
                    wellKnownToHex.containsKey(tagContent) -> currentColor = wellKnownToHex[tagContent]
                    tagContent == "BOLD" || tagContent == "B" -> currentBold = true
                    tagContent == "/BOLD" || tagContent == "/B" -> currentBold = false
                    tagContent == "ITALIC" || tagContent == "I" -> currentItalic = true
                    tagContent == "/ITALIC" || tagContent == "/I" -> currentItalic = false
                }
            }
            lastIdx = match.range.last + 1
        }

        if (lastIdx < input.length) {
            segments.add(TextSegment(input.substring(lastIdx), currentColor, currentBold, currentItalic))
        }

        val returnMessage = Message.empty()
        segments.filter { it.text.isNotEmpty() }.forEach { segment ->
            var msg = Message.raw(segment.text)
            segment.colorHex?.let { msg = msg.color(it) }
            msg = msg.bold(segment.bold).italic(segment.italic)
            returnMessage.insert(msg)
        }

        return returnMessage
    }
}