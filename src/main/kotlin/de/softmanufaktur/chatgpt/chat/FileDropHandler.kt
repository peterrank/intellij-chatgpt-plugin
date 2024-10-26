package de.softmanufaktur.chatgpt.chat

import javax.swing.JTextArea
import javax.swing.TransferHandler
import java.awt.datatransfer.DataFlavor
import java.io.File

fun JTextArea.initFileDropHandler() {
    transferHandler = object : TransferHandler() {
        override fun canImport(support: TransferSupport): Boolean {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
        }

        override fun importData(support: TransferSupport): Boolean {
            if (!canImport(support)) {
                return false
            }

            val transferable = support.transferable
            val data = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>

            // Verarbeite alle Dateien in der Liste
            for (item in data) {
                if (item is File) {
                    val file = item
                    val fileName = file.name
                    val fileText = file.readText()

                    try {
                        val cursorPosition = caretPosition
                        // Füge den Text mit Dateititel ein
                        insert("\nFILE: $fileName\n$fileText\n\n", cursorPosition)
                        positionCaretAt(cursorPosition) // Platziere den Cursor am Ende nach jedem Einfügen
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        return false
                    }
                }
            }
            return true
        }

        // Eine neue Funktion, um den Cursor korrekt zu platzieren
        private fun JTextArea.positionCaretAt(position: Int) {
            caretPosition = position + text.length
        }
    }
}