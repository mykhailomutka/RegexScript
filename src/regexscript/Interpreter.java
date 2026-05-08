package regexscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import regexscript.Ast.*;
import regexscript.ControlFlow.*;

public final class Interpreter {

  private final Env globals;
  private Env env;

  public Interpreter(List<String> cliArgs) {
    this.globals = new Env(null);
    Builtins.install(globals, cliArgs);
    this.env = globals;
  }

  public Value run(Program program) {
    try {
      for (Stmt s : program.statements) execStmt(s);
      return NullValue.INSTANCE;
    } catch (Return r) {
      return r.value;
    }
  }

  private void execBlock(List<Stmt> stmts, Env newEnv) {
    Env prev = env;
    env = newEnv;
    try {
      for (Stmt s : stmts) execStmt(s);
    } finally {
      env = prev;
    }
  }

  private void execStmt(Stmt s) {
    if (s instanceof LetStmt) {
      LetStmt st = (LetStmt) s;
      env.define(st.name, eval(st.expr), false);
      return;
    }
    if (s instanceof VarStmt) {
      VarStmt st = (VarStmt) s;
      env.define(st.name, eval(st.expr), true);
      return;
    }
    if (s instanceof AssignStmt) {
      AssignStmt st = (AssignStmt) s;
      env.assign(st.name, eval(st.expr), st.at());
      return;
    }
    if (s instanceof PrintStmt) {
      PrintStmt st = (PrintStmt) s;
      Value v = eval(st.expr);
      if (st.newline) System.out.println(Value.stringify(v));
      else System.out.print(Value.stringify(v));
      return;
    }
    if (s instanceof ReturnStmt) {
      ReturnStmt st = (ReturnStmt) s;
      Value v = (st.expr == null) ? NullValue.INSTANCE : eval(st.expr);
      throw new Return(v);
    }
    if (s instanceof BreakStmt) throw new Break();
    if (s instanceof ContinueStmt) throw new Continue();

    if (s instanceof IfStmt) {
      IfStmt st = (IfStmt) s;
      Value cond = eval(st.condition);
      if (!(cond instanceof BoolValue)) throw new RuntimeError("if condition must be Bool", st.at());
      if (((BoolValue) cond).value) execBlock(st.thenBlock, new Env(env));
      else if (st.elseBlock != null) execBlock(st.elseBlock, new Env(env));
      return;
    }

    if (s instanceof WhileStmt) {
      WhileStmt st = (WhileStmt) s;
      while (true) {
        Value cond = eval(st.condition);
        if (!(cond instanceof BoolValue)) throw new RuntimeError("while condition must be Bool", st.at());
        if (!((BoolValue) cond).value) break;
        try { execBlock(st.body, new Env(env)); }
        catch (Continue c) { continue; }
        catch (Break b) { break; }
      }
      return;
    }

    if (s instanceof ForStmt) {
      ForStmt st = (ForStmt) s;
      List<Value> items = iterItems(eval(st.iterable), st.at());
      for (Value v : items) {
        Env loopEnv = new Env(env);
        loopEnv.define(st.varName, v, true);
        try { execBlock(st.body, loopEnv); }
        catch (Continue c) { continue; }
        catch (Break b) { break; }
      }
      return;
    }

    if (s instanceof FnStmt) {
      FnStmt st = (FnStmt) s;
      env.define(st.name, new FunctionValue(st.params, st.body, env), false);
      return;
    }

    throw new RuntimeError("Unknown statement type", s.at());
  }

  private List<Value> iterItems(Value it, Token at) {
    List<Value> items = new ArrayList<>();
    if (it instanceof ListValue) { items.addAll(((ListValue) it).items); return items; }
    if (it instanceof StringValue) {
      String s = ((StringValue) it).value;
      for (int i = 0; i < s.length(); i++) items.add(new StringValue(String.valueOf(s.charAt(i))));
      return items;
    }
    if (it instanceof MapValue) {
      for (String k : ((MapValue) it).map.keySet()) items.add(new StringValue(k));
      return items;
    }
    throw new RuntimeError("for-in expects List/String/Map", at);
  }

