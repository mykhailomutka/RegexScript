package regexscript;

public final class NullValue implements Value {
  public static final NullValue INSTANCE = new NullValue();
  private NullValue() {}
  @Override public String typeName() { return "Null"; }
  @Override public Object unwrap() { return null; }
}
