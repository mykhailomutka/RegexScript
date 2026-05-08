package regexscript;

import java.util.List;

public final class Ast {

  public interface Node { Token at(); }

  public interface Stmt extends Node {}

  public interface Expr extends Node {}

  public static final class Program implements Node {
    public final List<Stmt> statements;
    public final Token at;
    public Program(List<Stmt> statements, Token at) { this.statements = statements; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class LetStmt implements Stmt {
    public final String name;
    public final Expr expr;
    public final Token at;
    public LetStmt(String name, Expr expr, Token at) { this.name = name; this.expr = expr; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class VarStmt implements Stmt {
    public final String name;
    public final Expr expr;
    public final Token at;
    public VarStmt(String name, Expr expr, Token at) { this.name = name; this.expr = expr; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class AssignStmt implements Stmt {
    public final String name;
    public final Expr expr;
    public final Token at;
    public AssignStmt(String name, Expr expr, Token at) { this.name = name; this.expr = expr; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class PrintStmt implements Stmt {
    public final Expr expr;
    public final boolean newline;
    public final Token at;
    public PrintStmt(Expr expr, boolean newline, Token at) { this.expr = expr; this.newline = newline; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class ReturnStmt implements Stmt {
    public final Expr expr;
    public final Token at;
    public ReturnStmt(Expr expr, Token at) { this.expr = expr; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class BreakStmt implements Stmt {
    public final Token at;
    public BreakStmt(Token at) { this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class ContinueStmt implements Stmt {
    public final Token at;
    public ContinueStmt(Token at) { this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class IfStmt implements Stmt {
    public final Expr condition;
    public final List<Stmt> thenBlock;
    public final List<Stmt> elseBlock;
    public final Token at;
    public IfStmt(Expr condition, List<Stmt> thenBlock, List<Stmt> elseBlock, Token at) {
      this.condition = condition; this.thenBlock = thenBlock; this.elseBlock = elseBlock; this.at = at;
    }
    @Override public Token at() { return at; }
  }

  public static final class WhileStmt implements Stmt {
    public final Expr condition;
    public final List<Stmt> body;
    public final Token at;
    public WhileStmt(Expr condition, List<Stmt> body, Token at) { this.condition = condition; this.body = body; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class ForStmt implements Stmt {
    public final String varName;
    public final Expr iterable;
    public final List<Stmt> body;
    public final Token at;
    public ForStmt(String varName, Expr iterable, List<Stmt> body, Token at) {
      this.varName = varName; this.iterable = iterable; this.body = body; this.at = at;
    }
    @Override public Token at() { return at; }
  }

  public static final class FnStmt implements Stmt {
    public final String name;
    public final List<String> params;
    public final List<Stmt> body;
    public final Token at;
    public FnStmt(String name, List<String> params, List<Stmt> body, Token at) {
      this.name = name; this.params = params; this.body = body; this.at = at;
    }
    @Override public Token at() { return at; }
  }

  public static final class IntLit implements Expr {
    public final long value;
    public final Token at;
    public IntLit(long value, Token at) { this.value = value; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class StringLit implements Expr {
    public final String value;
    public final Token at;
    public StringLit(String value, Token at) { this.value = value; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class BoolLit implements Expr {
    public final boolean value;
    public final Token at;
    public BoolLit(boolean value, Token at) { this.value = value; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class NullLit implements Expr {
    public final Token at;
    public NullLit(Token at) { this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class RegexLit implements Expr {
    public final String pattern;
    public final String flags;
    public final Token at;
    public RegexLit(String pattern, String flags, Token at) { this.pattern = pattern; this.flags = flags; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class Identifier implements Expr {
    public final String name;
    public final Token at;
    public Identifier(String name, Token at) { this.name = name; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class ListLit implements Expr {
    public final List<Expr> elements;
    public final Token at;
    public ListLit(List<Expr> elements, Token at) { this.elements = elements; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class MapLit implements Expr {
    public final List<String> keys;
    public final List<Expr> values;
    public final Token at;
    public MapLit(List<String> keys, List<Expr> values, Token at) { this.keys = keys; this.values = values; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class IndexExpr implements Expr {
    public final Expr target;
    public final Expr index;
    public final Token at;
    public IndexExpr(Expr target, Expr index, Token at) { this.target = target; this.index = index; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class CallExpr implements Expr {
    public final Expr callee;
    public final List<Expr> args;
    public final Token at;
    public CallExpr(Expr callee, List<Expr> args, Token at) { this.callee = callee; this.args = args; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class UnaryExpr implements Expr {
    public final Token op;
    public final Expr right;
    public final Token at;
    public UnaryExpr(Token op, Expr right, Token at) { this.op = op; this.right = right; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class BinaryExpr implements Expr {
    public final Expr left;
    public final Token op;
    public final Expr right;
    public final Token at;
    public BinaryExpr(Expr left, Token op, Expr right, Token at) { this.left = left; this.op = op; this.right = right; this.at = at; }
    @Override public Token at() { return at; }
  }

  public static final class PipelineExpr implements Expr {
    public final Expr value;
    public final Expr into;
    public final Token at;
    public PipelineExpr(Expr value, Expr into, Token at) { this.value = value; this.into = into; this.at = at; }
    @Override public Token at() { return at; }
  }
}
