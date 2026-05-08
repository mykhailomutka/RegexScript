package regexscript;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;

public final class Builtins {

  public interface BuiltinFn extends Value {
    Value call(List<Value> args, Token at);
  }

  public static void install(Env env, List<String> cliArgs) {
    env.define("print", fn("print", (args, at) -> { System.out.print(joinArgs(args)); return NullValue.INSTANCE; }), false);
    env.define("println", fn("println", (args, at) -> { System.out.println(joinArgs(args)); return NullValue.INSTANCE; }), false);

    env.define("len", fn("len", (args, at) -> {
      requireArity(args, 1, at);
      Value v = args.get(0);
      if (v instanceof StringValue) return new IntValue(((StringValue) v).value.length());
      if (v instanceof ListValue) return new IntValue(((ListValue) v).items.size());
      if (v instanceof MapValue) return new IntValue(((MapValue) v).map.size());
      throw new RuntimeError("len() expects String/List/Map", at);
    }), false);

    env.define("push", fn("push", (args, at) -> {
      requireArity(args, 2, at);
      if (!(args.get(0) instanceof ListValue)) throw new RuntimeError("push(list, value) expects List", at);
      ((ListValue) args.get(0)).items.add(args.get(1));
      return NullValue.INSTANCE;
    }), false);

    env.define("keys", fn("keys", (args, at) -> {
      requireArity(args, 1, at);
      if (!(args.get(0) instanceof MapValue)) throw new RuntimeError("keys(map) expects Map", at);
      ListValue out = new ListValue();
      for (String k : ((MapValue) args.get(0)).map.keySet()) out.items.add(new StringValue(k));
      return out;
    }), false);

    env.define("chars", fn("chars", (args, at) -> {
      requireArity(args, 1, at);
      if (!(args.get(0) instanceof StringValue)) throw new RuntimeError("chars(string) expects String", at);
      String s = ((StringValue) args.get(0)).value;
      ListValue out = new ListValue();
      for (int i = 0; i < s.length(); i++) out.items.add(new StringValue(String.valueOf(s.charAt(i))));
      return out;
    }), false);

    env.define("args", fn("args", (args2, at) -> {
      requireArity(args2, 0, at);
      ListValue out = new ListValue();
      for (String a : cliArgs) out.items.add(new StringValue(a));
      return out;
    }), false);

    env.define("readFile", fn("readFile", (args2, at) -> {
      requireArity(args2, 1, at);
      if (!(args2.get(0) instanceof StringValue)) throw new RuntimeError("readFile(path) path must be String", at);
      try { return new StringValue(Files.readString(Path.of(((StringValue) args2.get(0)).value))); }
      catch (IOException e) { throw new RuntimeError("readFile failed: " + e.getMessage(), at); }
    }), false);

    // Regex built-ins
    env.define("match", fn("match", (args2, at) -> {
      requireArity(args2, 2, at);
      String text = asString(args2.get(0), at);
      RegexValue rx = asRegex(args2.get(1), at);
      Matcher m = rx.compiled.matcher(text);
      if (!m.find()) return NullValue.INSTANCE;
      MapValue out = new MapValue();
      ListValue groups = new ListValue();
      for (int i = 0; i <= m.groupCount(); i++) groups.items.add(new StringValue(m.group(i)));
      out.map.put("groups", groups);
      out.map.put("start", new IntValue(m.start()));
      out.map.put("end", new IntValue(m.end()));
      return out;
    }), false);

    env.define("findAll", fn("findAll", (args2, at) -> {
      requireArity(args2, 2, at);
      String text = asString(args2.get(0), at);
      RegexValue rx = asRegex(args2.get(1), at);
      Matcher m = rx.compiled.matcher(text);
      ListValue out = new ListValue();
      while (m.find()) out.items.add(new StringValue(m.group(0)));
      return out;
    }), false);

    env.define("extract", fn("extract", (args2, at) -> {
      requireArity(args2, 2, at);
      String text = asString(args2.get(0), at);
      RegexValue rx = asRegex(args2.get(1), at);
      Matcher m = rx.compiled.matcher(text);
      if (!m.find()) return new ListValue();
      ListValue groups = new ListValue();
      for (int i = 0; i <= m.groupCount(); i++) groups.items.add(new StringValue(m.group(i)));
      return groups;
    }), false);

    env.define("replace", fn("replace", (args2, at) -> {
      requireArity(args2, 3, at);
      String text = asString(args2.get(0), at);
      RegexValue rx = asRegex(args2.get(1), at);
      String rep = asString(args2.get(2), at);
      return new StringValue(rx.compiled.matcher(text).replaceAll(rep));
    }), false);

    env.define("split", fn("split", (args2, at) -> {
      requireArity(args2, 2, at);
      String text = asString(args2.get(0), at);
      RegexValue rx = asRegex(args2.get(1), at);
      String[] parts = rx.compiled.split(text);
      ListValue out = new ListValue();
      for (String p : parts) out.items.add(new StringValue(p));
      return out;
    }), false);

    env.define("count", fn("count", (args2, at) -> {
      requireArity(args2, 2, at);
      String text = asString(args2.get(0), at);
      RegexValue rx = asRegex(args2.get(1), at);
      Matcher m = rx.compiled.matcher(text);
      long c = 0;
      while (m.find()) c++;
      return new IntValue(c);
    }), false);
  }

  private static BuiltinFn fn(String name, FnBody body) {
    return new BuiltinFn() {
      @Override public String typeName() { return "BuiltinFn"; }
      @Override public Object unwrap() { return this; }
      @Override public Value call(List<Value> args, Token at) { return body.call(args, at); }
      @Override public String toString() { return "<builtin " + name + ">"; }
    };
  }

  private interface FnBody { Value call(List<Value> args, Token at); }

  private static void requireArity(List<Value> args, int n, Token at) {
    if (args.size() != n) throw new RuntimeError("Expected " + n + " args, got " + args.size(), at);
  }

  private static String asString(Value v, Token at) {
    if (!(v instanceof StringValue)) throw new RuntimeError("Expected String", at);
    return ((StringValue) v).value;
  }

  private static RegexValue asRegex(Value v, Token at) {
    if (!(v instanceof RegexValue)) throw new RuntimeError("Expected Regex", at);
    return (RegexValue) v;
  }

  private static String joinArgs(List<Value> args) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < args.size(); i++) {
      if (i > 0) sb.append(" ");
      sb.append(Value.stringify(args.get(i)));
    }
    return sb.toString();
  }
}
