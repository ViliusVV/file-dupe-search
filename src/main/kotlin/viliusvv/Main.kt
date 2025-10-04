package viliusvv

import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableModel

fun main() {
    FlatDarkLaf.setup();

    SwingUtilities.invokeLater {
        val frame = JFrame("File Dupe Finder")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        // Sample data
        val columnNames = arrayOf("ID", "Name", "Age")
        val data = arrayOf(
            arrayOf<Any>(1, "Alice", 25),
            arrayOf<Any>(2, "Bob", 30),
            arrayOf<Any>(3, "Charlie", 28)
        )

        // Table model
        val tableModel = DefaultTableModel(data, columnNames)
        val table = JTable(tableModel)

        // Put inside a scroll pane
        val scrollPane = JScrollPane(table)
        scrollPane.preferredSize = Dimension(400, 200)

        frame.contentPane.add(scrollPane)
        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }
}