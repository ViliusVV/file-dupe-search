package viliusvv

import addFilesToTable
import com.formdev.flatlaf.FlatDarkLaf
import findIdenticalFileNames
import findIdenticalFileSizes
import findIdenticalFiles
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import listSubdirAndFiles
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.table.DefaultTableModel


lateinit var mainFrame: JFrame

// Create a CoroutineScope for background work and UI updates.
// We'll use Dispatchers.Default for background delays and Dispatchers.Swing for UI updates.
val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Swing)
val workScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

var selectedDirs = mutableListOf<File>()

var fileQueue = ConcurrentLinkedQueue<File>()
var fileEntries = mutableListOf<FileEntry>()



var filesTableModel = PersonTableModel(fileEntries)
var infoModel = MainDataModel()

fun main() {
    println("Starting....")
    FlatDarkLaf.setup();


    SwingUtilities.invokeLater {
        mainFrame = JFrame("File Dupe Finder")
        mainFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        mainFrame.isVisible = true
        mainFrame.requestFocus()
        mainFrame.layout = BorderLayout()


        val selectionTable = createSelectionTable(selectedDirs)
        val selectinTablePane = JScrollPane(selectionTable)

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

        val statusPanel = createStatusPanel()
        val startBtn = JButton("Start")
        startBtn.addActionListener {
            infoModel.inProgress = true

            workScope.launch {
                listSubdirAndFiles(selectedDirs)
            }

            workScope.launch {
                addFilesToTable()

                val names = findIdenticalFileNames()
                println("Names: ${names.size} ->\n ${names.map{ "${it.key} -> ${it.value.size}"}.joinToString("\n")}")

                val sizes = findIdenticalFileSizes()
                println("Sizes: ${sizes.size} ->\n ${names.map{ "${it.key} -> ${it.value.size}"}.joinToString("\n")}")

                infoModel.identicalFileNames = names.size
                infoModel.identicalFileSizes = sizes.size


                val hashes = findIdenticalFiles()
                infoModel.identicalHashes = hashes.size
                infoModel.identicalFiles = hashes.values.sumOf { it.size }
                println("Hashes: ${hashes.size} ->\n ${hashes.map{ "${it.key} -> ${it.value.size}"}.joinToString("\n")}")

                fileEntries.clear()
                filesTableModel.fireTableDataChanged()

                val identicalFiles = hashes.flatMap { it.value }
                    .filter { it.size > 0 }
                    .sortedBy { it.hash }
                    .sortedBy { it.fileName }
                fileEntries.addAll(identicalFiles)
                filesTableModel.fireTableDataChanged()

                infoModel.statusMessage = "Processing done"
                infoModel.inProgress = false
            }

            resetFocus()
        }


        val stopBtn = JButton("Stop")
        stopBtn.isEnabled = false
        stopBtn.addActionListener {
            infoModel.inProgress = false
            resetFocus()
        }

        val resetBtn = JButton("Reset")
        resetBtn.addActionListener {
            reset()
        }

        infoModel.addChangeListener {
            startBtn.isEnabled = !infoModel.inProgress
            stopBtn.isEnabled = infoModel.inProgress

        }

        // Put inside a scroll pane
        val filesTable = createFilesTable()
        val filesScrollPane = JScrollPane(filesTable)

        val buttonPanel = JPanel(GridLayout(4, 1, 0, 5))
        buttonPanel.add(dirButton)
        buttonPanel.add(startBtn)
        buttonPanel.add(stopBtn)
        buttonPanel.add(resetBtn)

        val checkBoxPanel = JPanel(GridLayout(4, 1, 0, 5))
        val nameCheck = JCheckBox("Compare names", infoModel.compareNames)
        nameCheck.addActionListener {
            infoModel.compareNames = nameCheck.isSelected
        }

        val hashCheck = JCheckBox("Compare hash", infoModel.compareHash)
        hashCheck.addActionListener {
            infoModel.compareHash = hashCheck.isSelected
        }
        checkBoxPanel.add(nameCheck)
        checkBoxPanel.add(hashCheck)


        val topbarPanel = JPanel(GridLayout(1,5))
        topbarPanel.border = EmptyBorder(2, 5, 5, 5)
        topbarPanel.size = Dimension(mainFrame.width, 40)
        topbarPanel.add(statusPanel)
        // add dummy panels to fill space
        topbarPanel.add(checkBoxPanel)
        topbarPanel.add(JPanel())
        topbarPanel.add(JPanel())
        topbarPanel.add(buttonPanel)

        val tablesPanel = JPanel(BorderLayout())
        tablesPanel.add(filesScrollPane, BorderLayout.CENTER)
        tablesPanel.add(selectinTablePane, BorderLayout.LINE_END)
        tablesPanel.border = EmptyBorder(0, 2, 2, 2)

        mainFrame.contentPane.add(topbarPanel, BorderLayout.NORTH)
        mainFrame.contentPane.add(tablesPanel, BorderLayout.CENTER)

        mainFrame.pack()
        mainFrame.setLocationRelativeTo(null)


        // Launch a repeating job that adds a row every second
        appScope.launch {
            while (isActive) {
                delay(500L) // wait 1 second

                // Switch to the Swing dispatcher to mutate the table model on the EDT
                withContext(Dispatchers.Swing) {
                    // update info model
                    if (infoModel.inProgress) {
                        infoModel.inQueue = fileQueue.size
                        infoModel.processedFiles = fileEntries.size
                        infoModel.totalFiles = infoModel.inQueue + infoModel.processedFiles
                    }
                }
            }
        }

//        appScope.launch {
//            while (isActive) {
//                delay(250L)
//                fileQueue.add(File("C:\\Temp\\testfile_${(1..1000).random()}.txt"))
//            }
//        }
    }
}

