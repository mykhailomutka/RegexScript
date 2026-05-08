# RegexScript
This repo includes a full RegexScript implementation:
1) Lexer
2) Parser (AST)
3) Interpreter (runs programs)
4) CLI: lex / parse / run
5) Tests
6) Example project script

## Compile
1) mkdir out

2) Compile

Windows (PowerShell):
  javac -d out (Get-ChildItem -Recurse src -Filter *.java | % { $_.FullName })

macOS/Linux:
  javac -d out $(find src -name "*.java")

## CLI
1) Lex
  java -cp out regexscript.Main lex examples/log_report/log_report.rx

2) Parse
  java -cp out regexscript.Main parse examples/log_report/log_report.rx

3) Run (example project)
  java -cp out regexscript.Main run examples/log_report/log_report.rx examples/log_report/sample.log

## Run tests
  java -cp out regexscript.TestRunner