  private Value eval(Expr e) {
    if (e instanceof IntLit) return new IntValue(((IntLit) e).value);
    if (e instanceof StringLit) return new StringValue(((StringLit) e).value);
    if (e instanceof BoolLit) return new BoolValue(((BoolLit) e).value);
    if (e instanceof NullLit) return NullValue.INSTANCE;

    if (e instanceof RegexLit) {
      RegexLit r = (RegexLit) e;
      try { return new RegexValue(r.pattern, r.flags); }
      catch (Exception ex) { throw new RuntimeError("Invalid regex: " + ex.getMessage(), e.at()); }
    }

    if (e instanceof Identifier) return env.get(((Identifier) e).name, e.at());

    if (e instanceof ListLit) {
      ListLit l = (ListLit) e;
      List<Value> vals = new ArrayList<>();
      for (Expr x : l.elements) vals.add(eval(x));
      return new ListValue(vals);
    }

    if (e instanceof MapLit) {
      MapLit m = (MapLit) e;
      Map<String, Value> out = new HashMap<>();
      for (int i = 0; i < m.keys.size(); i++) out.put(m.keys.get(i), eval(m.values.get(i)));
      return new MapValue(out);
    }

    if (e instanceof IndexExpr) {
      IndexExpr ix = (IndexExpr) e;
      Value target = eval(ix.target);
      Value idx = eval(ix.index);

      if (target instanceof ListValue) {
        if (!(idx instanceof IntValue)) throw new RuntimeError("List index must be Int", e.at());
        long i = ((IntValue) idx).value;
        ListValue l = (ListValue) target;
        if (i < 0 || i >= l.items.size()) throw new RuntimeError("Index out of bounds", e.at());
        return l.items.get((int) i);
      }

      if (target instanceof StringValue) {
        if (!(idx instanceof IntValue)) throw new RuntimeError("String index must be Int", e.at());
        long i = ((IntValue) idx).value;
        String s = ((StringValue) target).value;
        if (i < 0 || i >= s.length()) throw new RuntimeError("Index out of bounds", e.at());
        return new StringValue(String.valueOf(s.charAt((int) i)));
      }

      if (target instanceof MapValue) {
        if (!(idx instanceof StringValue)) throw new RuntimeError("Map index must be String", e.at());
        String k = ((StringValue) idx).value;
        Value v = ((MapValue) target).map.get(k);
        return v == null ? NullValue.INSTANCE : v;
      }

      throw new RuntimeError("Indexing supports List/String/Map", e.at());
    }

    if (e instanceof UnaryExpr) {
      UnaryExpr u = (UnaryExpr) e;
      Value r = eval(u.right);
      if (u.op.type == TokenType.MINUS) {
        if (!(r instanceof IntValue)) throw new RuntimeError("Unary - expects Int", e.at());
        return new IntValue(-((IntValue) r).value);
      }
      if (u.op.type == TokenType.NOT) {
        if (!(r instanceof BoolValue)) throw new RuntimeError("not expects Bool", e.at());
        return new BoolValue(!((BoolValue) r).value);
      }
      throw new RuntimeError("Unknown unary operator", e.at());
    }

    if (e instanceof BinaryExpr) return evalBinary((BinaryExpr) e);

    if (e instanceof CallExpr) {
      CallExpr c = (CallExpr) e;
      Value callee = eval(c.callee);
      List<Value> args = new ArrayList<>();
      for (Expr a : c.args) args.add(eval(a));
      return callValue(callee, args, e.at());
    }

    if (e instanceof PipelineExpr) {
      PipelineExpr p = (PipelineExpr) e;
      Value left = eval(p.value);

      if (p.into instanceof Identifier) {
        Value callee = eval(p.into);
        List<Value> args = new ArrayList<>();
        args.add(left);
        return callValue(callee, args, e.at());
      }
      if (p.into instanceof CallExpr) {
        CallExpr call = (CallExpr) p.into;
        Value callee = eval(call.callee);
        List<Value> args = new ArrayList<>();
        args.add(left);
        for (Expr a : call.args) args.add(eval(a));
        return callValue(callee, args, e.at());
      }
      throw new RuntimeError("Right side of |> must be a function name or function call", e.at());
    }

    throw new RuntimeError("Unknown expression type", e.at());
  }

  private Value callValue(Value callee, List<Value> args, Token at) {
    if (callee instanceof Builtins.BuiltinFn) return ((Builtins.BuiltinFn) callee).call(args, at);

    if (callee instanceof FunctionValue) {
      FunctionValue fn = (FunctionValue) callee;
      if (args.size() != fn.params.size()) throw new RuntimeError("Wrong arg count", at);
      Env callEnv = new Env(fn.closure);
      for (int i = 0; i < fn.params.size(); i++) callEnv.define(fn.params.get(i), args.get(i), true);
      try {
        execBlock(fn.body, callEnv);
        return NullValue.INSTANCE;
      } catch (Return r) {
        return r.value;
      }
    }

    throw new RuntimeError("Value is not callable", at);
  }

