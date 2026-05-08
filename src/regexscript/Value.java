package regexscript;

import java.util.Map;

public interface Value {
  String typeName();
  Object unwrap();

  static String stringify(Value v) {
    if (v == null) return "null";
    if (v instanceof NullValue) return "null";
    if (v instanceof IntValue) return Long.toString(((IntValue) v).value);
    if (v instanceof BoolValue) return ((BoolValue) v).value ? "true" : "false";
    if (v instanceof StringValue) return ((StringValue) v).value;
    if (v instanceof RegexValue) {
      RegexValue r = (RegexValue) v;
      return "/" + r.pattern + "/" + r.flags;
    }
    if (v instanceof ListValue) {
      ListValue l = (ListValue) v;
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (int i = 0; i < l.items.size(); i++) {
        if (i > 0) sb.append(", ");
        sb.append(stringify(l.items.get(i)));
      }
      sb.append("]");
      return sb.toString();
    }
    if (v instanceof MapValue) {
      MapValue m = (MapValue) v;
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      boolean first = true;
      for (Map.Entry<String, Value> e : m.map.entrySet()) {
        if (!first) sb.append(", ");
        first = false;
        sb.append("\"").append(e.getKey()).append("\": ").append(stringify(e.getValue()));
      }
      sb.append("}");
      return sb.toString();
    }
    if (v instanceof FunctionValue) return "<fn>";
    if (v instanceof Builtins.BuiltinFn) return "<builtin>";
    return v.toString();
  }
}
