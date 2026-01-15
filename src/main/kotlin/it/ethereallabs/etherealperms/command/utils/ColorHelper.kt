package it.ethereallabs.etherealperms.command.utils

import com.hypixel.hytale.server.core.Message

object ColorHelper {
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

    data class TextSegment(val text: String, val colorHex: String?, val bold : Boolean = false, val italic : Boolean = false)

    fun translateMessageColors(input: String): Message {
        val segments = mutableListOf<TextSegment>()
        val pattern = Regex("<([^>]+)>")

        var lastIdx = 0

        var currentColor: String? = null
        var currentBold = false
        var currentItalic = false

        pattern.findAll(input).forEach { match ->
            val tagContent = match.groupValues[1].uppercase()
            val tagStart = match.range.first

            if (tagStart > lastIdx) {
                segments.add(
                    TextSegment(
                        text = input.substring(lastIdx, tagStart),
                        colorHex = currentColor,
                        bold = currentBold,
                        italic = currentItalic
                    )
                )
            }

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

            lastIdx = match.range.last + 1
        }

        if (lastIdx < input.length) {
            segments.add(
                TextSegment(
                    text = input.substring(lastIdx),
                    colorHex = currentColor,
                    bold = currentBold,
                    italic = currentItalic
                )
            )
        }

        val returnMessage = Message.empty()

        segments.forEach { segment ->
            var msg = Message.raw(segment.text)

            segment.colorHex?.let {
                msg = msg.color(it)
            }
            msg = msg.bold(segment.bold).italic(segment.italic)

            returnMessage.insert(msg)
        }

        return returnMessage
    }

}