  private Value evalBinary(BinaryExpr b) {
    Value left = eval(b.left);
    Value right = eval(b.right);

    switch (b.op.type) {
      case PLUS:
        if (left instanceof IntValue && right instanceof IntValue) return new IntValue(((IntValue) left).value + ((IntValue) right).value);
        if (left instanceof StringValue && right instanceof StringValue) return new StringValue(((StringValue) left).value + ((StringValue) right).value);
        throw new RuntimeError("+ expects (Int,Int) or (String,String)", b.at);

      case MINUS:
        requireInt(left, right, b.at, "-");
        return new IntValue(((IntValue) left).value - ((IntValue) right).value);

      case STAR:
        requireInt(left, right, b.at, "*");
        return new IntValue(((IntValue) left).value * ((IntValue) right).value);

      case SLASH:
        requireInt(left, right, b.at, "/");
        long denom = ((IntValue) right).value;
        if (denom == 0) throw new RuntimeError("Division by zero", b.at);
        return new IntValue(((IntValue) left).value / denom);

      case PERCENT:
        requireInt(left, right, b.at, "%");
        long d = ((IntValue) right).value;
        if (d == 0) throw new RuntimeError("Division by zero", b.at);
        return new IntValue(((IntValue) left).value % d);

      case LT: case LTE: case GT: case GTE:
        requireInt(left, right, b.at, "comparison");
        long a = ((IntValue) left).value;
        long c = ((IntValue) right).value;
        if (b.op.type == TokenType.LT) return new BoolValue(a < c);
        if (b.op.type == TokenType.LTE) return new BoolValue(a <= c);
        if (b.op.type == TokenType.GT) return new BoolValue(a > c);
        return new BoolValue(a >= c);

      case EQEQ: return new BoolValue(equalsValue(left, right));
      case NEQ: return new BoolValue(!equalsValue(left, right));

      case AND:
        if (!(left instanceof BoolValue) || !(right instanceof BoolValue)) throw new RuntimeError("and expects (Bool,Bool)", b.at);
        return new BoolValue(((BoolValue) left).value && ((BoolValue) right).value);

      case OR:
        if (!(left instanceof BoolValue) || !(right instanceof BoolValue)) throw new RuntimeError("or expects (Bool,Bool)", b.at);
        return new BoolValue(((BoolValue) left).value || ((BoolValue) right).value);

      case MATCH:
      case NOT_MATCH:
        if (!(left instanceof StringValue)) throw new RuntimeError("=~ expects left to be String", b.at);
        if (!(right instanceof RegexValue)) throw new RuntimeError("=~ expects right to be Regex", b.at);
        boolean found = ((RegexValue) right).compiled.matcher(((StringValue) left).value).find();
        return new BoolValue(b.op.type == TokenType.MATCH ? found : !found);

      default:
        throw new RuntimeError("Unsupported operator " + b.op.type, b.at);
    }
  }

  private static void requireInt(Value l, Value r, Token at, String op) {
    if (!(l instanceof IntValue) || !(r instanceof IntValue)) throw new RuntimeError(op + " expects (Int,Int)", at);
  }

  private static boolean equalsValue(Value a, Value b) {
    if (a instanceof NullValue && b instanceof NullValue) return true;
    if (a instanceof IntValue && b instanceof IntValue) return ((IntValue) a).value == ((IntValue) b).value;
    if (a instanceof BoolValue && b instanceof BoolValue) return ((BoolValue) a).value == ((BoolValue) b).value;
    if (a instanceof StringValue && b instanceof StringValue) return ((StringValue) a).value.equals(((StringValue) b).value);
    if (a instanceof RegexValue && b instanceof RegexValue) {
      RegexValue ra = (RegexValue) a, rb = (RegexValue) b;
      return ra.pattern.equals(rb.pattern) && ra.flags.equals(rb.flags);
    }
    if (a instanceof ListValue && b instanceof ListValue) {
      ListValue la = (ListValue) a, lb = (ListValue) b;
      if (la.items.size() != lb.items.size()) return false;
      for (int i = 0; i < la.items.size(); i++) if (!equalsValue(la.items.get(i), lb.items.get(i))) return false;
      return true;
    }
    if (a instanceof MapValue && b instanceof MapValue) {
      MapValue ma = (MapValue) a, mb = (MapValue) b;
      if (ma.map.size() != mb.map.size()) return false;
      for (String k : ma.map.keySet()) {
        if (!mb.map.containsKey(k)) return false;
        if (!equalsValue(ma.map.get(k), mb.map.get(k))) return false;
      }
      return true;
    }
    return false;
  }
}
