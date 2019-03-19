# Loyalty One Template

# Purpose
When creating a new service or repo, use this template as a jumping off point.  It is meant to serve as a guide for making projects as simple as possible to setup for new developers.  Users should ideally be able to download the project code and run it without needing to worry about dependencies, versioning, or environment setup.

# Setup
- Clone the repo.
- Open it up in IntelliJ without any edits to project settings.
- Test out gradle tasks such as clean, build, test, or bootRun.

# Best Practices
- Keep setup as close to 'clone and go home' as possible.
- Maintain a clean and organized build.gradle file.
- Create and share useful run configs specific to your project.
- Use gradle tools such as Gradle's tab in IntelliJ or its CLI tool ensure nothing is broken during a complete build process before committing.
- When pushing to master make sure to incrememnt the project version in gradle.properties

# Project Structure
---
### Build.Gradle
**Plugins**: Container of plugins used by the project.  There are three types of plugins.  **Core**, **Gradle**, and **Other**.  Core plugins are bundled with Gradle.  Gradle plugins are plugins hosted by Gradle [here](https://plugins.gradle.org/).  These can be called using the same convention as core plugins but require you to deal with versioning.  Other plugins aren't hosted by Gradle and require their own setups.  They make the build.gradle file messier but if they're needed then so be it.

**Group, version**:  Names packages that are built by the project.  Helps organize and version artifacts.

**SourceCompatability, TargetCompatability**:  Sets the source and target fields for the java compiler.  Source specifies the version of java to use for compiling.  Target specifies the lowest version to support when running.

**Repositories**:  The sources from which plugins download dependencies.  Gradle natively supports mavenCentral(), jcenter(), and google().  For projects that don’t utilize these repos you need to provide custom urls.

**Dependenies**:  Declares dependencies that plugins use.  Avoids bloating the project with unneeded libraries.  Each dependency is provided with a configurationName.  More info about configNames can be found [here](https://docs.gradle.org/current/userguide/java_library_plugin.html) but bear in mind that dependencies for the main folder and test folders need to be specified separately.

**Custom sections**:  Plugins can have their own sections in order to provide additional functionality.  For example PMD uses build.gradle to load rule sets when checking code quality.

---
### Gradle Folder
Responsible for declaring which Gradle build to use and downloads it if necessary.  Makes sure that Gradle versioning is standard for everyone and bypasses download setups.  Don't touch.

---
### .idea Folder
IntelliJ setup folder.  Nothing should be committed to this folder besides runConfigurations.

---
### src Folder
The src code.  Follows standard Java conventions with mirrored main and test folders.

---
### env Folder
Configuration files for connecting to aws services and databases.
Replace the **CONFIGURE** fields with data relevant to your specific project

---
### JenkinsFile
Configuration file for pushing artifacts to Jenkins.
Replace the **CONFIGURE** fields with data relevant to your specific project

---
### settings.xml File
Configuration file for connecting to Artifactory.
Replace the **CONFIGURE** fields with data relevant to your specific project

---
### Everything else
Don't worry about it.  No touch.

***
# Plugin Docs
### Core:
Java (Junit): https://docs.gradle.org/current/userguide/java_plugin.html

Pmd: https://docs.gradle.org/current/userguide/pmd_plugin.html

Idea: https://docs.gradle.org/current/userguide/idea_plugin.html

Maven-Publish: https://docs.gradle.org/current/userguide/publishing_maven.html

### Non Core:
Spring Boot: https://spring.io/guides

Artifactory :https://github.com/JFrog/project-examples/tree/master/gradle-examples

### Non Gradle
AWS: https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/welcome.html

Splunk: https://docs.splunk.com/Documentation

REST Assured: https://github.com/rest-assured/rest-assured/wiki

### Project Pipeline
TODO:  Add sections for making changes, writing tests, logging, building, and releasing
