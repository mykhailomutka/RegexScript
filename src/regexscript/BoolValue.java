package regexscript;

public final class BoolValue implements Value {
  public final boolean value;
  public BoolValue(boolean value) { this.value = value; }
  @Override public String typeName() { return "Bool"; }
  @Override public Object unwrap() { return value; }
}
