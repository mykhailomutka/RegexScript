package regexscript;

public final class Token {
  public final TokenType type;
  public final String lexeme;
  public final int line;
  public final int col;

  public Token(TokenType type, String lexeme, int line, int col) {
    this.type = type;
    this.lexeme = lexeme;
    this.line = line;
    this.col = col;
  }

  public String loc() {
    return line + ":" + col;
  }

  @Override
  public String toString() {
    if (type == TokenType.EOF) return "EOF";
    return type.name() + "(" + lexeme + ")";
  }
}
