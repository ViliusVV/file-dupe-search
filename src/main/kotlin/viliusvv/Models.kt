package viliusvv

import calcFileHash
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener


data class FileEntry(
    val fileName: String,
    val filePath: String,
    val size: Long,
    val modified: Long,
    val hash: String = calcFileHash(filePath)
) {
    val hashShort get() = hash.take(8)
}

class MainDataModel {
    val listeners = mutableListOf<ChangeListener>()

    var totalFiles by ModelListenerDelegate(0)
    var processedFiles by ModelListenerDelegate(0)
    var inQueue by ModelListenerDelegate(0)
    var inProgress by ModelListenerDelegate(false)
    var statusMessage by ModelListenerDelegate("-")

    var compareNames by ModelListenerDelegate(true)
    var compareHash by ModelListenerDelegate(false)


    val inProgressString get() = if (inProgress) "Processing" else "Idle"

    fun addChangeListener(listener: ChangeListener) {
        listeners.add(listener)
    }

    fun fireChangeListeners() {
        val event = ChangeEvent(this)
        listeners.forEach { it.stateChanged(event) }
    }

    private inner class ModelListenerDelegate<T>(initialValue: T): ListenerDelegate<T>(initialValue, ::fireChangeListeners)
}