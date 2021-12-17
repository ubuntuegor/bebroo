package to.bnt.draw.shared.util

import kotlinx.datetime.*

private val months = listOf(
    "января",
    "февраля",
    "марта",
    "апреля",
    "мая",
    "июня",
    "июля",
    "августа",
    "сентября",
    "октября",
    "ноября",
    "декабря"
)

private fun Int.withZero(): String {
    var result = this.toString()
    if (result.length < 2) result = "0$result"
    return result
}

fun formatTimestamp(timestamp: Long): String {
    val currentInstant = Clock.System.now()
    val timestampInstant = Instant.fromEpochSeconds(timestamp)
    val currentDateTime = currentInstant.toLocalDateTime(TimeZone.currentSystemDefault())
    val dateTime = timestampInstant.toLocalDateTime(TimeZone.currentSystemDefault())

    val dateString = when (dateTime.date) {
        currentDateTime.date -> {
            null
        }
        currentDateTime.date.minus(1, DateTimeUnit.DAY) -> {
            "вчера"
        }
        else -> {
            "${dateTime.dayOfMonth} ${months[dateTime.monthNumber - 1]} " +
                    "${if (dateTime.year != currentDateTime.year) dateTime.year else ""}"
        }
    }

    val timeString = "${dateTime.hour.withZero()}:${dateTime.minute.withZero()}"

    return dateString?.let {
        "$dateString в $timeString"
    } ?: timeString
}
