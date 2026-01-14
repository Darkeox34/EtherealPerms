package it.ethereallabs.etherealperms.command.utils

import java.util.concurrent.TimeUnit
import kotlin.text.iterator

object CommandUtils {

    fun parseContexts(args: Array<String>, startIndex: Int): Map<String, String> {
        val contexts = mutableMapOf<String, String>()
        for (i in startIndex until args.size) {
            val arg = args[i]
            if (arg.contains("=")) {
                val parts = arg.split("=", limit = 2)
                if (parts.size == 2) {
                    contexts[parts[0]] = parts[1]
                }
            }
        }
        return contexts
    }

    fun parseDuration(input: String): Long? {
        if (input.equals("null", ignoreCase = true) || input.equals("false", ignoreCase = true)) return null

        var duration = 0L
        var currentNumber = StringBuilder()

        for (char in input) {
            if (char.isDigit()) {
                currentNumber.append(char)
            } else {
                val number = currentNumber.toString().toLongOrNull() ?: 0L
                currentNumber = StringBuilder()
                when (char.lowercaseChar()) {
                    'd' -> duration += TimeUnit.DAYS.toMillis(number)
                    'h' -> duration += TimeUnit.HOURS.toMillis(number)
                    'm' -> duration += TimeUnit.MINUTES.toMillis(number)
                    's' -> duration += TimeUnit.SECONDS.toMillis(number)
                }
            }
        }
        return if (duration > 0) System.currentTimeMillis() + duration else null
    }
}