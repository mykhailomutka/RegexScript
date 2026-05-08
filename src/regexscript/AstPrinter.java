package regexscript;

import java.util.List;

import regexscript.Ast.*;

public final class AstPrinter {

  public static String print(Program program) {
    StringBuilder sb = new StringBuilder();
    line(sb, 0, "Program");
    for (Stmt s : program.statements) printStmt(sb, s, 1);
    return sb.toString();
  }

  private static void printStmt(StringBuilder sb, Stmt s, int indent) {
    if (s instanceof LetStmt) {
      LetStmt st = (LetStmt) s;
      line(sb, indent, "LetStmt");
      line(sb, indent + 1, "Identifier(" + st.name + ")");
      printExpr(sb, st.expr, indent + 1);
      return;
    }
    if (s instanceof VarStmt) {
      VarStmt st = (VarStmt) s;
      line(sb, indent, "VarStmt");
      line(sb, indent + 1, "Identifier(" + st.name + ")");
      printExpr(sb, st.expr, indent + 1);
      return;
    }
    if (s instanceof AssignStmt) {
      AssignStmt st = (AssignStmt) s;
      line(sb, indent, "AssignStmt");
      line(sb, indent + 1, "Identifier(" + st.name + ")");
      printExpr(sb, st.expr, indent + 1);
      return;
    }
    if (s instanceof PrintStmt) {
      PrintStmt st = (PrintStmt) s;
      line(sb, indent, st.newline ? "PrintlnStmt" : "PrintStmt");
      printExpr(sb, st.expr, indent + 1);
      return;
    }
    if (s instanceof ReturnStmt) {
      ReturnStmt st = (ReturnStmt) s;
      line(sb, indent, "ReturnStmt");
      if (st.expr == null) line(sb, indent + 1, "null");
      else printExpr(sb, st.expr, indent + 1);
      return;
    }
    if (s instanceof BreakStmt) { line(sb, indent, "BreakStmt"); return; }
    if (s instanceof ContinueStmt) { line(sb, indent, "ContinueStmt"); return; }

    if (s instanceof IfStmt) {
      IfStmt st = (IfStmt) s;
      line(sb, indent, "IfStmt");
      line(sb, indent + 1, "Condition");
      printExpr(sb, st.condition, indent + 2);
      line(sb, indent + 1, "Then");
      for (Stmt t : st.thenBlock) printStmt(sb, t, indent + 2);
      if (st.elseBlock != null) {
        line(sb, indent + 1, "Else");
        for (Stmt e : st.elseBlock) printStmt(sb, e, indent + 2);
      }
      return;
    }

    if (s instanceof WhileStmt) {
      WhileStmt st = (WhileStmt) s;
      line(sb, indent, "WhileStmt");
      line(sb, indent + 1, "Condition");
      printExpr(sb, st.condition, indent + 2);
      line(sb, indent + 1, "Body");
      for (Stmt b : st.body) printStmt(sb, b, indent + 2);
      return;
    }

    if (s instanceof ForStmt) {
      ForStmt st = (ForStmt) s;
      line(sb, indent, "ForStmt");
      line(sb, indent + 1, "Var(" + st.varName + ")");
      line(sb, indent + 1, "Iterable");
      printExpr(sb, st.iterable, indent + 2);
      line(sb, indent + 1, "Body");
      for (Stmt b : st.body) printStmt(sb, b, indent + 2);
      return;
    }

    if (s instanceof FnStmt) {
      FnStmt st = (FnStmt) s;
      line(sb, indent, "FnStmt(" + st.name + ")");
      line(sb, indent + 1, "Params " + st.params);
      line(sb, indent + 1, "Body");
      for (Stmt b : st.body) printStmt(sb, b, indent + 2);
      return;
    }

    line(sb, indent, "UnknownStmt");
  }

  private static void printExpr(StringBuilder sb, Expr e, int indent) {
    if (e instanceof IntLit) { line(sb, indent, "IntLit(" + ((IntLit) e).value + ")"); return; }
    if (e instanceof StringLit) { line(sb, indent, "StringLit(\"" + escape(((StringLit) e).value) + "\")"); return; }
    if (e instanceof BoolLit) { line(sb, indent, "BoolLit(" + ((BoolLit) e).value + ")"); return; }
    if (e instanceof NullLit) { line(sb, indent, "NullLit"); return; }
    if (e instanceof RegexLit) {
      RegexLit r = (RegexLit) e;
      line(sb, indent, "RegexLit(/" + escape(r.pattern) + "/" + r.flags + ")");
      return;
    }
    if (e instanceof Identifier) { line(sb, indent, "Identifier(" + ((Identifier) e).name + ")"); return; }
    if (e instanceof ListLit) {
      ListLit l = (ListLit) e;
      line(sb, indent, "ListLit");
      for (Expr x : l.elements) printExpr(sb, x, indent + 1);
      return;
    }
    if (e instanceof MapLit) {
      MapLit m = (MapLit) e;
      line(sb, indent, "MapLit");
      for (int k = 0; k < m.keys.size(); k++) {
        line(sb, indent + 1, "Key(\"" + escape(m.keys.get(k)) + "\")");
        printExpr(sb, m.values.get(k), indent + 2);
      }
      return;
    }
    if (e instanceof IndexExpr) {
      IndexExpr ix = (IndexExpr) e;
      line(sb, indent, "IndexExpr");
      printExpr(sb, ix.target, indent + 1);
      printExpr(sb, ix.index, indent + 1);
      return;
    }
    if (e instanceof CallExpr) {
      CallExpr c = (CallExpr) e;
      line(sb, indent, "CallExpr");
      line(sb, indent + 1, "Callee");
      printExpr(sb, c.callee, indent + 2);
      line(sb, indent + 1, "Args");
      for (Expr a : c.args) printExpr(sb, a, indent + 2);
      return;
    }
    if (e instanceof UnaryExpr) {
      UnaryExpr u = (UnaryExpr) e;
      line(sb, indent, "UnaryExpr(" + u.op.lexeme + ")");
      printExpr(sb, u.right, indent + 1);
      return;
    }
    if (e instanceof BinaryExpr) {
      BinaryExpr b = (BinaryExpr) e;
      line(sb, indent, "BinaryExpr(" + b.op.lexeme + ")");
      printExpr(sb, b.left, indent + 1);
      printExpr(sb, b.right, indent + 1);
      return;
    }
    if (e instanceof PipelineExpr) {
      PipelineExpr p = (PipelineExpr) e;
      line(sb, indent, "PipelineExpr(|>)");
      line(sb, indent + 1, "Value");
      printExpr(sb, p.value, indent + 2);
      line(sb, indent + 1, "Into");
      printExpr(sb, p.into, indent + 2);
      return;
    }
    line(sb, indent, "UnknownExpr");
  }

  private static void line(StringBuilder sb, int indent, String text) {
    for (int i = 0; i < indent; i++) sb.append("  ");
    sb.append(text).append("\n");
  }

  private static String escape(String s) {
    return s.replace("\\", "\\\\").replace("\n", "\\n").replace("\t", "\\t").replace("\"", "\\\""); 
  }
}
