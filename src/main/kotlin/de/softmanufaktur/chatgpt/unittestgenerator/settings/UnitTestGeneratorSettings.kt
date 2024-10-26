package de.softmanufaktur.chatgpt.unittestgenerator.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.application.ApplicationManager

@State(name = "UnitTestGeneratorSettings", storages = [Storage("UnitTestGeneratorPlugin.xml")])
class UnitTestGeneratorSettings : PersistentStateComponent<UnitTestGeneratorSettings.State> {

    companion object {
        val instance: UnitTestGeneratorSettings
            get() = ApplicationManager.getApplication().getService(UnitTestGeneratorSettings::class.java)
    }

    var customPrompt: String = """
        Write unit test cases for a class.
        The class is as follows:
        ```
        ${'$'}classContent
        ```
        The test class should be named `Test${'$'}className` and should be located in the package `${'$'}packagePath`. 
        Use JUnit 5 and Mockito for mocking dependencies. 
        Cover edge cases, typical usage, and null values.
        Note that the implementation may be flawed, so prioritize the class description in the comments for guidance.
        Write tests for 100% path coverage.
        The response should exclusively contain the code.
        Write a comment to each testfunction.
        If there is a private function then access it per reflection.
    """.trimIndent()

    var gptModel: String = "gpt-4o"

    class State {
        var customPrompt: String = ""
        var gptModel: String = ""
    }

    override fun getState(): State {
        val state = State()
        state.customPrompt = customPrompt
        state.gptModel = gptModel
        return state
    }

    override fun loadState(state: State) {
        customPrompt = state.customPrompt
        gptModel = state.gptModel
    }
}
