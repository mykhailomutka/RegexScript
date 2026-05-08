package regexscript;

import java.util.HashMap;
import java.util.Map;

public final class Env {
  private final Env parent;
  private final Map<String, Binding> values = new HashMap<>();

  public Env(Env parent) { this.parent = parent; }

  public void define(String name, Value value, boolean mutable) {
    values.put(name, new Binding(value, mutable));
  }

  public Binding resolve(String name) {
    if (values.containsKey(name)) return values.get(name);
    if (parent != null) return parent.resolve(name);
    return null;
  }

  public Value get(String name, Token at) {
    Binding b = resolve(name);
    if (b == null) throw new RuntimeError("Undefined variable '" + name + "'", at);
    return b.value;
  }

  public void assign(String name, Value value, Token at) {
    Binding b = resolve(name);
    if (b == null) throw new RuntimeError("Undefined variable '" + name + "'", at);
    if (!b.mutable) throw new RuntimeError("Cannot assign to immutable 'let' variable '" + name + "'", at);
    b.value = value;
  }
}
