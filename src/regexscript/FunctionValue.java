package regexscript;

import java.util.List;
import regexscript.Ast.Stmt;

public final class FunctionValue implements Value {
  public final List<String> params;
  public final List<Stmt> body;
  public final Env closure;

  public FunctionValue(List<String> params, List<Stmt> body, Env closure) {
    this.params = params;
    this.body = body;
    this.closure = closure;
  }

  @Override public String typeName() { return "Function"; }
  @Override public Object unwrap() { return this; }
}