fun createStatusPanel(): JPanel {
    val statusPanel = JPanel(GridLayout(9,1))
    val statusLabel = JLabel("Status: ${infoModel.inProgressString}")
    val msgLabel = JLabel("Message: ${infoModel.statusMessage}")
    val filesLabel = JLabel("Total files: ${infoModel.totalFiles}")
    val queueLabel = JLabel("Files in queue: ${infoModel.inQueue}")
    val processedLabel = JLabel("Files processed: ${infoModel.processedFiles}")
    val sameFileNamesLabel = JLabel("Identical file names: ${infoModel.identicalFileNames}")
    val sameFileSizesLabel = JLabel("Identical file sizes: ${infoModel.identicalFileSizes}")
    val sameFileHashesLabel = JLabel("Identical file hashes: ${infoModel.identicalHashes}")
    val identicalFilesLabel = JLabel("Identical file files: ${infoModel.identicalFiles}")


    statusPanel.add(statusLabel)
    statusPanel.add(msgLabel)
    statusPanel.add(processedLabel)
    statusPanel.add(filesLabel)
    statusPanel.add(queueLabel)
    statusPanel.add(sameFileNamesLabel)
    statusPanel.add(sameFileSizesLabel)
    statusPanel.add(sameFileHashesLabel)
    statusPanel.add(identicalFilesLabel)

    infoModel.addChangeListener {
        msgLabel.text = "Message: ${infoModel.statusMessage}"
        statusLabel.text = "Status: ${infoModel.inProgressString}"
        filesLabel.text = "Total files: ${infoModel.totalFiles}"
        queueLabel.text = "Files in queue: ${infoModel.inQueue}"
        processedLabel.text = "Files processed: ${infoModel.processedFiles}"
        sameFileNamesLabel.text = "Identical file names: ${infoModel.identicalFileNames}"
        sameFileSizesLabel.text = "Identical file sizes: ${infoModel.identicalFileSizes}"
        sameFileHashesLabel.text = "Identical file hashes: ${infoModel.identicalHashes}"
        identicalFilesLabel.text = "Identical file files: ${infoModel.identicalFiles}"
    }

    return statusPanel
}

fun reset() {
    selectedDirs.clear()
    fileQueue.clear()
    fileEntries.clear()
    filesTableModel.fireTableDataChanged()

    infoModel.inProgress = false
    infoModel.inQueue = 0
    infoModel.processedFiles = 0
    infoModel.totalFiles = 0
    infoModel.statusMessage = "Reset done"
}

fun resetFocus() {
    mainFrame.requestFocus()
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
    table.maximumSize = Dimension(250, 500)

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

fun createFilesTable(): JTable {
    val table = JTable(filesTableModel)
    table.fillsViewportHeight = true

    return table
}