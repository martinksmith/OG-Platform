@echo off
REM PLAT-1527
pushd %~dp0\..

if "%JAVA_HOME%" == "" echo Warning: JAVA_HOME is not set
set JAVACMD=%JAVA_HOME%\bin\java.exe

echo ### Creating empty database

"%JAVACMD%" -cp "og-bloombergexample.jar;lib\*" ^
  com.opengamma.util.test.DbTool ^
  -jdbcUrl jdbc:hsqldb:file:install/db/hsqldb/bloombergexample-db ^
  -database og-financial ^
  -user "OpenGamma" ^
  -password "OpenGamma" ^
  -drop true ^
  -create true ^
  -createtables true ^
  -dbscriptbasedir .
  
"%JAVACMD%" -cp "og-bloombergexample.jar;lib\*" ^
  com.opengamma.util.test.DbTool ^
  -jdbcUrl jdbc:hsqldb:file:temp/hsqldb/og-fin-user ^
  -database og-financial ^
  -user "OpenGamma" ^
  -password "OpenGamma" ^
  -drop true ^
  -create true ^
  -createtables true ^
  -dbscriptbasedir .
  
echo ### Adding example data

setlocal ENABLEDELAYEDEXPANSION
set CLASSPATH=og-bloombergexample.jar;lib\*;config
FOR /R lib %%a IN (*.zip) DO set CLASSPATH=!CLASSPATH!;%%a

"%JAVACMD%" -cp "%CLASSPATH%" ^
  -Xms1024M ^
  -Xmx1024M ^
  -Dlogback.configurationFile=jetty-logback.xml ^
  com.opengamma.examples.tool.ExampleDatabasePopulater

REM PLAT-1527
popd