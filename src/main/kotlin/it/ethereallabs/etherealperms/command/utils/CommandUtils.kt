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

        if (input.all { it.isDigit() }) {
            return input.toLongOrNull()
        }

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

    fun formatRemainingTime(expiry: Long): String {
        val diff = expiry - System.currentTimeMillis()
        if (diff <= 0) return "Expired"

        val days = TimeUnit.MILLISECONDS.toDays(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

        return when {
            days > 0 -> "$days days ${hours}h left"
            hours > 0 -> "$hours hours ${minutes}m left"
            minutes > 0 -> "$minutes minutes ${seconds}s left"
            else -> "$seconds seconds left"
        }
    }
}