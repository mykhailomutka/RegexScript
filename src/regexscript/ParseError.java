package regexscript;

public final class ParseError extends RuntimeException {
  public final Token token;

  public ParseError(String msg, Token token) {
    super("ParseError at " + token.loc() + " " + msg);
    this.token = token;
  }
}
