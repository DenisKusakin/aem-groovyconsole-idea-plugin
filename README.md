# AEM Groovy Console IntelliJ Plugin
<!-- Plugin description -->
Adds functionality for executing groovy scripts on running AEM server from Intellij IDEA.

Powered by [AEM Groovy Console](https://github.com/OlsonDigital/aem-groovy-console).

## Features

- Execute groovy scripts on running AEM server from editor
- Script can be executed on multiple servers
- Console output provides a link to the script's source if exception occurred
- Support code completion in groovy editor. **Note**: com.adobe.aem:uber-jar:x.x.x should be in project classpath to provide full code completion
<!-- Plugin description end -->

## How to use
Plugin is available in Jetbrains plugin repository - [AEM Groovy Console](https://plugins.jetbrains.com/plugin/10893-aem-groovy-console)

AEM Servers should be configured in Settings tab

![Screenshot](screenshot2.png)

Plugin applied to any *.groovy* file, file's directory should be marked as AEM Groovy Scripts Root (since v0.2.0-beta),
it is also applied if file path contains *groovysonsole* substring

![Screenshot](screenshot1.png)