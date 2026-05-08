package regexscript;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public final class TestRunner {
  public static void main(String[] args) throws Exception {
    int validParsed = runValidParse("tests/valid");
    int invalidRejected = runInvalidParse("tests/invalid");
    System.out.println("Valid parsed: " + validParsed);
    System.out.println("Invalid rejected: " + invalidRejected);
    if (invalidRejected == 0) System.exit(1);
  }

  private static int runValidParse(String dir) throws IOException {
    int passed = 0;
    for (Path p : Files.newDirectoryStream(Path.of(dir), "*.rx")) {
      try {
        String src = Files.readString(p);
        List<Token> tokens = new Lexer(src).lex();
        new Parser(tokens).parseProgram();
        passed++;
      } catch (RuntimeException e) {
        System.out.println("FAIL(valid) " + p + " -> " + e.getMessage());
      }
    }
    return passed;
  }

  private static int runInvalidParse(String dir) throws IOException {
    int passed = 0;
    for (Path p : Files.newDirectoryStream(Path.of(dir), "*.rx")) {
      try {
        String src = Files.readString(p);
        List<Token> tokens = new Lexer(src).lex();
        new Parser(tokens).parseProgram();
        System.out.println("FAIL(invalid) " + p + " -> parsed but should fail");
      } catch (RuntimeException e) {
        passed++;
      }
    }
    return passed;
  }
}
