@echo off
if not exist out (
  echo Output folder "out" not found. Compile first.
  exit /b 1
)
java -cp out regexscript.Main %*
