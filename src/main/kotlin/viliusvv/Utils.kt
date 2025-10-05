package viliusvv

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.reflect.KProperty

fun formatDate(millis: Long): String {
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return dateTime.format(formatter)
}

open class ListenerDelegate<T>(initialValue: T, private val listener: () -> Unit) {
    private var backingField: T = initialValue

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return backingField
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        // Only fire listeners if the value actually changes
        if (backingField != value) {
            backingField = value
            listener()
        }
    }
}