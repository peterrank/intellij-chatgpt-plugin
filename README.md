# IntelliJ ChatGPT Plugin

## Getting started

Create an environment variable OPENAI_API_KEY and give it the value of your OpenAI API key.  
You can generate an API-Key here: https://platform.openai.com/api-keys

Set the correct Version of IntelliJ in build.gradle.kts. Use IC as type for community-edition and IU for the ultimate one.

Start 'runIde' with gradle.

## Use the plugin

### Generate unittests
Select a Java class in the newly appearing IDEA window in the project tree and select the context menu “create unittests”.

### Chat
Drag'n'Drop of Textfiles (one or multiple) from the projecttree or from an external Filemanager are possible.
Also the filename will appear in the prompt.

## Settings


Use “Settings | Unit Test Generator Settings” to make adjustments to the prompt template.

The following variables are available:

- $classContent = the class as a character string  
- $className = the name of the class  
- $packagePath = the package name  

The GPT model can also be selected. 
Information about models can be found here: https://platform.openai.com/docs/models

