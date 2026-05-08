package regexscript;

public final class Binding {
  public Value value;
  public final boolean mutable;
  public Binding(Value value, boolean mutable) { this.value = value; this.mutable = mutable; }
}
