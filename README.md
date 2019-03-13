# !!PROJECT NAME!! Setup

# Setup

  - Clone project
  - Open up in IntelliJ
  - IntelliJ should auto download External Libraries.  
  - Click on the gradle tab 
  - Under "Run Configurations" double click "template[clean build test bootRun]" to confirm everything is kosher.


# Sections
- Src contains project code directories and test directories.
- Build (created during a gradle run) contains useful error reports.
- build.gradle contains bulk of project setup code.

# Plugins used (so far)
- Java:  Let's you use java
- base:  adds additional gradle commands
- PMD:  Code quality checker with pre-defined rulesets and custom support
- Spring Boot:  Makes setting up Spring easy(er)
- Spring Dependency Management: Required to set up spring boot
- Jfrog Artifactory: Support for building and uploading artifacts

# Good things to keep in mind
- Keep process down to "clone, run"
- Keep repo down to src code and gradle settings as much as possible.
- Keep gradle file lookin *CLEAN*.
- Make a "pre-commit" checklist for nonsense like versions.

# Q's
- What other plugins should we include?
- Should we include the aws plugin?
- build.gradle has a version.  How to auto update it upon a push?
- Should we add useful runConfigurations to the repo code?  Or keep it out?
-- Side Q:  What configs should we have if we do add them?
- How to deal with plugin versioning?
