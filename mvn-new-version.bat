@ECHO OFF
:INPUT
SET /P newVersion=Please enter new version number(1.0.0):
IF "%newVersion%"=="" GOTO INPUT
ECHO The new version is %newVersion%, continue?
pause
GOTO End
:End
mvn versions:set -DnewVersion=%newVersion% -DgenerateBackupPoms=false
pause