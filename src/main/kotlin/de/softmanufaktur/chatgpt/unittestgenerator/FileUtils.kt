package com.solvares.unittestgeneratorplugin.utils

import com.intellij.openapi.actionSystem.AnActionEvent
import java.io.File

object FileUtils {

    fun saveTestClass(e: AnActionEvent, className: String, packagePath: String, testContent: String) {
        val projectBasePath = e.project?.basePath ?: throw IllegalStateException("Project path not found")
        val testDirPath = "$projectBasePath/src/test/java/${packagePath.replace('.', '/')}"

        // Testverzeichnis erstellen, falls es nicht existiert
        val testDir = File(testDirPath)
        if (!testDir.exists()) {
            testDir.mkdirs()
        }

        // Testdatei schreiben
        val testFile = File(testDir, "Test$className.java")
        testFile.writeText(testContent)

        println("Test class was successfully saved under: ${testFile.path}")
    }
}
