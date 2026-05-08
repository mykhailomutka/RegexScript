package regexscript;

public final class ControlFlow {
  public static final class Return extends RuntimeException {
    public final Value value;
    public Return(Value value) { this.value = value; }
  }
  public static final class Break extends RuntimeException {}
  public static final class Continue extends RuntimeException {}
}
