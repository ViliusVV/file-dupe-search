package viliusvv

import calcFileHash
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.table.AbstractTableModel


data class FileEntry(
    var fileName: String,
    var filePath: String,
    var size: Long,
    var modified: Long,
    var hash: String = ""
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

    var identicalFileNames by ModelListenerDelegate(0)
    var identicalFileSizes by ModelListenerDelegate(0)
    var identicalHashes by ModelListenerDelegate(0)
    var identicalFiles by ModelListenerDelegate(0)

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

class PersonTableModel(private val files: MutableList<FileEntry>) : AbstractTableModel() {
    private val columnNames = listOf("Path", "Size", "Modified", "MD5 Hash")

    override fun getRowCount(): Int = files.size

    override fun getColumnCount(): Int = columnNames.size

    override fun getColumnName(column: Int): String = columnNames[column]

    override fun getColumnClass(columnIndex: Int): Class<*> =
        when (columnIndex) {
            0 -> String::class.java // Path
            1 -> Long::class.java // Size
            2 -> String::class.java // Modified
            3 -> String::class.java // MD5 Hash
            else -> super.getColumnClass(columnIndex)
        }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = false

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        println("getValueAt called for row $rowIndex, column $columnIndex")
        val file = files[rowIndex]
        return when (columnIndex) {
            0 -> file.filePath
            1 -> file.size
            2 -> file.modified.toString()
            3 -> file.hashShort
            else -> throw IllegalArgumentException("Invalid column index")
        }
    }

    override fun setValueAt(aValue: Any, rowIndex: Int, columnIndex: Int) {
        val person = files[rowIndex]
        when (columnIndex) {
            3 -> person.hash = aValue as String
        }

        // Very important: notify the JTable that the cell has changed
        fireTableCellUpdated(rowIndex, columnIndex)
    }

    // Custom function to add data and update the table
    fun addRow(person: FileEntry) {
        val newRowIndex = files.size
        files.add(person)
        // Notify the JTable that rows were inserted
        fireTableRowsInserted(newRowIndex, newRowIndex)
    }
}