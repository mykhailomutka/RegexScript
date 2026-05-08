package regexscript;

public final class LexError extends RuntimeException {
  public LexError(String msg, int line, int col) {
    super("LexError at " + line + ":" + col + " " + msg);
  }
}
