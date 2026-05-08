package regexscript;

public final class StringValue implements Value {
  public final String value;
  public StringValue(String value) { this.value = value; }
  @Override public String typeName() { return "String"; }
  @Override public Object unwrap() { return value; }
}
