package regexscript;

public final class IntValue implements Value {
  public final long value;
  public IntValue(long value) { this.value = value; }
  @Override public String typeName() { return "Int"; }
  @Override public Object unwrap() { return value; }
}
