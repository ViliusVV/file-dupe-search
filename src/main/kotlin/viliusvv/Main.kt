package viliusvv

import com.formdev.flatlaf.FlatDarkLaf
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.io.File
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.table.DefaultTableModel


lateinit var mainFrame: JFrame

var selectedDirs = mutableListOf<File>()

var isProcessing = false
var fileQueue = ConcurrentLinkedQueue<File>()
var fileMap = mutableMapOf<String, FileEntry>()

fun main() {
    println("Starting....")
    FlatDarkLaf.setup();

    // get current dir
//    val files = listSubdirAndFiles(selectedDirs)
    val files = emptyList<FileEntry>()


    SwingUtilities.invokeLater {
        mainFrame = JFrame("File Dupe Finder")
        mainFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        mainFrame.isVisible = true
        mainFrame.requestFocus()
        mainFrame.layout = BorderLayout()


        val selectionTable = createSelectionTable(selectedDirs)
        val scrollPane2 = JScrollPane(selectionTable)

        val chooser = JFileChooser()
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        chooser.dialogType = JFileChooser.OPEN_DIALOG
        chooser.isMultiSelectionEnabled = false
        chooser.toolTipText = "Select directory to scan"

        val dirButton = JButton("Add directory")
        dirButton.addActionListener {
            val res = chooser.showOpenDialog(mainFrame)
            if (res == JFileChooser.APPROVE_OPTION) {
                val dir = chooser.selectedFile
                addDirToSelection(selectionTable, dir)
            } else {
                println("Dialog canceled or error")
            }

            println("Selected dirs: $selectedDirs")
        }

        val procButton = JButton("Process")
        procButton.addActionListener {
            isProcessing = !isProcessing
            procButton.text = if (isProcessing) "Stop" else "Process"
        }

        // Put inside a scroll pane
        val table = createTable(files)
        val scrollPane = JScrollPane(table)

        val buttonPanel = JPanel(BorderLayout())
        buttonPanel.add(dirButton, BorderLayout.WEST)
        buttonPanel.add(procButton, BorderLayout.EAST)


        val topbarPanel = JPanel(BorderLayout())
        topbarPanel.border = EmptyBorder(2, 5, 5, 5)
        topbarPanel.size = Dimension(mainFrame.width, 40)
        topbarPanel.add(JLabel("Files found: ${files.size}"), BorderLayout.LINE_START)
        topbarPanel.add(buttonPanel, BorderLayout.LINE_END)

        val tablesPanel = JPanel(BorderLayout())
        tablesPanel.add(scrollPane, BorderLayout.CENTER)
        tablesPanel.add(scrollPane2, BorderLayout.EAST)
        tablesPanel.border = EmptyBorder(0, 2, 2, 2)
//        tablesPanel.background = Color.RED

        mainFrame.contentPane.add(topbarPanel, BorderLayout.NORTH)
        mainFrame.contentPane.add(tablesPanel, BorderLayout.CENTER)

        mainFrame.pack()
        mainFrame.setLocationRelativeTo(null)

        // Create a CoroutineScope for background work and UI updates.
        // We'll use Dispatchers.Default for background delays and Dispatchers.Swing for UI updates.
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        // Launch a repeating job that adds a row every second
        scope.launch {
            var counter = 1
            while (isActive) {
                delay(500L) // wait 1 second

                val id = counter++
//                val timestamp = java.time.LocalTime.now().withNano(0).toString()
//                val name = listOf("Alice", "Bob", "Charlie", "Dana").random()
//                val value = Random.nextInt(0, 100)

                // Switch to the Swing dispatcher to mutate the table model on the EDT
                withContext(Dispatchers.Swing) {
                    val dir = File("C:/Temp/TestDir${(1..5).random()}")
                    addDirToSelection(selectionTable, dir)
//                    model.addRow(arrayOf(timestamp, id, name, value))
//
//                    // Optionally scroll to the newly added row
                    val lastRow = selectedDirs.size - 1
                    if (lastRow >= 0) {
                        selectionTable.scrollRectToVisible(selectionTable.getCellRect(lastRow, 0, true))
                    }
                }
            }
        }
    }
}

fun addDirToSelection(table: JTable, dir: File) {
    val model = table.model as DefaultTableModel
    model.addRow(arrayOf(dir.absolutePath))
    selectedDirs.add(dir)
    println("Selected dirs after add: $selectedDirs")
}

fun removeDirFromSelection(table: JTable, dirIdx: Int) {
    val model = table.model as DefaultTableModel
    model.removeRow(dirIdx)
    selectedDirs.removeAt(dirIdx)
    println("Selected dirs after remove: $selectedDirs")
}

fun createSelectionTable(dirs: List<File>): JTable {
    val columnNames = arrayOf("Directory Path")

    val data = arrayOfNulls<Array<Any>>(dirs.size)
    for(i in dirs.indices) {
        data[i] = arrayOf(
            dirs[i].absolutePath,
            JButton("Remove")
        )
    }

    // Table model
    val tableModel = DefaultTableModel(data, columnNames)
    val table = JTable(tableModel)

    val popupMenu = JPopupMenu()
    val deleteItem = JMenuItem("Remove from selection")
    popupMenu.add(deleteItem)
    table.componentPopupMenu = popupMenu
    table.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
    table.fillsViewportHeight = true

    deleteItem.addActionListener {
        val selectedRow = table.selectedRow
        if (selectedRow != -1) {
            val rowContent = table.getValueAt(selectedRow, 0)
            removeDirFromSelection(table, selectedRow)
            JOptionPane.showMessageDialog(mainFrame, "Row $rowContent unselected")
        } else {
            JOptionPane.showMessageDialog(mainFrame, "No row selected")
        }
    }


    return table
}

fun createTable(files: List<FileEntry>): JTable {
    // Sample data
    val columnNames = arrayOf("File Path", "Size", "Modified", "Hash")

    val data = arrayOfNulls<Array<Any>>(files.size)
    for(i in files.indices) {
        data[i] = arrayOf(
            files[i].filePath,
            files[i].size,
            formatDate(files[i].modified),
            files[i].hashShort
        )
    }

    // Table model
    val tableModel = DefaultTableModel(data, columnNames)
    val table = JTable(tableModel)
    table.fillsViewportHeight = true

    return table
}