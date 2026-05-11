# RegexScript

RegexScript is a small **regex-first scripting language** implemented in Java. It’s designed for text processing and pattern matching, with a simple end-to-end toolchain:

**Source file → Lexer → Tokens → Parser → AST → Interpreter → Output**

This repo contains a full working build, including:
1) **Lexer** (tokenizes source code)
2) **Parser** (builds an AST with correct precedence/associativity)
3) **Interpreter** (executes programs)
4) **CLI commands**: `lex`, `parse`, `run`
5) **Tests** (valid scripts that parse, invalid scripts that fail)
6) **Example project script** (a log report demo that uses regex + text utilities)

## Compile

1) Create output folder:
```bash
mkdir out
