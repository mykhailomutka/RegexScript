package regexscript;

public final class RuntimeError extends RuntimeException {
  public RuntimeError(String msg, Token at) {
    super("RuntimeError at " + (at == null ? "?" : at.loc()) + " " + msg);
  }
  public RuntimeError(String msg) {
    super("RuntimeError " + msg);
  }
}
