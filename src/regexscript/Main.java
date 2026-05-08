package regexscript;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Main {
  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      usage();
      System.exit(1);
    }

    String cmd = args[0];
    String file = args[1];

    String src = Files.readString(Path.of(file));
    List<Token> tokens = new Lexer(src).lex();

    if (cmd.equals("lex")) {
      for (Token t : tokens) System.out.println(t.toString());
      return;
    }

    if (cmd.equals("parse")) {
      Ast.Program program = new Parser(tokens).parseProgram();
      System.out.print(AstPrinter.print(program));
      return;
    }

    if (cmd.equals("run")) {
      List<String> cliArgs = new ArrayList<>();
      for (int i = 2; i < args.length; i++) cliArgs.add(args[i]);
      Ast.Program program = new Parser(tokens).parseProgram();
      Interpreter it = new Interpreter(cliArgs);
      it.run(program);
      return;
    }

    usage();
    System.exit(1);
  }

  private static void usage() {
    System.out.println("Usage:");
    System.out.println("  regexscript lex <file.rx>");
    System.out.println("  regexscript parse <file.rx>");
    System.out.println("  regexscript run <file.rx> [args...]");
  }
}
